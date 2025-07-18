package alexis.practice.item.hotbar.staff;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.handler.StaffHandler;
import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;

public class LeaveModeItem extends ItemCustom {

    public LeaveModeItem() {
        super("&cLeave Mode", 331, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || !player.hasPermission("staffmode.permission") || !profile.getProfileData().inStaffMode()) return;

        StaffHandler.getInstance().get(profile).stop();
        player.sendMessage(TextFormat.colorize("&cYou have deactivated staff mode"));
    }

}
