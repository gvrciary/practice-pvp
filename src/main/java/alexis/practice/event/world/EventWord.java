package alexis.practice.event.world;

import alexis.practice.Practice;
import alexis.practice.event.games.EventType;
import alexis.practice.util.world.CopyWorldAsync;
import cn.nukkit.Server;
import cn.nukkit.math.Vector3;
import lombok.Getter;

import java.io.File;
import java.util.List;

@Getter
public class EventWord {
    private final String name;
    private final Vector3 lobbyPosition;
    private final Vector3 firstPosition;
    private final Vector3 secondPosition;
    private final EventType eventType;

    private final List<Vector3> spawns;

    public EventWord(String name, Vector3 lobbyPosition, Vector3 firstPosition, Vector3 secondPosition, boolean isNew, EventType eventType, List<Vector3> spawns) {
        this.name = name;
        this.lobbyPosition = lobbyPosition;
        this.firstPosition = firstPosition;
        this.secondPosition = secondPosition;
        this.eventType = eventType;
        this.spawns = spawns;

        if (isNew) {
            Practice.getInstance().getServer().getScheduler().scheduleAsyncTask(Practice.getInstance(), new CopyWorldAsync(name, Server.getInstance().getDataPath() + File.separator + "worlds", name, Practice.getInstance().getDataFolder() + File.separator + "storage" + File.separator + "backups", null));
        }
    }

    public void copyWorld(String newName, String newDirectory, Runnable callback) {
        Practice.getInstance().getServer().getScheduler().scheduleAsyncTask(Practice.getInstance(), new CopyWorldAsync(name, Practice.getInstance().getDataFolder() + File.separator + "storage" + File.separator + "backups", newName, newDirectory, callback));
    }

}
