package alexis.practice.event.games.types.meetup.border;

import alexis.practice.Practice;
import alexis.practice.event.games.types.meetup.Meetup;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.TaskHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Border {

    @Getter @Setter
    private int size = 125;

    private final Level world;
    private final Meetup eventArena;

    private final List<TaskHandler> tasks = new ArrayList<>();

    public Border(Meetup eventArena, Level world){
        this.world = world;
        this.eventArena = eventArena;
    }

    public void destroy() {
        tasks.forEach(TaskHandler::cancel);
    }

    public void shrink() {
        if (size == 25) {
            createArena();
        }

        teleportPlayers();
        createBorder();
    }

    public void teleportPlayers() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> eventArena.getEvent().getPlayers().forEach(profile -> {
            try {
                Player player = profile.getPlayer();

                if (!insideBorder(player.getPosition())) {
                    teleportInside(player, false);
                }
            } catch (Exception ignored) {}
        }));

        executor.shutdown();
    }

    public void createBorder() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            createWall(-size, size, size, size);
            createWall(-size, size, -size, -size);
            createWall(-size, -size, -size, size);
            createWall(size, size, -size, size);
        });

        executor.shutdown();
    }

    public void createArena() {
        int size = getSize();
        loadChunks(world, -size, size, -size, size);

        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                setBlock(world, x, 100, z, Block.get(Block.BEDROCK));
                setBlock(world, x, 101, z, Block.get(Block.GRASS));
            }
        }
    }

    private void createWall(int firstX, int secondX, int firstZ, int secondZ) {
        tasks.add(Practice.getInstance().getServer().getScheduler().scheduleDelayedTask(Practice.getInstance(), () -> createLayer(firstX, secondX, firstZ, secondZ), 5, true));
    }

    private void createLayer(int firstX, int secondX, int firstZ, int secondZ) {
        int minX = Math.min(firstX, secondX);
        int minZ = Math.min(firstZ, secondZ);

        int maxX = Math.max(firstX, secondX);
        int maxZ = Math.max(firstZ, secondZ);

        loadChunks(world, minX, maxX, minZ, maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int y = world.getHighestBlockAt(x, z);

                if (y >= 0) {
                    if (getSize() == 25) {
                        y = 102;
                    }

                    for (int i = 0; i < 4; i++) {
                        setBlock(world, x, y + i, z, Block.get(Block.BEDROCK));
                    }
                }
            }
        }
    }

    public void teleportInside(Player living, boolean highestCache) {
        Level world = living.getLevel();
        Position pos = living.getPosition().clone();

        boolean outsideX = (living.getFloorX() < size ? living.getFloorX() <= -size : living.getFloorX() >= size);
        boolean outsideZ = (living.getFloorZ() < size ? living.getFloorZ() <= -size : living.getFloorZ() >= size);

        pos.level = world;
        pos.x = outsideX ? (pos.getFloorX() < 0 ? -size + 1.6 : size - 1.6) : pos.x;
        pos.z = outsideZ ? (pos.getFloorZ() < 0 ? -size + 1.6 : size - 1.6) : pos.z;

        if (size != 25) {
            pos.y = pos.getLevel().getHighestBlockAt((int) pos.x, (int) pos.z, highestCache);
        } else {
            pos.y = 103;
        }

        PlayerUtil.playSound(living, "random.explode");
        living.teleport(pos);
    }

    public boolean insideBorder(Position position) {
        return position.getFloorX() <= size && position.getFloorX() >= -size && position.getFloorZ() <= size && position.getFloorZ() >= -size;
    }

    private void setBlock(Level level, int x, int y, int z, Block block) {
        level.setBlock(new Vector3(x, y, z), block);
    }

    private void loadChunks(Level level, int minX, int maxX, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (level.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                level.loadChunk(x >> 4, z >> 4);
            }
        }
    }

}
