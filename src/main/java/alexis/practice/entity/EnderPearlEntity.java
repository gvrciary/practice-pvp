package alexis.practice.entity;

import alexis.practice.util.server.Mechanics;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.PortalParticle;
import cn.nukkit.level.sound.EndermanTeleportSound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.MovePlayerPacket;

final public class EnderPearlEntity extends EntityProjectile {

    public EnderPearlEntity(FullChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
        setScale(0.6F);
    }

    @Override
    public int getNetworkId() {
        return 87;
    }

    public float getWidth() {
        return 0.25F;
    }

    public float getLength() {
        return 0.25F;
    }

    public float getHeight() {
        return 0.25F;
    }

    protected float getGravity() {
        return Mechanics.getInstance().getEnderPearlMechanics().getGravity();
    }

    protected float getDrag() {
        return Mechanics.getInstance().getEnderPearlMechanics().getDrag();
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        if (this.isCollided && this.shootingEntity instanceof Player) {
            boolean portal = false;
            for (Block collided : this.getCollisionBlocks()) {
                if (collided.getId() == Block.NETHER_PORTAL) {
                    portal = true;
                }
            }

            if (!portal) {
                teleport();
            }

            this.close();
            return false;
        }

        if (this.age > 1200 || this.isCollided) {
            this.close();
        }

        return super.onUpdate(currentTick);
    }

    public void onCollideWithEntity(Entity entity) {
        if (this.shootingEntity instanceof Player) {
            teleport();
        }

        super.onCollideWithEntity(entity);
    }

    private void teleport() {
        Vector3 to = new Vector3(this.x, this.y, this.z);

        if (this.level.equals(this.shootingEntity.getLevel())) {
            this.level.addParticle(new PortalParticle(shootingEntity));
            this.level.addSound(new EndermanTeleportSound(shootingEntity));

            if (shootingEntity instanceof Player player) {
                final double yaw = player.getYaw();
                final double pitch = player.getPitch();
                final double headYaw = player.getHeadYaw();

                player.setPositionAndRotation(to, yaw, pitch, headYaw);
                player.sendPosition(to, yaw, pitch, MovePlayerPacket.MODE_TELEPORT);
            }

            this.level.addParticle(new PortalParticle(this));
            this.level.addSound(new EndermanTeleportSound(this));
        }
    }

}
