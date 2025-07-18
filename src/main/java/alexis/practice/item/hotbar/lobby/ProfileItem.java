package alexis.practice.item.hotbar.lobby;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.profile.settings.SettingType;
import alexis.practice.profile.settings.types.Disguise;
import alexis.practice.profile.settings.types.GameTime;
import alexis.practice.profile.settings.types.PotionColor;
import alexis.practice.profile.settings.types.ScoreboardColor;
import alexis.practice.profile.settings.util.Colors;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.Dropdown;
import com.denzelcode.form.element.Toggle;
import com.denzelcode.form.window.CustomWindowForm;
import com.denzelcode.form.window.SimpleWindowForm;

import java.util.Arrays;

public class ProfileItem extends ItemCustom {

    public ProfileItem() {
        super("&6Profile", Item.SKULL, 3);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || !profile.getProfileData().isInLobby()) return;

        sendProfileForm(profile);
    }

    private void sendProfileForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("profile", "Profile")
                .addButton("stats", "Your Stats")
                .addButton("settings", "Settings")
                .addButton("settings_premium", "Settings Premium")
                .addHandler(h -> {
                    if (!h.isFormValid("profile")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        switch (button.getName()) {
                            case "stats" -> player.addWindow(PlayerUtil.getStatsMenu(profile));
                            case "settings" -> sendSettingForm(profile);
                            case "settings_premium" -> {
                                if (!player.hasPermission("settings.permission")) {
                                    player.sendMessage(TextFormat.colorize("&4You need &dBooster &4or higher to access Premium. To buy a rank, open a ticket at: dsc.gg/Practice"));
                                    return;
                                }

                                sendSettingPremiumForm(profile);
                            }
                        }
                    }
                });

        form.sendTo(player);
    }

    private void sendSettingForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        CustomWindowForm form = FormAPI.customWindowForm("setting_menu", "Settings")
                .addHandler(h -> {
                    if (!h.isFormValid("setting_menu")) return;

                    h.getForm().getElements().forEach(button -> {
                        if (button instanceof Toggle toggle) {
                            if (profile.getSettingsData().isEnabled(toggle.getName()) != toggle.getValue()) {
                                profile.getSettingsData().toggleEnabled(toggle.getName());
                            }
                        }
                    });
                });

        Arrays.stream(SettingType.values()).filter(settingType -> !settingType.isPremium()).forEach(setting -> form.addToggle(setting.toString(), setting.getSetting().getName(), profile.getSettingsData().isEnabled(setting.toString())));

        form.sendTo(player);
    }

    private void sendSettingPremiumForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        CustomWindowForm form = FormAPI.customWindowForm("setting_menu", "Settings")
                .addHandler(h -> {
                    if (!h.isFormValid("setting_menu")) return;

                    h.getForm().getElements().forEach(button -> {

                        if (!player.hasPermission("settings.permission")) {
                            player.sendMessage(TextFormat.colorize("&4You need &dBooster &4or higher to access Premium. To buy a rank, open a ticket at: dsc.gg/Practice"));
                            return;
                        }

                        if (button instanceof Toggle toggle) {
                            if (profile.getSettingsData().isEnabled(toggle.getName()) != toggle.getValue()) {
                                profile.getSettingsData().toggleEnabled(toggle.getName());
                            }
                        } else if (button instanceof Dropdown dropdown) {
                            if (dropdown.getName().equals(SettingType.SCOREBOARD_COLOR.getSetting().getName()) && ((ScoreboardColor)SettingType.SCOREBOARD_COLOR.getSetting()).getCurrentColor(profile) != dropdown.getValue()) {
                                ((ScoreboardColor)SettingType.SCOREBOARD_COLOR.getSetting()).setCurrentColor(profile, dropdown.getValue());
                            } else if (dropdown.getName().equals(SettingType.POTION_COLOR.getSetting().getName()) && ((PotionColor)SettingType.POTION_COLOR.getSetting()).getCurrentColor(profile) != dropdown.getValue()) {
                                ((PotionColor)SettingType.POTION_COLOR.getSetting()).setCurrentColor(profile, dropdown.getValue());
                            } else if (dropdown.getName().equals(SettingType.GAME_TIME.getSetting().getName()) && ((GameTime)SettingType.GAME_TIME.getSetting()).getCurrentTime(profile) != dropdown.getValue()) {
                                ((GameTime)SettingType.GAME_TIME.getSetting()).setCurrentTime(profile, dropdown.getValue());
                            }
                        }

                    });
                });

        Arrays.stream(SettingType.values()).filter(SettingType::isPremium).forEach(setting -> {
            if (setting.getSetting() instanceof Disguise) return;

            if (setting.getSetting() instanceof ScoreboardColor) {
                form.addDropdown(setting.getSetting().getName(), setting.getSetting().getName(), Colors.getScoreboardOptions(), ((ScoreboardColor) setting.getSetting()).getCurrentColor(profile));
            } else if (setting.getSetting() instanceof GameTime) {
                form.addDropdown(setting.getSetting().getName(), setting.getSetting().getName(), Arrays.stream(new String[]{"Day", "Sunset", "Night"}).toList(), ((GameTime) setting.getSetting()).getCurrentTime(profile));
            } else if (setting.getSetting() instanceof PotionColor) {
                form.addDropdown(setting.getSetting().getName(), setting.getSetting().getName(), Colors.getPotionOptions(), ((PotionColor) setting.getSetting()).getCurrentColor(profile));
            } else form.addToggle(setting.toString(), setting.getSetting().getName(), profile.getSettingsData().isEnabled(setting.toString()));
        });

        form.sendTo(player);
    }
}
