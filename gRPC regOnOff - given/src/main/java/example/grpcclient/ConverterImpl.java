package example.grpcclient;

import io.grpc.stub.StreamObserver;
import service.*;

class ConverterImpl extends ConverterGrpc.ConverterImplBase{
    @Override
    public void convert(ConversionRequest req, StreamObserver<ConversionResponse> responseObserver) {
        System.out.println("Recieved from client: " + req.getValue());
        ConversionResponse.Builder response = ConversionResponse.newBuilder();

        double value = req.getValue();
        String from = req.getFromUnit().trim().toLowerCase();
        String to = req.getToUnit().trim().toLowerCase();

        if(req.getFromUnit().isEmpty()){
            response.setIsSuccess(false).setError("No from unit provided");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            return;
        }
        if(req.getToUnit().isEmpty()){
            response.setIsSuccess(false).setError("No to unit provided");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            return;
        }
        if(value == 0){
            response.setIsSuccess(false).setError("No value provided");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            return;
        }

        String fromType = getType(from);
        String toType = getType(to);

        if(fromType == null){
            response.setIsSuccess(false).setError("Unsupported unit " + from);
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            return;
        }
        if(toType == null){
            response.setIsSuccess(false).setError("Unsupported unit " + to);
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            return;
        }
        if(!fromType.equals(toType)){
            response.setIsSuccess(false).setError("Units do not match cannot convert");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            return;
        }
        if(from.equals(to)){
            response.setIsSuccess(false).setError("Same unit no need to convert");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            return;
        }

        double result = 0;
        System.out.println("Check after valiation");
        switch(fromType) {
            case "length":
                result = convertLength(value, from, to);
                break;
            case "weight":
                result = convertWeight(value, from, to);
                break;
            case "temperature":
                result = convertTemperature(value, from, to);
                if ((from.equals("celsius") && value < -273.15) || (from.equals("fahrenheit") && value < -459.67)) {
                    response.setIsSuccess(false).setError("temp below absolute zero");
                    responseObserver.onNext(response.build());
                    responseObserver.onCompleted();
                }
                break;        
        }

        result = Math.round(result * 100.0) / 100.0; 

        response.setIsSuccess(true).setResult(result);
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    private String getType(String unit) {
        switch (unit) {
            case "kilometer":
            case "mile":
            case "yard":
            case "foot":
                return "length";
            case "kilogram":
            case "pound":
                return "weight";
            case "celsius":
            case "fahrenheit":
                return "temperature";
            default:
                return null;
        }
    }

    private double convertLength(double value, String from, String to) {
        double meters = switch (from) {
            case "kilometer" -> value * 1000;
            case "mile" -> value * 1609.34;
            case "yard" -> value * 0.9144;
            case "foot" -> value * 0.3048;
            default -> 0;
        };

        return switch (to) {
            case "kilometer" -> meters / 1000;
            case "mile" -> meters / 1609.34;
            case "yard" -> meters / 0.9144;
            case "foot" -> meters / 0.3048;
            default -> 0;
        };
    }

    private double convertWeight(double value, String from, String to) {
        double kg = (from.equals("kilogram")) ? value : value / 2.20462;
        return (to.equals("kilogram")) ? kg : kg * 2.20462;
    }

    private double convertTemperature(double value, String from, String to) {
        if (from.equals("celsius") && to.equals("fahrenheit")) {
            return (value * 9 / 5) + 32;
        }
        if (from.equals("fahrenheit") && to.equals("celsius")) {
            return (value - 32) * 5 / 9;
        }
        return value;
    }
}
