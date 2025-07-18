package alexis.practice.profile.settings;

import alexis.practice.profile.settings.types.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum SettingType {
    JOIN_QUIT_MESSAGES(new Setting("Join and Quit Messages")),
    AUTO_RESPAWN(new Setting("Auto Respawn")),
    AUTO_GG(new Setting("Auto GG", false)),
    CPS_COUNTER(new CPSCounter("CPS Counter")),
    POTS_COUNTER(new Setting("Pots Counter", false)),
    SOUP_COUNTER(new Setting("Soup Counter", false)),
    NO_DUEL_INVITATIONS(new Setting("No Duel Invitations", false)),
    NO_PARTY_INVITATIONS(new Setting("No Party Invitations", false)),
    NO_PRIVATE_MESSAGES(new Setting("No Private Messages", false)),
    SCOREBOARD(new Scoreboard("Scoreboard")),
    HIDDEN_NON_OPPONENTS(new HiddenNonOpponents("Hidden Non Opponents")),

    MORE_CRITICAL(new MoreCritical("More Critical", false), true),
    DISGUISE(new Disguise("Disguise", false), true),
    PRIVATE_MESSAGE_SOUND(new Setting("Private Message Sound", false), true),
    LOBBY_VISIBILITY(new LobbyVisibility("Lobby Visibility"), true),
    SCOREBOARD_COLOR(new ScoreboardColor("Scoreboard Color"), true),
    GAME_TIME(new GameTime("Game Time"), true),
    POTION_COLOR(new PotionColor("Potion Color"), true);

    private final Setting setting;
    private final boolean isPremium;

    SettingType(Setting setting) {
        this(setting, false);
    }

    public int getDefaultValue() {
        return setting.defaultEnabled ? 1 : 0;
    }

    public static SettingType get(String typeName) {
        return Arrays.stream(values())
                .filter(type -> type.toString().equals(typeName))
                .findFirst()
                .orElse(null);
    }

}
