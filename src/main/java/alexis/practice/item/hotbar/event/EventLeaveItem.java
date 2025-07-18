package alexis.practice.item.hotbar.event;

import alexis.practice.event.Event;
import alexis.practice.event.games.EventState;
import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;

public class EventLeaveItem extends ItemCustom {

    public EventLeaveItem() {
        super("&cLeave Event", Item.REDSTONE, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getEvent() == null || profile.getProfileData().getEvent().getEventArena() == null) return;

        Event event = profile.getProfileData().getEvent();

        if (!event.getEventArena().getCurrentState().equals(EventState.WAITING)) {
            player.sendMessage(TextFormat.colorize("&cYou can't get out because it's already started."));
            return;
        }

        event.removePlayer(profile);
    }

}
