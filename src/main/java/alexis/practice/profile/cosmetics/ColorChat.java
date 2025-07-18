package alexis.practice.profile.cosmetics;

import cn.nukkit.utils.TextFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ColorChat {
    GREEN("Green", TextFormat.GREEN),
    RED("Red", TextFormat.RED),
    BLUE("Blue", TextFormat.BLUE),
    YELLOW("Yellow", TextFormat.YELLOW),
    AQUA("Aqua", TextFormat.AQUA),
    GOLD("Gold", TextFormat.GOLD),
    LIGHT_PURPLE("Light Purple", TextFormat.LIGHT_PURPLE),
    BLACK("Black", TextFormat.BLACK),
    DARK_BLUE("Dark Blue", TextFormat.DARK_BLUE),
    DARK_GREEN("Dark Green", TextFormat.DARK_GREEN),
    DARK_AQUA("Dark Aqua", TextFormat.DARK_AQUA),
    DARK_RED("Dark Red", TextFormat.DARK_RED),
    DARK_PURPLE("Dark Purple", TextFormat.DARK_PURPLE),
    DARK_GRAY("Dark Gray", TextFormat.DARK_GRAY),
    GRAY("Gray", TextFormat.GRAY);

    private final String name;
    private final TextFormat color;

    public static ColorChat get(String typeName) {
        return Arrays.stream(values())
                .filter(type -> type.toString().equals(typeName))
                .findFirst()
                .orElse(null);
    }

}
