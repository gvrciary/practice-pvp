package alexis.practice.item.object;

import alexis.practice.profile.Profile;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class ItemDefault {
    private final Item item;
    private final boolean isCountable;

    public boolean executeUse(Profile profile, Item item) {
        if (!profile.isOnline()) return false;

        try {
            Player player = profile.getPlayer();

            if (!isCountable) {
                item.setDamage(item.getDamage() + 1);
                if (item.getDamage() >= item.getMaxDurability()) player.getOffhandInventory().remove(item);
                else player.getInventory().setItemInHand(item);
            } else {
                final int count = item.getCount();

                if (count > 1) {
                    item.setCount(count - 1);
                    player.getInventory().setItem(player.getInventory().getHeldItemIndex(), item);
                } else player.getInventory().remove(item);
            }

            return true;
        } catch (Exception ignored) {}

        return false;
    }

}
