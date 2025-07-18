package alexis.practice.util;

import alexis.practice.Practice;
import alexis.practice.arena.world.ArenaWorld;
import alexis.practice.duel.world.DuelWorld;
import alexis.practice.event.games.EventType;
import alexis.practice.event.world.EventWord;
import alexis.practice.kit.Kit;
import alexis.practice.kit.KitManager;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;

import java.util.*;
import java.util.stream.Collectors;

public class SerializerUtil {

    public static ArenaWorld parseArena(String name, Map<String, Object> data) {
        if (!data.containsKey("kit-name")) {
            throw new RuntimeException("Kit name does not exist.");
        }
        String kitName = (String) data.get("kit-name");
        Kit kit = KitManager.getInstance().getKit(kitName);

        if (kit == null) {
            throw new RuntimeException("Kit does not exist: " + kitName);
        }

        if (!data.containsKey("world-name")) {
            throw new RuntimeException("World name does not exist.");
        }

        String worldName = (String) data.get("world-name");
        Level world = Practice.getInstance().getServer().getLevelByName(worldName);

        if (world == null) {
            throw new RuntimeException("World does not exist: " + worldName);
        }

        if (!Practice.getInstance().getServer().isLevelLoaded(worldName)) {
            Practice.getInstance().getServer().loadLevel(worldName);
        }

        if (!data.containsKey("can-build")) {
            throw new RuntimeException("Can-build property not specified.");
        }

        boolean canPlaceBlock = (boolean) data.get("can-build");

        if (!data.containsKey("spawns")) {
            throw new RuntimeException("Spawns do not exist.");
        }

        List<String> spawnsData = (List<String>) data.get("spawns");
        List<Vector3> spawns = spawnsData.stream()
                .map(spawnStr -> {
                    String[] coords = spawnStr.split(":");
                    if (coords.length != 3) {
                        throw new RuntimeException("Invalid spawn format: " + spawnStr);
                    }
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int z = Integer.parseInt(coords[2]);
                    return new Vector3(x, y, z);
                })
                .collect(Collectors.toList());

        return new ArenaWorld(name, kitName, canPlaceBlock, spawns, world);
    }

    public static DuelWorld parseDuelWorld(String name, Map<String, Object> data) {
        if (!data.containsKey("first-position") || !data.containsKey("second-position")) {
            throw new RuntimeException("First position or Second position does not exist.");
        }

        Vector3 firstPosition = Utils.parseVector3((String) data.get("first-position"));
        Vector3 secondPosition = Utils.parseVector3((String) data.get("second-position"));
        Vector3 firstPortal = null;
        Vector3 secondPortal = null;

        if (data.containsKey("first-portal") && data.containsKey("second-portal") && data.get("first-portal") != null && data.get("second-portal") != null) {
            firstPortal = Utils.parseVector3((String) data.get("first-portal"));
            secondPortal = Utils.parseVector3((String) data.get("second-portal"));
        }

        boolean canDrop = (boolean) data.get("can-drop");
        List<String> duelType = (List<String>) data.get("duel-type");

        return new DuelWorld(name, firstPosition, secondPosition, false, duelType, firstPortal, secondPortal, canDrop);
    }

