package alexis.practice.event.games.types.skywars.chest;

import alexis.practice.util.Utils;
import cn.nukkit.block.BlockChest;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockTransparentMeta;
import cn.nukkit.block.BlockTrappedChest;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.Location;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChestManager {

    private static final List<ItemChest> items = new ArrayList<>() {{
        add(new ItemChest(16, 48, 4, 0, false));
        add(new ItemChest(16, 48, 5, 0, false));
        add(new ItemChest(0, 1, ItemID.GOLD_AXE, 0, false));
        add(new ItemChest(0, 1, ItemID.IRON_AXE, 0, false));
        add(new ItemChest(0, 1, ItemID.GOLD_SWORD, 0, false));
        add(new ItemChest(0, 1, ItemID.IRON_SWORD, 0, false));
        add(new ItemChest(0, 8, ItemID.ARROW, 0, false));
        add(new ItemChest(0, 1, ItemID.BOW, 0, false));
        add(new ItemChest(0, 1, ItemID.CHAIN_HELMET, 0, false));
        add(new ItemChest(0, 1, ItemID.CHAIN_CHESTPLATE, 0, false));
        add(new ItemChest(0, 1, ItemID.CHAIN_LEGGINGS, 0, false));
        add(new ItemChest(0, 1, ItemID.CHAIN_BOOTS, 0, false));
        add(new ItemChest(0, 6, ItemID.SNOWBALL, 0, false));
        add(new ItemChest(0, 1, ItemID.ENDER_PEARL, 0, false));
        add(new ItemChest(0, 1, ItemID.GOLD_CHESTPLATE, 0, false));
        add(new ItemChest(0, 1, ItemID.GOLD_LEGGINGS, 0, false));
        add(new ItemChest(0, 2, ItemID.GOLDEN_APPLE, 0, false));
        add(new ItemChest(0, 3, ItemID.EGG, 0, false));
        add(new ItemChest(0, 1, ItemID.IRON_HELMET, 0, false));
        add(new ItemChest(0, 1, ItemID.IRON_BOOTS, 0, false));
        add(new ItemChest(0, 1, ItemID.BUCKET, 8, false));
        add(new ItemChest(0, 1, ItemID.BUCKET, 10, false));

        add(new ItemChest(16, 48, 1, 0, true));
        add(new ItemChest(0, 1, Item.DIAMOND_AXE, 0, true));
        add(new ItemChest(0, 2, Item.DIAMOND, 0, true));
        add(new ItemChest(0, 1, ItemID.IRON_CHESTPLATE, 0, true));
        add(new ItemChest(0, 1, ItemID.IRON_LEGGINGS, 0, true));
        add(new ItemChest(0, 1, ItemID.DIAMOND_HELMET, 0, true));
        add(new ItemChest(0, 1, ItemID.DIAMOND_SWORD, 0, true));
        add(new ItemChest(0, 1, ItemID.DIAMOND_BOOTS, 0, true));
        add(new ItemChest(0, 1, ItemID.FISHING_ROD, 0, true));
        add(new ItemChest(0, 2, BlockID.TNT, 0, true));
        add(new ItemChest(0, 3, BlockID.COBWEB, 0, true));
    }};

    private final List<Location> chests = new ArrayList<>();

    public void setChest(Location location, BlockTransparentMeta blockChest) {
        if (!chests.contains(location)) {
            chests.add(location);

            BlockEntityChest chest = (BlockEntityChest) blockChest.getLevel().getBlockEntity(blockChest);
            if (blockChest instanceof BlockTrappedChest) {
                Map<Integer, Item> items = getRandomInventory(chest.getInventory().getSize(), true);
                chest.getInventory().setContents(items);
            } else if (blockChest instanceof BlockChest) {
                Map<Integer, Item> items = getRandomInventory(chest.getInventory().getSize(), false);
                chest.getInventory().setContents(items);
            }
        }
    }

    public void removeChest(Location location) {
        chests.remove(location);
    }

    public void refillChest() {
        chests.clear();
    }

    public Map<Integer, Item> getRandomInventory(int size, boolean isChestOP) {
        List<ItemChest> filteredItems = items.stream()
                .filter(item -> item.isChestOP() == isChestOP)
                .collect(Collectors.toList());

        int countItems = Utils.randomInteger(6, 12);
        Map<Integer, Item> randomItems = new HashMap<>();

        for (int i = 0; i < countItems; i++) {
            int slot = Utils.randomInteger(0, size - 1);
            int random = Utils.randomInteger(0, filteredItems.size() - 1);
            Item item = filteredItems.get(random).getItem();
            randomItems.put(slot, item);

            filteredItems.remove(random);
        }

        return randomItems;
    }

    static class ItemChest {

        private final int min;
        private final int max;
        private final int id;
        private final int meta;
        @Getter
        private final boolean isChestOP;

        public ItemChest(int min, int max, int id, int meta, boolean isChestOP) {
            this.min = min;
            this.max = max;
            this.id = id;
            this.meta = meta;
            this.isChestOP = isChestOP;
        }

        public Item getItem() {
            Item item = Item.get(id, meta);
            item.setCount(Utils.randomInteger(min, max));
            return item;
        }

    }

}
