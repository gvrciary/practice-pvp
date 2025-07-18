package alexis.practice.item.hotbar.lobby;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.kit.setup.KitEditor;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.item.Item;

public class LayoutEditorItem extends ItemCustom {

    public LayoutEditorItem() {
        super("&6Layout Editor", Item.BOOK, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || !profile.getProfileData().isInLobby()) return;

        KitEditor.getInstance().sendKitForm(profile);
    }
}
