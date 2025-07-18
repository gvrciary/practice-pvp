package alexis.practice.item.hotbar.staff;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.handler.StaffHandler;
import cn.nukkit.Player;
import cn.nukkit.item.ItemID;
import cn.nukkit.utils.TextFormat;

public class ToggleVanishItem extends ItemCustom {

    public ToggleVanishItem() {
        super("&6Toggle Vanish", ItemID.EMERALD, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || !player.hasPermission("staffmode.permission") || !profile.getProfileData().inStaffMode()) return;

        StaffHandler.Data data = StaffHandler.getInstance().get(profile);
        data.toggleVanish();

        if (data.isVanish()) {
            player.sendMessage(TextFormat.colorize("&aYou have activated vanish"));
            return;
        }

        player.sendMessage(TextFormat.colorize("&cYou have disabled vanish"));
    }

}
