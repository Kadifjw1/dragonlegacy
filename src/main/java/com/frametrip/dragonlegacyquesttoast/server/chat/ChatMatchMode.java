package com.frametrip.dragonlegacyquesttoast.server.chat;

public enum ChatMatchMode {
    CONTAINS_WORD,
    CONTAINS_PHRASE,
    EXACT_MATCH,
    ANY_OF_LIST,
    ALL_WORDS_REQUIRED;

    public String label() {
        return switch (this) {
            case CONTAINS_WORD     -> "Содержит слово";
            case CONTAINS_PHRASE   -> "Содержит фразу";
            case EXACT_MATCH       -> "Точное совпадение";
            case ANY_OF_LIST       -> "Любое из списка";
            case ALL_WORDS_REQUIRED -> "Все слова";
        };
    }
}
