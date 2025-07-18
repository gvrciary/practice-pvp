package alexis.practice.entity;

import alexis.practice.profile.Profile;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntitySlenderProjectile;
import cn.nukkit.event.entity.EntityCombustByEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;
import lombok.Getter;
import lombok.Setter;

public class ArrowEntity extends EntitySlenderProjectile {

    public static final int NETWORK_ID = 80;

    public static final int DATA_SOURCE_ID = 17;

    @Setter
    @Getter
    protected int pickupMode;
    public boolean isFromStray;
    @Getter
    @Setter
    protected boolean critical;

    private final Profile profile;

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.05f;
    }

    @Override
    public float getLength() {
        return 0.5f;
    }

    @Override
    public float getHeight() {
        return 0.05f;
    }

    @Override
    public float getGravity() {
        return 0.05f;
    }

    @Override
    public float getDrag() {
        return 0.01f;
    }

    public ArrowEntity(FullChunk chunk, CompoundTag nbt, Entity shootingEntity, boolean critical, Profile profile) {
        super(chunk, nbt, shootingEntity);
        this.setCritical(critical);

        this.profile = profile;
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.pickupMode = namedTag.contains("pickup") ? namedTag.getByte("pickup") : PICKUP_ANY;
    }

    public void setCritical() {
        this.setCritical(true);
    }

    @Override
    public int getResultDamage() {
        int base = super.getResultDamage();

        if (this.isCritical()) {
            base += Utils.random.nextInt((base >> 1) + 2);
        }

        return base;
    }

    @Override
    protected double getBaseDamage() {
        return 2;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) return false;

        if (this.age > 1200) {
            this.close();
            return false;
        }

        if (profile.isOnline() && profile.getCosmeticData().hasProjectileTrail()) profile.getCosmeticData().getProjectileTrail().getEffect().sendToPosition(this.getPosition());

        if (this.onGround || this.hadCollision) {
            this.setCritical(false);
        }

        if (this.fireTicks > 0 && this.level.isRaining() && this.canSeeSky()) {
            this.extinguish();
        }

        return super.onUpdate(currentTick);
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putByte("pickup", this.pickupMode);
    }

    @Override
    public void onHit() {
        this.getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_BOW_HIT);
        this.close();
    }

    @Override
    public void onCollideWithEntity(Entity entity) {
        if (!(shootingEntity instanceof Player player)) return;

        if (entity.equals(shootingEntity)) {
            if (entity.attack(new EntityDamageByEntityEvent(this, entity, EntityDamageEvent.DamageCause.PROJECTILE, 0))) {
                this.hadCollision = true;
                this.onHit();

                if (this.fireTicks > 0) {
                    EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(this, entity, 5);
                    this.server.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        entity.setOnFire(event.getDuration());
                    }
                }
            }

            Vector3 direction = shootingEntity.getDirectionVector().normalize();
            if (Math.abs(direction.getY()) < 0.5) {
                Vector3 knockbackVector = direction.multiply(2.0);
                shootingEntity.setMotion(knockbackVector);
            }
        } else {
            if (entity.attack(new EntityDamageByEntityEvent(shootingEntity, entity, EntityDamageEvent.DamageCause.PROJECTILE,  this.getResultDamage()))) {
                this.hadCollision = true;
                this.onHit();

                if (this.fireTicks > 0) {
                    EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(this, entity, 5);
                    this.server.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        entity.setOnFire(event.getDuration());
                    }
                }

                if (entity instanceof Player target) {
                    PlayerUtil.playSound(player, "random.orb");
                    player.sendMessage(TextFormat.colorize( "&c" + target.getName() + "&e is now at &c" + String.format("%.1f", ((target.getHealth() + target.getAbsorption())))));
                }
            }
        }

        this.close();

        if (this.isFromStray) {
            entity.addEffect(Effect.getEffect(Effect.SLOWNESS).setDuration(600));
        }
    }

}
