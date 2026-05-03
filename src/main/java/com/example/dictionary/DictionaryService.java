package com.example.dictionary;

import org.springframework.stereotype.Service;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DictionaryService {
    private final Map<String, List<String>> enToUk = new TreeMap<>();
    private final Map<String, List<String>> ukToEn = new TreeMap<>();
    private static final String FILE_NAME = "dictionary.txt";

    public DictionaryService() {
        // зчитуємо локальний файл
        loadFromFile();
    }

    //  Читання локального файлу
    public void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("Локальний файл не знайдено. Словник порожній.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            parseAndFillMaps(br, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromUrl(String urlString) throws Exception {
        java.net.URL url = new java.net.URL(urlString);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();


        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestMethod("GET");

        StringBuilder contentForFile = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            enToUk.clear();
            ukToEn.clear();

            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(":")) {
                    contentForFile.append(line).append("\n");
                    processLine(line);
                }
            }
            System.out.println("Синхронізація з хмарою успішна!");
        } catch (Exception e) {

            System.err.println("ПОМИЛКА МЕРЕЖІ: " + e.getMessage());
            e.printStackTrace();
            throw e; // Передаємо помилку далі в контролер
        }

        // Збереження у файл
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_NAME), StandardCharsets.UTF_8))) {
            writer.write(contentForFile.toString());
        }
    }

    // Допоміжний метод для розбору рядка
    private void processLine(String line) {
        String[] parts = line.split(":");
        String eng = parts[0].trim().toLowerCase();
        List<String> ukrList = Arrays.asList(parts[1].split(",\\s*"));

        enToUk.put(eng, new ArrayList<>(ukrList));
        for (String ukr : ukrList) {
            ukToEn.computeIfAbsent(ukr.trim().toLowerCase(), k -> new ArrayList<>()).add(eng);
        }
    }

    // Універсальний парсер
    private void parseAndFillMaps(BufferedReader br, boolean clearFirst) throws IOException {
        if (clearFirst) { enToUk.clear(); ukToEn.clear(); }
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains(":")) processLine(line);
        }
    }

    public List<String> translate(String word, String mode) {
        String q = word.toLowerCase().trim();
        return "en-uk".equals(mode) ? enToUk.get(q) : ukToEn.get(q);
    }
}