package alexis.practice.profile.data;

import alexis.practice.kit.KitLoadout;
import alexis.practice.kit.KitManager;
import alexis.practice.profile.Profile;
import cn.nukkit.item.Item;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class KitData {

    private final Map<String, Data> kits = new HashMap<>(KitManager.getInstance().getKits().size());

    public KitData(Profile profile) {
        KitManager.getInstance().getKits().values().forEach(kit -> kits.put(kit.getName(), new Data(KitLoadout.getInstance().getLoadInventory(profile, kit))));
    }

    public void setData(String name, Map<Integer, Item> inventory) {
        kits.put(name, new Data(inventory));
    }

    public Data getData(String name) {
        return kits.get(name);
    }

    @Setter
    @Getter
    public static final class Data {
        private int elo = 1000;
        private Map<Integer, Item> inventory;

        Data(Map<Integer, Item> inventory) {
            this.inventory = inventory;
        }
    }

}
