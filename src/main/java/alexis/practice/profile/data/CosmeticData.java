package alexis.practice.profile.data;

import alexis.practice.profile.Profile;
import alexis.practice.profile.cosmetics.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class CosmeticData {

    private final Profile profile;

    private KillEffects killEffect = null;
    private ProjectileTrails projectileTrail = null;

    private KillMessages killMessage = null;
    private JoinMessages joinMessages = null;
    private ColorChat colorChat = null;

    public CosmeticData(Profile profile) {
        this.profile = profile;
    }

    public boolean hasKillEffect() {
        return killEffect != null;
    }

    public boolean hasColorChat() {
        return colorChat != null;
    }

    public boolean hasProjectileTrail() {
        return projectileTrail != null;
    }

    public boolean hasJoinMessages() {
        return joinMessages != null;
    }

    public boolean hasKillMessage() {
        return killMessage != null;
    }

    public void clearKillEffect() {
        killEffect = null;
    }

    public void clearProjectileTrail() {
        projectileTrail = null;
    }

    public void clearColorChat() {
        colorChat = null;
    }

    public void clearJoinMessages() {
        joinMessages = null;
    }

    public void clearKillMessage() {
        killMessage = null;
    }

}