    public static EventWord parseEvent(String name, Map<String, Object> data) {
        if (!data.containsKey("event-type")) {
            throw new RuntimeException("Event Type does not exist.");
        }

        EventType eventType = EventType.get((String) data.get("event-type"));

        if (eventType == null) {
            throw new RuntimeException("Event type does not exist: " + data.get("event-type"));
        }

        if (eventType.equals(EventType.MEETUP)) {
            return new EventWord(name, null, null, null, false, EventType.MEETUP, null);
        }

        if (!data.containsKey("lobby-position")) {
            throw new RuntimeException("Lobby position does not exist.");
        }

        Vector3 lobbyPosition = Utils.parseVector3((String) data.get("lobby-position"));
        Vector3 firstPosition = null;
        Vector3 secondPosition = null;
        List<Vector3> spawns = null;

        if (eventType.equals(EventType.SKYWARS)) {
            List<String> spawnsData = (List<String>) data.get("spawns");
            spawns = spawnsData.stream()
                    .map(spawnStr -> {
                        String[] coords = spawnStr.split(":");
                        if (coords.length != 3) {
                            throw new RuntimeException("Invalid spawn format: " + spawnStr);
                        }
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        int z = Integer.parseInt(coords[2]);
                        return new Vector3(x, y, z);
                    })
                    .collect(Collectors.toList());
        } else if (eventType.equals(EventType.SUMO)) {
            if (!data.containsKey("first-position") || !data.containsKey("second-position")) {
                throw new RuntimeException("First position or Second position does not exist.");
            }

            firstPosition = Utils.parseVector3((String) data.get("first-position"));
            secondPosition = Utils.parseVector3((String) data.get("second-position"));
        }

        return new EventWord(name, lobbyPosition, firstPosition, secondPosition, false, eventType, spawns);
    }

    public static Map<String, Object> serializeArenaWorld(ArenaWorld arenaWorld) {
        Map<String, Object> serializedData = new HashMap<>();
        serializedData.put("kit-name", arenaWorld.getKit().getName());
        serializedData.put("can-build", arenaWorld.isCanBuild());
        serializedData.put("world-name", arenaWorld.getWorld().getName());
        List<String> spawnData = new ArrayList<>();

        for (Vector3 spawn : arenaWorld.getSpawns()) {
            spawnData.add(spawn.getFloorX() + ":" + spawn.getFloorY() + ":" + spawn.getFloorZ());
        }

        serializedData.put("spawns", spawnData);
        return serializedData;
    }

    public static Map<String, Object> serializeDuelWorld(DuelWorld duelWorld) {
        Map<String, Object> serializedData = new HashMap<>();
        serializedData.put("can-drop", duelWorld.isCanDrop());
        Optional.ofNullable(duelWorld.getFirstPosition()).ifPresent(position ->
                serializedData.put("first-position", position.getFloorX() + ":" + position.getFloorY() + ":" + position.getFloorZ()));
        Optional.ofNullable(duelWorld.getSecondPosition()).ifPresent(position ->
                serializedData.put("second-position", position.getFloorX() + ":" + position.getFloorY() + ":" + position.getFloorZ()));
        Optional.ofNullable(duelWorld.getDuelType()).ifPresent(type ->
                serializedData.put("duel-type", type));
        Optional.ofNullable(duelWorld.getFirstPortal()).ifPresent(portal ->
                serializedData.put("first-portal", portal.getFloorX() + ":" + portal.getFloorY() + ":" + portal.getFloorZ()));
        Optional.ofNullable(duelWorld.getSecondPortal()).ifPresent(portal ->
                serializedData.put("second-portal", portal.getFloorX() + ":" + portal.getFloorY() + ":" + portal.getFloorZ()));

        return serializedData;
    }

    public static Map<String, Object> serializeEventWorld(EventWord eventWord) {
        Map<String, Object> serializedData = new HashMap<>();
        Optional.ofNullable(eventWord.getFirstPosition()).ifPresent(position ->
                serializedData.put("first-position", position.getFloorX() + ":" + position.getFloorY() + ":" + position.getFloorZ()));
        Optional.ofNullable(eventWord.getSecondPosition()).ifPresent(position ->
                serializedData.put("second-position", position.getFloorX() + ":" + position.getFloorY() + ":" + position.getFloorZ()));
        Optional.ofNullable(eventWord.getEventType()).ifPresent(type ->
                serializedData.put("event-type", type.getName()));
        Optional.ofNullable(eventWord.getLobbyPosition()).ifPresent(lobby ->
                serializedData.put("lobby-position", lobby.getFloorX() + ":" + lobby.getFloorY() + ":" + lobby.getFloorZ()));

        if (eventWord.getSpawns() != null) {
            List<String> spawnData = new ArrayList<>();
            for (Vector3 spawn : eventWord.getSpawns()) {
                spawnData.add(spawn.getFloorX() + ":" + spawn.getFloorY() + ":" + spawn.getFloorZ());
            }
            serializedData.put("spawns", spawnData);
        }

        return serializedData;
    }

