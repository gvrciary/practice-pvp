package alexis.practice.entity;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityExplosive;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Explosion;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.CriticalParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.List;

public class FireballEntity extends EntityProjectile implements EntityExplosive {
    public static final int NETWORK_ID = 85;
    protected boolean critical;
    protected boolean canExplode;

    @Override
    public int getNetworkId() {
        return 85;
    }

    @Override
    public float getWidth() {
        return 0.45F;
    }

    @Override
    public float getHeight() {
        return 0.45F;
    }

    @Override
    public float getGravity() {
        return 0.00F;
    }

    @Override
    public void saveNBT() {}

    @Override
    public float getDrag() {
        return 0.01F;
    }

    public FireballEntity(FullChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    @Override
    protected double getDamage() {
        return 2;
    }

    public FireballEntity(FullChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        this(chunk, nbt, shootingEntity, false);
    }

    public FireballEntity(FullChunk chunk, CompoundTag nbt, Entity shootingEntity, boolean critical) {
        super(chunk, nbt, shootingEntity);
        this.canExplode = false;
        this.critical = critical;
    }

    public boolean isExplode() {
        return this.canExplode;
    }

    public void setExplode(boolean bool) {
        this.canExplode = bool;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        } else {
            if (!this.hadCollision && this.critical) {
                this.level.addParticle(new CriticalParticle(this.add((double)(this.getWidth() / 2.0F) + Utils.rand(-100.0D, 100.0D) / 500.0D, (double)(this.getHeight() / 2.0F) + Utils.rand(-100.0D, 100.0D) / 500.0D, (double)(this.getWidth() / 2.0F) + Utils.rand(-100.0D, 100.0D) / 500.0D)));
            } else if (this.onGround) {
                this.critical = false;
            }

            if (this.age > 1200 || this.isCollided) {
                if (this.isCollided && this.canExplode) {
                    this.explode();
                }

                this.close();
            }

            return super.onUpdate(currentTick);
        }
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (source instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)source).getDamager() instanceof Player) {
            this.setMotion(((EntityDamageByEntityEvent)source).getDamager().getLocation().getDirectionVector());
        }

        return true;
    }

    @Override
    public void explode() {
        List<Entity> nearbyEntities = List.of(this.level.getEntities());
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player && ((Player) entity).getGamemode() == Player.SURVIVAL && entity.distance(this.getLocation()) <= 7) {
                setMotionEntity(entity);
            }
        }

        Explosion explosion = new Explosion(this.getPosition(), 4, this);
        explosion.explodeA();
        explosion.explodeB();
    }

    @Override
    public void onCollideWithEntity(Entity entity) {
        super.onCollideWithEntity(entity);

        double explosionX = this.getX();
        double explosionY = this.getY() - 3.0;
        double explosionZ = this.getZ();
        Vector3 direction = entity.subtract(explosionX, explosionY, explosionZ).normalize();

        double knockbackX = direction.x * 4.0;
        double knockbackY = direction.y * 1.5;
        double knockbackZ = direction.z * 4.0;

        Vector3 knockbackVector = new Vector3(knockbackX, knockbackY, knockbackZ);
        entity.setMotion(knockbackVector);
    }

    public void setMotionEntity(Entity entity) {
        Vector3 direction = entity.getLocation().subtract(this.getLocation()).normalize();
        Vector3 knockbackVector = new Vector3(
                direction.x * 2.2,
                direction.y * 1.5 + 0.5,
                direction.z * 2.2
        );

        entity.setMotion(knockbackVector);
    }

}
