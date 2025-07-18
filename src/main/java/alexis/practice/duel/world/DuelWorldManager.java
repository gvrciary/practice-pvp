package alexis.practice.duel.world;

import alexis.practice.Practice;
import alexis.practice.util.SerializerUtil;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DuelWorldManager {

    @Getter
    private final static DuelWorldManager instance = new DuelWorldManager();

    @Getter
    private final Map<String, DuelWorld> worlds = new HashMap<>();

    public DuelWorld getWorld(String name) {
        return worlds.get(name);
    }

    public DuelWorld getRandomWorld(String type) {
        return getWorldsByType(type)
                .stream()
                .findAny()
                .orElse(null);
    }

    public List<DuelWorld> getWorldsByType(String type) {
        return worlds.values()
                .stream()
                .filter(world -> world.allowDuel(type))
                .collect(Collectors.toList());
    }

    public void createWorld(String name, Vector3 firstPosition, Vector3 secondPosition, List<String> duelType, Vector3 firstPortal, Vector3 secondPortal, boolean isCanDrop) {
        worlds.put(name, new DuelWorld(name, firstPosition, secondPosition, true, duelType, firstPortal, secondPortal, isCanDrop));
    }

    public void removeWorld(String name) {
        worlds.remove(name);
    }

    public void load() {
        File dir = new File(Practice.getInstance().getDataFolder() + File.separator, "storage");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        Config config = new Config(new File(dir, "duels.json"), Config.JSON);

        for (String name : config.getKeys(false)) {
            try {
                Map<String, Object> data = config.getSection(name);
                worlds.put(name, SerializerUtil.parseDuelWorld(name, data));
            } catch (RuntimeException e) {
                Practice.getInstance().getLogger().info("Duel World parse error: " + e.getMessage());
            }
        }
    }

    public void save() {
        File dir = new File(Practice.getInstance().getDataFolder() + File.separator, "storage");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        Config config = new Config(new File(dir, "duels.json"), Config.JSON);

        Map<String, Object> worldData = new HashMap<>();
        for (Map.Entry<String, DuelWorld> entry : worlds.entrySet()) {
            worldData.put(entry.getKey(), SerializerUtil.serializeDuelWorld(entry.getValue()));
        }

        for (Map.Entry<String, Object> entry : worldData.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }

        config.save();
    }
}

