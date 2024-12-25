package com.ccs.gencorelite.editor;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class SyntaxHighlighter {

    private static final int COLOR_KEYWORD = Color.GREEN;
    private static final int COLOR_COMMENT = Color.GRAY;

    private static final String[] KEYWORDS = {"MUSIC", "BACKGROUND", "SOUND", "DIALOG", "GOTO", "END", "SCENE"};

    public static void applySyntaxHighlighting(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                SpannableString spannableString = new SpannableString(text);

                // Подсветка ключевых слов
                for (String keyword : KEYWORDS) {
                    int index = text.indexOf(keyword);
                    while (index >= 0) {
                        spannableString.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), index, index + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        index = text.indexOf(keyword, index + keyword.length());
                    }
                }

                // Подсветка комментариев и текста после CHOICE
                int commentIndex = text.indexOf('#');
                while (commentIndex >= 0) {
                    int endOfLine = text.indexOf('\n', commentIndex);
                    if (endOfLine == -1) {
                        endOfLine = text.length();
                    }
                    spannableString.setSpan(new ForegroundColorSpan(COLOR_COMMENT), commentIndex, endOfLine, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    commentIndex = text.indexOf('#', endOfLine);
                }

                int choiceIndex = text.indexOf("CHOICE");
                while (choiceIndex >= 0) {
                    int endOfLine = text.indexOf('\n', choiceIndex);
                    if (endOfLine == -1) {
                        endOfLine = text.length();
                    }
                    spannableString.setSpan(new ForegroundColorSpan(COLOR_COMMENT), choiceIndex + "CHOICE".length(), endOfLine, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    choiceIndex = text.indexOf("CHOICE", endOfLine);
                }

                editText.removeTextChangedListener(this);
                editText.setText(spannableString);
                editText.setSelection(editText.getText().length());
                editText.addTextChangedListener(this);
            }
        });
    }
}