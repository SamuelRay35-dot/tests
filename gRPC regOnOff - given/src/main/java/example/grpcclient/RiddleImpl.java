package example.grpcclient;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerMethodDefinition;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.Empty;

import service.*;
import java.util.Stack;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import buffers.RequestProtos.Request;
import buffers.RequestProtos.Request.RequestType;
import buffers.ResponseProtos.Response;


class RiddleImpl extends RiddleGrpc.RiddleImplBase {
    
    private Stack<String> riddles = new Stack<>();
    private Stack<String> answers = new Stack<>();
    
    private boolean answered = false;
    
    public RiddleImpl(){
        super();
        riddles.add("What has to be broken before you can use it?");
        answers.add("an egg");
        riddles.add("What month of the year has 28 days?");       
        answers.add("all of them");
    }
    
    @Override
    public void getRiddle(Empty req, StreamObserver<RiddleQues> responseObserver) {
        if (riddles.isEmpty()) {
            responseObserver.onNext(
                RiddleQues.newBuilder()
                    .setRiddle("I am out of riddles...")
                    .build()
            );
            responseObserver.onCompleted();
            return;
        }
        answered = false;
        responseObserver.onNext(
            RiddleQues.newBuilder()
                .setRiddle(riddles.pop())
                .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void answerRiddle(RiddleAnswer req, StreamObserver<RiddleResult> responseObserver) {
        if (answered) {
            responseObserver.onNext(
                RiddleResult.newBuilder()
                    .setIsCorrect(false)
                    .setMessage("You already answered. Only one attempt allowed.")
                    .build()
            );
            responseObserver.onCompleted();
            return;
        }
        answered = true;

        String correctAnswer = answers.pop();
        String userAnswer = req.getAnswer().trim().toLowerCase();
        boolean isCorrect = userAnswer.equals(correctAnswer);

        responseObserver.onNext(
            RiddleResult.newBuilder()
                .setIsCorrect(isCorrect)
                .setMessage(isCorrect ? "Correct!" : "Wrong! Answer: " + correctAnswer)
                .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void addRiddle(AddRiddleRequest req, StreamObserver<AddRiddleResponse> responseObserver) {
        AddRiddleResponse.Builder response = AddRiddleResponse.newBuilder();

        if (req.getRiddle().isEmpty() || req.getAnswer().isEmpty()) {
            response.setSuccess(false)
                .setMessage("missing field");
        } else {
            riddles.add(req.getRiddle().trim());
            answers.add(req.getAnswer().trim().toLowerCase());
            response.setSuccess(true)
              .setMessage("riddle added successfully");
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
} 
