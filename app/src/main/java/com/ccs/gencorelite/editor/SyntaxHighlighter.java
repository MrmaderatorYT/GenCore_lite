package com.ccs.gencorelite.editor;

import android.graphics.Color;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {

    // Кольори для підсвітки (контрастні для Material Design 3)
    private static final int COLOR_KEYWORD = Color.parseColor("#1976D2");      // Синій
    private static final int COLOR_COMMENT = Color.parseColor("#388E3C");      // Зелений  
    private static final int COLOR_STRING = Color.parseColor("#F57C00");       // Помаранчевий
    private static final int COLOR_DIALOG = Color.parseColor("#7B1FA2");       // Фіолетовий

    // Ключові слова вашої мови
    private static final String[] KEYWORDS = {
        "SCENE", "BACKGROUND", "MUSIC", "SOUND", "DIALOG", "CHOICE", "GOTO", "END"
    };

    private static boolean isUpdating = false; // Запобігаємо рекурсії

    public static void applySyntaxHighlighting(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (isUpdating) return; // Запобігаємо рекурсії
                
                isUpdating = true;
                
                // Очищаємо всі попередні спани
                ForegroundColorSpan[] spans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);
                for (ForegroundColorSpan span : spans) {
                    editable.removeSpan(span);
                }
                
                String text = editable.toString();
                
                // 1. Підсвітка ключових слів
                highlightKeywords(editable, text);
                
                // 2. Підсвітка коментарів (рядки що починаються з #)
                highlightComments(editable, text);
                
                // 3. Підсвітка діалогів (після DIALOG:)
                highlightDialogs(editable, text);
                
                // 4. Підсвітка строк в лапках
                highlightStrings(editable, text);
                
                isUpdating = false;
            }
        });
    }

    private static void highlightKeywords(Editable editable, String text) {
        for (String keyword : KEYWORDS) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                editable.setSpan(
                    new ForegroundColorSpan(COLOR_KEYWORD),
                    matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
    }

    private static void highlightComments(Editable editable, String text) {
        // Коментарі - рядки що починаються з #
        Pattern commentPattern = Pattern.compile("^\\s*#.*$", Pattern.MULTILINE);
        Matcher matcher = commentPattern.matcher(text);
        while (matcher.find()) {
            editable.setSpan(
                new ForegroundColorSpan(COLOR_COMMENT),
                matcher.start(),
                matcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    private static void highlightDialogs(Editable editable, String text) {
        // Діалоги - текст після "DIALOG ім'я:"
        Pattern dialogPattern = Pattern.compile("DIALOG\\s+\\w+:\\s*(.*)$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = dialogPattern.matcher(text);
        while (matcher.find()) {
            // Підсвічуємо тільки текст діалогу, не ключове слово
            int start = text.indexOf(":", matcher.start()) + 1;
            if (start < matcher.end()) {
                editable.setSpan(
                    new ForegroundColorSpan(COLOR_DIALOG),
                    start,
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
    }

    private static void highlightStrings(Editable editable, String text) {
        // Строки в лапках
        Pattern stringPattern = Pattern.compile("\"[^\"]*\"");
        Matcher matcher = stringPattern.matcher(text);
        while (matcher.find()) {
            editable.setSpan(
                new ForegroundColorSpan(COLOR_STRING),
                matcher.start(),
                matcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }
}