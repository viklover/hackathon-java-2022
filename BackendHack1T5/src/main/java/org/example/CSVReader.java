package org.example;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class CSVReader {
    private List<String[]> allData;

    public CSVReader(String s) throws  Exception {
        this(s, "cp1251");
    }
    public CSVReader(String s, String CHARSET) throws Exception {
        FileReader fileReader = new FileReader(s, Charset.forName(CHARSET));

        try {
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(';')
                    .build();

            com.opencsv.CSVReader csvReader = new CSVReaderBuilder(fileReader)
                    .withCSVParser(parser)
                    .build();

            allData = csvReader.readAll();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public JSONObject get_json() {
        JSONObject json = new JSONObject();
        JSONArray list = new JSONArray();

        list.addAll(Arrays.asList(allData.get(0)));
        json.put("columns", list);

        list = new JSONArray();
        JSONArray line = new JSONArray();

        for (int i = 1; i < allData.size(); i++) {
            for (String l : allData.get(i)) {
                line.add(l);
            }
            list.add(line);
            line = new JSONArray();
        }
        json.put("documents", list);
        return json;
    }
}
