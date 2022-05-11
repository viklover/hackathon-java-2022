package org.example.documents;

import org.example.CSVReader;
import org.example.Database;
import org.example.Logger;
import org.example.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.xml.crypto.Data;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/*
 МЕНЕДЖЕР НУЖЕН, ЧТОБЫ СОЗДАВАТЬ НОВЫЕ ТИПЫ ДОКУМЕНТОВ и ПОЛУЧАТЬ ХАРАКТЕРИСТИКИ ТИПОВ
*/

public class DocumentManager {

    public static Map<Integer, Document> documents = new HashMap<>();
    public static Map<Integer, DocumentType> types = new HashMap<>();

    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS `docs` (
            `id` INT unsigned NOT NULL AUTO_INCREMENT,
            `name` TEXT NOT NULL,
            `type` INT NOT NULL,
            `author` TEXT NOT NULL,
            `current_version` INT unsigned NOT NULL DEFAULT 1,
            `was_uploaded` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            `was_updated` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            `checked` BOOLEAN NOT NULL DEFAULT 0,
            `path` TEXT NOT NULL,
            PRIMARY KEY (`id`)
        );""";

    public static void init() throws IOException, ParseException, SQLException {
        Logger.print("DocumentManager", "Creating table");
        Database.executeUpdate(CREATE_TABLE);

        Logger.print("DocumentManager", "Loading existing document types from 'types.json'");

        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader("types.json");
        JSONArray list = (JSONArray) jsonParser.parse(reader);

        for (int i = 0; i < list.size(); ++i) {
            types.put(i, new DocumentType(i));
        }

        initDocuments();
    }

    public static void initDocuments() throws SQLException {

        Logger.print("DocumentManager", "Initialization all instances of documents");

        ResultSet response = Database.executeQuery("SELECT * FROM docs;");

        while (response.next()) {
            Document document = new Document().withId((response.getInt("id")))
                    .withType(response.getInt("type"))
                    .withName(response.getString("name"))
                    .withPath(response.getString("path"))
                    .withAuthor(response.getString("author"))
                    .withCurrentVersion(response.getInt("current_version"))
                    .withChecked(response.getBoolean("checked"))
                    .withDateUpload(response.getTimestamp("was_uploaded").getTime())
                    .withDateUpdate(response.getTimestamp("was_updated").getTime())
                    .updateFieldsFromDB();

            documents.put(document.id, document);
        }

        response.close();
    }

    public static void uploadDocuments(String type_name, String csv_path) throws Exception {
        CSVReader reader = new CSVReader(csv_path);
        JSONObject response = reader.get_json();

        JSONArray columns = (JSONArray) response.get("columns");
        JSONArray docs = (JSONArray) response.get("documents");

        DocumentType documentType = getTypeDocumentByColumns(type_name, columns);

        System.out.println("---- " + type_name);

        for (Object obj: docs) {
            JSONArray fields = (JSONArray) obj;

            Document doc = new Document();
            doc.withAuthor("root");
            doc.withType(documentType.id);

            boolean is_valid = true;
            boolean already_exists = false;

            for (int i = 0; i < columns.size() && !already_exists && is_valid; ++i) {
                try {
                    String column = (String) columns.get(i);
                    String field = (String) fields.get(i);

                    if (column.toLowerCase(Locale.ROOT).equals("файл")) {
                        doc.withPath(String.format("%s/%s", type_name, field));
                        doc.withName(field);
                        already_exists = getDocumentByName(field) != null;
                    } else {
                        doc.setField("field"+(i+1), field.replaceAll("'", ""));
                    }
                } catch (java.lang.IndexOutOfBoundsException e) {
                    is_valid = false;
                }
            }

            if (!already_exists && is_valid) {
                uploadDocument(doc);
            }
        }
    }

    public static DocumentType getTypeDocumentByColumns(String type_name, JSONArray columns) {

        for (Integer type: types.keySet()) {
            boolean is_equals = (types.get(type).columns.size() + 1) == columns.size();

            if (!is_equals) {
                continue;
            }

            for (int i = 0; i < columns.size() && is_equals; ++i) {
                String value = (String) columns.get(i);
                if (!types.get(type).columns.containsValue(value) && !value.toLowerCase(Locale.ROOT).equals("файл")) {
                    is_equals = false;
                }
            }

            if (is_equals) {
                return types.get(type);
            }
        }

        return createNewDocumentType(type_name, columns);
    }

    public static DocumentType createNewDocumentType(String type_name, JSONArray new_fields) {

        JSONArray array = new JSONArray();

        try (FileWriter file = new FileWriter("types.json")) {

            for (Integer type: types.keySet()) {
                array.add(types.get(type).getConfig());
            }

            JSONObject obj = new JSONObject();
            obj.put("id", types.size());
            obj.put("name", type_name);

            JSONArray fields = new JSONArray();

            for (int i = 0; i < new_fields.size(); ++i) {
                if (!((String) new_fields.get(i)).toLowerCase(Locale.ROOT).equals("файл")) {
                    JSONArray field = new JSONArray();
                    field.add("field" + (i+1));
                    field.add(new_fields.get(i));
                    fields.add(field);
                }
            }

            obj.put("fields", fields);
            array.add(obj);

            file.write(array.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        DocumentType documentType = new DocumentType(types.size());
        types.put(types.size(), documentType);
        return documentType;
    }

    public static int uploadDocument(Document document) throws SQLException {

        String INSERT_INTO = "INSERT INTO `docs` SET `author`='"+document.author+"', `name`='"+document.name+"', `path`='"+document.path+"', `type`="+document.type+"; SELECT ID AS LastID FROM docs WHERE ID = @@Identity;";

        Statement statement = Database.executeStatement(INSERT_INTO);
        statement.getMoreResults();

        ResultSet result = statement.getResultSet();
        result.next();

        int id = result.getInt("LastID");
        statement.close();

        String INSERT_INTO_DOCS_TYPE = "INSERT INTO `docs_type" + document.type + "` SET `doc_id`="+id+", `author`='"+document.current_version_author+"' %s;";

        String fields = (document.getFieldsMap().size() != 0 ? ", " : "");

        boolean first = true;
        for (String field : document.getFieldsMap().keySet()) {
            if (!first) {
                fields += ", ";
            }
            fields += "`" + field + "`='" + document.getField(field) + "'";
            first = false;
        }

        INSERT_INTO_DOCS_TYPE = String.format(INSERT_INTO_DOCS_TYPE, fields);

        Database.executeUpdate(INSERT_INTO_DOCS_TYPE);

        document.withId(id);
        documents.put(id, document);

        return id;
    }


    public static Document getDocumentById(int id) {
        return documents.get(id);
    }

    public static Document getDocumentByName(String name) {
        for (Document document : getAllDocuments()) {
            if (document.name.equals(name))
                return document;
        }
        return null;
    }

    public static List<Document> getDocumentsByType(int type) {

        List<Document> data = new ArrayList<>();

        for (Document document : documents.values()) {
            if (document.type == type) {
                data.add(document);
            }
        }

        return data;
    }

    public static List<Document> getAllDocuments() {
        return documents.values().stream().toList();
    }

    public static List<DocumentType> getDocumentTypes() {
        return types.values().stream().toList();
    }
}
