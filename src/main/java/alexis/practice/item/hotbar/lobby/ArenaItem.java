package alexis.practice.item.hotbar.lobby;

import alexis.practice.arena.ArenaManager;
import alexis.practice.arena.world.ArenaWorld;
import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.ImageType;
import com.denzelcode.form.window.SimpleWindowForm;

public class ArenaItem extends ItemCustom {

    public ArenaItem() {
        super("&6Arenas", Item.GOLDEN_AXE, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || !profile.getProfileData().isInLobby()) return;

        if (ArenaManager.getInstance().getArenas().isEmpty()) {
            player.sendMessage(TextFormat.colorize("&cNo arenas available"));
            return;
        }

        sendArenasForm(profile);
    }

    public void sendArenasForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("list_arenas", "Arenas")
                .addHandler(h -> {
                    if (!h.isFormValid("list_arenas")) {
                        return;
                    }

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        ArenaWorld arenaWorld = ArenaManager.getInstance().getArenaWorld(button.getName());

                        if (arenaWorld != null) {
                            arenaWorld.getArena().addPlayer(profile);
                        }
                    }
                });

        ArenaManager.getInstance().getArenas().values().forEach(arena -> form.addButton(arena.getName(), arena.getName() + "\n Players: " + arena.getArena().getPlayers().size(), ImageType.PATH, arena.getKit().getIcon()));

        form.sendTo(player);
    }
}
