package org.example.enums;

public enum CommandService {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start"),
    BUY("/buy"),
    WALLET_MONEY("/wallet"),
    WALLET_LOOK_BALANCE("/look_balance"),
    WALLET_TOP_UP_CMD("/top_up"),
    SUPPORT("/support"),
    DEVELOPMENT("/development"),
    SHOW_BAG("/show_bag"),
    SEND("/send"),
    LEARNING("/learning"),
    SELL("/sell");

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