    public static Kit parseKit(String name, ConfigSection kit) {
        String customName = Utils.cleanString(kit.getString("customName", "None"));
        String icon = kit.getString("icon", "textures/ui/feedIcon.png");

        Item[] armor = new Item[4];
        if (kit.exists("armor")) {
            ConfigSection armorSection = kit.getSection("armor");
            if (armorSection.exists("0")) {
                armor[0] = parseItem(armorSection.getSection("0"));
            }
            if (armorSection.exists("1")) {
                armor[1] = parseItem(armorSection.getSection("1"));
            }
            if (armorSection.exists("2")) {
                armor[2] = parseItem(armorSection.getSection("2"));
            }
            if (armorSection.exists("3")) {
                armor[3] = parseItem(armorSection.getSection("3"));
            }
        }

        Map<Integer, Item> inventory = new HashMap<>();
        if (kit.exists("inventory")) {
            ConfigSection itemsSection = kit.getSection("inventory");
            itemsSection.getSections().getKeys(false).forEach(key -> {
                ConfigSection currentItemSection = itemsSection.getSection(key);
                Item item = parseItem(currentItemSection);
                inventory.put(Integer.parseInt(key), item);
            });
        }

        List<Effect> effect = new ArrayList<>();
        if (kit.exists("effects")) {
            effect = parseEffects((kit.getSection("effects")));
        }

        ConfigSection knockback = kit.getSection("knockback");

        float verticalKnockback = (float) knockback.getDouble("vertical", 0.4);
        float horizontalKnockback = (float) knockback.getDouble("horizontal", 0.4);
        float maxHeight = (float) knockback.getDouble("max-height", 0.2);
        float limiter = (float) knockback.getDouble("limiter-kb", 0.005);
        int attackCooldown = knockback.getInt("attack-cooldown", 10);

        return new Kit(name, customName, icon, effect, armor, inventory, verticalKnockback, horizontalKnockback, maxHeight, limiter, attackCooldown);
    }

    private static Item parseItem(ConfigSection section) {
        Item item = Item.get(section.getInt("id", 0), section.getInt("meta", 0), section.getInt("count", 1));

        if (section.exists("customName")) {
            item.setCustomName(TextFormat.colorize(section.getString("customName", item.getCustomName())));
        }

        if (section.exists("damage")) {
            item.setDamage(section.getInt("damage", item.getMaxDurability()));
        }

        if (section.exists("enchantments")) {
            List<String> enchantmentsList = section.getStringList("enchantments");
            if (!enchantmentsList.isEmpty()) {
                enchantmentsList.forEach(enchantment -> {
                    String[] split = enchantment.split(":");
                    if (!enchantment.isEmpty() && split.length > 0) {
                        Enchantment enchantment1 = Enchantment.getEnchantment(Integer.parseInt(split[0]))
                                .setLevel(split.length == 2 ? Integer.parseInt(split[1]) : 1, false);
                        item.addEnchantment(enchantment1);
                    }
                });
            }
        }

        return item;
    }

    private static List<Effect> parseEffects(ConfigSection section) {
        List<Effect> effects = new ArrayList<>();

        section.getSections().getKeys(false).forEach(key -> {
            ConfigSection effectSection = section.getSection(key);
            Effect effect = Effect.getEffect(Integer.parseInt(key));
            effect.setAmplifier(effectSection.getInt("amplifier", 0));
            effect.setDuration(effectSection.getInt("duration", 1));
            effects.add(effect);
        });

        return effects;
    }

}
