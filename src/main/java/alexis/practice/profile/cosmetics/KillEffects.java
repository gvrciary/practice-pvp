package alexis.practice.profile.cosmetics;

import alexis.practice.profile.cosmetics.object.KillEffect;
import alexis.practice.util.Fireworks;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.particle.DestroyBlockParticle;
import cn.nukkit.level.particle.ExplodeParticle;
import cn.nukkit.level.particle.HugeExplodeSeedParticle;
import cn.nukkit.network.protocol.AddEntityPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum KillEffects {
    BLOOD("Blood", (target, player) -> player.getLevel().addParticle(new DestroyBlockParticle(player.getPosition(), Block.get(Block.REDSTONE_BLOCK)))),
    EXPLODE("Explode", (target, player) -> player.getLevel().addParticle(new HugeExplodeSeedParticle(player.getPosition()))),
    FIRE_WORK("Firework",(target, player) -> Fireworks.spawnFirework(player)),
    LIGHTING("Lighting",(target, player) -> {
        AddEntityPacket light = new AddEntityPacket();
        light.type = 93;
        light.entityRuntimeId = Entity.entityCount++;
        light.yaw = (float) player.getYaw();
        light.pitch = (float) player.getPitch();
        light.x = player.getFloorX();
        light.y = player.getFloorY();
        light.z = player.getFloorZ();
        light.protocol = target.protocol;

        target.getNetworkSession().sendPacket(light);
    }),
    TNT_EXPLOSION("TNT Explosion",(target, player) -> {
        AddEntityPacket tnt = new AddEntityPacket();
        tnt.type = 65;
        tnt.entityRuntimeId = Entity.entityCount++;
        tnt.yaw = (float) player.getYaw();
        tnt.pitch = (float) player.getPitch();
        tnt.x = player.getFloorX();
        tnt.y = player.getFloorY();
        tnt.z = player.getFloorZ();
        tnt.protocol = target.protocol;

        target.getNetworkSession().sendPacket(tnt);

        for (int i = 0; i < 5; i++) {
            player.getLevel().addParticle(new ExplodeParticle(player.getPosition()));
        }
    });

    private final String name;
    private final KillEffect effect;

    public static KillEffects get(String typeName) {
        return Arrays.stream(values())
                .filter(type -> type.toString().equals(typeName))
                .findFirst()
                .orElse(null);
    }

}
