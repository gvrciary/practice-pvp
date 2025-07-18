package alexis.practice.duel.world;

import alexis.practice.duel.DuelType;
import alexis.practice.kit.Kit;
import alexis.practice.kit.KitManager;
import alexis.practice.profile.Profile;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Dropdown;
import com.denzelcode.form.element.Toggle;
import com.denzelcode.form.window.CustomWindowForm;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuelWorldSetup {

    @Getter
    private final static DuelWorldSetup instance = new DuelWorldSetup();
    private final Map<String, Setup> setup = new HashMap<>();

    public Setup get(Profile profile) {
        return setup.get(profile.getIdentifier());
    }

    public void sendSetupForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        List<String> duels = DuelWorldManager.getInstance().getWorlds().values().stream().map(DuelWorld::getName).toList();
        List<String> worlds = player.getServer().getLevels().values()
                .stream()
                .map(Level::getFolderName)
                .filter(world ->  !duels.contains(world) && !world.equals(player.getServer().getDefaultLevel().getFolderName()))
                .toList();

        if (worlds.isEmpty()) {
            player.sendMessage(TextFormat.colorize("&cNo worlds available"));
            return;
        }

        CustomWindowForm form = FormAPI.customWindowForm("duel_setup", "Duel Setup")
                .addHandler(h -> {
                    if (!h.isFormValid("duel_setup")) return;

                    Dropdown duelWorld = h.getForm().getElement("duel_world");
                    Toggle duelPortal = h.getForm().getElement("duel_portal");
                    Toggle duelCanDrop = h.getForm().getElement("duel_canDrop");

                    Level world = player.getServer().getLevelByName(worlds.get(duelWorld.getValue()));
                    sendSetupMenu(profile, world, duelPortal.getValue(), duelCanDrop.getValue());
                });

        form.addDropdown("duel_world", "Duel World", worlds);
        form.addToggle("duel_portal", "Duel Portal", false);
        form.addToggle("duel_canDrop", "Duel isCanDrop", false);

        form.sendTo(player);
    }

    private void sendSetupMenu(Profile profile, Level world, boolean isNeedPortal, boolean isCanDrop) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        CustomWindowForm form = FormAPI.customWindowForm("duel_setup", "Duel Setup")
                .addHandler(h -> {
                    if (!h.isFormValid("duel_setup")) return;

                    final List<String> duelTypes = new ArrayList<>();

                    h.getForm().getElements().forEach(element -> {
                        if (element instanceof Toggle && ((Toggle) element).getValue()) {
                            duelTypes.add(((Toggle) element).getName());
                        }
                    });

                    if (duelTypes.isEmpty()) {
                        player.sendMessage(TextFormat.colorize("&cYou must select at least 1"));
                        return;
                    }

                    setup.putIfAbsent(profile.getIdentifier(), new Setup(profile, world, duelTypes, isNeedPortal, isCanDrop));
                });

        if (isNeedPortal) {
            Kit kit = KitManager.getInstance().getKit("bridge");
            form.addToggle(kit.getName(), kit.getCustomName(), false);
            Kit kit2 = KitManager.getInstance().getKit("battlerush");
            form.addToggle(kit2.getName(), kit2.getCustomName(), false);
        } else {
            KitManager.getInstance().getKits().values().stream().filter(type -> DuelType.get(type.getName()) != null).
                    forEach(type -> form.addToggle(type.getName(), type.getCustomName(), false));
        }

        form.sendTo(player);
    }

    public void remove(Profile profile) {
        setup.remove(profile.getIdentifier());
    }

    @Getter
    public static final class Setup {
        @Setter
        private Vector3 firstPosition = null;
        @Setter
        private Vector3 secondPosition = null;
        @Setter
        private Vector3 firstPortal = null;
        @Setter
        private Vector3 secondPortal = null;

        private final boolean needPortal;
        private final boolean isCanDrop;

        private final Profile profile;
        private final Level world;
        private final List<String> type;

        public Setup(Profile profile, Level world, List<String> type, boolean needPortal, boolean isCanDrop) {
            this.profile = profile;
            this.world = world;
            this.type = type;
            this.needPortal = needPortal;
            this.isCanDrop = isCanDrop;

            init();
        }

        public void init() {
            try {
                Player player = profile.getPlayer();
                profile.clear();

                player.teleport(world.getSpawnLocation());
                player.setGamemode(Player.CREATIVE);

                Item firstPos = Block.get(Block.DIAMOND_BLOCK).toItem().setCustomName(TextFormat.colorize("&r&eSelect First Position"));
                firstPos.setNamedTag(firstPos.getNamedTag().putString("setup_item", "first_pos"));

                Item secondPos = Block.get(Block.GOLD_BLOCK).toItem().setCustomName(TextFormat.colorize("&r&eSelect Second Position"));
                secondPos.setNamedTag(secondPos.getNamedTag().putString("setup_item", "second_pos"));

                player.getInventory().setItem(0, firstPos);
                player.getInventory().setItem(1, secondPos);

                if (needPortal) {
                    Item firstPortal = Block.get(Block.EMERALD_BLOCK).toItem().setCustomName(TextFormat.colorize("&r&9Select First Portal"));
                    firstPortal.setNamedTag(firstPortal.getNamedTag().putString("setup_item", "first_portal"));

                    Item secondPortal = Block.get(Block.REDSTONE_BLOCK).toItem().setCustomName(TextFormat.colorize("&r&9Select Second Portal"));
                    secondPortal.setNamedTag(secondPortal.getNamedTag().putString("setup_item", "second_portal"));

                    player.getInventory().setItem(7, firstPortal);
                    player.getInventory().setItem(8, secondPortal);
                }
            } catch (Exception ignored) {}
        }

        public void save() {
            try {
                Player player = profile.getPlayer();

                if (DuelWorldManager.getInstance().getWorld(world.getFolderName()) != null) {
                    player.sendMessage(TextFormat.colorize("&cWorld already exists."));
                    return;
                }

                if (firstPosition == null || secondPosition == null) {
                    player.sendMessage(TextFormat.colorize("&cFirst position or Second position not defined."));
                    return;
                }

                if (needPortal && (firstPortal == null || secondPortal == null)) {
                    player.sendMessage(TextFormat.colorize("&cFirst portal or Second portal not defined."));
                    return;
                }

                DuelWorldManager.getInstance().createWorld(world.getFolderName(), firstPosition, secondPosition, type, firstPortal, secondPortal, isCanDrop);
                destroy();
            } catch (Exception ignored) {}
        }

        public void destroy() {
            try {
                profile.clear();
                DuelWorldSetup.getInstance().remove(profile);

                profile.getPlayer().sendMessage(TextFormat.colorize("&aYou have finished the duel setup!"));
                PlayerUtil.getLobbyKit(profile.getPlayer());
            } catch (Exception ignored) {}
        }
    }
}
