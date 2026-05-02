import com.google.protobuf.Empty;
import example.grpcclient.Client;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.JSONObject;
import service.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Server unit tests for Assignment 6.
 *
 * IMPORTANT: These tests require the server to be running BEFORE you run them.
 *
 * To run these tests:
 * 1. First, start the server in one terminal: gradle runNode
 * 2. Then, in another terminal, run: gradle test
 *
 * The tests connect to localhost:8000 (the default port for runNode).
 * Make sure your server is running on this port before running tests.
 *
 * TODO for students:
 * This file contains example tests for the Echo and Joke services.
 * You need to add your own tests for:
 * - Converter service (happy path and error cases)
 * - Library service (happy path, error cases, and persistence testing)
 *
 * Your tests should follow the same pattern as the examples below.
 */
public class ServerTest {

    ManagedChannel channel;
    private EchoGrpc.EchoBlockingStub blockingStub;
    private JokeGrpc.JokeBlockingStub blockingStub2;
    private ConverterGrpc.ConverterBlockingStub blockingStub5;
    private LibraryGrpc.LibraryBlockingStub blockingStub7;


    @org.junit.Before
    public void setUp() throws Exception {
        // assuming default port and localhost for our testing, make sure Node runs on this port
        channel = ManagedChannelBuilder.forTarget("localhost:8000").usePlaintext().build();

        blockingStub = EchoGrpc.newBlockingStub(channel);
        blockingStub2 = JokeGrpc.newBlockingStub(channel);
        blockingStub5 = ConverterGrpc.newBlockingStub(channel);
        blockingStub7 = LibraryGrpc.newBlockingStub(channel);
    }

