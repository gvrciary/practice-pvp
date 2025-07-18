package alexis.practice.entity;

import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityExplosive;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Explosion;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.utils.TextFormat;

import java.util.List;

public class TntEntity extends EntityProjectile implements EntityExplosive {

    public static final int NETWORK_ID = 65;
    protected long fuse = 2 * 20;

    @Override
    public float getWidth() {
        return 0.98f;
    }

    @Override
    public float getLength() {
        return 0.98f;
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getHeight() {
        return 0.98f;
    }

    @Override
    public float getGravity() {
        return 0.08f;
    }

    @Override
    protected float getDrag() {
        return 0.02f;
    }

    @Override
    protected float getBaseOffset() {
        return 0.49f;
    }

    @Override
    public boolean canCollide() {
        return false;
    }

    public TntEntity(FullChunk fullChunk, CompoundTag compoundTag) {
        super(fullChunk, compoundTag);
        this.setNameTagVisible(true);
        this.setNameTagAlwaysVisible(true);
        this.setDataProperty(new IntEntityData(55, (int) fuse));

        resetNameTag();
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_IGNITED, true);
        this.setDataProperty(new IntEntityData(DATA_FUSE_LENGTH, (int) fuse));

        this.getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_FIZZ);
    }

    @Override
    public String getName() {
        return "TNT";
    }

    @Override
    public boolean onUpdate(int i) {
        int var2 = i - this.lastUpdate;
        resetNameTag();
        updateMovement();

        fuse -= var2;
        if (this.fuse % 5 == 0) {
            this.setDataProperty(new IntEntityData(55, (int) fuse));
        }

        if (fuse <= 0) {
            if (this.level.getGameRules().getBoolean(GameRule.TNT_EXPLODES)) {
                explode();
            }
            this.close();
        }

        return super.onUpdate(i);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        return source.getCause() == EntityDamageEvent.DamageCause.VOID && super.attack(source);
    }

    private void resetNameTag() {
        setNameTag(TextFormat.colorize((fuse > 20 ? "&a" : "&c")) + Utils.formatTime(fuse * 100));
    }

    @Override
    public void explode() {
        List<Entity> nearbyEntities = List.of(this.level.getEntities());
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player && ((Player) entity).getGamemode() == Player.SURVIVAL && entity.distance(this) <= 3) {
                setMotionEntity(entity);
            }
        }

        Explosion explosion = new Explosion(this.getLocation(), 4, this);
        explosion.explodeA();
        explosion.explodeB();
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
