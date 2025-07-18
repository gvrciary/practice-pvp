package alexis.practice.util;

import lombok.Getter;

@Getter
public enum InputMode {
    UNKNOWN("Unknown"),
    KEYBOARD("Keyboard"),
    TOUCH("Touch"),
    GAME_PAD("Game Pad"),
    CONTROLLER("Controller");

    private static final InputMode[] VALUES = values();
    private final String enumName;

    InputMode(String enumName) {
        this.enumName = enumName;
    }

    public static InputMode fromOrdinal(int ordinal) {
        return VALUES[ordinal];
    }

}
