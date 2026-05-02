# Completed Description

## How to Run

First Terminal

    gradle runNode

Second Terminal

    gradle runClient

## Description

This project allows users to choose from a few services via terminal inputs to receive results depending on the service chosen. Said services include echomessage, converter, joke, library, and riddle. The project covers all of the requirements listed in the assignment pdf. 
List of requirements:
    -Run service node as well as client through default values
    -Implement converter service 
    -Implement the library service 
    -Design your calls and user interaction in a way that they are easy(UI terminal)
    -Unit tests 
    -Library persistence
    -Handles errors(from what I have tested)
    -Implemented own service Riddles

## How it Works

After connecting the second terminal via the commands above the client will be able to choose from a few different services.
Echo:
    When selecting this option the user will be prompted to enter a message. After entering and sending this message, the user will get the message repeated back to them.
Joke:
    When selecting this option the user will be prompted to choose either to Ask for a Joke or Set a Joke. If Ask for a Joke is selected the user will be then asked "How many jokes would you like?". Upon selecting a number that many number of jokes will be give. If it exceeds the available jokes they will get a "I am out of jokes..." message. If the second option is chosen then the user can add their own joke to the list which can be seen when again choosing the Ask for a Joke selection again.
Convert:
    When selecting this option the user will be prompted to enter a value. Then to enter a "from" unit as well as a "to" unit. If the values are of similar type (weight, length, etc.) then the number will be converted into the "to" unit. 
Riddles:
    Similar to the joke service the user has a choice of getting or setting a riddle. If the Get Riddle option is chosen the user will be prompted to answer a riddle. After sending an answer the user will be told whether they were correct or not. If not then the answer will be given. This is very case sensitive so answer carefully. If Add Riddle is chosen then the user will be prompted to add a riddle along with an answer. Like the joke service you will have an opportunity to then see your riddle if choosing the previous option.
Library:
    When this is chosen you have a few options to choose from. If List Books is chosen then all books will be listed. If a book is borrowed then you will see that next to said book. Search Books allows a user to search for a book via title or author name(first, last or both). If Borrow Book is chosen then the user will be prompted to enter a ISBN as well as name to be borrowed under. If Return book is chosen then a borrowed book can be returned via the ISBN. 

## Video Link

[Video](link)

# GRPC Services and Registry

The following folder contains a Registry.jar which includes a Registering service where Nodes can register to allow clients to find them and use their implemented GRPC services. 

Some more detailed explanations will follow and please also check the build.gradle file

## Run things locally without registry
To run see also video. To run locally and without Registry which you should do for the beginning

First Terminal

    gradle runNode

Second Terminal

    gradle runClient

## Run things locally with registry

First terminal

    gradle runRegistryServer

Second terminal

    gradle runNode -PregOn=true 

Third Terminal

    gradle runClient -PregOn=true

### gradle runRegistryServer
Will run the Registry node on localhost (arguments are possible see gradle). This node will run and allows nodes to register themselves. 

The Server allows Protobuf, JSON and gRPC. We will only be using gRPC

### gradle runNode
Will run a node with services. The starter code includes Echo and Joke services as examples. You will need to implement and add the Converter and Library services.

For the Library service: A books.txt file is provided with initial book data (format: title|author|isbn, one per line). Your server should load this on first run and create library_data.json for persistence.

The node registers itself on the Registry. You can change the host and port the node runs on and this will register accordingly with the Registry

### gradle runClient
Will run a client which will call the services from the node, it talks to the node directly not through the registry. At the end the client does some calls to the Registry to pull the services, this will be needed later.

### gradle runDiscovery
Will create a couple of threads with each running a node with services in JSON and Protobuf. This is just an example and not needed for assignment 6. 

### gradle testProtobufRegistration
Registers the protobuf nodes from runDiscovery and do some calls. 

### gradle testJSONRegistration
Registers the json nodes from runDiscovery and do some calls. 

### gradle test
Runs the test cases. The starter code includes example tests for Joke and Echo in ServerTest.java. You need to add your own tests for Converter and Library services in the same file.

IMPORTANT: Tests expect the server to be running first!
First run in one terminal:
    gradle runNode
Then in second terminal:
    gradle test

The tests connect to localhost:8000 by default.

To run in IDE:
- go about it like in the ProtoBuf assignment to get rid of errors
- all mains expect input, so if you want to run them in your IDE you need to provide the inputs for them, see build.gradle