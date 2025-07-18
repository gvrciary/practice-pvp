package alexis.practice.entity;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;
import lombok.Getter;

@Getter
public class DeathAnimation extends EntityHuman {

    private final float fallSpeed = 0.4F;
    private final float moveX;
    private final float moveZ;

    private int tick = 0;

    public DeathAnimation(FullChunk chunk, CompoundTag nbt, float x, float z) {
        super(chunk, nbt);

        this.moveX = x;
        this.moveZ = z;
    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        ev.setCancelled();
        return super.attack(ev);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.isClosed()) return false;

        if (!this.isOnGround()) {
            this.move(moveX / 20, -fallSpeed, moveZ  / 20);
            this.updateMovement();
        }

        if (tick++ == 25) {
            this.despawnFromAll();
            this.close();
            return false;
        }

        return super.onUpdate(currentTick);
    }

    public void killAnimation() {
        EntityEventPacket pk = new EntityEventPacket();
        pk.eid = this.getId();
        pk.event = EntityEventPacket.DEATH_ANIMATION;
        Server.broadcastPacket(this.getViewers().values(), pk);
    }

    @Override
    public void collidingWith(Entity ent) {}

    @Override
    public boolean canBeMovedByCurrents() {
        return false;
    }

    @Override
    public boolean canClimbWalls() {
        return false;
    }

}
