package alexis.practice.item.defaults;

import alexis.practice.entity.FishingHookEntity;
import alexis.practice.item.object.ItemDefault;
import alexis.practice.profile.Profile;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.ProtocolInfo;

import java.util.Random;

public class FishingRod extends ItemDefault {

    public FishingRod() {
        super(Item.get(ItemID.FISHING_ROD), false);
    }

    @Override
    public boolean executeUse(Profile profile, Item item) {
        if (super.executeUse(profile, item)) {
            try {
                Player player = profile.getPlayer();
                if (profile.getFishing() != null) profile.stopFishing();
                else {
                    CompoundTag nbt = new CompoundTag()
                            .putList(new ListTag<DoubleTag>("Pos")
                                    .add(new DoubleTag("", player.x))
                                    .add(new DoubleTag("", player.y + player.getEyeHeight()))
                                    .add(new DoubleTag("", player.z)))
                            .putList(new ListTag<DoubleTag>("Motion")
                                    .add(new DoubleTag("", -Math.sin(Math.toRadians(player.yaw)) * Math.cos(Math.toRadians(player.pitch)) * 1.3))
                                    .add(new DoubleTag("", -Math.sin(Math.toRadians(player.pitch)) * 1.3))
                                    .add(new DoubleTag("", Math.cos(Math.toRadians(player.yaw)) * Math.cos(Math.toRadians(player.pitch)) * 1.3)))
                            .putList(new ListTag<FloatTag>("Rotation")
                                    .add(new FloatTag("", (float) player.yaw))
                                    .add(new FloatTag("", (float) player.pitch)));

                    FishingHookEntity fishingHook = new FishingHookEntity(player.chunk, nbt, player, profile);

                    double x = fishingHook.motionX, y = fishingHook.motionY, z = fishingHook.motionZ;
                    double f2 = 1.0;
                    double f1 = 1.5;

                    Random rand = new Random();
                    double f = Math.sqrt(x * x + y * y + z * z);
                    x = x / f;
                    y = y / f;
                    z = z / f;
                    x = x + (rand.nextDouble() * 2 - 1) * 0.007499999832361937 * f2;
                    y = y + (rand.nextDouble() * 2 - 1) * 0.007499999832361937 * f2;
                    z = z + (rand.nextDouble() * 2 - 1) * 0.007499999832361937 * f2;
                    x = x * f1;
                    y = y * f1;
                    z = z * f1;

                    Vector3 motion = fishingHook.getMotion().add(x, y, z);

                    fishingHook.setMotion(motion);
                    profile.startFishing(fishingHook);

                    if (player.protocol >= ProtocolInfo.v1_20_0_23) {
                        player.level.addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_THROW, -1, "minecraft:player", false, false);
                    }
                }

                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

}
