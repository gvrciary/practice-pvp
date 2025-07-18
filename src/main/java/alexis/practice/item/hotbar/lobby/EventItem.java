package alexis.practice.item.hotbar.lobby;

import alexis.practice.event.Event;
import alexis.practice.event.EventManager;
import alexis.practice.event.games.EventState;
import alexis.practice.event.games.EventType;
import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.ImageType;
import com.denzelcode.form.window.SimpleWindowForm;

import java.util.Arrays;
import java.util.Optional;

public class EventItem extends ItemCustom {

    public EventItem() {
        super("&6Events", Item.ENDER_EYE, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || !profile.getProfileData().isInLobby()) return;

        sendEventsForm(profile);
    }

    private void sendEventsForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }
        EventManager eventManager = EventManager.getInstance();

        SimpleWindowForm form = FormAPI.simpleWindowForm("events", "Event Selector")
                .addHandler(h -> {
                    if (!h.isFormValid("events")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        EventType type = EventType.get(button.getName());
                        Optional<Event> event = eventManager.getEvent(type);

                        if (event.isPresent()) {
                            if (!event.get().getEventArena().getCurrentState().equals(EventState.WAITING)) {
                                player.sendMessage(TextFormat.colorize("&cThe event has already started"));
                                return;
                            }

                            event.get().addPlayer(profile);
                            return;
                        }

                        player.sendMessage(TextFormat.colorize("&cNo event available"));
                    }
                });

        Arrays.stream(EventType.values()).forEach(eventType -> form.addButton(eventType.getName(), eventType.getName() + "\n" + TextFormat.colorize((eventManager.checkEventType(eventType) ? eventManager.getInfo(eventType): "&cInactive")), ImageType.PATH, eventType.getImage()));

        form.sendTo(player);
    }



}
