package alexis.practice.storage;

import alexis.practice.Practice;
import alexis.practice.duel.DuelType;
import alexis.practice.event.games.EventType;
import alexis.practice.profile.Profile;
import alexis.practice.profile.cosmetics.*;
import alexis.practice.profile.settings.SettingType;
import alexis.practice.profile.settings.types.GameTime;
import alexis.practice.profile.settings.types.PotionColor;
import alexis.practice.profile.settings.types.ScoreboardColor;
import alexis.practice.util.Utils;
import cn.nukkit.utils.ConfigSection;
import com.code.advancedsql.MySQL;
import com.code.advancedsql.query.Create;
import com.code.advancedsql.query.Insert;
import com.code.advancedsql.query.Select;
import com.code.advancedsql.query.Update;
import com.code.advancedsql.table.ITable;
import lombok.Getter;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class SQLStorage {

    public static final String STATS_TABLE = "player_stats";
    public static final String SETTINGS_TABLE = "player_settings";

    @Getter
    private static final SQLStorage instance = new SQLStorage();

    private MySQL connection;

    public void update(Profile profile) {
        CompletableFuture.runAsync(() -> {
            try {
                MySQL connection = this.connection;
                Update updateSettings = connection.table(SETTINGS_TABLE).update();

                updateSettings.field("name", profile.getRealName());

                updateSettings.field("join_and_quit_messages", profile.getSettingsData().isEnabled(SettingType.JOIN_QUIT_MESSAGES.toString()) ? 1 : 0);
                updateSettings.field("scoreboard", profile.getSettingsData().isEnabled(SettingType.SCOREBOARD.toString()) ? 1 : 0);
                updateSettings.field("more_critical", profile.getSettingsData().isEnabled(SettingType.MORE_CRITICAL.toString()) ? 1 : 0);
                updateSettings.field("auto_respawn", profile.getSettingsData().isEnabled(SettingType.AUTO_RESPAWN.toString()) ? 1 : 0);
                updateSettings.field("auto_gg", profile.getSettingsData().isEnabled(SettingType.AUTO_GG.toString()) ? 1 : 0);
                updateSettings.field("no_duel_invitation", profile.getSettingsData().isEnabled(SettingType.NO_DUEL_INVITATIONS.toString()) ? 1 : 0);
                updateSettings.field("no_party_invitation", profile.getSettingsData().isEnabled(SettingType.NO_PARTY_INVITATIONS.toString()) ? 1 : 0);
                updateSettings.field("no_private_messages", profile.getSettingsData().isEnabled(SettingType.NO_PRIVATE_MESSAGES.toString()) ? 1 : 0);
                updateSettings.field("cps_counter", profile.getSettingsData().isEnabled(SettingType.CPS_COUNTER.toString()) ? 1 : 0);
                updateSettings.field("pots_counter", profile.getSettingsData().isEnabled(SettingType.POTS_COUNTER.toString()) ? 1 : 0);
                updateSettings.field("soup_counter", profile.getSettingsData().isEnabled(SettingType.SOUP_COUNTER.toString()) ? 1 : 0);
                updateSettings.field("disguise", profile.getSettingsData().isEnabled(SettingType.DISGUISE.toString()) ? 1 : 0);
                updateSettings.field("scoreboard_color", ((ScoreboardColor) SettingType.SCOREBOARD_COLOR.getSetting()).getCurrentColor(profile));
                updateSettings.field("potion_color", ((PotionColor) SettingType.POTION_COLOR.getSetting()).getCurrentColor(profile));
                updateSettings.field("hidden_non_opponent", profile.getSettingsData().isEnabled(SettingType.HIDDEN_NON_OPPONENTS.toString()) ? 1 : 0);
                updateSettings.field("game_time", ((GameTime) SettingType.GAME_TIME.getSetting()).getCurrentTime(profile));
                updateSettings.field("lobby_visibility", profile.getSettingsData().isEnabled(SettingType.LOBBY_VISIBILITY.toString()) ? 1 : 0);

                updateSettings.field("killEffect", (!profile.getCosmeticData().hasKillEffect()) ? "" : profile.getCosmeticData().getKillEffect().toString());
                updateSettings.field("projectileTrail", (!profile.getCosmeticData().hasProjectileTrail()) ? "" : profile.getCosmeticData().getProjectileTrail().toString());
                updateSettings.field("killMessages", (!profile.getCosmeticData().hasKillMessage()) ? "" : profile.getCosmeticData().getKillMessage().toString());
                updateSettings.field("joinMessages", (!profile.getCosmeticData().hasJoinMessages()) ? "" : profile.getCosmeticData().getJoinMessages().toString());
                updateSettings.field("colorChat", (!profile.getCosmeticData().hasColorChat()) ? "" : profile.getCosmeticData().getColorChat().toString());

                updateSettings.where("xuid = ?", profile.getIdentifier());
                updateSettings.execute();

                Update updateStats = connection.table(STATS_TABLE).update();

                updateStats.field("name", profile.getRealName());
                updateStats.field("matches", profile.getStatisticsData().getMatches());
                updateStats.field("deaths", profile.getStatisticsData().getDeaths());

                updateStats.field("kills", profile.getStatisticsData().getKills());
                updateStats.field("killStreak", profile.getStatisticsData().getKillStreak());
                updateStats.field("highestKillStreak", profile.getStatisticsData().getHighestKillStreak());

                updateStats.field("wins", profile.getStatisticsData().getWins());
                updateStats.field("winStreak", profile.getStatisticsData().getWinStreak());
                updateStats.field("highestWinStreak", profile.getStatisticsData().getHighestWinStreak());

                updateStats.field("events", profile.getStatisticsData().getEvents());

                Arrays.stream(DuelType.values()).toList().forEach(duel -> updateStats.field("elo_" + duel.getName(), profile.getStatisticsData().getElo(duel.getName())));
                Arrays.stream(EventType.values()).toList().forEach(eventType -> updateStats.field("win_" + eventType.getName(), profile.getStatisticsData().getEventWin(eventType.getName())));

                updateStats.where("xuid = ?", profile.getIdentifier());
                updateStats.execute();
            } catch (SQLException e) {
                System.err.println("SQL Update Error: " + e.getMessage());
            }
        }).thenRun(() -> Arrays.stream(SettingType.values()).forEach(setting -> setting.getSetting().clearCache(profile)));
    }

    public void get(Profile profile, Runnable callback) {
        CompletableFuture.runAsync(() -> {
            try {
                MySQL connection = this.connection;

                Select selectTableSettings = connection.table(SETTINGS_TABLE).select();
                selectTableSettings.where("xuid = ?", profile.getIdentifier());

                Map<String, Object> resultTableSettings= selectTableSettings.fetch();

                if (resultTableSettings == null || resultTableSettings.isEmpty()) {
                    Insert insert = connection.table(SETTINGS_TABLE).insert();

                    insert.field("xuid", profile.getIdentifier());
                    insert.field("name", profile.getRealName());

                    insert.field("join_and_quit_messages", profile.getSettingsData().isEnabled(SettingType.JOIN_QUIT_MESSAGES.toString()) ? 1 : 0);
                    insert.field("scoreboard", profile.getSettingsData().isEnabled(SettingType.SCOREBOARD.toString()) ? 1 : 0);
                    insert.field("more_critical", profile.getSettingsData().isEnabled(SettingType.MORE_CRITICAL.toString()) ? 1 : 0);
                    insert.field("auto_respawn", profile.getSettingsData().isEnabled(SettingType.AUTO_RESPAWN.toString()) ? 1 : 0);
                    insert.field("auto_gg", profile.getSettingsData().isEnabled(SettingType.AUTO_GG.toString()) ? 1 : 0);
                    insert.field("no_duel_invitation", profile.getSettingsData().isEnabled(SettingType.NO_DUEL_INVITATIONS.toString()) ? 1 : 0);
                    insert.field("no_party_invitation", profile.getSettingsData().isEnabled(SettingType.NO_PARTY_INVITATIONS.toString()) ? 1 : 0);
                    insert.field("no_private_messages", profile.getSettingsData().isEnabled(SettingType.NO_PRIVATE_MESSAGES.toString()) ? 1 : 0);
                    insert.field("cps_counter", profile.getSettingsData().isEnabled(SettingType.CPS_COUNTER.toString()) ? 1 : 0);
                    insert.field("pots_counter", profile.getSettingsData().isEnabled(SettingType.POTS_COUNTER.toString()) ? 1 : 0);
                    insert.field("soup_counter", profile.getSettingsData().isEnabled(SettingType.SOUP_COUNTER.toString()) ? 1 : 0);
                    insert.field("disguise", profile.getSettingsData().isEnabled(SettingType.DISGUISE.toString()) ? 1 : 0);
                    insert.field("scoreboard_color", ((ScoreboardColor) SettingType.SCOREBOARD_COLOR.getSetting()).getCurrentColor(profile));
                    insert.field("potion_color", ((PotionColor) SettingType.POTION_COLOR.getSetting()).getCurrentColor(profile));
                    insert.field("hidden_non_opponent", profile.getSettingsData().isEnabled(SettingType.HIDDEN_NON_OPPONENTS.toString()) ? 1 : 0);
                    insert.field("game_time", ((GameTime) SettingType.GAME_TIME.getSetting()).getCurrentTime(profile));
                    insert.field("lobby_visibility", profile.getSettingsData().isEnabled(SettingType.LOBBY_VISIBILITY.toString()) ? 1 : 0);

                    insert.field("killEffect", (!profile.getCosmeticData().hasKillEffect()) ? "" : profile.getCosmeticData().getKillEffect().toString());
                    insert.field("projectileTrail", (!profile.getCosmeticData().hasProjectileTrail()) ? "" : profile.getCosmeticData().getProjectileTrail().toString());
                    insert.field("killMessages", (!profile.getCosmeticData().hasKillMessage()) ? "" : profile.getCosmeticData().getKillMessage().toString());
                    insert.field("joinMessages", (!profile.getCosmeticData().hasJoinMessages()) ? "" : profile.getCosmeticData().getJoinMessages().toString());
                    insert.field("colorChat", (!profile.getCosmeticData().hasColorChat()) ? "" : profile.getCosmeticData().getColorChat().toString());

                    insert.execute();
                } else {
                    profile.getSettingsData().setEnabled(SettingType.JOIN_QUIT_MESSAGES.toString(), (int) resultTableSettings.get("join_and_quit_messages") == 1);
                    profile.getSettingsData().setEnabled(SettingType.SCOREBOARD.toString(), (int) resultTableSettings.get("scoreboard") == 1);
                    profile.getSettingsData().setEnabled(SettingType.MORE_CRITICAL.toString(), (int) resultTableSettings.get("more_critical") == 1);
                    profile.getSettingsData().setEnabled(SettingType.AUTO_RESPAWN.toString(), (int) resultTableSettings.get("auto_respawn") == 1);
                    profile.getSettingsData().setEnabled(SettingType.AUTO_GG.toString(), (int) resultTableSettings.get("auto_gg") == 1);
                    profile.getSettingsData().setEnabled(SettingType.NO_DUEL_INVITATIONS.toString(), (int) resultTableSettings.get("no_duel_invitation") == 1);
                    profile.getSettingsData().setEnabled(SettingType.NO_PARTY_INVITATIONS.toString(), (int) resultTableSettings.get("no_party_invitation") == 1);
                    profile.getSettingsData().setEnabled(SettingType.NO_PRIVATE_MESSAGES.toString(), (int) resultTableSettings.get("no_private_messages") == 1);
                    profile.getSettingsData().setEnabled(SettingType.DISGUISE.toString(), (int) resultTableSettings.get("disguise") == 1);
                    profile.getSettingsData().setEnabled(SettingType.CPS_COUNTER.toString(), (int) resultTableSettings.get("cps_counter") == 1);
                    profile.getSettingsData().setEnabled(SettingType.SOUP_COUNTER.toString(), (int) resultTableSettings.get("soup_counter") == 1);
                    profile.getSettingsData().setEnabled(SettingType.POTS_COUNTER.toString(), (int) resultTableSettings.get("pots_counter") == 1);
                    profile.getSettingsData().setEnabled(SettingType.HIDDEN_NON_OPPONENTS.toString(), (int) resultTableSettings.get("hidden_non_opponent") == 1);
                    profile.getSettingsData().setEnabled(SettingType.LOBBY_VISIBILITY.toString(), (int) resultTableSettings.get("lobby_visibility") == 1);

                    ((GameTime) SettingType.GAME_TIME.getSetting()).setCurrentTime(profile, (int) resultTableSettings.get("game_time"));
                    ((PotionColor) SettingType.POTION_COLOR.getSetting()).setCurrentColor(profile, (int) resultTableSettings.get("potion_color"));
                    ((ScoreboardColor) SettingType.SCOREBOARD_COLOR.getSetting()).setCurrentColor(profile, (int) resultTableSettings.get("scoreboard_color"));

                    String killEffect = (String) resultTableSettings.get("killEffect");
                    if (!killEffect.isEmpty()) profile.getCosmeticData().setKillEffect(KillEffects.get(killEffect));

                    String projectileTrail = (String) resultTableSettings.get("projectileTrail");
                    if (!projectileTrail.isEmpty()) profile.getCosmeticData().setProjectileTrail(ProjectileTrails.get(projectileTrail));

                    String killMessages = (String) resultTableSettings.get("killMessages");
                    if (!killMessages.isEmpty()) profile.getCosmeticData().setKillMessage(KillMessages.get(killMessages));

                    String joinMessages = (String) resultTableSettings.get("joinMessages");
                    if (!joinMessages.isEmpty()) profile.getCosmeticData().setJoinMessages(JoinMessages.get(joinMessages));

                    String colorChat = (String) resultTableSettings.get("colorChat");
                    if (!colorChat.isEmpty()) profile.getCosmeticData().setColorChat(ColorChat.get(colorChat));
                }

                Select selectTableStats = connection.table(STATS_TABLE).select();
                selectTableStats.where("xuid = ?", profile.getIdentifier());

                Map<String, Object> resultTableStats = selectTableStats.fetch();

                if (resultTableStats == null || resultTableStats.isEmpty()) {
                    Insert insert = connection.table(STATS_TABLE).insert();

                    insert.field("xuid", profile.getIdentifier());
                    insert.field("name", profile.getRealName());

                    insert.field("matches", profile.getStatisticsData().getMatches());
                    insert.field("deaths", profile.getStatisticsData().getDeaths());

                    insert.field("kills", profile.getStatisticsData().getKills());
                    insert.field("killStreak", profile.getStatisticsData().getKillStreak());
                    insert.field("highestKillStreak", profile.getStatisticsData().getHighestKillStreak());

                    insert.field("wins", profile.getStatisticsData().getWins());
                    insert.field("winStreak", profile.getStatisticsData().getWinStreak());
                    insert.field("highestWinStreak", profile.getStatisticsData().getHighestWinStreak());

                    insert.field("events", profile.getStatisticsData().getEvents());

                    Arrays.stream(DuelType.values()).toList().forEach(duel -> insert.field("elo_" + duel.getName(), profile.getStatisticsData().getElo(duel.getName())));
                    Arrays.stream(EventType.values()).toList().forEach(eventType -> insert.field("win_" + eventType.getName(), profile.getStatisticsData().getEventWin(eventType.getName())));

                    insert.execute();
                } else {
                    profile.getStatisticsData().setMatches((int) resultTableStats.get("matches"));
                    profile.getStatisticsData().setDeaths((int) resultTableStats.get("deaths"));

                    profile.getStatisticsData().setWins((int) resultTableStats.get("wins"));
                    profile.getStatisticsData().setWinStreak((int) resultTableStats.get("winStreak"));
                    profile.getStatisticsData().setHighestWinStreak((int) resultTableStats.get("highestWinStreak"));

                    profile.getStatisticsData().setKills((int) resultTableStats.get("kills"));
                    profile.getStatisticsData().setKillStreak((int) resultTableStats.get("killStreak"));
                    profile.getStatisticsData().setHighestKillStreak((int) resultTableStats.get("highestKillStreak"));

                    profile.getStatisticsData().setEvents((int) resultTableStats.get("events"));

                    Arrays.stream(DuelType.values()).toList().forEach(duel -> profile.getStatisticsData().updateElo(duel.getName(), (int) resultTableStats.get("elo_" + duel.getName())));
                    Arrays.stream(EventType.values()).toList().forEach(eventType -> profile.getStatisticsData().setEventWins(eventType.getName(), (int) resultTableStats.get("win_" + eventType.getName())));
                }

            } catch (SQLException e) {
                System.err.println("SQL Get Error: " + e.getMessage());
            }
        }).thenRun(callback);
    }

    public CompletableFuture<String> getLeaderboard(DuelType duelType) {
        return CompletableFuture.supplyAsync(() -> {
            String type = "elo_" + duelType.getName();
            StringBuilder leaderboardString = new StringBuilder();

            try {
                List<Map<String, Object>> leaderboard = connection.table(STATS_TABLE)
                        .select()
                        .columns(new String[]{"name", type})
                        .orderBy(type + " DESC")
                        .limit(10)
                        .fetchAllAsList();

                leaderboardString.append("&6Leaderboard &8:&f ").append(duelType.getCustomName()).append("\n&r&6\n");
                for (int i = 0; i < leaderboard.size(); i++) {
                    Map<String, Object> row = leaderboard.get(i);

                    String name = (String) row.get("name");
                    int kills = (int) row.get(type);

                    leaderboardString.append("&6").append(Utils.intToRoman(i + 1)).append(" - &f")
                            .append(name).append(" :&7 ").append(kills).append("\n");
                }

            } catch (SQLException e) {
                System.err.println("Error leaderboard: " + e.getMessage());
            }

            return leaderboardString.toString();
        });
    }

    public void load() {
        try {
            ConfigSection info = Practice.getInstance().getConfig().getSection("database");
            connection = new MySQL(info.getString("host"), 3306, info.getString("username"), info.getString("password"), info.getString("schema"));

            if (connection.isConnected()) {
                System.out.println("Successfully connected to the database");
            }

            ITable tableSettings = connection.table(SETTINGS_TABLE);
            Create createTableSettings = tableSettings.create().ifNotExists();

            createTableSettings.id();
            createTableSettings.string("xuid");
            createTableSettings.string("name");

            createTableSettings.integer("join_and_quit_messages").defaultValue(SettingType.JOIN_QUIT_MESSAGES.getDefaultValue());
            createTableSettings.integer("scoreboard").defaultValue(SettingType.SCOREBOARD.getDefaultValue());
            createTableSettings.integer("more_critical").defaultValue(SettingType.MORE_CRITICAL.getDefaultValue());
            createTableSettings.integer("auto_respawn").defaultValue(SettingType.AUTO_RESPAWN.getDefaultValue());
            createTableSettings.integer("auto_gg").defaultValue(SettingType.AUTO_GG.getDefaultValue());
            createTableSettings.integer("no_duel_invitation").defaultValue(SettingType.NO_DUEL_INVITATIONS.getDefaultValue());
            createTableSettings.integer("no_party_invitation").defaultValue(SettingType.NO_PARTY_INVITATIONS.getDefaultValue());
            createTableSettings.integer("no_private_messages").defaultValue(SettingType.NO_PRIVATE_MESSAGES.getDefaultValue());
            createTableSettings.integer("disguise").defaultValue(SettingType.DISGUISE.getDefaultValue());
            createTableSettings.integer("scoreboard_color").defaultValue(0);
            createTableSettings.integer("potion_color").defaultValue(0);
            createTableSettings.integer("game_time").defaultValue(0);
            createTableSettings.integer("lobby_visibility").defaultValue(SettingType.LOBBY_VISIBILITY.getDefaultValue());
            createTableSettings.integer("cps_counter").defaultValue(SettingType.CPS_COUNTER.getDefaultValue());
            createTableSettings.integer("pots_counter").defaultValue(SettingType.POTS_COUNTER.getDefaultValue());
            createTableSettings.integer("soup_counter").defaultValue(SettingType.SOUP_COUNTER.getDefaultValue());
            createTableSettings.integer("hidden_non_opponent").defaultValue(SettingType.HIDDEN_NON_OPPONENTS.getDefaultValue());

            createTableSettings.string("killEffect").defaultValue("");
            createTableSettings.string("projectileTrail").defaultValue("");
            createTableSettings.string("killMessages").defaultValue("");
            createTableSettings.string("joinMessages").defaultValue("");
            createTableSettings.string("colorChat").defaultValue("");

            createTableSettings.execute();

            ITable tableStats = connection.table(STATS_TABLE);
            Create createTableStats = tableStats.create().ifNotExists();

            createTableStats.id();
            createTableStats.string("xuid");
            createTableStats.string("name");
            createTableStats.integer("deaths").defaultValue(0);
            createTableStats.integer("matches").defaultValue(0);

            createTableStats.integer("kills").defaultValue(0);
            createTableStats.integer("killStreak").defaultValue(0);
            createTableStats.integer("highestKillStreak").defaultValue(0);

            createTableStats.integer("wins").defaultValue(0);
            createTableStats.integer("winStreak").defaultValue(0);
            createTableStats.integer("highestWinStreak").defaultValue(0);

            createTableStats.integer("events").defaultValue(0);
            createTableStats.integer("meetupWins").defaultValue(0);
            createTableStats.integer("tournamentWins").defaultValue(0);
            createTableStats.integer("skyWarsWins").defaultValue(0);

            Arrays.stream(DuelType.values()).toList().forEach(duel -> createTableStats.integer("elo_" + duel.getName()).defaultValue(1000));
            Arrays.stream(EventType.values()).toList().forEach(eventType -> createTableStats.integer("win_" + eventType.getName()).defaultValue(0));

            createTableStats.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
