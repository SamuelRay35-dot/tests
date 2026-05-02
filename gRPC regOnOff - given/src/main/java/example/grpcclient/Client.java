package example.grpcclient;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import service.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.google.protobuf.Empty; // needed to use Empty
import java.util.Scanner;


/**
 * Client that requests `parrot` method from the `EchoServer`.
 */
public class Client {
  private final EchoGrpc.EchoBlockingStub blockingStub;
  private final JokeGrpc.JokeBlockingStub blockingStub2;
  private final RegistryGrpc.RegistryBlockingStub blockingStub3;
  private final RegistryGrpc.RegistryBlockingStub blockingStub4;
  private final ConverterGrpc.ConverterBlockingStub blockingStub5;
  private final RiddleGrpc.RiddleBlockingStub blockingStub6;
  private final LibraryGrpc.LibraryBlockingStub blockingStub7;

  /** Construct client for accessing server using the existing channel. */
  public Client(Channel channel, Channel regChannel) {
    // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's
    // responsibility to
    // shut it down.

    // Passing Channels to code makes code easier to test and makes it easier to
    // reuse Channels.
    blockingStub = EchoGrpc.newBlockingStub(channel);
    blockingStub2 = JokeGrpc.newBlockingStub(channel);
    blockingStub3 = RegistryGrpc.newBlockingStub(regChannel);
    blockingStub4 = RegistryGrpc.newBlockingStub(channel);
    blockingStub5 = ConverterGrpc.newBlockingStub(channel);
    blockingStub6 = RiddleGrpc.newBlockingStub(channel);
    blockingStub7 = LibraryGrpc.newBlockingStub(channel);
  }

  /** Construct client for accessing server using the existing channel. */
  public Client(Channel channel) {
    // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's
    // responsibility to
    // shut it down.

    // Passing Channels to code makes code easier to test and makes it easier to
    // reuse Channels.
    blockingStub = EchoGrpc.newBlockingStub(channel);
    blockingStub2 = JokeGrpc.newBlockingStub(channel);
    blockingStub3 = null;
    blockingStub4 = null;
    blockingStub5 = ConverterGrpc.newBlockingStub(channel);
    blockingStub6 = RiddleGrpc.newBlockingStub(channel);
    blockingStub7 = LibraryGrpc.newBlockingStub(channel);
  }

  public void askServerToParrot(String message) {

    ClientRequest request = ClientRequest.newBuilder().setMessage(message).build();
    ServerResponse response;
    try {
      response = blockingStub.parrot(request);
    } catch (Exception e) {
      System.err.println("RPC failed: " + e.getMessage());
      return;
    }
    System.out.println("Received from server: " + response.getMessage());
  }

  public void askConverter(double value, String fromUnit, String toUnit) {
    ConversionRequest request = ConversionRequest.newBuilder()
      .setValue(value)
      .setFromUnit(fromUnit)
      .setToUnit(toUnit)
      .build();
    ConversionResponse response;
    try {
      response = blockingStub5.convert(request);
    } catch (Exception e) {
      System.err.println("RPC failed");
      return;
    }
    if (response.getIsSuccess()) {
        System.out.println("Converted value: " + response.getResult());
    } else {
        System.out.println("Error: " + response.getError());
    }
  }

  public void askRiddle(BufferedReader reader) {
    try {
      RiddleQues question = blockingStub6.getRiddle(
        Empty.newBuilder().build()
      );
      if(question.getRiddle().equals("I am out of riddles...")){
        System.out.println(question.getRiddle());
        return;
      }
      System.out.println("Riddle: " + question.getRiddle());
      System.out.print("Your answer: ");
      String answer = reader.readLine();

      RiddleResult result = blockingStub6.answerRiddle(
        RiddleAnswer.newBuilder().setAnswer(answer).build()
      );

      System.out.println(result.getMessage());
    } catch (Exception e) {
        System.err.println("RPC failed: " + e.getMessage());
    }
  }

  public void addRiddle(String riddle, String answer){
    AddRiddleRequest req = AddRiddleRequest.newBuilder()
      .setRiddle(riddle)
      .setAnswer(answer)
      .build();

    AddRiddleResponse res;
    try {
      res = blockingStub6.addRiddle(req);
      System.out.println(res.getMessage());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
    }
  }

