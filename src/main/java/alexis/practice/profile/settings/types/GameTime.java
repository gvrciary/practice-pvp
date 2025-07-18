package alexis.practice.profile.settings.types;

import alexis.practice.profile.Profile;
import alexis.practice.profile.settings.Setting;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.network.protocol.SetTimePacket;

import java.util.HashMap;
import java.util.Map;

public class GameTime extends Setting {

    private final Map<String, Integer> currentTime = new HashMap<>();

    public GameTime(String name) {
        super(name);
    }

    public GameTime(String name, boolean enabled) {
        super(name, enabled);
    }

    public void setCurrentTime(Profile profile, int time) {
        currentTime.put(profile.getIdentifier(), time);

        execute(profile, true);
    }

    public int getCurrentTime(Profile profile) {
        return currentTime.getOrDefault(profile.getIdentifier(), 0);
    }

    @Override
    public void execute(Profile profile, boolean value) {
        final SetTimePacket packet = new SetTimePacket();

        try {
            Player player = profile.getPlayer();

            packet.protocol = player.protocol;

            int currentTime = getCurrentTime(profile);

            if (currentTime == 0) packet.time = Level.TIME_DAY;
            else if (currentTime == 1) packet.time = Level.TIME_SUNSET;
            else packet.time = Level.TIME_MIDNIGHT;

            player.getNetworkSession().sendPacket(packet);
        } catch (Exception ignored) {}
    }

    @Override
    public void clearCache(Profile profile) {
        currentTime.remove(profile.getIdentifier());
    }

}
