package org.example.documents;

import org.example.Database;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Document {

    public int id;
    public int type;
    public String name;
    public String author;
    public String path;

    public int current_version = 1;
    public String current_version_author;

    private Map<String, String> fields = new HashMap<>();
    public boolean fields_were_updated = false;

    public long was_uploaded;
    public long was_updated;

    public boolean checked = false;
    private boolean checked_was_updated = false;

    public Document() {

    }

    public Document withId(int id) {
        this.id = id;
        return this;
    }

    public Document withType(int type) {
        this.type = type;
        return this;
    }

    public Document withName(String name) {
        this.name = name;
        return this;
    }

    public Document withAuthor(String author) {
        this.author = author;
        this.current_version_author = author;
        return this;
    }

    public Document withDateUpload(long was_uploaded) {
        this.was_uploaded = was_uploaded;
        return this;
    }

    public Document withDateUpdate(long was_updated) {
        this.was_updated = was_updated;
        return this;
    }

    public Document withCurrentVersion(int version) {
        this.current_version = version;
        return this;
    }

    public Document withPath(String path) {
        this.path = path;
        return this;
    }

    public Document withChecked(boolean checked) {
        this.checked = checked;
        return this;
    }

    public Document setChecked(boolean checked) {
        this.checked = checked;
        checked_was_updated = true;
        return this;
    }


    public boolean hasRecord() throws SQLException {
        String sql = "SELECT 1 FROM `docs` WHERE `id`=" + id + ";";
        Statement statement = Database.getConnection().createStatement();
        return statement.executeQuery(sql).next();
    }


    public void save(String author) throws SQLException {
        this.current_version_author = author;
        this.save();
    }

    private void save() throws SQLException {

        if (!hasRecord()) {
            this.id = DocumentManager.uploadDocument(this);
            return;
        }

        if (checked_was_updated) {
            String UPDATE_CHECKED = "UPDATE `docs` SET `checked`=" + (checked ? 1 : 0) + " WHERE `id`=" + id + ";";
            Database.executeUpdate(UPDATE_CHECKED);
        }

        if (fields_were_updated) {
            current_version++;

            String UPDATE = "UPDATE `docs` SET `current_version`=" + current_version + " WHERE `id`=" + id + ";";
            Database.executeUpdate(UPDATE);

            String INSERT_INTO = "INSERT INTO `docs_type"+type+"` SET `doc_id`="+id+", `version`="+current_version+", `author`='"+current_version_author+"' %s;";

            String fields = (this.fields.size() != 0 ? ", " : "");

            boolean first = true;
            for (String field : this.fields.keySet()) {
                if (!first) {
                    fields += ", ";
                }
                fields += "`" + field + "`='" + this.fields.get(field) + "'";
                first = false;
            }

            INSERT_INTO = String.format(INSERT_INTO, fields);

            Database.executeUpdate(INSERT_INTO);
        }
    }

    public Document updateFieldsFromDB() throws SQLException {
        this.fields = getFieldsFromDB();
        return this;
    }

    public Map<String, String> getFieldsFromDB() throws SQLException {

        Map<String, String> fields = new HashMap<>();

        DocumentType type_configs = DocumentManager.types.get(type);

        String query = "SELECT `author`, %s FROM `docs_type" + type_configs.id + "` WHERE `doc_id`=" + id + " ORDER BY id DESC LIMIT 1;";

        ResultSet response = Database.executeQuery(String.format(query, String.join(", ",  type_configs.columns.keySet().stream().map(i -> "`"+i+"`").toList())));

        if (response.next()) {
            for (String column : type_configs.columns.keySet()) {
                if (column.equals("author")) {
                    current_version_author = response.getString(column);
                }
                fields.put(column, response.getString(column));
            }
        }

        response.close();

        return fields;
    }


    public void setField(String field, String value) {
        fields.put(field, value);
        fields_were_updated = true;
    }

    public String getField(String field) {
        if (fields.containsKey(field))
            return fields.get(field);
        return null;
    }

    public Map<String, String> getFieldsMap() {
        return this.fields;
    }


    public JSONArray getHistory() throws SQLException {
        String SELECT_ALL_VERSIONS = "SELECT `version`, `author`, %s FROM `docs_type"+type+"` WHERE `doc_id`="+id+";";
        DocumentType type_configs = DocumentManager.types.get(type);
        ResultSet data = Database.executeQuery(String.format(SELECT_ALL_VERSIONS, String.join(", ",  type_configs.columns.keySet().stream().map(i -> "`"+i+"`").toList())));

        JSONArray history = new JSONArray();

        while (data.next()) {
            JSONArray revision = new JSONArray();
            revision.add(data.getInt("version"));
            revision.add(data.getString("author"));

            for (String field : type_configs.columns.keySet()) {
                revision.add(data.getString(field));
            }
            history.add(revision);
        }

        return history;
    }

    public JSONObject getJSON() {
        return getJSON(false);
    }

    public JSONObject getJSON(boolean with_fields) {
        JSONObject data = new JSONObject();
        DocumentType type_config = DocumentManager.types.get(type);
        data.put("id", id);
        data.put("name", name);
        data.put("type", type);
        data.put("path", path);
        data.put("type_name", type_config.name);
        data.put("author", author);
        data.put("checked", checked);
        data.put("current_version", current_version);
        data.put("was_uploaded", was_uploaded);
        data.put("was_updated", was_updated);

        JSONObject fields = new JSONObject();

        if (with_fields) {
            for (String column: this.fields.keySet()) {
                JSONArray map_and_value = new JSONArray();
                map_and_value.add(type_config.columns.get(column));
                map_and_value.add(this.fields.get(column));
                fields.put(column, map_and_value);
            }
            data.put("fields", fields);
        }

        return data;
    }
}
