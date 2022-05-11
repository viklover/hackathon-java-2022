package org.example.httpserver.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Main;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class RequestHandler implements HttpHandler {

    public JSONObject request = null;
    public Headers request_headers;
    public Headers response_headers;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        request_headers = exchange.getRequestHeaders();
        response_headers = exchange.getResponseHeaders();
        try {
            request = (JSONObject) new JSONParser().parse(parseParameters(exchange));
        } catch (ParseException e) {
            request = null;
        }
    }

    public StringBuilder getStringBuilderOfFile(String path) {
        StringBuilder response = new StringBuilder();

        try {
            File newFile = new File(Main.RESOURCES_DIRECTORY + path);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(newFile)));

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
                response.append("/n");
            }
            bufferedReader.close();
        } catch (IOException e) {

        }

        return response;
    }

    public void sendResponseBody(HttpExchange exchange, JSONObject jsonObject) throws IOException {
        sendResponseBody(exchange, 200, jsonObject.toString());
    }

    public void sendResponseBody(HttpExchange exchange, StringBuilder builder) throws IOException {
        sendResponseBody(exchange, 200, builder);
    }

    public void sendResponseBody(HttpExchange exchange, Integer rCode, StringBuilder builder) throws IOException {
        byte[] bytes = builder.toString().getBytes();
        exchange.sendResponseHeaders(rCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    public void sendResponseBody(HttpExchange exchange, Integer rCode, String string) throws IOException {
        byte[] bytes = string.getBytes();
        exchange.sendResponseHeaders(rCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    public String parseParameters(HttpExchange exchange) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader bufferReader = new BufferedReader(inputStreamReader);
        return bufferReader.readLine();
    }
}
