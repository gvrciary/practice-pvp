package alexis.practice.event.games.types.meetup.kit;

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.utils.TextFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum KitStorage {
    KIT_ONE(new HashMap<>() {{
        put(0, getItem(ItemID.DIAMOND_SWORD, 1, Enchantment.get(Enchantment.ID_DAMAGE_ALL).setLevel(1)));
        put(1, Item.get(ItemID.FISHING_ROD));
        put(2, getItem(ItemID.GOLDEN_APPLE, 10, null));
        put(3, Item.get(ItemID.GOLDEN_APPLE, 10, 6).setCustomName(TextFormat.colorize("&r&6Golden Apple")));
        put(4, getItem(ItemID.BOW, 1, Enchantment.get(Enchantment.ID_BOW_POWER).setLevel(3)));

       setDefaultKit(this);
    }}, new Item[] {
            getItem(ItemID.IRON_HELMET, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(3)),
            getItem(ItemID.DIAMOND_CHESTPLATE, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(2)),
            getItem(ItemID.IRON_LEGGINGS, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(3)),
            getItem(ItemID.DIAMOND_BOOTS, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(2))
    }),
    KIT_TWO(new HashMap<>() {{
        put(0, getItem(ItemID.IRON_SWORD, 1, Enchantment.get(Enchantment.ID_DAMAGE_ALL).setLevel(2)));
        put(1, Item.get(ItemID.FISHING_ROD));
        put(2, getItem(ItemID.GOLDEN_APPLE, 10, null));
        put(3, Item.get(ItemID.GOLDEN_APPLE, 10, 8).setCustomName(TextFormat.colorize("&r&6Golden Apple")));
        put(4, getItem(ItemID.BOW, 1, Enchantment.get(Enchantment.ID_BOW_POWER).setLevel(1)));

        setDefaultKit(this);
    }}, new Item[] {
        getItem(ItemID.IRON_HELMET, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(2)),
                getItem(ItemID.IRON_CHESTPLATE, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(3)),
                getItem(ItemID.DIAMOND_LEGGINGS, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(3)),
                getItem(ItemID.DIAMOND_BOOTS, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(2))
    }),
    KIT_THREE(new HashMap<>() {{
        put(0, getItem(ItemID.DIAMOND_SWORD, 1, Enchantment.get(Enchantment.ID_DAMAGE_ALL).setLevel(2)));
        put(1, Item.get(ItemID.FISHING_ROD));
        put(2, getItem(ItemID.GOLDEN_APPLE, 12, null));
        put(3, Item.get(ItemID.GOLDEN_APPLE, 10, 6).setCustomName(TextFormat.colorize("&r&6Golden Apple")));
        put(4, getItem(ItemID.BOW, 1, Enchantment.get(Enchantment.ID_BOW_POWER).setLevel(3)));

        setDefaultKit(this);
    }}, new Item[] {
            getItem(ItemID.DIAMOND_HELMET, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(2)),
            getItem(ItemID.IRON_CHESTPLATE, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(3)),
            getItem(ItemID.IRON_LEGGINGS, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(3)),
            getItem(ItemID.DIAMOND_BOOTS, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(2))
    }),
    KIT_FOUR(new HashMap<>() {{
        put(0, getItem(ItemID.IRON_SWORD, 1, Enchantment.get(Enchantment.ID_DAMAGE_ALL).setLevel(2)));
        put(1, Item.get(ItemID.FISHING_ROD));
        put(2, getItem(ItemID.GOLDEN_APPLE, 13, null));
        put(3, Item.get(ItemID.GOLDEN_APPLE, 10, 5).setCustomName(TextFormat.colorize("&r&6Golden Apple")));
        put(4, getItem(ItemID.BOW, 1, Enchantment.get(Enchantment.ID_BOW_POWER).setLevel(2)));

        setDefaultKit(this);
    }}, new Item[] {
            getItem(ItemID.DIAMOND_HELMET, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(2)),
            getItem(ItemID.IRON_CHESTPLATE, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(3)),
            getItem(ItemID.DIAMOND_LEGGINGS, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(2)),
            getItem(ItemID.IRON_BOOTS, 1, Enchantment.get(Enchantment.ID_PROTECTION_ALL).setLevel(2))
    });

    private final Map<Integer, Item> inventory;
    private final Item[] armor;

    private static Item getItem(int id, int count) {
        return getItem(id, count, null);
    }

    private static Item getItem(int id, int count, Enchantment enchantment) {
        Item item = Item.get(id);
        item.setCount(count);

        if (enchantment != null) {
            item.addEnchantment(enchantment);
        }

        return item;
    }

    private static void setDefaultKit(Map<Integer, Item> map) {
        map.put(5, Item.get(ItemID.BUCKET, 8, 1));
        map.put(6, Item.get(ItemID.BUCKET, 10, 1));
        map.put(7, getItem(ItemID.STEAK, 64, null));
        map.put(8, getBlockToItem(Block.COBBLESTONE, 64));
        map.put(9, getBlockToItem(Block.WOOD, 64));
        map.put(10, getItem(ItemID.DIAMOND_PICKAXE, 8));
        map.put(11, getItem(ItemID.DIAMOND_AXE, 10));
        map.put(12, Item.get(ItemID.BUCKET, 8, 1));
        map.put(13, Item.get(ItemID.BUCKET, 10, 1));
        map.put(14, getItem(ItemID.ARROW, 16, null));
        map.put(15, getBlockToItem(Block.ANVIL, 64));
        map.put(16, getBlockToItem(Block.COBBLESTONE, 64));
        map.put(17, getBlockToItem(Block.WOOD, 64));
    }

    private static Item getBlockToItem(int id, int count) {
        Item item = Block.get(id).toItem();
        item.setCount(count);
        return item;
    }

}
