package example.grpcclient;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerMethodDefinition;
import io.grpc.stub.StreamObserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.Empty;

import service.*;
import service.LibraryGrpc.LibraryImplBase;

import java.util.Stack;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import buffers.RequestProtos.Request;
import buffers.RequestProtos.Request.RequestType;
import buffers.ResponseProtos.Response;

public class LibraryImpl extends LibraryGrpc.LibraryImplBase{

    private List<Book> books = new ArrayList<>();
    private final String FILE_NAME = "library_data.json";

    public LibraryImpl() {
        loadData();
    }

    private void loadData() {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader("books.json"));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line.trim());
                }
                reader.close();
                parseJson(sb.toString());
                saveData();
            } else {
                BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line.trim());
                }
                reader.close();
                parseJson(sb.toString());
            }
        } catch (Exception e) {
            System.err.println("Error loading data: " + e);
            books = new ArrayList<>();
        }
    }

    private void saveData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {

            writer.write("[");
            writer.newLine();

            for (int i = 0; i < books.size(); i++) {
                Book b = books.get(i);
                writer.write("  {");
                writer.newLine();
                writer.write("    \"title\": \"" + escape(b.getTitle()) + "\",");
                writer.newLine();
                writer.write("    \"author\": \"" + escape(b.getAuthor()) + "\",");
                writer.newLine();
                writer.write("    \"isbn\": \"" + escape(b.getIsbn()) + "\",");
                writer.newLine();
                writer.write("    \"isBorrowed\": " + b.getIsBorrowed() + ",");
                writer.newLine();
                writer.write("    \"borrowedBy\": \"" + escape(b.getBorrowedBy()) + "\",");
                writer.newLine();
                writer.write("    \"returnBy\": \"" + escape(b.getReturnBy()) + "\"");
                writer.newLine();
                writer.write("  }");
                if (i < books.size() - 1) {
                    writer.write(",");
                }
                writer.newLine();
            }
            writer.write("]");
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e);
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String filter(String obj, String key) {
        String pattern = key + ":";
        int start = obj.indexOf(pattern);
        if (start == -1) return "";

        start += pattern.length();

        int end = obj.indexOf(",", start);
        if (end == -1) end = obj.length();

        return obj.substring(start, end).trim();
    }

    private void parseJson(String json) {
        books = new ArrayList<>();

        json = json.substring(json.indexOf("[") + 1, json.lastIndexOf("]"));

        String[] objects = json.split("\\},\\s*\\{");
        for (String obj : objects) {
            obj = obj.replace("{", "")
                    .replace("}", "")
                    .replace("\"", "");

            String title = filter(obj, "title");
            String author = filter(obj, "author");
            String isbn = filter(obj, "isbn");
            String isBorrowed = filter(obj, "isBorrowed");
            String borrowedBy = filter(obj, "borrowedBy");
            String returnBy = filter(obj, "returnBy");

            Book book = Book.newBuilder()
                    .setTitle(title)
                    .setAuthor(author)
                    .setIsbn(isbn)
                    .setIsBorrowed(isBorrowed.trim().equals("true"))
                    .setBorrowedBy(borrowedBy)
                    .setReturnBy(returnBy)
                    .build();

            books.add(book);
        }
    }

    @Override
    public void listBooks(Empty req, StreamObserver<BookListResponse> responseObserver) {
        BookListResponse.Builder response = BookListResponse.newBuilder();

        if (books.isEmpty()) {
            response.setIsSuccess(false)
                    .setError("no books in library yet");
        } else {
            response.setIsSuccess(true)
                    .addAllBooks(books);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void searchBooks(BookSearchRequest req, StreamObserver<BookListResponse> responseObserver) {
        BookListResponse.Builder response = BookListResponse.newBuilder();

        if (req.getQuery().isEmpty()) {
            response.setIsSuccess(false).setError("missing field");
        } else if (books.isEmpty()) {
            response.setIsSuccess(false).setError("no books in library yet");
        } else {
            List<Book> results = new ArrayList<>();
            for (Book b : books) {
                if (b.getTitle().toLowerCase().contains(req.getQuery().toLowerCase()) ||
                    b.getAuthor().toLowerCase().contains(req.getQuery().toLowerCase())) {
                    results.add(b);
                }
            }
            if (results.isEmpty()) {
                response.setIsSuccess(false).setError("no books found matching query");
            } else {
                response.setIsSuccess(true).addAllBooks(results);
            }
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void borrowBook(BorrowRequest req, StreamObserver<BorrowResponse> responseObserver) {
        BorrowResponse.Builder response = BorrowResponse.newBuilder();

        if (req.getIsbn().isEmpty() || req.getBorrowerName().isEmpty()) {
            response.setIsSuccess(false).setError("missing field");
        } else {
            Book found = null;

            for (Book b : books) {
                if (b.getIsbn().equals(req.getIsbn())) {
                    found = b;
                    break;
                }
            }

            if (found == null) {
                response.setIsSuccess(false).setError("book not found");
            } else if (found.getIsBorrowed()) {
                response.setIsSuccess(false).setError("book is already borrowed");
            } else {
                Book updated = Book.newBuilder(found)
                    .setIsBorrowed(true)
                    .setBorrowedBy(req.getBorrowerName())
                    .setReturnBy("2026-05-04")
                    .build();

                books.remove(found);
                books.add(updated);
                saveData();
                response.setIsSuccess(true)
                        .setMessage("book borrowed successfully, return by " + updated.getReturnBy());
            }
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void returnBook(ReturnRequest req, StreamObserver<ReturnResponse> responseObserver) {
        ReturnResponse.Builder response = ReturnResponse.newBuilder();
        if (req.getIsbn().isEmpty()) {
            response.setIsSuccess(false).setError("missing field");
        } else {
            Book found = null;
            for (Book b : books) {
                if (b.getIsbn().equals(req.getIsbn())) {
                    found = b;
                    break;
                }
            }
            if (found == null) {
                response.setIsSuccess(false).setError("book not found");
            } else if (!found.getIsBorrowed()) {
                response.setIsSuccess(false).setError("book is not borrowed");
            } else {
                Book updated = Book.newBuilder(found)
                    .setIsBorrowed(false)
                    .setBorrowedBy("")
                    .setReturnBy("")
                    .build();
                books.remove(found);
                books.add(updated);
                saveData();
                response.setIsSuccess(true)
                        .setMessage("book returned successfully");
            }
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
