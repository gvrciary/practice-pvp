package alexis.practice.item.defaults;

import alexis.practice.item.object.ItemDefault;
import alexis.practice.profile.Profile;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;

public class Soup extends ItemDefault {

    public Soup() {
        super(Item.get(ItemID.MUSHROOM_STEW), true);
    }

    @Override
    public boolean executeUse(Profile profile, Item item) {
        try {
            Player player = profile.getPlayer();

            if (player.getMaxHealth() > player.getHealth()) {
                if (super.executeUse(profile, item)) {
                    player.setHealth(player.getHealth() + 7);
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

}
