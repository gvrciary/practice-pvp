package alexis.practice.item.defaults;

import alexis.practice.entity.TntEntity;
import alexis.practice.item.object.ItemDefault;
import alexis.practice.profile.Profile;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.item.Item;
import cn.nukkit.level.sound.TNTPrimeSound;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;

public class TNT extends ItemDefault {

    public TNT() {
        super(Block.get(BlockID.TNT).toItem(), true);
    }

    @Override
    public boolean executeUse(Profile profile, Item item) {
        if (super.executeUse(profile, item)) {
            Block block = item.getBlock();

            double mot = cn.nukkit.utils.Utils.nukkitRandom.nextSignedFloat() * 6.283185307179586;
            CompoundTag nbt = new CompoundTag()
                    .putList(new ListTag<DoubleTag>("Pos")
                            .add(new DoubleTag("", block.x + 0.5))
                            .add(new DoubleTag("", block.y))
                            .add(new DoubleTag("", block.z + 0.5)))
                    .putList(new ListTag<DoubleTag>("Motion")
                            .add(new DoubleTag("", -Math.sin(mot) * 0.02))
                            .add(new DoubleTag("", 0.2))
                            .add(new DoubleTag("", -Math.cos(mot) * 0.02)))
                    .putList(new ListTag<FloatTag>("Rotation")
                            .add(new FloatTag("", 0))
                            .add(new FloatTag("", 0)))
                    .putShort("Fuse", 5);

            TntEntity entityTnt = new TntEntity(block.getLevel().getChunk(block.getFloorX() >> 4, block.getFloorZ() >> 4), nbt);
            entityTnt.spawnToAll();
            block.level.addSound(new TNTPrimeSound(block));
            return true;
        }

        return false;
    }

}
