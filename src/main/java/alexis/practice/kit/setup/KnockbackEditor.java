package alexis.practice.kit.setup;

import alexis.practice.kit.Kit;
import alexis.practice.kit.KitManager;
import alexis.practice.profile.Profile;
import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.ImageType;
import com.denzelcode.form.element.Input;
import com.denzelcode.form.window.CustomWindowForm;
import com.denzelcode.form.window.SimpleWindowForm;

public final class KnockbackEditor {

    public static void sendKitForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("kit_editor", "Kit Editor")
                .addHandler(h -> {
                    if (!h.isFormValid("kit_editor")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        Kit kit = KitManager.getInstance().getKit(button.getName());

                        if (kit != null) {
                            sendKnockBackForm(player, kit);
                        }
                    }
                });

        KitManager.getInstance().getKits().values().forEach(kit -> form.addButton(kit.getName(), kit.getCustomName() + "\nClick to Select", ImageType.PATH, kit.getIcon()));

        form.sendTo(player);
    }

    private static void sendKnockBackForm(Player player, Kit kit) {
        FormAPI.customWindowForm("knockback.values", TextFormat.colorize(kit.getCustomName() + " : Knockback Values"))
                .addInput("knockback.horizontalValue", "Horizontal Knockback", "", "" + kit.getHorizontalKnockback())
                .addInput("knockback.verticalValue", "Vertical Knockback", "", "" + kit.getVerticalKnockback())
                .addInput("knockback.attackCooldownValue", "Attack Cooldown", "", "" + kit.getAttackCooldown())
                .addInput("knockback.maxHeightValue", "Max Height", "", "" + kit.getMaxHeight())
                .addInput("knockback.limiterValue", "Limiter", "", "" + kit.getLimiter())
                .addHandler(h -> {
                    CustomWindowForm form = h.getForm();
                    if (!h.isFormValid("knockback.values")) return;

                    Input horizontalKnockback = form.getElement("knockback.horizontalValue");
                    Input verticalKnockback = form.getElement("knockback.verticalValue");
                    Input attackCooldown = form.getElement("knockback.attackCooldownValue");
                    Input maxHeight = form.getElement("knockback.maxHeightValue");
                    Input limiter = form.getElement("knockback.limiterValue");

                    if (!horizontalKnockback.getValue().isEmpty()) {
                        try {
                            kit.setHorizontalKnockback(Float.parseFloat(horizontalKnockback.getValue()));
                        } catch (NumberFormatException e) {
                            player.sendMessage(TextFormat.colorize("&cError" + horizontalKnockback.getName() + ": " + e.getMessage()));
                            return;
                        }
                    }

                    if (!verticalKnockback.getValue().isEmpty()) {
                        try {
                            kit.setVerticalKnockback(Float.parseFloat(verticalKnockback.getValue()));
                        } catch (NumberFormatException e) {
                            player.sendMessage(TextFormat.colorize("&cError " + verticalKnockback.getName() + ": " + e.getMessage()));
                            return;
                        }
                    }

                    if (!attackCooldown.getValue().isEmpty()) {
                        try {
                            kit.setAttackCooldown(Integer.parseInt(attackCooldown.getValue()));
                        } catch (NumberFormatException e) {
                            player.sendMessage(TextFormat.colorize("&cError " + attackCooldown.getName() + ": " + e.getMessage()));
                        }
                    }

                    if (!maxHeight.getValue().isEmpty()) {
                        try {
                            kit.setMaxHeight(Float.parseFloat(maxHeight.getValue()));
                        } catch (NumberFormatException e) {
                            player.sendMessage(TextFormat.colorize("&cError " + maxHeight.getName() + ": " + e.getMessage()));
                        }
                    }

                    if (!limiter.getValue().isEmpty()) {
                        try {
                            kit.setLimiter(Float.parseFloat(limiter.getValue()));
                        } catch (NumberFormatException e) {
                            player.sendMessage(TextFormat.colorize("&cError " + limiter.getName() + ": " + e.getMessage()));
                        }
                    }

                })
                .sendTo(player);
    }

}
