package alexis.practice.util;

import alexis.practice.profile.Profile;
import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;

public class CustomCooldown {
    private final String name;
    private final Profile profile;
    private final int duration;
    private long cooldown;

    public CustomCooldown(String name, Profile profile, int duration) {
        this.name = name;
        this.profile = profile;
        this.duration = duration;
        this.cooldown = -1;
    }

    public boolean isInCooldown() {
        return cooldown != -1;
    }

    public void set() {
        cooldown = System.currentTimeMillis() + duration * 1000L;
    }

    public void clear() {
        cooldown = -1;

        try {
            Player player = profile.getPlayer();

            player.setExperience(0, 0);
        } catch (Exception ignored) {}
    }

    public void tick() {
        if (!profile.isOnline() || !isInCooldown()) return;

        try {
            Player player = profile.getPlayer();

            long remainingTimeMillis = cooldown - System.currentTimeMillis();
            float remainingSeconds = (float) remainingTimeMillis / 1000;

            if (remainingSeconds <= 0.0) {
                player.sendMessage(TextFormat.colorize("&aYour " + name + " cooldown expired."));
                clear();
            } else PlayerUtil.setExperience(player, Math.round(remainingSeconds), remainingSeconds, duration);
        } catch (Exception ignored) {}
    }

}
