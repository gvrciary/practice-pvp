package alexis.practice.entity;

import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.handler.HologramsHandler;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.Map;

public class HologramEntity extends Entity {

    private final HologramsHandler.HologramType hologramType;
    private final HologramsHandler hologramsHandler;

    public HologramEntity(FullChunk chunk, CompoundTag nbt, HologramsHandler.HologramType hologramType) {
        super(chunk, nbt);

        this.hologramType = hologramType;
        this.hologramsHandler = HologramsHandler.getInstance();

        if (hologramType.equals(HologramsHandler.HologramType.LOBBY)) setNameTag(hologramsHandler.getLobbyText());
        else if (hologramType.equals(HologramsHandler.HologramType.LEADERBOARD)) setNameTag(hologramsHandler.getLeaderboardText());
        else if (hologramType.equals(HologramsHandler.HologramType.PERSONAL_STATISTICS)) setNameTag(TextFormat.colorize("&aLoading Statistics.."));
    }

    @Override
    public String getName() {
        return "Hologram";
    }

    @Override
    public int getNetworkId() {
        return 64;
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        setNameTagVisible(true);
        setNameTagAlwaysVisible(true);
        setImmobile(true);
        getDataProperties().putLong(0, 65536L);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        this.getServer().getOnlinePlayers().values()
                .stream()
                .filter(player -> player.getLevel().equals(this.getLevel()) && !this.hasSpawned.containsKey(player.getLoaderId()))
                .forEach(this::spawnTo);

        for (Map.Entry<Integer, Player> entry : new ArrayList<>(this.hasSpawned.entrySet())) {
            Player player = entry.getValue();
            if (!player.isOnline() || !player.getLevel().equals(this.getLevel())) {
                this.despawnFrom(player);
                this.hasSpawned.remove(entry.getKey());
            }
        }

        if (hologramType.equals(HologramsHandler.HologramType.LEADERBOARD) && currentTick % 120 == 0) {
            setNameTag(hologramsHandler.getLeaderboardText());
        } else if (hologramType.equals(HologramsHandler.HologramType.PERSONAL_STATISTICS) && currentTick % 50 == 0) {
            EntityMetadata metadata = getDataProperties().clone();

            for (Map.Entry<Integer, Player> entry : new ArrayList<>(this.hasSpawned.entrySet())) {
                Player player = entry.getValue();
                Profile profile = ProfileManager.getInstance().get(player);

                if (profile != null) {
                    metadata.put(new StringEntityData(4, hologramsHandler.getStatisticText(profile)));
                    this.sendData(new Player[]{player}, metadata);
                }
            }
        }

        return super.onUpdate(currentTick);
    }

}
