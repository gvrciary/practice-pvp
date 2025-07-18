package alexis.practice.profile.cosmetics;

import alexis.practice.profile.cosmetics.object.ProjectileTrail;
import cn.nukkit.level.particle.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ProjectileTrails {
    SMOKE("Smoke", position -> position.getLevel().addParticle(new SmokeParticle(position))),
    HEARTS("Hearts", position -> position.getLevel().addParticle(new HeartParticle(position))),
    SPARKS("Sparks", position -> position.getLevel().addParticle(new RedstoneParticle(position))),
    FIRE("Fire", position -> position.getLevel().addParticle(new FlameParticle(position))),
    BUBBLE("Bubble", position -> position.getLevel().addParticle(new BubbleParticle(position))),
    CRITICAL("Critical", position -> position.getLevel().addParticle(new CriticalParticle(position))),
    MAGIC_CRITICAL("Magic Critical", position -> position.getLevel().addParticle(new EnchantmentTableParticle(position))),
    PORTAL("Portal", position -> position.getLevel().addParticle(new PortalParticle(position))),
    VILLAGER_HAPPY("Villager Happy", position -> position.getLevel().addParticle(new HappyVillagerParticle(position))),
    VILLAGER_ANGRY("Villager Angry", position -> position.getLevel().addParticle(new AngryVillagerParticle(position))),
    LAVA("Lava", position -> position.getLevel().addParticle(new LavaDripParticle(position)));

    private final String name;
    private final ProjectileTrail effect;

    public static ProjectileTrails get(String typeName) {
        return Arrays.stream(values())
                .filter(type -> type.toString().equals(typeName))
                .findFirst()
                .orElse(null);
    }

}