    @org.junit.After
    public void close() throws Exception {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);

    }


    @Test
    public void parrot() {
        // success case
        ClientRequest request = ClientRequest.newBuilder().setMessage("test").build();
        ServerResponse response = blockingStub.parrot(request);
        assertTrue(response.getIsSuccess());
        assertEquals("test", response.getMessage());

        // error cases
        request = ClientRequest.newBuilder().build();
        response = blockingStub.parrot(request);
        assertFalse(response.getIsSuccess());
        assertEquals("No message provided", response.getError());

        request = ClientRequest.newBuilder().setMessage("").build();
        response = blockingStub.parrot(request);
        assertFalse(response.getIsSuccess());
        assertEquals("No message provided", response.getError());
    }

    // For this test the server needs to be started fresh AND the list of jokes needs to be the initial list
    @Test
    public void joke() {
        // getting first joke
        JokeReq request = JokeReq.newBuilder().setNumber(1).build();
        JokeRes response = blockingStub2.getJoke(request);
        assertEquals(1, response.getJokeCount());
        assertEquals("Did you hear the rumor about butter? Well, I'm not going to spread it!", response.getJoke(0));

        // getting next 2 jokes
        request = JokeReq.newBuilder().setNumber(2).build();
        response = blockingStub2.getJoke(request);
        assertEquals(2, response.getJokeCount());
        assertEquals("What do you call someone with no body and no nose? Nobody knows.", response.getJoke(0));
        assertEquals("I don't trust stairs. They're always up to something.", response.getJoke(1));

        // getting 2 more but only one more on server
        request = JokeReq.newBuilder().setNumber(2).build();
        response = blockingStub2.getJoke(request);
        assertEquals(2, response.getJokeCount());
        assertEquals("How do you get a squirrel to like you? Act like a nut.", response.getJoke(0));
        assertEquals("I am out of jokes...", response.getJoke(1));

        // trying to get more jokes but out of jokes
        request = JokeReq.newBuilder().setNumber(2).build();
        response = blockingStub2.getJoke(request);
        assertEquals(1, response.getJokeCount());
        assertEquals("I am out of jokes...", response.getJoke(0));

        // trying to add joke without joke field
        JokeSetReq req2 = JokeSetReq.newBuilder().build();
        JokeSetRes res2 = blockingStub2.setJoke(req2);
        assertFalse(res2.getOk());

        // trying to add empty joke
        req2 = JokeSetReq.newBuilder().setJoke("").build();
        res2 = blockingStub2.setJoke(req2);
        assertFalse(res2.getOk());

        // adding a new joke (well word)
        req2 = JokeSetReq.newBuilder().setJoke("whoop").build();
        res2 = blockingStub2.setJoke(req2);
        assertTrue(res2.getOk());

        // should have the new "joke" now and return it
        request = JokeReq.newBuilder().setNumber(1).build();
        response = blockingStub2.getJoke(request);
        assertEquals(1, response.getJokeCount());
        assertEquals("whoop", response.getJoke(0));
    }

    @Test
    public void converterKilometerToMile() {
        ConversionRequest req = ConversionRequest.newBuilder()
                .setValue(1)
                .setFromUnit("kilometer")
                .setToUnit("mile")
                .build();

        ConversionResponse res = blockingStub5.convert(req);

        assertTrue(res.getIsSuccess());
        assertEquals(.62, res.getResult(), .01);
    }

    @Test
    public void converterWeight() {
        ConversionRequest req = ConversionRequest.newBuilder()
                .setValue(10)
                .setFromUnit("kilogram")
                .setToUnit("pound")
                .build();

        ConversionResponse res = blockingStub5.convert(req);

        assertTrue(res.getIsSuccess());
        assertEquals(22.05, res.getResult(), .01);
    }

    @Test
    public void converterTemp() {
        ConversionRequest req = ConversionRequest.newBuilder()
                .setValue(10)
                .setFromUnit("celsius")
                .setToUnit("fahrenheit")
                .build();

        ConversionResponse res = blockingStub5.convert(req);

        assertTrue(res.getIsSuccess());
        assertEquals(50, res.getResult(), .01);
    }

    @Test
    public void converterErrors() {
        ConversionRequest req = ConversionRequest.newBuilder()
                .setValue(10)
                .setToUnit("mile")
                .build();

        ConversionResponse res = blockingStub5.convert(req);
        assertFalse(res.getIsSuccess());
        assertEquals("No from unit provided", res.getError());

        req = ConversionRequest.newBuilder()
                .setValue(10)
                .setFromUnit("kilometer")
                .build();

        res = blockingStub5.convert(req);
        assertFalse(res.getIsSuccess());
        assertEquals("No to unit provided", res.getError());

        req = ConversionRequest.newBuilder()
                .setValue(10)
                .setFromUnit("random")
                .setToUnit("mile")
                .build();

        res = blockingStub5.convert(req);
        assertFalse(res.getIsSuccess());
        assertEquals("Unsupported unit random", res.getError());

        req = ConversionRequest.newBuilder()
                .setValue(10)
                .setFromUnit("kilometer")
                .setToUnit("kilogram")
                .build();

        res = blockingStub5.convert(req);
        assertFalse(res.getIsSuccess());
        assertEquals("Units do not match cannot convert", res.getError());

        req = ConversionRequest.newBuilder()
                .setValue(10)
                .setFromUnit("mile")
                .setToUnit("mile")
                .build();

        res = blockingStub5.convert(req);
        assertFalse(res.getIsSuccess());
        assertEquals("Same unit no need to convert", res.getError());
    }

    @Test
    public void library() {
        BookListResponse list = blockingStub7.listBooks(Empty.newBuilder().build());
        assertTrue(list.getIsSuccess());
        assertTrue(list.getBooksCount() > 0);

        BookSearchRequest searchReq = BookSearchRequest.newBuilder()
                .setQuery("1984")
                .build();

        BookListResponse search = blockingStub7.searchBooks(searchReq);
        assertTrue(search.getIsSuccess());
    }
    
    @Test
    public void libraryErrors() {
        BookSearchRequest req = BookSearchRequest.newBuilder().build();
        BookListResponse res = blockingStub7.searchBooks(req);

        assertFalse(res.getIsSuccess());
        assertEquals("missing field", res.getError());

        BorrowRequest bad = BorrowRequest.newBuilder().build();
        BorrowResponse br = blockingStub7.borrowBook(bad);

        assertFalse(br.getIsSuccess());
        assertEquals("missing field", br.getError());

        BorrowRequest invalid = BorrowRequest.newBuilder()
                .setIsbn("bad")
                .setBorrowerName("sam")
                .build();

        br = blockingStub7.borrowBook(invalid);
        assertFalse(br.getIsSuccess());
        assertEquals("book not found", br.getError());

        ReturnRequest rr = ReturnRequest.newBuilder()
                .setIsbn("978-0061120084")
                .build();

        ReturnResponse rrRes = blockingStub7.returnBook(rr);
        assertFalse(rrRes.getIsSuccess());
        assertEquals("book is not borrowed", rrRes.getError());
    }

    @Test
    public void libraryPersistence() {
        BorrowRequest borrow = BorrowRequest.newBuilder()
                .setIsbn("978-0451524935")
                .setBorrowerName("sam")
                .build();

        blockingStub7.borrowBook(borrow);

        BookListResponse list = blockingStub7.listBooks(
                Empty.newBuilder().build()
        );

        boolean foundBorrowed = false;

        for (Book b : list.getBooksList()) {
            if (b.getIsbn().equals("978-0451524935")) {
                foundBorrowed = b.getIsBorrowed();
            }
        }
        assertTrue(foundBorrowed);
    }
}