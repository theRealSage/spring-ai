package com.ayush.learn.ai_beginner.utils;

import org.springframework.ui.Model;

public class UIUtils {

    public static String getView(Model model, String demoName, String question, String answer) {
        model.addAttribute("demo", demoName);
        model.addAttribute("question", question);
        model.addAttribute("answer", answer);
        return "demo";
    }
}
