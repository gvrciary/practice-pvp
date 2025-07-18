package alexis.practice.item.hotbar.party;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.item.ItemID;
import cn.nukkit.utils.TextFormat;

public class PartyInfotItem extends ItemCustom {

    public PartyInfotItem() {
        super("&6Party Info", ItemID.NETHER_STAR, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null || !profile.getProfileData().getParty().isInLobby()) return;

        player.sendMessage(TextFormat.colorize(profile.getProfileData().getParty().toString()));
    }

}
