package alexis.practice.kit;

import alexis.practice.Practice;
import alexis.practice.profile.Profile;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class KitLoadout {
    @Getter
    private static final KitLoadout instance = new KitLoadout();

    private final Map<String, Map<String, Map<String, String>>> kits = new TreeMap<>();

    public void saveLoad(Profile profile, Kit kit) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        final List<Integer> slots = new ArrayList<>();
        final TreeMap<Integer, Item> inventory = new TreeMap<>(player.getInventory().getContents());

        kits.computeIfAbsent(kit.getName(), k -> new HashMap<>());
        kits.get(kit.getName()).put(profile.getIdentifier(), new TreeMap<>());

        for (Map.Entry<Integer, Item> entry : inventory.entrySet()) {
            int slot = entry.getKey();
            int id = entry.getValue().getId();
            int damage = entry.getValue().getDamage();
            int count = entry.getValue().getCount();

            Optional<Item> items = kit.getInventory().values().stream().filter(item -> item.getId() == id && item.getDamage() == damage).findFirst();
            if (items.isPresent() && !slots.contains(slot)) {
                kits.get(kit.getName()).get(profile.getIdentifier()).put(String.valueOf(slot), id + "-" + damage + "-" + count);
                slots.add(slot);
            }
        }

        profile.getKitData().setData(kit.getName(), getLoadInventory(profile, kit));
    }

    public Map<Integer, Item> getLoadInventory(Profile profile, Kit kit) {
        final Map<Integer, Item> inventory = new HashMap<>();
        final String uuid = profile.getIdentifier();
        final String kitName = kit.getName();

        if (kits.get(kitName) == null || !kits.get(kitName).containsKey(uuid)) {
            kits.computeIfAbsent(kitName, k -> new HashMap<>());
            kits.get(kitName).put(uuid, new TreeMap<>());

            kit.getInventory().forEach((slot, item) -> {
                kits.get(kitName).get(uuid).put(String.valueOf(slot), item.getId() + "-" + item.getDamage() + "-" + item.getCount());
                inventory.put(slot, item);
            });

        } else {
            final List<Integer> slots = new ArrayList<>();

            for (Map.Entry<String, String> entry : kits.get(kitName).get(uuid).entrySet()) {
                int slot = Integer.parseInt(entry.getKey());
                String[] split = entry.getValue().split("-");
                int id = Integer.parseInt(split[0]);
                int damage = Integer.parseInt(split[1]);
                int count = Integer.parseInt(split[2]);

                Optional<Item> items = kit.getInventory().values().stream().filter(item -> item.getId() == id && item.getDamage() == damage).findFirst();
                if (items.isPresent() && !slots.contains(slot)) {
                    Item cloneItem = items.get().clone();
                    cloneItem.setCount(count);
                    inventory.put(slot, cloneItem);
                    slots.add(slot);
                }

            }
        }

        return inventory;
    }

    public void load() {
        String kitsFolder = Practice.getInstance().getDataFolder() + File.separator + "kits" +  File.separator;

        try {
            Files.createDirectories(Path.of(kitsFolder));

            for (Kit kit : KitManager.getInstance().getKits().values()) {
                String filePath = kitsFolder + kit.getName() + ".json";
                Path path = Path.of(filePath);

                if (!Files.exists(path)) {
                    Files.createFile(path);
                }

                String data = Files.readString(path);

                Type mapType = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
                kits.put(kit.getName(), new Gson().fromJson(data, mapType));
            }

        } catch (IOException e) {
            System.err.println("Error loading kits: " + e.getMessage());
        }
    }

    public void save() {
        String kitsFolder = Practice.getInstance().getDataFolder() + File.separator + "kits" +  File.separator;

        try {
            Files.createDirectories(Path.of(kitsFolder));

            for (Kit kit : KitManager.getInstance().getKits().values()) {
                String filePath = kitsFolder + kit.getName() + ".json";
                Path path = Path.of(filePath);

                Type mapType = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
                String jsonData = new GsonBuilder().setPrettyPrinting().create().toJson(kits.get(kit.getName()), mapType);

                try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                    writer.write(jsonData);
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving kits: " + e.getMessage());
        }
    }

}
