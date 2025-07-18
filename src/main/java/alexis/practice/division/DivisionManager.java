package alexis.practice.division;

import alexis.practice.Practice;
import alexis.practice.profile.Profile;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.window.SimpleWindowForm;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class DivisionManager {
    @Getter
    private static final DivisionManager instance = new DivisionManager();
    private final Map<String, Division> divisions = new LinkedHashMap<>();

    private static final StringBuilder info = new StringBuilder();

    public void update(Profile profile, int elo) {
        if (!profile.isOnline()) return;

        Division currentDivision = getDivisionByElo(elo);
        Division newDivision = getNextDivision(currentDivision.getName());

        if (newDivision != null && elo >= newDivision.getElo()) {
            try {
                profile.getPlayer().sendTitle(TextFormat.colorize("&aYou have been upgrade to "), newDivision.getRankFormat());
                PlayerUtil.playSound(profile.getPlayer(), "random.levelup");
            } catch (Exception ignored) {}
        } else if (currentDivision.getElo() > elo) {
            Division defaultDivision = getDefaultDivision();
            Division backDivision = getBackDivision(currentDivision.getName());

            if (backDivision != null && !defaultDivision.equals(currentDivision)) {
                try {
                    profile.getPlayer().sendTitle(TextFormat.colorize("&cYou have been downgrade to "), backDivision.getRankFormat());
                } catch (Exception ignored) {}
            }
        }
    }

    public Division getDefaultDivision() {
        return divisions.values().stream().findFirst().orElse(null);
    }

    public Division getDivisionByElo(int elo) {
        return divisions.values().stream().filter(division -> elo >= division.getElo()).findFirst().orElse(getDefaultDivision());
    }

    public Division getNextDivision(String currentDivisionName) {
        List<String> divisionNames = new ArrayList<>(divisions.keySet());
        int currentIndex = divisionNames.indexOf(currentDivisionName);

        if (currentIndex == -1 || currentIndex + 1 >= divisionNames.size()) {
            return null;
        }

        String nextDivisionName = divisionNames.get(currentIndex + 1);
        return divisions.get(nextDivisionName);
    }

    public Division getBackDivision(String currentDivisionName) {
        List<String> divisionNames = new ArrayList<>(divisions.keySet());
        int currentIndex = divisionNames.indexOf(currentDivisionName);

        if (currentIndex == -1 || currentIndex + 1 >= divisionNames.size() || currentIndex == 0) {
            return null;
        }

        String nextDivisionName = divisionNames.get(currentIndex - 1);
        return divisions.get(nextDivisionName);
    }

    public void sendForm(Player player) {
        SimpleWindowForm form = FormAPI.simpleWindowForm("list_division", "Divisions");
        form.setContent(info.toString());
        form.sendTo(player);
    }

    public void load() {
        AtomicReference<Integer> elo = new AtomicReference<>(1000);
        ConfigSection division = Practice.getInstance().getConfig().getSection("divisions");

        if (division.isEmpty()) {
            throw new RuntimeException("Config division does not exist.");
        }

        AtomicBoolean isFirstRank = new AtomicBoolean(true);
        division.getSections().getKeys(false).forEach(key -> {
            int subDivisions = division.getSection(key).getInt("subdivisions", 3);

            for (int i = subDivisions; i > 0; i--) {
                String nameDivision = subDivisions == 1 ? key : key + " " + Utils.intToRoman(i);
                if (!isFirstRank.get()) {
                    elo.set(elo.get() + division.getSection(key).getInt("range-increase", 100));
                }

                divisions.put(nameDivision, new Division(nameDivision, division.getSection(key).getString("color", "&7"), elo.get()));
                if (isFirstRank.get()) isFirstRank.set(false);
            }
        });

        divisions.values().forEach(d -> info.append(d.getRankFormat()).append(": ").append(d.getElo()).append(" Elo\n"));
    }

    @Getter
    public static final class Division {

        private final String name;
        private final String color;
        private final int elo;

        Division(String name, String color, int elo) {
            this.name = name;
            this.color = color;
            this.elo = elo;
        }

        public String getRankFormat() {
            return TextFormat.colorize(color + Character.toUpperCase(name.charAt(0)) + name.substring(1));
        }
    }
}
