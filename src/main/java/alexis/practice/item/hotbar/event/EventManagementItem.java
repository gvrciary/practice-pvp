package alexis.practice.item.hotbar.event;

import alexis.practice.event.Event;
import alexis.practice.event.games.EventState;
import alexis.practice.event.games.types.meetup.Meetup;
import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.window.SimpleWindowForm;

public class EventManagementItem extends ItemCustom {

    public EventManagementItem() {
        super("&6Event Management", Item.CLOCK, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getEvent() == null || profile.getProfileData().getEvent().getEventArena() == null) return;

        Event event = profile.getProfileData().getEvent();

        if (!event.isCreator(profile)) {
            player.sendMessage(TextFormat.colorize("&cYou are not the owner"));
            return;
        }

        if (!event.getEventArena().getCurrentState().equals(EventState.WAITING)) {
            player.sendMessage(TextFormat.colorize("&cYou can't get out because it's already started."));
            return;
        }

        sendManagementForm(profile, event);
    }

    public void sendManagementForm(Profile profile, Event event) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("event_management", "Event Management")
                .addButton("force_start", "Force Start")
                .addHandler(h -> {
                    if (!h.isFormValid("event_management")) {
                        return;
                    }

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        switch(button.getName()) {
                            case "force_start" -> {
                                if (event.getEventArena() != null && !event.getEventArena().getCurrentState().equals(EventState.WAITING)) {
                                    player.sendMessage(TextFormat.colorize("&cYou can't use it after starting"));
                                    return;
                                }

                                event.executeForceStart();
                            }
                            case "scenarios" -> {
                                if (!event.getEventArena().getCurrentState().equals(EventState.WAITING)) {
                                    player.sendMessage(TextFormat.colorize("&cYou can't use it after starting"));
                                    return;
                                }

                                if (!(event.getEventArena() instanceof Meetup)) {
                                    player.sendMessage(TextFormat.colorize("&cThis game does not support scenarios."));
                                    return;
                                }

                                player.addWindow(((Meetup) event.getEventArena()).getScenarioManager().getScenariosMenu());
                            }
                        }
                    }
                });

        if (event.getEventArena() instanceof Meetup) {
            form.addButton("scenarios", "Scenarios");
        }

        form.sendTo(player);
    }

}