  public void askForJokes(int num) {
    JokeReq request = JokeReq.newBuilder().setNumber(num).build();
    JokeRes response;

    // just to show how to use the empty in the protobuf protocol
    Empty empt = Empty.newBuilder().build();

    try {
      response = blockingStub2.getJoke(request);
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
    System.out.println("Your jokes: ");
    for (String joke : response.getJokeList()) {
      System.out.println("--- " + joke);
    }
  }

  public void setJoke(String joke) {
    JokeSetReq request = JokeSetReq.newBuilder().setJoke(joke).build();
    JokeSetRes response;

    try {
      response = blockingStub2.setJoke(request);
      System.out.println(response.getOk());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void listBooks() {
    try {
        BookListResponse response = blockingStub7.listBooks(Empty.newBuilder().build());

        if (!response.getIsSuccess()) {
          System.out.println(response.getError());
          return;
        }
        for (Book b : response.getBooksList()) {
            System.out.println(b.getTitle() + 
              " | " + b.getAuthor() +
              " | " + b.getIsbn() +
              (b.getIsBorrowed() ? " (Borrowed by " + b.getBorrowedBy() + ")" : ""));
        }
    } catch (Exception e) {
        System.err.println("RPC failed: " + e);
    }
  }

  public void searchBooks(String query) {
    try {
        BookSearchRequest req = BookSearchRequest.newBuilder()
          .setQuery(query)
          .build();
        BookListResponse response = blockingStub7.searchBooks(req);
        if (!response.getIsSuccess()) {
          System.out.println(response.getError());
          return;
        }
        for (Book b : response.getBooksList()) {
          System.out.println(b.getTitle() + " | " + b.getAuthor());
        }
    } catch (Exception e) {
        System.err.println("RPC failed: " + e);
    }
  }

  public void borrowBook(String isbn, String name) {
    try {
        BorrowRequest req = BorrowRequest.newBuilder()
          .setIsbn(isbn)
          .setBorrowerName(name)
          .build();
        BorrowResponse res = blockingStub7.borrowBook(req);
        if (!res.getIsSuccess()) {
          System.out.println(res.getError());
        } else {
          System.out.println(res.getMessage());
        }
    } catch (Exception e) {
        System.err.println("RPC failed: " + e);
    }
  }

  public void returnBook(String isbn) {
    try {
        ReturnRequest req = ReturnRequest.newBuilder()
          .setIsbn(isbn)
          .build();

        ReturnResponse res = blockingStub7.returnBook(req);
        if (!res.getIsSuccess()) {
          System.out.println(res.getError());
        } else {
          System.out.println(res.getMessage());
        }
    } catch (Exception e) {
        System.err.println("RPC failed: " + e);
    }
  }

  public void getNodeServices() {
    GetServicesReq request = GetServicesReq.newBuilder().build();
    ServicesListRes response;
    try {
      response = blockingStub4.getServices(request);
      System.out.println(response.toString());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void getServices() {
    GetServicesReq request = GetServicesReq.newBuilder().build();
    ServicesListRes response;
    try {
      response = blockingStub3.getServices(request);
      System.out.println(response.toString());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void findServer(String name) {
    FindServerReq request = FindServerReq.newBuilder().setServiceName(name).build();
    SingleServerRes response;
    try {
      response = blockingStub3.findServer(request);
      System.out.println(response.toString());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void findServers(String name) {
    FindServersReq request = FindServersReq.newBuilder().setServiceName(name).build();
    ServerListRes response;
    try {
      response = blockingStub3.findServers(request);
      System.out.println(response.toString());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 6) {
      System.out
          .println("Expected arguments: <host(String)> <port(int)> <regHost(string)> <regPort(int)> <message(String)> <regOn(bool)>");
      System.exit(1);
    }
    int port = 9099;
    int regPort = 9003;
    String host = args[0];
    String regHost = args[2];
    String message = args[4];
    try {
      port = Integer.parseInt(args[1]);
      regPort = Integer.parseInt(args[3]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port] must be an integer");
      System.exit(2);
    }

    // Create a communication channel to the server (Node), known as a Channel. Channels
    // are thread-safe
    // and reusable. It is common to create channels at the beginning of your
    // application and reuse
    // them until the application shuts down.
    String target = host + ":" + port;
    ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
        // Channels are secure by default (via SSL/TLS). For the example we disable TLS
        // to avoid
        // needing certificates.
        .usePlaintext().build();

    String regTarget = regHost + ":" + regPort;
    ManagedChannel regChannel = ManagedChannelBuilder.forTarget(regTarget).usePlaintext().build();
    try {

      // ##############################################################################
      // ## Assume we know the port here from the service node it is basically set through Gradle
      // here.
      // In your version you should first contact the registry to check which services
      // are available and what the port
      // etc is.

      /**
       * Your client should start off with 
       * 1. contacting the Registry to check for the available services
       * 2. List the services in the terminal and the client can
       *    choose one (preferably through numbering) 
       * 3. Based on what the client chooses
       *    the terminal should ask for input, eg. a new sentence, a sorting array or
       *    whatever the request needs 
       * 4. The request should be sent to one of the
       *    available services (client should call the registry again and ask for a
       *    Server providing the chosen service) should send the request to this service and
       *    return the response in a good way to the client
       * 
       * You should make sure your client does not crash in case the service node
       * crashes or went offline.
       */

      // Just doing some hard coded calls to the service node without using the
      // registry
      // create client
      Client client = new Client(channel, regChannel);

      // call the parrot service on the server
      boolean active = true;
      while(active){
        System.out.println("Select a service: ");
        System.out.println("1: Echo");
        System.out.println("2: Jokes");
        System.out.println("3: Convert");
        System.out.println("4: Riddles");
        System.out.println("5: Library");
        System.out.println("6: Quit");

        int choice = -1;
        Scanner scanner = new Scanner(System.in);
        try {
          choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
          choice = -1;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        switch (choice) {
          case 1:
            System.out.println("Enter thy message");
            String echo = reader.readLine();
            client.askServerToParrot(echo);  
            break;
          case 2:
            System.out.println("Pick choice: ");
            System.out.println("1: Ask for joke");
            System.out.println("2: Set joke");

            Scanner scnr = new Scanner(System.in);
            int choiceJoke = -1;
            try {
              choiceJoke = Integer.parseInt(scnr.nextLine().trim());
            } catch (NumberFormatException e) {
              choiceJoke = -1;
            }
            switch(choiceJoke){
              case 1:
                // ask the user for input how many jokes the user wants
                // Reading data using readLine
                System.out.println("How many jokes would you like?"); // NO ERROR handling of wrong input here.
                String num = reader.readLine();
                int jokeNum;
                try {
                  jokeNum = Integer.parseInt(num.trim());
                  if(jokeNum <= 0){
                    System.out.println("Enter positive number");
                    break;
                  }
                } catch (NumberFormatException e){
                  System.out.println("Enter only numbers");
                  break;
                }
                // calling the joked service from the server with num from user input
                client.askForJokes(jokeNum);
                break;
              case 2:
                // adding a joke to the server
                System.out.println("Enter thy joke");
                String joke = reader.readLine();
                client.setJoke(joke);
                break;
              default:
                System.out.println("Invalid choice. Please try again.");  
                break;
            }
            // showing 6 joked
            //client.askForJokes(Integer.valueOf(6));
            break;
          case 3:
            System.out.println("Enter number to convert: ");
            double value;
            try {
                value = Double.parseDouble(reader.readLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
                break;
            }
            System.out.println("Enter from unit: ");
            System.out.println("Possible units: ");
            System.out.println("Length: Kilometer, Mile, Yard, Foot");
            System.out.println("Weight: Kilogram, Pound");
            System.out.println("Temperature: Fahrenheit, Celsius");
            String fromUnit = reader.readLine();
            System.out.println("Enter to unit: ");
            System.out.println("Possible units: ");
            System.out.println("Length: Kilometer, Mile, Yard, Foot");
            System.out.println("Weight: Kilogram, Pound");
            System.out.println("Temperature: Fahrenheit, Celsius");
            String toUnit = reader.readLine();
            client.askConverter(value, fromUnit, toUnit);
            break;
          case 4:
            boolean ridActive = true;
            while(ridActive){
              System.out.println("Pick choice: ");
              System.out.println("1: Get Riddle");
              System.out.println("2: Add Riddle");
              System.out.println("3: Quit Riddles");

              Scanner scnrR = new Scanner(System.in);
              int choiceR = -1;
              try {
                  choiceR = Integer.parseInt(scnrR.nextLine().trim());
              } catch (NumberFormatException e) {
                  choiceR = -1;
              }
              switch (choiceR) {
                case 1: 
                  client.askRiddle(reader);
                  break;
                case 2:  
                  System.out.println("Enter riddle:");
                  String riddle = reader.readLine();
                  System.out.println("Enter answer:");
                  String correct = reader.readLine();
                  client.addRiddle(riddle, correct);
                  break;
                case 3:
                  ridActive = false;
                  break;  
                default:
                  System.out.println("Invalid choice. Please try again.");
                  break;
              }
            }
            break;
          case 5:
            boolean libActive = true;
            while(libActive){
              System.out.println("Pick choice: ");
              System.out.println("1: List books");
              System.out.println("2: Search books");
              System.out.println("3: Borrow book");
              System.out.println("4: Return book");
              System.out.println("5: Quit Library");

              Scanner scnrL = new Scanner(System.in);
              int choiceL = -1;
              try {
                  choiceL = Integer.parseInt(scnrL.nextLine().trim());
              } catch (NumberFormatException e) {
                  choiceL = -1;
              }
              switch (choiceL) {
                case 1:
                  client.listBooks();
                  break;
                case 2:
                  System.out.println("Enter search query (title or author): ");
                  String query = reader.readLine();

                  if (query == null || query.trim().isEmpty()) {
                      System.out.println("missing field");
                      break;
                  }
                  client.searchBooks(query.trim());
                  break;
                case 3:
                  System.out.println("Enter ISBN: ");
                  String isbnBorrow = reader.readLine();
                  System.out.println("Enter borrower name: ");
                  String name = reader.readLine();

                  if (isbnBorrow == null || isbnBorrow.trim().isEmpty() ||
                      name == null || name.trim().isEmpty()) {
                      System.out.println("missing field");
                      break;
                  }
                  client.borrowBook(isbnBorrow.trim(), name.trim());
                  break;
                case 4:
                  System.out.println("Enter ISBN: ");
                  String isbnReturn = reader.readLine();
                  if (isbnReturn == null || isbnReturn.trim().isEmpty()) {
                      System.out.println("missing field");
                      break;
                  }
                  client.returnBook(isbnReturn.trim());
                  break;
                case 5:
                  libActive = false;
                  break;  
                default:
                  System.out.println("Invalid choice. Please try again.");
                  break;
              }
            }
            break;  
          case 6: 
            active = false;
            break;
          default:   
            System.out.println("Invalid choice. Please try again.");
        }
      }

      // list all the services that are implemented on the node that this client is connected to

      System.out.println("Services on the connected node. (without registry)");
      client.getNodeServices(); // get all registered services 

      // ############### Contacting the registry just so you see how it can be done

      if (args[5].equals("true")) { 
        // Comment these last Service calls while in Activity 1 Task 1, they are not needed and wil throw issues without the Registry running
        // get thread's services
        client.getServices(); // get all registered services 

        // get parrot
        client.findServer("services.Echo/parrot"); // get ONE server that provides the parrot service
        
        // get all setJoke
        client.findServers("services.Joke/setJoke"); // get ALL servers that provide the setJoke service

        // get getJoke
        client.findServer("services.Joke/getJoke"); // get ALL servers that provide the getJoke service

        // does not exist
        client.findServer("random"); // shows the output if the server does not find a given service
      }

    } finally {
      // ManagedChannels use resources like threads and TCP connections. To prevent
      // leaking these
      // resources the channel should be shut down when it will no longer be used. If
      // it may be used
      // again leave it running.
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
      if (args[5].equals("true")) { 
        regChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
      }
    }
  }
}
