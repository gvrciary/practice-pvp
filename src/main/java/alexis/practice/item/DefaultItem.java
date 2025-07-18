package alexis.practice.item;

import alexis.practice.item.defaults.FireCharger;
import alexis.practice.item.defaults.FishingRod;
import alexis.practice.item.defaults.Soup;
import alexis.practice.item.defaults.TNT;
import alexis.practice.item.object.ItemDefault;
import cn.nukkit.block.BlockID;
import cn.nukkit.item.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum DefaultItem {
    FISHING_ROD(new FishingRod()),
    FIRE_CHARGER(new FireCharger()),
    SOUP(new Soup()),
    TNT(new TNT());

    private final ItemDefault itemInstance;

    public static ItemDefault getItemDefault(Item item) {
        return Arrays.stream(DefaultItem.values())
                .map(DefaultItem::getItemInstance)
                .filter(instance -> instance.getItem().getId() == item.getId() && instance.getItem().getId() != BlockID.TNT)
                .findFirst()
                .orElse(null);
    }

}
