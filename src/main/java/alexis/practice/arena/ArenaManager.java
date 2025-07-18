package alexis.practice.arena;

import alexis.practice.Practice;
import alexis.practice.arena.world.ArenaWorld;
import alexis.practice.util.SerializerUtil;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import lombok.Getter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ArenaManager {
    @Getter
    private static final ArenaManager instance = new ArenaManager();
    @Getter
    private final ConcurrentHashMap<String, ArenaWorld> arenas = new ConcurrentHashMap<>();

    ArenaManager() {
        Practice.getInstance().getServer().getScheduler().scheduleRepeatingTask(Practice.getInstance(), () -> arenas.values().stream().filter(arenaWorld -> arenaWorld.isCanBuild() && !arenaWorld.getArena().getPlayers().isEmpty()).forEach(arenaWorld -> arenaWorld.getArena().tick()), 40, true);
    }

    public ArenaWorld getArenaWorld(String name) {
        return arenas.get(name);
    }

    public void createArena(String name, String kit, boolean canBuild, List<Vector3> spawns, Level world) {
        arenas.put(name, new ArenaWorld(name, kit, canBuild, spawns, world));
    }

    public void removeArena(String name) {
        arenas.remove(name);
    }

    public void load() {
        String dir = Practice.getInstance().getDataFolder() + File.separator + "storage";

        File storageDir = new File(dir);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        Config configSection = new Config(new File(dir, "arenas.json"), Config.JSON);

        for (String name : configSection.getKeys(false)) {
            try {
                Map<String, Object> data = configSection.getSection(name);
                arenas.put(name, SerializerUtil.parseArena(name, data));
            } catch (RuntimeException exception) {
                Practice.getInstance().getLogger().info("Arena World parse error: " + exception.getMessage());
            }
        }
    }

    public void save() {
        String dir = Practice.getInstance().getDataFolder() + File.separator + "storage";

        File storageDir = new File(dir);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        Config config = new Config(dir + File.separator + "arenas.json", Config.JSON);
        ConfigSection configSection = new ConfigSection();

        for (Map.Entry<String, ArenaWorld> entry : arenas.entrySet()) {
            configSection.set(entry.getKey(), SerializerUtil.serializeArenaWorld(entry.getValue()));
        }

        config.setAll(configSection);
        config.save();
    }

}
