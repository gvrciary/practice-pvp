package alexis.practice.item.hotbar.duel;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.queue.QueueManager;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;

public class LeaveQueueItem extends ItemCustom {

    public LeaveQueueItem() {
        super("&cLeave Queue", 331, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return;

        if (profile.getProfileData().getQueue() == null || profile.getProfileData().getDuel() != null) return;

        QueueManager.getInstance().removeQueue(profile);
        player.sendMessage(TextFormat.colorize("&cYou have left the queue"));
        profile.clear();
        PlayerUtil.getLobbyKit(player, false);
    }

}
