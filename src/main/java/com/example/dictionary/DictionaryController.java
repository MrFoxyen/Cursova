package com.example.dictionary;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller // МАЄ БУТИ САМЕ @Controller
public class DictionaryController {
    private final DictionaryService service;

    public DictionaryController(DictionaryService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String word,
                        @RequestParam(required = false) String mode,
                        HttpSession session, Model model) {

        // Безпечна ініціалізація обраного
        @SuppressWarnings("unchecked")
        Set<String> favorites = (Set<String>) session.getAttribute("favorites");
        if (favorites == null) {
            favorites = new LinkedHashSet<>();
            session.setAttribute("favorites", favorites);
        }
        model.addAttribute("favorites", favorites);

        if (word != null && !word.isBlank() && mode != null) {
            model.addAttribute("results", service.translate(word, mode));
            model.addAttribute("lastWord", word);
            model.addAttribute("currentMode", mode);
        }

        return "index"; // Spring шукатиме файл templates/index.html
    }

    @PostMapping("/add-favorite")
    public String addFavorite(@RequestParam String word, @RequestParam String mode, HttpSession session) {
        @SuppressWarnings("unchecked")
        Set<String> favorites = (Set<String>) session.getAttribute("favorites");
        if (favorites != null && word != null && !word.isBlank()) {
            favorites.add(word + ":" + mode);
        }
        String encodedWord = URLEncoder.encode(word, StandardCharsets.UTF_8);
        return "redirect:/?word=" + encodedWord + "&mode=" + mode;
    }

    @GetMapping("/cloud-update")
    public String cloudUpdate() {
        try {
            String cloudUrl = "https://gist.githubusercontent.com/Alex-Cloud-Dev/7e5f3f888f8d9f1c7d2c3e4b5a6f7e8d/raw/dictionary.txt";
            service.loadFromUrl(cloudUrl);
            return "redirect:/?msg=success";
        } catch (Exception e) {
            return "redirect:/?msg=error";
        }
    }
}