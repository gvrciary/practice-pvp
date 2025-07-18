package alexis.practice.item.hotbar.duel;

import alexis.practice.duel.Duel;
import alexis.practice.duel.DuelState;
import alexis.practice.duel.DuelType;
import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.queue.QueueManager;
import cn.nukkit.Player;
import cn.nukkit.item.ItemID;

public class RequestMatchItem extends ItemCustom {

    public RequestMatchItem() {
        super("&6Play Again", ItemID.PAPER, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().isInLobby() || profile.getProfileData().getDuel() == null) return;

        Duel duel = profile.getProfileData().getDuel();

        if (duel.isDuel()) return;

        if (!duel.getState().equals(DuelState.ENDING)) return;

        duel.setRematch(profile);
        profile.getProfileData().setDuel();
        player.teleport(player.getServer().getDefaultLevel().getSpawnLocation());
        DuelType type = DuelType.get(duel.getKit().getName());

        QueueManager.getInstance().createQueue(profile, duel.isRanked(), type);
    }

}
