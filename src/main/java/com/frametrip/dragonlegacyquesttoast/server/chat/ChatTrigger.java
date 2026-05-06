package com.frametrip.dragonlegacyquesttoast.server.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatTrigger {
    public String id             = UUID.randomUUID().toString().substring(0, 8);
    public String name           = "Триггер";
    public List<String> phrases  = new ArrayList<>();
    public ChatMatchMode matchMode = ChatMatchMode.CONTAINS_WORD;
    public int cooldownTicks     = 200;
    public ChatReaction reaction = new ChatReaction();
    public int priority          = 0;
    public boolean caseSensitive = false;

    public ChatTrigger copy() {
        ChatTrigger c = new ChatTrigger();
        c.id            = this.id;
        c.name          = this.name;
        c.phrases       = new ArrayList<>(this.phrases);
        c.matchMode     = this.matchMode;
        c.cooldownTicks = this.cooldownTicks;
        c.reaction      = this.reaction.copy();
        c.priority      = this.priority;
        c.caseSensitive = this.caseSensitive;
        return c;
    }

    /** Returns true if the given chat message matches this trigger's phrases/mode. */
    public boolean matches(String message) {
        if (phrases.isEmpty()) return false;
        String msg = caseSensitive ? message : message.toLowerCase();

        return switch (matchMode) {
            case CONTAINS_WORD -> phrases.stream().anyMatch(p -> containsWord(msg, p));
            case CONTAINS_PHRASE -> phrases.stream().anyMatch(p -> {
                String phrase = caseSensitive ? p : p.toLowerCase();
                return msg.contains(phrase);
            });
            case EXACT_MATCH -> phrases.stream().anyMatch(p -> {
                String phrase = caseSensitive ? p : p.toLowerCase();
                return msg.equals(phrase);
            });
            case ANY_OF_LIST -> phrases.stream().anyMatch(p -> {
                String phrase = caseSensitive ? p : p.toLowerCase();
                return containsWord(msg, phrase);
            });
            case ALL_WORDS_REQUIRED -> phrases.stream().allMatch(p -> {
                String phrase = caseSensitive ? p : p.toLowerCase();
                return containsWord(msg, phrase);
            });
        };
    }

    private boolean containsWord(String text, String word) {
        if (word.isEmpty()) return false;
        int idx = 0;
        while ((idx = text.indexOf(word, idx)) >= 0) {
            boolean beforeOk = (idx == 0) || !Character.isLetterOrDigit(text.charAt(idx - 1));
            boolean afterOk  = (idx + word.length() >= text.length())
                    || !Character.isLetterOrDigit(text.charAt(idx + word.length()));
            if (beforeOk && afterOk) return true;
            idx++;
        }
        return false;
    }
}
