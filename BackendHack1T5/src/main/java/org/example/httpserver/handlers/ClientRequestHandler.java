package org.example.httpserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.example.documents.Document;
import org.example.documents.DocumentManager;
import org.example.documents.DocumentType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

public class ClientRequestHandler extends RequestHandler {

    public void handle(HttpExchange exchange) throws IOException {
        super.handle(exchange);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

//        if (request == null || !request.containsKey("request")) {
//            System.out.println(exchange.getRequestURI().toString());
//            sendResponseBody(exchange, getStringBuilderOfFile(exchange.getRequestURI().toString()));
//            return;
//        }

        JSONObject response = new JSONObject();
        response.put("response", (String) request.get("request"));

        System.out.println(request);

        JSONArray documents = new JSONArray();
        int doc_id;

        System.out.println((String) request.get("request"));

        switch ((String) request.get("request")) {
            case "get_documents":
                System.out.println(DocumentManager.getAllDocuments().size());
                for (Document document : DocumentManager.getAllDocuments()) {
                    documents.add(document.getJSON());
                }
                response.put("documents", documents);
                break;

            case "get_types_documents":
                JSONArray types_obj = new JSONArray();
                List<DocumentType> types = DocumentManager.getDocumentTypes();
                for (DocumentType type : types) {
                    types_obj.add(type.getJSON());
                }
                response.put("types", types_obj);
                break;

            case "get_documents_by_type":
                documents = new JSONArray();
                int type = (((Long) request.get("type")).intValue());
                for (Document document : DocumentManager.getDocumentsByType(type)) {
                    documents.add(document.getJSON());
                }
                response.put("documents", documents);
                break;

            case "get_document_by_id":
                int id = Integer.parseInt(String.valueOf(request.get("id")));
                response.put("document", DocumentManager.getDocumentById(id).getJSON(true));
                break;

            case "set_document_fields":
                doc_id = Integer.parseInt(String.valueOf(request.get("id")));
                String author = String.valueOf(request.get("author"));
                boolean checked = Boolean.parseBoolean(String.valueOf(request.get("checked")));

                Document document = DocumentManager.getDocumentById(doc_id);
                DocumentType type_config = DocumentManager.types.get(document.type);

                for (String field : type_config.columns.keySet()) {
                    System.out.print(field);
                    System.out.print(": ");
                    System.out.println(String.valueOf(((JSONObject)request.get("fields")).get(field)));
                    document.setField(field, String.valueOf(((JSONObject) request.get("fields")).get(field)));
                }

                document.setChecked(checked);

                try {
                    document.save(author);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;

            case "get_document_history":
                doc_id = Integer.parseInt(String.valueOf(request.get("id")));
                try {
                    response.put("versions", DocumentManager.getDocumentById(doc_id).getHistory());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
        }

        System.out.println(response);

        exchange.getResponseHeaders().add("Content-Type", "application/json");

        sendResponseBody(exchange, response);
    }
}
