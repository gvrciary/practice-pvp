package alexis.practice.item.hotbar.party;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;

public class PartyLeaveItem extends ItemCustom {

    public PartyLeaveItem() {
        super("&cParty Leave", 331, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null || !profile.getProfileData().getParty().isInLobby()) return;

        profile.getProfileData().getParty().removeMember(profile);
    }

}
