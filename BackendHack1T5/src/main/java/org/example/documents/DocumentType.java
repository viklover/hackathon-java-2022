package org.example.documents;

import org.example.Database;
import org.example.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentType {

    public int id;
    public String name;
    public Map<String, String> columns = new HashMap<>();

    public String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS `docs_type%s` (
            \t`id` INT unsigned NOT NULL AUTO_INCREMENT,
            \t`doc_id` INT unsigned NOT NULL,
            \t`author` TEXT NOT NULL,
            \t`version` INT unsigned NOT NULL DEFAULT 1, %s
            \tPRIMARY KEY (`id`)
            );""";

    public DocumentType(int type) {

        JSONObject info = readTypeConfigurations(type);

        id = (((Long) info.get("id")).intValue());
        name = (String) info.get("name");

        for (Object object : (JSONArray) info.get("fields")) {
            JSONArray pair = (JSONArray) object;
            columns.put((String) pair.get(0), (String) pair.get(1));
        }

        StringBuilder query_columns = new StringBuilder();

        for (String column: this.columns.keySet()) {
            query_columns.append("`").append(column).append("`");
            query_columns.append(" TEXT, ");
        };

        Database.executeUpdate(String.format(CREATE_TABLE, id, query_columns));
    }

    public JSONObject readTypeConfigurations(int type) {
        JSONParser jsonParser = new JSONParser();
        JSONObject info = new JSONObject();

        try (FileReader reader = new FileReader("types.json"))
        {
            for (Object object : (JSONArray) jsonParser.parse(reader)) {
                JSONObject type_object = (JSONObject) object;
                int current_id = (((Long) type_object.get("id")).intValue());

                if (type == current_id) {
                    info = type_object;
                    break;
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return info;
    }

    public JSONObject getJSON() {
        JSONObject data = new JSONObject();

        data.put("id", id);
        data.put("name", name);

        JSONObject fields = new JSONObject();

        for (String key : columns.keySet()) {
            fields.put(columns.get(key), key);
        }

        data.put("fields", fields);

        return data;
    }

    public JSONObject getConfig() {
        JSONObject data = new JSONObject();

        data.put("id", id);
        data.put("name", name);

        JSONArray fields = new JSONArray();

        for (String key : columns.keySet()) {
            JSONArray field = new JSONArray();
            field.add(key);
            field.add(columns.get(key));
            fields.add(field);
        }

        data.put("fields", fields);

        return data;
    }
}
