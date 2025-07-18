package alexis.practice.util;

import cn.nukkit.Player;
import cn.nukkit.entity.item.EntityFirework;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.DyeColor;

public final class Fireworks {

    private static final ItemFirework FIREWORK = new ItemFirework();

    static {
        FIREWORK.setNamedTag(new CompoundTag().putCompound("Fireworks", new CompoundTag("Fireworks")
                .putList(new ListTag<>("Explosions").add(new CompoundTag()
                        .putByteArray("FireworkColor", new byte[]{(byte) DyeColor.ORANGE.getDyeData()})
                        .putByteArray("FireworkFade", new byte[]{})
                        .putBoolean("FireworkFlicker", true)
                        .putBoolean("FireworkTrail", true)
                        .putByte("FireworkType", ItemFirework.FireworkExplosion.ExplosionType.LARGE_BALL.ordinal())))
                .putByte("Flight", 1)));
    }

    public static void spawnFirework(Player player) {
        if (!player.isOnline()) return;

        CompoundTag nbt = Utils.createCompoundTag(player)
                .putCompound("FireworkItem", NBTIO.putItemHelper(FIREWORK));
        FullChunk chunk = player.getLevel().getChunkIfLoaded(player.getPosition().getChunkX(), player.getPosition().getChunkZ());

        if (chunk != null) {
            new EntityFirework(chunk, nbt).spawnToAll();
        }
    }
}
