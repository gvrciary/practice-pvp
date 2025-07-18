package alexis.practice.arena.world;

import alexis.practice.arena.ArenaManager;
import alexis.practice.kit.Kit;
import alexis.practice.kit.KitManager;
import alexis.practice.profile.Profile;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Dropdown;
import com.denzelcode.form.element.Input;
import com.denzelcode.form.element.Toggle;
import com.denzelcode.form.window.CustomWindowForm;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArenaWorldSetup {

    @Getter
    private final static ArenaWorldSetup instance = new ArenaWorldSetup();

    private final Map<String, ArenaWorldSetup.Setup> setup = new HashMap<>();

    public ArenaWorldSetup.Setup get(Profile profile) {
        return setup.get(profile.getIdentifier());
    }

    public void sendSetupForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        List<String> arenas = ArenaManager.getInstance().getArenas().values().stream().map(map -> map.getWorld().getFolderName()).toList();
        List<String> worlds = player.getServer().getLevels().values()
                .stream()
                .map(Level::getFolderName)
                .filter(world ->  !arenas.contains(world) && !world.equals(player.getServer().getDefaultLevel().getFolderName()))
                .toList();

        List<String> kitsAvailable = KitManager.getInstance().getKits().values().stream()
                .filter(kit ->
                            kit.getName().equals("nodebuff") ||
                            kit.getName().equals("sumo") ||
                            kit.getName().equals("combo") ||
                            kit.getName().equals("tntsumo") ||
                            kit.getName().equals("midfight") ||
                            kit.getName().equals("build-ffa") ||
                            kit.getName().equals("skywars-ffa") ||
                            kit.getName().equals("builduhc"))
                .map(Kit::getName)
                .toList();

        if (worlds.isEmpty()) {
            player.sendMessage(TextFormat.colorize("&cNo worlds available"));
            return;
        }

        CustomWindowForm form = FormAPI.customWindowForm("arena_setup", "Arena Setup")
                .addHandler(h -> {
                    if (!h.isFormValid("arena_setup")) return;

                    Input arenaName = h.getForm().getElement("arena_name");
                    Dropdown arenaKit = h.getForm().getElement("arena_kit");
                    Dropdown arenaWorld = h.getForm().getElement("arena_world");
                    Toggle arenaBuild = h.getForm().getElement("arena_build");

                    if (arenaName.getValue().isBlank()) {
                        player.sendMessage(TextFormat.colorize("&cYou need to enter a name"));
                        return;
                    }

                    Level world = player.getServer().getLevelByName(worlds.get(arenaWorld.getValue()));


                    setup.putIfAbsent(profile.getIdentifier(), new Setup(profile, arenaName.getValue(), world, kitsAvailable.get(arenaKit.getValue()), arenaBuild.getValue()));
                });

        form.addInput("arena_name", "Arena Name", "Arena");
        form.addDropdown("arena_kit", "Arena Kit", kitsAvailable);
        form.addDropdown("arena_world", "Arena World", worlds);
        form.addToggle("arena_build", "Arena Build", false);

        form.sendTo(player);
    }

    public void remove(Profile profile) {
        setup.remove(profile.getIdentifier());
    }

    @Getter
    public static final class Setup {
        private final Profile profile;
        private final String arenaName;
        private final Level arenaWorld;
        private final String arenaKit;
        private final boolean canBuild;

        private final List<Vector3> arenaSpawns = new ArrayList<>();

        public Setup(Profile profile, String arenaName, Level arenaWorld, String arenaKit, boolean canBuild) {
            this.profile = profile;
            this.arenaName = arenaName;
            this.arenaWorld = arenaWorld;
            this.arenaKit = arenaKit;
            this.canBuild = canBuild;

            init();
        }

        public void addArenaSpawn(Vector3 spawn) {
            arenaSpawns.add(spawn);
        }

        public void removeArenaSpawns() {
            arenaSpawns.clear();
        }

        public void init() {
            try {
                Player player = profile.getPlayer();
                profile.clear();
                player.setGamemode(Player.CREATIVE);

                Item selectSpawns = Item.get(Item.STICK).setCustomName(TextFormat.colorize("&r&eSelect Spawns"));
                selectSpawns.setNamedTag(selectSpawns.getNamedTag().putString("setup_item", "add_spawns"));

                player.getInventory().setItem(0, selectSpawns);

                player.teleport(arenaWorld.getSpawnLocation());
                player.setGamemode(Player.CREATIVE);
            } catch (Exception ignored) {}
        }

        public void save() {
            try {
                Player player = profile.getPlayer();

                if (arenaSpawns.isEmpty()) {
                    player.sendMessage(TextFormat.colorize("&cNo spawns added."));
                    return;
                }

                if (ArenaManager.getInstance().getArenaWorld(arenaName) != null) {
                    player.sendMessage(TextFormat.colorize("&cArena already exists."));
                    destroy();
                    return;
                }

                ArenaManager.getInstance().createArena(arenaName, arenaKit, canBuild, arenaSpawns, arenaWorld);
                player.sendMessage(TextFormat.colorize("&aYou have created the " + arenaName + " arena."));

                destroy();
            } catch (Exception ignored) {}
        }

        public void destroy() {
            profile.clear();
            ArenaWorldSetup.getInstance().remove(profile);

            try {
                profile.getPlayer().sendMessage(TextFormat.colorize("&aYou have finished the arena setup!"));
                PlayerUtil.getLobbyKit(profile.getPlayer());
            } catch (Exception ignored) {}
        }

    }
}
