package org.example.enums;

public enum CommandService {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start");

    private final String value;

    CommandService(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static CommandService fromValue(String v) {
        for (CommandService c: CommandService.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }
}