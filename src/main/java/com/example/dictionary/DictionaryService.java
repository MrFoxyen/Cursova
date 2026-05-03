package com.example.dictionary;

import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DictionaryService {
    private final Map<String, List<String>> enToUk = new TreeMap<>();
    private final Map<String, List<String>> ukToEn = new TreeMap<>();
    private static final String FILE_NAME = "dictionary.txt";

    public DictionaryService() {
        loadFromFile();
    }

    public void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            parseData(br);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            enToUk.clear(); ukToEn.clear();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(":")) {
                    sb.append(line).append("\n");
                    processLine(line);
                }
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_NAME), StandardCharsets.UTF_8))) {
            writer.write(sb.toString());
        }
    }

    private void parseData(BufferedReader br) throws IOException {
        enToUk.clear(); ukToEn.clear();
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains(":")) processLine(line);
        }
    }

    private void processLine(String line) {
        String[] parts = line.split(":");
        String eng = parts[0].trim().toLowerCase();
        List<String> ukrList = Arrays.asList(parts[1].split(",\\s*"));
        enToUk.put(eng, new ArrayList<>(ukrList));
        for (String ukr : ukrList) {
            ukToEn.computeIfAbsent(ukr.trim().toLowerCase(), k -> new ArrayList<>()).add(eng);
        }
    }

    public List<String> translate(String word, String mode) {
        return "en-uk".equals(mode) ? enToUk.get(word.toLowerCase().trim()) : ukToEn.get(word.toLowerCase().trim());
    }
}