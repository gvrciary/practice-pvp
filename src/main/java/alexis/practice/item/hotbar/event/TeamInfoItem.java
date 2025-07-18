package alexis.practice.item.hotbar.event;

import alexis.practice.event.Event;
import alexis.practice.event.games.EventState;
import alexis.practice.event.team.Team;
import alexis.practice.item.HotbarItem;
import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.item.ItemID;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.window.SimpleWindowForm;

public class TeamInfoItem extends ItemCustom {

    public TeamInfoItem() {
        super("&6Team Info", ItemID.COMPASS, 0);
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

        if (team == null) {
            player.sendMessage(TextFormat.colorize("&cYou don't have team"));
            return;
        }

        sendTeamInfoForm(profile, team);
    }

    public void sendTeamInfoForm(Profile profile, Team team) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("team_info", "Team information")
                .addButton("leave_team", "Leave Team")
                .addHandler(h -> {
                    if (!h.isFormValid("team_info")) {
                        return;
                    }

                    Button button = h.getButton();

                    if (button.getName() != null && button.getName().equals("leave_team")) {
                        team.removeMember(profile);
                        player.sendMessage(TextFormat.colorize("&cHas leaved the team " + team.getId()));
                        player.getInventory().setItem(4, HotbarItem.TEAM_SELECTOR.getItem());
                    }
                });

        form.setContent(TextFormat.colorize(team.toString()));

        form.sendTo(player);
    }
}
