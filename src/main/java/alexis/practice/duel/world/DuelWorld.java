package alexis.practice.duel.world;

import alexis.practice.Practice;
import alexis.practice.util.world.CopyWorldAsync;
import cn.nukkit.Server;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import lombok.Getter;

import java.io.File;
import java.util.List;

@Getter
public final class DuelWorld {

    private final String name;
    private final Vector3 firstPosition;
    private final Vector3 secondPosition;
    private final List<String> duelType;
    private final Vector3 firstPortal;
    private final Vector3 secondPortal;

    private final boolean isCanDrop;

    private Portal portalFirst = null;
    private Portal portalSecond = null;

    public DuelWorld(String name, Vector3 firstPosition, Vector3 secondPosition, boolean isNew, List<String> duelType, Vector3 firstPortal, Vector3 secondPortal, boolean isCanDrop) {
        this.name = name;
        this.firstPosition = firstPosition;
        this.secondPosition = secondPosition;
        this.duelType = duelType;
        this.firstPortal = firstPortal;
        this.secondPortal = secondPortal;
        this.isCanDrop = isCanDrop;

        if (firstPortal != null && secondPortal != null) {
            portalFirst = new Portal(firstPortal.getX() - 4.0, firstPortal.getY() - 20.0, firstPortal.getZ() - 4.0, firstPortal.getX() + 4.0, firstPortal.getY() + 20.0, firstPortal.getZ() + 4.0);
            portalSecond = new Portal(secondPortal.getX() - 4.0, secondPortal.getY() - 20.0, secondPortal.getZ() - 4.0, secondPortal.getX() + 4.0, secondPortal.getY() + 20.0, secondPortal.getZ() + 4.0);
        }

        if (isNew) {
            Practice.getInstance().getServer().getScheduler().scheduleAsyncTask(Practice.getInstance(), new CopyWorldAsync(name, Server.getInstance().getDataPath() + File.separator + "worlds", name, Practice.getInstance().getDataFolder() + File.separator + "storage" + File.separator + "backups", null));
        }
    }

    public void copyWorld(String newName, String newDirectory, Runnable callback) {
        Practice.getInstance().getServer().getScheduler().scheduleAsyncTask(Practice.getInstance(), new CopyWorldAsync(name, Practice.getInstance().getDataFolder() + File.separator + "storage" + File.separator + "backups", newName, newDirectory, callback));
    }

    public boolean allowDuel(String type) {
        return duelType.contains(type);
    }

    public static final class Portal {
        private final double minX, minY, minZ, maxX, maxY, maxZ;

        public Portal(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public boolean contains(Position position) {
            double x = position.getX();
            double y = position.getY();
            double z = position.getZ();

            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }

        public boolean isWithinPortalRadius(Location pos) {
            final int radius = 10;

            double centerX = (minX + maxX) / 2.0;
            double centerZ = (minZ + maxZ) / 2.0;

            double distanceSquared = Math.pow(pos.getX() - centerX, 2) +
                    Math.pow(pos.getZ() - centerZ, 2);

            return distanceSquared <= Math.pow(radius, 2);
        }
    }
}
