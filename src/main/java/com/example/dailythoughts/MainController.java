package com.example.dailythoughts;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class MainController {

    private final FirebaseService firebaseService;

    // Constructor injection
    public MainController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    // 1. LOGIN PAGE
    @GetMapping("/")
    public String login() {
        System.out.println("GET / - Loading login page");
        return "login"; // your login.html
    }

    // 2. HANDLE LOGIN
    @PostMapping("/login")
    public String handleLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        System.out.println("POST /login - User: " + username);

        // Simple hardcoded auth (you can improve this later)
        if (("you".equals(username) && "yourpassword".equals(password)) ||
                ("her".equals(username) && "herpassword".equals(password))) {

            session.setAttribute("username", username);
            System.out.println("✅ Login successful for: " + username);
            return "redirect:/diary";
        }

        model.addAttribute("errorMessage", "Wrong username or password!");
        return "login";
    }

    // 3. DIARY/HOME PAGE
    @GetMapping("/diary")
    public String diary(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            System.out.println("❌ No session, redirecting to login");
            return "redirect:/";
        }

        System.out.println("📖 Loading diary for: " + username);
        model.addAttribute("username", username);

        try {
            // Load thoughts from Firebase
            Map<String, Map<String, String>> allThoughts = firebaseService.loadAllThoughts();

            // Format for your HTML template
            Map<String, Map<String, String>> history = new LinkedHashMap<>();

            // Sort by timestamp (newest first)
            List<Map.Entry<String, Map<String, String>>> sortedEntries = new ArrayList<>(allThoughts.entrySet());
            sortedEntries.sort((a, b) -> {
                String timestampA = a.getValue().get("timestamp");
                String timestampB = b.getValue().get("timestamp");
                if (timestampA != null && timestampB != null) {
                    return Long.compare(Long.parseLong(timestampB), Long.parseLong(timestampA));
                }
                return 0;
            });

            // Create history structure for your HTML
            for (Map.Entry<String, Map<String, String>> entry : sortedEntries) {
                String date = entry.getKey();
                Map<String, String> thoughtData = entry.getValue();
                String author = thoughtData.get("author");
                String thought = thoughtData.get("thought");

                Map<String, String> historyEntry = new HashMap<>();

                if ("you".equals(author)) {
                    historyEntry.put("yourThought", thought);
                    historyEntry.put("herThought", "");
                } else if ("her".equals(author)) {
                    historyEntry.put("yourThought", "");
                    historyEntry.put("herThought", thought);
                }

                history.put(date, historyEntry);
            }

            model.addAttribute("history", history);
            System.out.println("✅ Loaded " + history.size() + " thoughts");

        } catch (Exception e) {
            System.err.println("❌ Error loading thoughts: " + e.getMessage());
            model.addAttribute("error", "Could not load thoughts: " + e.getMessage());
        }

        return "diary"; // Change to your actual filename (home.html or diary.html)
    }

    // 4. SUBMIT A THOUGHT
    @PostMapping("/submit")
    public String submitThought(
            @RequestParam(required = false) String yourThought,
            @RequestParam(required = false) String herThought,
            HttpSession session,
            Model model) {

        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/";
        }

        // Get current date/time
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try {
            if ("you".equals(username) && yourThought != null && !yourThought.trim().isEmpty()) {
                // Save "your" thought
                firebaseService.saveThought(date, "you", yourThought.trim());
                model.addAttribute("feedbackMessage", "✅ Your thought saved permanently!");

            } else if ("her".equals(username) && herThought != null && !herThought.trim().isEmpty()) {
                // Save "her" thought
                firebaseService.saveThought(date, "her", herThought.trim());
                model.addAttribute("feedbackMessage", "✅ Her thought saved permanently!");

            } else {
                model.addAttribute("feedbackMessage", "⚠️ Please write something!");
            }

        } catch (Exception e) {
            model.addAttribute("feedbackMessage", "❌ Error saving: " + e.getMessage());
        }

        return "redirect:/diary";
    }

    // 5. CLEAR ALL THOUGHTS (optional - for testing)
    @GetMapping("/clear")
    @ResponseBody
    public String clearThoughts(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (!"you".equals(username)) {
            return "Only 'you' can clear thoughts";
        }

        try {
            // This would delete all thoughts - use carefully!
            // FirebaseDatabase.getInstance().getReference("thoughts").removeValueAsync();
            return "Clear functionality disabled for safety";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // 6. LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // 7. HEALTH CHECK
    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "✅ Server is running with Firebase!";
    }
}