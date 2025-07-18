package alexis.practice.util.handler;

import alexis.practice.Practice;
import alexis.practice.duel.DuelType;
import alexis.practice.entity.HologramEntity;
import alexis.practice.profile.Profile;
import alexis.practice.profile.data.StatisticsData;
import alexis.practice.util.Utils;
import alexis.practice.util.server.ServerEssential;
import cn.nukkit.entity.Entity;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HologramsHandler {
    @Getter
    private final static HologramsHandler instance = new HologramsHandler();

    @Getter @Setter
    private Vector3 lobbyPosition;
    @Getter @Setter
    private boolean lobbyEnabled = false;

    @Getter @Setter
    private Vector3 leaderboardPosition;
    @Getter @Setter
    private boolean leaderboardEnabled = false;

    @Getter @Setter
    private Vector3 personalPosition;
    @Getter @Setter
    private boolean personalEnabled = false;

    @Getter
    private HologramEntity lobbyHologram;
    @Getter
    private HologramEntity leaderboardHologram;
    @Getter
    private HologramEntity personalHologram;

    private List<String> lobbyText;
    private int indexLeaderboard = 0;

    public void load() {
        ConfigSection holograms = Practice.getInstance().getConfig().getSection("holograms");

        ConfigSection lobbySection = holograms.getSection("lobby");
        ConfigSection leaderboardSection = holograms.getSection("leaderboard");
        ConfigSection personalSection = holograms.getSection("personal");

        if (!lobbySection.isEmpty()) {
            lobbyEnabled = lobbySection.getBoolean("enabled", false);
            lobbyText = lobbySection.getStringList("text");

            if (lobbyEnabled) {
                lobbyPosition = Utils.parseVector3(lobbySection.getString("location"));
            }
        }

        if (!leaderboardSection.isEmpty()) {
            leaderboardEnabled = leaderboardSection.getBoolean("enabled", false);

            if (leaderboardEnabled) {
                leaderboardPosition = Utils.parseVector3(leaderboardSection.getString("location"));
            }
        }

        if (!personalSection.isEmpty()) {
            personalEnabled = personalSection.getBoolean("enabled", false);

            if (personalEnabled) {
                personalPosition = Utils.parseVector3(personalSection.getString("location"));
            }
        }

        Arrays.stream(HologramType.values()).forEach(this::execute);
    }

    public void save() {
        ConfigSection holograms = Practice.getInstance().getConfig().getSection("holograms");

        ConfigSection lobbySection = holograms.getSection("lobby");
        ConfigSection leaderboardSection = holograms.getSection("leaderboard");
        ConfigSection personalSection = holograms.getSection("personal");

        Optional.ofNullable(lobbyPosition).ifPresent(lobby ->
                lobbySection.set("location", lobby.getFloorX() + ":" + lobby.getFloorY() + ":" + lobby.getFloorZ()));
        lobbySection.set("enabled", lobbyEnabled);
        lobbySection.set("text", lobbyText);

        Optional.ofNullable(leaderboardPosition).ifPresent(lb ->
                leaderboardSection.set("location", lb.getFloorX() + ":" + lb.getFloorY() + ":" + lb.getFloorZ()));
        leaderboardSection.set("enabled", leaderboardEnabled);

        Optional.ofNullable(personalPosition).ifPresent(lb ->
                personalSection.set("location", lb.getFloorX() + ":" + lb.getFloorY() + ":" + lb.getFloorZ()));
        personalSection.set("enabled", personalEnabled);

        Practice.getInstance().getConfig().save();
    }

    public void execute(HologramType hologramType) {
        Practice practice = Practice.getInstance();

        if (hologramType.equals(HologramType.LEADERBOARD)) {
            if (!leaderboardEnabled || leaderboardPosition == null) {
                restoreHologram(HologramType.LEADERBOARD);
            } else {
                restoreHologram(HologramType.LEADERBOARD);

                practice.getServer().getDefaultLevel().loadChunk(leaderboardPosition.getFloorZ(), leaderboardPosition.getFloorZ());
                leaderboardHologram = new HologramEntity(Practice.getInstance().getServer().getDefaultLevel().getChunk(leaderboardPosition.getChunkX(), leaderboardPosition.getChunkZ()), Entity.getDefaultNBT(leaderboardPosition), HologramType.LEADERBOARD);
                leaderboardHologram.spawnToAll();
                leaderboardHologram.scheduleUpdate();
            }
        } else if (hologramType.equals(HologramType.LOBBY)) {
            if (!lobbyEnabled || lobbyPosition == null) {
                restoreHologram(HologramType.LOBBY);
            } else {
                restoreHologram(HologramType.LOBBY);

                practice.getServer().getDefaultLevel().loadChunk(lobbyPosition.getFloorZ(), lobbyPosition.getFloorZ());
                lobbyHologram = new HologramEntity(Practice.getInstance().getServer().getDefaultLevel().getChunk(lobbyPosition.getChunkX(), lobbyPosition.getChunkZ()), Entity.getDefaultNBT(lobbyPosition), HologramType.LOBBY);
                lobbyHologram.spawnToAll();
                lobbyHologram.scheduleUpdate();
            }
        } else if (hologramType.equals(HologramType.PERSONAL_STATISTICS)) {
            if (!personalEnabled || personalPosition == null) {
                restoreHologram(HologramType.PERSONAL_STATISTICS);
            } else {
                restoreHologram(HologramType.PERSONAL_STATISTICS);

                practice.getServer().getDefaultLevel().loadChunk(personalPosition.getFloorZ(), personalPosition.getFloorZ());
                personalHologram = new HologramEntity(Practice.getInstance().getServer().getDefaultLevel().getChunk(personalPosition.getChunkX(), personalPosition.getChunkZ()), Entity.getDefaultNBT(personalPosition), HologramType.PERSONAL_STATISTICS);
                personalHologram.spawnToAll();
                personalHologram.scheduleUpdate();
            }
        }
    }

    public void restoreHologram(HologramType hologramType) {
        if (hologramType.equals(HologramType.LEADERBOARD)) {
            if (leaderboardHologram != null) {
                leaderboardHologram.despawnFromAll();
                leaderboardHologram.close();

                leaderboardHologram = null;
            }
        } else if (hologramType.equals(HologramType.LOBBY)) {
            if (lobbyHologram != null) {
                lobbyHologram.despawnFromAll();
                lobbyHologram.close();

                lobbyHologram = null;
            }
        } else if (hologramType.equals(HologramType.PERSONAL_STATISTICS)) {
            if (personalHologram != null) {
                personalHologram.despawnFromAll();
                personalHologram.close();

                personalHologram= null;
            }
        }
    }

    public String getLobbyText() {
        return lobbyText.isEmpty() ? "Lobby Hologram" : TextFormat.colorize(String.join("\n", lobbyText));
    }

    public String getLeaderboardText() {
        DuelType duel = DuelType.values()[indexLeaderboard++ % DuelType.values().length];
        return TextFormat.colorize(ServerEssential.getInstance().getLeaderboard(duel.getName()));
    }

    public String getStatisticText(Profile profile) {
        StatisticsData data = profile.getStatisticsData();
        String skills = "\n\n&r&7Kills: &6" + data.getKills() + "\n&r&7Deaths: &6" + data.getDeaths() + "\n&r&7K/D: &6" + data.getKDRate() +
                "\n\n&r&7KillStreak: &6" + data.getKillStreak() + "\n&r&7Highest Kill Streak: &6" + data.getHighestKillStreak();

        return TextFormat.colorize("&r&6Your statistics" + skills);
    }

    @AllArgsConstructor
    @Getter
    public enum HologramType {
        LOBBY("Lobby Hologram"),
        LEADERBOARD("Leaderboard Hologram"),
        PERSONAL_STATISTICS("Personal Statistics");

        private final String name;
    }

}
