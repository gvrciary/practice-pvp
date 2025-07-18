package alexis.practice.event.world;

import alexis.practice.Practice;
import alexis.practice.event.games.EventType;
import alexis.practice.util.SerializerUtil;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventWorldManager {

    @Getter
    private static final EventWorldManager instance = new EventWorldManager();
    @Getter
    private final Map<String, EventWord> eventsWorlds = new HashMap<>();

    public EventWord getWorld(String name) {
        return eventsWorlds.get(name);
    }

    public EventWord getRandomWorld(EventType type) {
        return getWorldsByType(type)
                .stream()
                .findAny()
                .orElse(null);
    }

    public List<EventWord> getWorldsByType(EventType type) {
        return eventsWorlds.values()
                .stream()
                .filter(world -> world.getEventType().equals(type))
                .collect(Collectors.toList());
    }

    public void createEventWorld(String name, Vector3 lobbyPosition, Vector3 firstPosition, Vector3 secondPosition, EventType type, List<Vector3> spawns) {
        eventsWorlds.put(name, new EventWord(name, lobbyPosition, firstPosition, secondPosition, true, type, spawns));
    }

    public void removeWorld(String name) {
        eventsWorlds.remove(name);
    }

    public void load() {
        File dir = new File(Practice.getInstance().getDataFolder() + File.separator, "storage");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        Config config = new Config(new File(dir, "events.json"), Config.JSON);

        for (String name : config.getKeys(false)) {
            try {
                Map<String, Object> data = config.getSection(name);
                eventsWorlds.put(name, SerializerUtil.parseEvent(name, data));
            } catch (RuntimeException e) {
                Practice.getInstance().getLogger().info("Event World parse error: " + e.getMessage());
            }
        }
    }

    public void save() {
        File dir = new File(Practice.getInstance().getDataFolder() + File.separator, "storage");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        Config config = new Config(new File(dir, "events.json"), Config.JSON);

        Map<String, Object> worldData = new HashMap<>();
        for (Map.Entry<String, EventWord> entry : eventsWorlds.entrySet()) {
            worldData.put(entry.getKey(), SerializerUtil.serializeEventWorld(entry.getValue()));
        }

        for (Map.Entry<String, Object> entry : worldData.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }

        config.save();
    }

}
