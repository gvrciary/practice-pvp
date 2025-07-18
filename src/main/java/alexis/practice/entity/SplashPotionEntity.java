package alexis.practice.entity;

import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.profile.settings.SettingType;
import alexis.practice.profile.settings.types.PotionColor;
import alexis.practice.util.server.Mechanics;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityPotion;
import cn.nukkit.event.potion.PotionCollideEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.level.particle.SpellParticle;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import cn.nukkit.potion.Potion;

final public class SplashPotionEntity extends EntityPotion {

    public SplashPotionEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public SplashPotionEntity(FullChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
    }

    @Override
    protected float getDrag() {
        return Mechanics.getInstance().getSplashPotionMechanics().getDrag();
    }

    @Override
    protected float getGravity() {
        return Mechanics.getInstance().getSplashPotionMechanics().getGravity();
    }

    protected void splash(Entity collidedWith) {
        Potion potion = Potion.getPotion(this.potionId);
        PotionCollideEvent event = new PotionCollideEvent(potion, this);
        this.server.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            this.close();
            potion = event.getPotion();
            if (potion != null) {
                potion.setSplash(true);
                Effect effect = Potion.getEffect(potion.getId(), true);
                int r = 40;
                int g = 40;
                int b = 255;

                if (effect != null) {
                    if (shootingEntity instanceof Player player && player.hasPermission("settings.permission")) {
                        Profile profile = ProfileManager.getInstance().get(player);

                        if (profile != null) {
                            int[] potionColor = ((PotionColor) SettingType.POTION_COLOR.getSetting()).getColorRGB(profile).getRgb();

                            r = potionColor[0];
                            g = potionColor[1];
                            b = potionColor[2];
                        }
                    } else {
                        int[] colors = effect.getColor();
                        r = colors[0];
                        g = colors[1];
                        b = colors[2];
                    }
                }

                Particle particle = new SpellParticle(this, r, g, b);
                this.getLevel().addParticle(particle);
                this.getLevel().addLevelSoundEvent(this, 127);

                Entity[] entities = this.getLevel().getNearbyEntities(this.getBoundingBox().grow(1.95, 2.85, 1.95));

                for (Entity anEntity : entities) {
                    potion.applyPotion(anEntity, 1.0);
                }
            }
        }
    }
}
