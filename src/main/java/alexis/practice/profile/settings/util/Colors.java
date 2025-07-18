package alexis.practice.profile.settings.util;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Colors {

    public static final Map<Integer, String> scoreboardColors = new HashMap<>() {{
        put(0, "§6Default");
        put(1, "§aGreen");
        put(2, "§9Blue");
        put(3, "§cRed");
        put(4, "§eYellow");
        put(5, "§dPurple");
        put(6, "§3Dark Aqua");
        put(7, "§5Dark Purple");
        put(8, "§4Dark Red");
        put(9, "§6Gold");
        put(10, "§2Dark Green");
        put(11, "§bAqua");
    }};

    public static final Map<Integer, RGB> potionColors = new HashMap<>() {{
        put(0, new RGB("§cDefault", 255, 0, 0));
        put(1, new RGB("§aGreen", 0, 255, 0));
        put(2, new RGB("§9Blue", 0, 0, 255));
        put(3, new RGB("§eYellow", 255, 255, 0));
        put(4, new RGB("§dPurple", 128, 0, 128));
        put(5, new RGB("§3Dark Aqua", 0, 128, 128));
        put(6, new RGB("§5Dark Purple", 128, 0, 128));
        put(7, new RGB("§4Dark Red", 139, 0, 0));
        put(8, new RGB("§6Gold", 255, 215, 0));
        put(9, new RGB("§2Dark Green", 0, 100, 0));
        put(10, new RGB("§bAqua", 0, 255, 255));
    }};

    public static List<String> getScoreboardOptions() {
        return scoreboardColors.values().stream().toList();
    }

    public static List<String> getPotionOptions() {
        return potionColors.values().stream()
                .map(RGB::getName)
                .toList();
    }
    
    @Getter
    public static class RGB {
        private final String name;
        private final int[] rgb;

        public RGB(String name, int red, int green, int blue) {
            this.name = name;
            this.rgb = new int[]{red, green, blue};
        }
    }
}
