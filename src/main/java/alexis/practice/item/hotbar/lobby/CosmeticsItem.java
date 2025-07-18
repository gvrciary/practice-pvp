package alexis.practice.item.hotbar.lobby;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.profile.cosmetics.*;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.window.SimpleWindowForm;

import java.util.Arrays;

public class CosmeticsItem extends ItemCustom {

    public CosmeticsItem() {
        super("&6Cosmetics", Item.EMERALD, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || !profile.getProfileData().isInLobby()) return;

        if (!player.hasPermission("cosmetics.permission")) {
            player.sendMessage(TextFormat.colorize("&4You need &dBooster &4or higher to access Premium. To buy a rank, open a ticket at: dsc.gg/Practice"));
            return;
        }

        sendCosmeticForm(profile);
    }

    private void sendCosmeticForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("cosmetics", "Cosmetics")
                .addButton("kill_effects", "Kill Effects")
                .addButton("projectile_trails", "Projectile Trails")
                .addButton("color_chat", "Color Chat")
                .addButton("kill_messages", "Kill Messages")
                .addButton("join_messages", "Join Messages")
                .addHandler(h -> {
                    if (!h.isFormValid("cosmetics")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        switch (button.getName()) {
                            case "kill_effects" -> sendKillEffects(profile);
                            case "projectile_trails" -> sendProjectileEffects(profile);
                            case "kill_messages" -> sendKillMessages(profile);
                            case "color_chat" -> sendColorChats(profile);
                            case "join_messages" -> sendJoinMessages(profile);
                        }
                    }
                });

        form.sendTo(player);
    }

    private void sendKillEffects(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("kill_effects", "Kill Effects")
                .addHandler(h -> {
                    if (!h.isFormValid("kill_effects")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        if (button.getName().equals("clear")) {
                            profile.getCosmeticData().clearKillEffect();
                            return;
                        }

                        profile.getCosmeticData().setKillEffect(KillEffects.get(button.getName()));
                        player.sendMessage(TextFormat.colorize("&aYou have correctly selected " + button.getName()));
                    }
                });

        if (profile.getCosmeticData().hasKillEffect()) {
            form.addButton("clear", "Clear");
        }

        Arrays.stream(KillEffects.values()).forEach(killEffect -> form.addButton(killEffect.toString(), killEffect.getName() + TextFormat.colorize("\n" + (profile.getCosmeticData().hasKillEffect() && profile.getCosmeticData().getKillEffect().equals(killEffect) ? "&aSelected" : "&cNo Selected"))));

        form.sendTo(player);
    }

    private void sendProjectileEffects(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("projectile_effects", "Projectile Effects")
                .addHandler(h -> {
                    if (!h.isFormValid("projectile_effects")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        if (button.getName().equals("clear")) {
                            profile.getCosmeticData().clearProjectileTrail();
                            return;
                        }

                        profile.getCosmeticData().setProjectileTrail(ProjectileTrails.get(button.getName()));
                        player.sendMessage(TextFormat.colorize("&aYou have correctly selected " + button.getName()));
                    }
                });

        if (profile.getCosmeticData().hasProjectileTrail()) {
            form.addButton("clear", "Clear");
        }

        Arrays.stream(ProjectileTrails.values()).forEach(projectileTrails -> form.addButton(projectileTrails.toString(), projectileTrails.getName()  + TextFormat.colorize("\n" + (profile.getCosmeticData().hasProjectileTrail() && profile.getCosmeticData().getProjectileTrail().equals(projectileTrails) ? "&aSelected" : "&cNo Selected"))));

        form.sendTo(player);
    }

    private void sendColorChats(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("color_chat", "Color Chat")
                .addHandler(h -> {
                    if (!h.isFormValid("color_chat")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        if (button.getName().equals("clear")) {
                            profile.getCosmeticData().clearColorChat();
                            return;
                        }

                        profile.getCosmeticData().setColorChat(ColorChat.get(button.getName()));
                        player.sendMessage(TextFormat.colorize("&aYou have correctly selected " + button.getName()));
                    }
                });

        if (profile.getCosmeticData().hasColorChat()) {
            form.addButton("clear", "Clear");
        }

        Arrays.stream(ColorChat.values()).forEach(colorChat -> form.addButton(colorChat.toString(), colorChat.getName()  + TextFormat.colorize("\n" + (profile.getCosmeticData().hasColorChat() && profile.getCosmeticData().getColorChat().equals(colorChat) ? "&aSelected" : "&cNo Selected"))));

        form.sendTo(player);
    }

    private void sendJoinMessages(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("join_messages", "Join Messages")
                .addHandler(h -> {
                    if (!h.isFormValid("join_messages")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        if (button.getName().equals("clear")) {
                            profile.getCosmeticData().clearJoinMessages();
                            return;
                        }

                        profile.getCosmeticData().setJoinMessages(JoinMessages.get(button.getName()));
                        player.sendMessage(TextFormat.colorize("&aYou have correctly selected " + button.getName()));
                    }
                });

        if (profile.getCosmeticData().hasJoinMessages()) {
            form.addButton("clear", "Clear");
        }

        Arrays.stream(JoinMessages.values()).forEach(joinMessages -> form.addButton(joinMessages.toString(), joinMessages.getName()  + TextFormat.colorize("\n" + (profile.getCosmeticData().hasJoinMessages() && profile.getCosmeticData().getJoinMessages().equals(joinMessages) ? "&aSelected" : "&cNo Selected"))));

        form.sendTo(player);
    }

    private void sendKillMessages(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("kill_messages", "Kill Messages")
                .addHandler(h -> {
                    if (!h.isFormValid("kill_messages")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        if (button.getName().equals("clear")) {
                            profile.getCosmeticData().clearKillMessage();
                            return;
                        }

                        profile.getCosmeticData().setKillMessage(KillMessages.get(button.getName()));
                        player.sendMessage(TextFormat.colorize("&aYou have correctly selected " + button.getName()));
                    }
                });

        if (profile.getCosmeticData().hasKillMessage()) {
            form.addButton("clear", "Clear");
        }

        Arrays.stream(KillMessages.values()).forEach(killMessages -> form.addButton(killMessages.toString(), killMessages.getName()  + TextFormat.colorize("\n" + (profile.getCosmeticData().hasKillMessage() && profile.getCosmeticData().getKillMessage().equals(killMessages) ? "&aSelected" : "&cNo Selected"))));

        form.sendTo(player);
    }

}
