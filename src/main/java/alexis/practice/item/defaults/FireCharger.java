package alexis.practice.item.defaults;

import alexis.practice.entity.FireballEntity;
import alexis.practice.item.object.ItemDefault;
import alexis.practice.profile.Profile;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;

public class FireCharger extends ItemDefault {

    public FireCharger() {
        super(Item.get(ItemID.FIRE_CHARGE), true);
    }

    @Override
    public boolean executeUse(Profile profile, Item item) {
        try {
            Player player = profile.getPlayer();

            if (profile.getCacheData().getFireball().isInCooldown()) return false;

            if (super.executeUse(profile, item)) {
                double f = 1.8D;
                double yaw = player.yaw;
                double pitch = player.pitch;
                Location pos = new Location(player.x - Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 1.5D, player.y + (double) player.getEyeHeight(), player.z + Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 1.5D, yaw, pitch, player.level);
                FireballEntity fireBall = new FireballEntity(player.chunk, Entity.getDefaultNBT(pos));
                fireBall.setExplode(true);
                fireBall.setMotion(new Vector3(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f, -Math.sin(Math.toRadians(pitch)) * f * f, Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f));
                fireBall.spawnToAll();

                profile.getCacheData().getFireball().set();
                return true;
            }
        } catch (Exception ignored) {}

        return false;
    }

}
