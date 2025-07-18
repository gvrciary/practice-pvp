package alexis.practice.item.object;

import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

@Getter
public class ItemCustom {
    private final Item item;

    private final String name;

    public ItemCustom(String name, int id, int meta) {
        this.name = Utils.cleanString(name);

        item = Item.get(id, meta).setCustomName(TextFormat.colorize("&r" + name + "&7 (Use)"));
        item.setNamedTag(item.getNamedTag().putString("custom_item", this.name));
    }

    public void handleUse(Player player) {}

}
