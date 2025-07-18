package alexis.practice.profile.data;

import alexis.practice.arena.Arena;
import alexis.practice.arena.world.ArenaWorldSetup;
import alexis.practice.duel.Duel;
import alexis.practice.duel.world.DuelWorldSetup;
import alexis.practice.event.Event;
import alexis.practice.event.world.EventWorldSetup;
import alexis.practice.kit.setup.KitEditor;
import alexis.practice.party.Party;
import alexis.practice.profile.Profile;
import alexis.practice.queue.Queue;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.handler.StaffHandler;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
public class ProfileData {

    private final Profile profile;

    @Nullable
    private Duel duel = null;
    @Nullable
    private Arena arena = null;
    @Nullable
    private Party party = null;
    @Nullable
    private Event event = null;

    private Object spectate = null;

    @Nullable
    private Queue queue = null;

    public ProfileData(Profile profile) {
        this.profile = profile;
    }

    public boolean isInLobby() {
        return duel == null && arena == null && party == null && event == null && !isInSetupMode() && !isSpectator() && !inStaffMode();
    }

    public boolean isInSetupMode() {
        return DuelWorldSetup.getInstance().get(profile) != null || ArenaWorldSetup.getInstance().get(profile) != null || EventWorldSetup.getInstance().get(profile) != null || KitEditor.getInstance().get(profile) != null;
    }

    public boolean inStaffMode() {
        return StaffHandler.getInstance().get(profile) != null;
    }

    public boolean isSpectator() {
        return spectate != null;
    }

    public void setDuel() {
        duel = null;
    }

    public void setQueue() {
        queue = null;
    }

    public void setSpectate() {
        spectate = null;

        try {
            profile.clear();
            PlayerUtil.getLobbyKit(profile.getPlayer());
        } catch (Exception ignored) {}
    }

    public void setParty() {
        party = null;
    }

    public void setArena() {
        arena = null;
    }

    public void setEvent() {
        event = null;
    }

}
