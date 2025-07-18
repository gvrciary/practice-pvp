package alexis.practice.arena.world;

import alexis.practice.arena.Arena;
import alexis.practice.kit.Kit;
import alexis.practice.kit.KitManager;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import lombok.Getter;

import java.util.List;

@Getter
public final class ArenaWorld {
    private final String name;
    private final Level world;
    private final Kit kit;
    private final boolean canBuild;
    private final List<Vector3> spawns;
    private final Arena arena;

    public ArenaWorld(String name, String kitName, boolean canPlaceBlock, List<Vector3> spawns, Level world) {
        this.name = name;
        this.kit = KitManager.getInstance().getKit(kitName);
        this.canBuild = canPlaceBlock;
        this.spawns = spawns;
        this.world = world;

        this.arena = new Arena(this);
    }

}
