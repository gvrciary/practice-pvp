package alexis.practice.item.hotbar.staff;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TeleportRandomItem extends ItemCustom {

    public TeleportRandomItem() {
        super("&6Teleport Random", 345, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || !player.hasPermission("staffmode.permission") || !profile.getProfileData().inStaffMode()) return;

        List<Profile> players = new ArrayList<>(ProfileManager.getInstance().getProfiles().values().stream().filter(profiles -> profiles.isOnline() && (profiles.getProfileData().getDuel() != null || profiles.getProfileData().getArena() != null)).toList());

        if (players.isEmpty()) {
            player.sendMessage(TextFormat.colorize("&cThere are no players in fight"));
            return;
        }

        Collections.shuffle(players);
        Optional<Profile> findPlayer = players.stream().findAny();

        try {
            if (findPlayer.isEmpty()) return;

            Profile p = findPlayer.get();

            player.teleport(p.getPlayer().getPosition());
            player.sendMessage(TextFormat.colorize("&eTeleport to " + p.getName()));
        } catch (Exception ignored) {}
    }

}
