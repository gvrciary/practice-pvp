package alexis.practice.profile.data;

import alexis.practice.arena.Arena;
import alexis.practice.profile.Profile;
import alexis.practice.profile.cache.Combat;
import alexis.practice.profile.settings.SettingType;
import alexis.practice.util.CustomCooldown;
import alexis.practice.util.PlayerUtil;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
public class CacheData {
    private final Profile profile;
    private final Combat combat;
    private final CustomCooldown enderPearl;
    private final CustomCooldown fireball;

    @Nullable @Setter
    private Profile lastMessage = null;
    @Setter
    private long chatCooldown = 0;
    @Setter
    private long cooldownEvent = 0;
    @Setter
    private int cooldownFFA = 0;

    public CacheData(Profile profile) {
        this.profile = profile;

        combat = new Combat();
        enderPearl = new CustomCooldown("EnderPearl", profile, 15);
        fireball =  new CustomCooldown("Fireball", profile, 1);
    }

    public void update() {
        if (cooldownFFA > 0) {
            if (cooldownFFA == 1) {
                Arena arena = profile.getProfileData().getArena();

                if (profile.getSettingsData().isEnabled(SettingType.AUTO_RESPAWN.toString()) && arena != null) {
                    arena.addPlayer(profile);
                } else if (arena != null) {
                    profile.clear();
                    arena.removeDataPlayer(profile);

                    try {
                        PlayerUtil.getLobbyKit(profile.getPlayer());
                    } catch (Exception ignored) {}
                }
            }

            cooldownFFA--;
        }

        enderPearl.tick();
        fireball.tick();
    }

    public void clearCooldown() {
        cooldownFFA = 0;
        chatCooldown = 0;

        enderPearl.clear();
        fireball.clear();
    }

}
