package alexis.practice.util.server;

import alexis.practice.Practice;
import alexis.practice.duel.DuelType;
import alexis.practice.storage.SQLStorage;
import alexis.practice.util.Utils;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerEssential {
    @Getter
    private static final ServerEssential instance = new ServerEssential();

    private long timeToRestart = 0;
    private final Map<String, String> leaderboards = new HashMap<>();

    @Getter
    @Setter
    private boolean globalMute = false;
    @Getter
    private String joinText;
    private int messageIndex = 0;

    public void load() {
        Practice practice = Practice.getInstance();
        ConfigSection serverConfiguration = practice.getConfig().getSection("server-configuration");

        if (!serverConfiguration.getSection("message-join").isEmpty()) {
            joinText = TextFormat.colorize(String.join("\n", serverConfiguration.getSection("message-join").getStringList("content")));
        }

        if (!serverConfiguration.getSection("auto-messages").isEmpty()) {
            ConfigSection autoMessages = serverConfiguration.getSection("auto-messages");

            final List<String> messages = autoMessages.getStringList("content");
            int delay = autoMessages.getInt("delay", 60);

            if (!messages.isEmpty()) {
                practice.getServer().getScheduler().scheduleRepeatingTask(practice, () -> {
                    if (messageIndex >= messages.size()) messageIndex = 0;

                    practice.getServer().broadcastMessage(TextFormat.colorize(messages.get(messageIndex)));

                    messageIndex++;
                }, delay * 20, true);
            }
        }

        practice.getServer().getScheduler().scheduleRepeatingTask(practice, () -> {
            Arrays.stream(DuelType.values()).forEach(type -> SQLStorage.getInstance().getLeaderboard(type).thenAccept(leaderboardString -> setLeaderboard(type.getName(), leaderboardString)).exceptionally(e -> {
                System.err.println("Error leaderboard " + type.getCustomName() + ": " + e.getMessage());
                return null;
            }));

            timeToRestart = System.currentTimeMillis() + (60 * 5) * 1000L;
        }, 6000, true);
    }

    public void setLeaderboard(String type, String leaderboard) {
        leaderboards.put(type, leaderboard);
    }

    public String getLeaderboard(String type) {
        return leaderboards.getOrDefault(type, "&cError") + "\n\n&7Update in " + Utils.formatTime(timeToRestart - System.currentTimeMillis());
    }

}
