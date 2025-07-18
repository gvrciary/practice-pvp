package alexis.practice.profile.settings.types;

import alexis.practice.profile.Profile;
import alexis.practice.profile.settings.Setting;
import cn.nukkit.Player;
import cn.nukkit.network.protocol.AnimatePacket;

public class MoreCritical extends Setting {

    public MoreCritical(String name, boolean enabled) {
        super(name, enabled);
    }

    public MoreCritical(String name) {
        super(name);
    }

    @Override
    public void add(Profile profile, Player player) {
        if (!profile.isOnline()) return;

        try {
            AnimatePacket animate = new AnimatePacket();
            animate.action = AnimatePacket.Action.CRITICAL_HIT;
            animate.eid = player.getId();
            animate.protocol = profile.getPlayer().protocol;

            profile.getPlayer().getNetworkSession().sendPacket(animate);
        } catch (Exception ignored) {}

    }

}
