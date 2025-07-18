package alexis.practice.event;

import alexis.practice.Practice;
import alexis.practice.event.games.EventType;
import alexis.practice.event.world.EventWord;
import alexis.practice.event.world.EventWorldManager;
import alexis.practice.kit.Kit;
import alexis.practice.profile.Profile;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {
    @Getter
    private final static EventManager instance = new EventManager();

    @Getter
    private final ConcurrentHashMap<Integer, Event> events = new ConcurrentHashMap<>(3);

    EventManager() {
        Practice.getInstance().getServer().getScheduler().scheduleRepeatingTask(Practice.getInstance(), () -> events.values().forEach(event -> event.getEventArena().tick()), 20);
    }

    public void createEvent(Profile profile, EventType type, Kit kit, int team) {
        if (profile.getCacheData().getCooldownEvent() > System.currentTimeMillis()) {
            try {
                profile.getPlayer().sendMessage(TextFormat.colorize("&cYou are still on cooldown. Please wait " + Utils.formatTime(profile.getCacheData().getCooldownEvent() - System.currentTimeMillis())));
            } catch (Exception ignored) {}
            return;
        }

        if (checkEventType(type)) {
            try {
                profile.getPlayer().sendMessage(TextFormat.colorize("&cI cannot host an event of the same type as one that is active"));
            } catch (Exception ignored) {}
            return;
        }

        int id = 0;
        while (new File(Practice.getInstance().getServer().getDataPath() + "worlds" + File.separator + "event-" + id).exists()) {
            id++;
        }

        if (type.equals(EventType.TOURNAMENT)) {
            try {
                Event event = new Event(id, profile, kit, type, team, null);
                events.put(id, event);

                Player player = profile.getPlayer();
                if (!player.isOp()) profile.getCacheData().setCooldownEvent(System.currentTimeMillis() + 10 * 60 * 1000);
            } catch (Exception ignored) {}
            return;
        }

        EventWord worldData = EventWorldManager.getInstance().getRandomWorld(type);

        if (worldData == null) {
            try {
                profile.getPlayer().sendMessage(TextFormat.colorize("&cEvent not available."));
            } catch (Exception ignored) {}
            return;
        }

        int finalId = id;
        worldData.copyWorld("event-" + id, Practice.getInstance().getServer().getDataPath() + File.separator + "worlds" + File.separator, () -> {
            try {
                Event event = new Event(finalId, profile, kit, type, team, worldData);
                events.put(finalId, event);

                Player player = profile.getPlayer();

                if (!player.isOp()) profile.getCacheData().setCooldownEvent(System.currentTimeMillis() + 10 * 60 * 1000);
            } catch (Exception ignored) {}
        });
    }

    public boolean checkEventType(EventType type) {
        return events.values().stream().anyMatch(e -> e.getType().equals(type));
    }

    public String getInfo(EventType type) {
        Optional<Event> event = events.values().stream().filter(e -> e.getType().equals(type)).findAny();

        if (event.isEmpty()) {
            return "Error";
        }

        String mode = (event.get().isTeam() ? "TO" + event.get().getCountTeam() : "FFA");
        return "Players: " + event.get().getPlayers().size() + " : Mode: " + mode;
    }

    public Optional<Event> getEvent(EventType eventType) {
        return events.values().stream()
                .filter(event -> event.getType().equals(eventType))
                .findFirst();
    }

    public void removeEvent(int id) {
        events.remove(id);
    }

}
