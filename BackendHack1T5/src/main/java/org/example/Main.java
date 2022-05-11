package org.example;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;

import org.example.documents.DocumentManager;
import org.example.httpserver.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.print.Doc;

public class Main {

    public static String MYSQL_URL;
    public static String MYSQL_USERNAME;
    public static String MYSQL_PASSWORD;
    public static String RESOURCES_DIRECTORY;
    public static String ROOT_DIRECTORY;

    public static int HTTPSERVER_PORT;

    public static Server server;

    public static void main(String[] args) throws Exception {
        loadConfigFile();

        Database.init();
        DocumentManager.init();

        uploadDocumentsFromResourcesFolder();

        server = new Server(8080);
        server.start();
    }

    public static void uploadDocumentsFromResourcesFolder() throws Exception {

        File[] files = new File(RESOURCES_DIRECTORY).listFiles(File::isFile);

        assert files != null;
        for (File file : files) {
            String csv_path = file.getAbsolutePath();
            String docs_type = csv_path.substring(csv_path.lastIndexOf(File.separator)+1, csv_path.lastIndexOf("."));

            DocumentManager.uploadDocuments(docs_type, csv_path);
        }
    }

    public static void loadConfigFile() throws IOException {
        JSONParser jsonParser = new JSONParser();

        Logger.print("Main", "Loading project configurations");

        ROOT_DIRECTORY = new File(".").getCanonicalPath();

        try (FileReader reader = new FileReader("config.json"))
        {
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            MYSQL_URL = (String) ((JSONObject) obj.get("mysql")).get("url");
            MYSQL_USERNAME = (String) ((JSONObject) obj.get("mysql")).get("user");
            MYSQL_PASSWORD = (String) ((JSONObject) obj.get("mysql")).get("password");
            RESOURCES_DIRECTORY = ROOT_DIRECTORY + File.separator + (String) obj.get("resources_directory");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
