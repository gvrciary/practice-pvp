package alexis.practice.entity;

import alexis.practice.profile.Profile;
import alexis.practice.util.server.Mechanics;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntitySlenderProjectile;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.DataPacket;

public class FishingHookEntity extends EntitySlenderProjectile {

    public static final int NETWORK_ID = 77;

    public final boolean canCollide = true;
    private final Profile profile;

    public FishingHookEntity(FullChunk chunk, CompoundTag nbt, Entity shootingEntity, Profile profile) {
        super(chunk, nbt, shootingEntity);

        this.profile = profile;
        this.setLevel(shootingEntity.getLevel());
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        if (this.age > 0) {
            profile.stopFishing();
        }
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.25f;
    }

    @Override
    public float getLength() {
        return 0.25f;
    }

    @Override
    public float getHeight() {
        return 0.25f;
    }

    @Override
    public float getGravity() {
        return Mechanics.getInstance().getFishingHookMechanics().getGravity();
    }

    @Override
    public float getDrag() {
        return Mechanics.getInstance().getFishingHookMechanics().getDrag();
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.isClosed()) return false;

        if (shootingEntity != null && shootingEntity instanceof Player player && player.isOnline()) {
            if (profile.getCosmeticData().hasProjectileTrail()) profile.getCosmeticData().getProjectileTrail().getEffect().sendToPosition(this.getPosition());

            if (player.getInventory() != null && player.getInventory().getItemInHand() != null && player.getInventory().getItemInHand().getId() != ItemID.FISHING_ROD || !player.isAlive() || player.isClosed() || !player.getLevel().equals(this.level)) {
                profile.stopFishing();
                return false;
            }

            if (player.distance(this.getPosition()) > 24) {
                profile.stopFishing();
                return false;
            }
        }

        return super.onUpdate(currentTick);
    }

    @Override
    protected void updateMotion() {
        if (this.isInsideOfWater() && this.getY() < this.getWaterHeight() - 2) {
            this.motionX = 0;
            this.motionY += getGravity();
            this.motionZ = 0;
        } else if (this.isInsideOfWater() && this.getY() >= this.getWaterHeight() - 2) {
            this.motionX = 0;
            this.motionZ = 0;
            this.motionY = 0;
        } else {
            super.updateMotion();
        }
    }

    public int getWaterHeight() {
        for (int y = this.getFloorY(); y < 256; y++) {
            int id = this.level.getBlockIdAt(chunk, this.getFloorX(), y, this.getFloorZ());
            if (id == Block.AIR) {
                return y;
            }
        }
        return this.getFloorY();
    }

    @Override
    protected DataPacket createAddEntityPacket() {
        AddEntityPacket pk = new AddEntityPacket();
        pk.entityRuntimeId = this.getId();
        pk.entityUniqueId = this.getId();
        pk.type = NETWORK_ID;
        pk.x = (float) this.x;
        pk.y = (float) this.y;
        pk.z = (float) this.z;
        pk.speedX = (float) this.motionX;
        pk.speedY = (float) this.motionY;
        pk.speedZ = (float) this.motionZ;
        pk.yaw = (float) this.yaw;
        pk.pitch = (float) this.pitch;

        long ownerId = this.shootingEntity.getId();
        pk.metadata = this.dataProperties.putLong(DATA_OWNER_EID, ownerId).clone();
        return pk;
    }

    @Override
    public boolean canCollide() {
        return this.canCollide;
    }

    @Override
    public void onHit() {
        profile.stopFishing();
    }

    @Override
    public void onCollideWithEntity(Entity entity) {
        if (entity == this.shootingEntity) return;

        if (!(shootingEntity instanceof Player player)) return;

        if (!player.isOnline()) return;

        EntityDamageEvent ev = new EntityDamageByChildEntityEvent(this.shootingEntity, this, entity, DamageCause.PROJECTILE, 0);

        if (entity.attack(ev)) {
            this.hadCollision = true;
            profile.stopFishing();
        }
    }
}
