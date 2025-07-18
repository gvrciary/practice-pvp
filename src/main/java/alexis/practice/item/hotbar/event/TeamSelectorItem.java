package alexis.practice.item.hotbar.event;

import alexis.practice.event.Event;
import alexis.practice.event.games.EventState;
import alexis.practice.event.team.Team;
import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;

public class TeamSelectorItem extends ItemCustom {

    public TeamSelectorItem() {
        super("&6Team Selector", Item.CHEST, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getEvent() == null || profile.getProfileData().getEvent().getEventArena() == null) return;

        Event event = profile.getProfileData().getEvent();

        if (!event.getEventArena().getCurrentState().equals(EventState.WAITING)) {
            player.sendMessage(TextFormat.colorize("&cCannot complete action"));
            return;
        }

        if (!event.isTeam()) {
            player.sendMessage(TextFormat.colorize("&cThis game does not allow teams"));
            return;
        }

        Team team = event.getTeamManager().getTeam(profile);

        if (team != null) {
            player.sendMessage(TextFormat.colorize("&cYou already have team"));
            return;
        }

        event.getTeamManager().sendTeamForm(profile);
    }

}
