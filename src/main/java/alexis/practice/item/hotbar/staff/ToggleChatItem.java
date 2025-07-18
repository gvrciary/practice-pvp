package alexis.practice.item.hotbar.staff;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.handler.StaffHandler;
import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;

public class ToggleChatItem extends ItemCustom {

    public ToggleChatItem() {
        super("&6Toggle Chat", 399, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || !player.hasPermission("staffmode.permission") || !profile.getProfileData().inStaffMode()) return;


        StaffHandler.Data data = StaffHandler.getInstance().get(profile);
        data.toggleChat();

        if (data.isChat()) {
            player.sendMessage(TextFormat.colorize("&aYou have activated the staff chat"));
            return;
        }

        player.sendMessage(TextFormat.colorize("&cYou have disabled staff chat"));
    }

}
