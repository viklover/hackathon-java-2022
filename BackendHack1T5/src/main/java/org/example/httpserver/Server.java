package org.example.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.example.Logger;
import org.example.httpserver.handlers.ClientRequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Server {

    private int port = 8080;
    private int backlog = 128;

    private HttpServer server;

    public Server() throws IOException {
        this(8080, 128);
    }

    public Server(int port) throws IOException {
        this(port, 128);
    }

    public Server(int port, int backlog) throws IOException {
        this.port = port;
        this.backlog = backlog;
        initHttpServer();
        initHandlers();
    }

    private void initHttpServer() throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(port), backlog);
        server.setExecutor(null);
    }

    private void initHandlers() {
        server.createContext("/", new ClientRequestHandler());
    }

    public void start() {
        Logger.print("HttpServer", "Listening requests");
        server.start();
    }
}
