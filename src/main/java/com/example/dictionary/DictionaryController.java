package com.example.dictionary;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
public class DictionaryController {
    private final DictionaryService service;

    public DictionaryController(DictionaryService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String word,
                        @RequestParam(required = false) String mode,
                        HttpSession session, Model model) {

        @SuppressWarnings("unchecked")
        Set<String> favorites = (Set<String>) session.getAttribute("favorites");
        if (favorites == null) {
            favorites = new LinkedHashSet<>();
            session.setAttribute("favorites", favorites);
        }
        model.addAttribute("favorites", favorites);

        if (word != null && !word.isBlank() && mode != null) {
            model.addAttribute("results", service.translate(word, mode));
            model.addAttribute("example", service.getExample(word)); // ДОДАНО ЦЕЙ РЯДОК
            model.addAttribute("lastWord", word);
            model.addAttribute("currentMode", mode);
        }
        return "index";
    }

    @PostMapping("/add-favorite")
    public String addFavorite(@RequestParam String word, @RequestParam String mode, HttpSession session) {
        @SuppressWarnings("unchecked")
        Set<String> favorites = (Set<String>) session.getAttribute("favorites");
        if (favorites != null && !word.isBlank()) {
            favorites.add(word + ":" + mode);
        }
        return "redirect:/?word=" + URLEncoder.encode(word, StandardCharsets.UTF_8) + "&mode=" + mode;
    }

    @GetMapping("/cloud-update")
    public String cloudUpdate() {
        try {
            service.loadFromUrl("https://gist.githubusercontent.com/MrFoxyen/a155e3435932943734978df24f4bf2ce/raw/39ab083a24b44c194fb53b36b019a2f7c9cf4cb5/dictionary.txt");
            return "redirect:/?msg=success";
        } catch (Exception e) {
            return "redirect:/?msg=error";
        }
    }
}