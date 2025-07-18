package alexis.practice.duel;

import alexis.practice.Practice;
import alexis.practice.duel.world.DuelWorld;
import alexis.practice.duel.world.DuelWorldManager;
import alexis.practice.kit.Kit;
import alexis.practice.profile.Profile;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.Dropdown;
import com.denzelcode.form.element.ImageType;
import com.denzelcode.form.element.Slider;
import com.denzelcode.form.window.CustomWindowForm;
import com.denzelcode.form.window.SimpleWindowForm;
import lombok.Getter;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DuelManager {

    @Getter
    private final static DuelManager instance = new DuelManager();
    @Getter
    private final ConcurrentHashMap<Integer, Duel> duels = new ConcurrentHashMap<>();

    DuelManager() {
        Practice.getInstance().getServer().getScheduler().scheduleRepeatingTask(Practice.getInstance(), () -> duels.values().forEach(Duel::tick), 20);
    }

    public void createDuel(Profile firstProfile, Profile secondProfile, boolean ranked, DuelType type, int limit, DuelWorld worldData, boolean isDuel) {
        int id = 0;
        Practice practice = Practice.getInstance();

        while (duels.containsKey(id) || new File(practice.getServer().getDataPath() + "worlds" + File.separator + "duel-" + id).exists()) {
            id++;
        }

        Kit kit = type.getType();

        if (worldData == null) {
            worldData = DuelWorldManager.getInstance().getRandomWorld(type.getName());

            if (worldData == null || kit == null) {
                resetProfile(firstProfile);
                resetProfile(secondProfile);
                return;
            }
        }

        int finalId = id;
        DuelWorld finalWorldData = worldData;
        worldData.copyWorld("duel-" + id, practice.getServer().getDataPath() + File.separator + "worlds" + File.separator, () -> {
            try {
                Level world = practice.getServer().getLevelByName("duel-" + finalId);

                Duel duel = type.getClassDuel().getDeclaredConstructor(
                        int.class,
                        Profile.class,
                        Profile.class,
                        Level.class,
                        DuelWorld.class,
                        Kit.class,
                        boolean.class,
                        int.class,
                        boolean.class
                ).newInstance(finalId, firstProfile, secondProfile, world, finalWorldData, kit, ranked, limit, isDuel);

                duels.put(finalId, duel);
            } catch (Exception e) {
                System.out.println("Error when creating the duel " + finalId + ", Type: " + type.getName());
                System.out.println(e.getMessage());

                resetProfile(firstProfile);
                resetProfile(secondProfile);
            }
        });
    }

    public void createDuel2vs2(List<Profile> profiles, DuelType type) {
        Practice practice = Practice.getInstance();
        Kit kit = type.getType();
        DuelWorld worldData = DuelWorldManager.getInstance().getRandomWorld(type.getName());

        if (profiles.size() != 4 || kit == null || worldData == null) {
            profiles.forEach(this::resetProfile);
            return;
        }

        int id = 0;

        while (duels.containsKey(id) || new File(practice.getServer().getDataPath() + "worlds" + File.separator + "duel-" + id).exists()) {
            id++;
        }

        int finalId = id;
        worldData.copyWorld("duel-" + id, practice.getServer().getDataPath() + File.separator + "worlds" + File.separator, () -> {
            try {
                Level world = practice.getServer().getLevelByName("duel-" + finalId);

                List<String> firstTeam = profiles.subList(0, 2).stream()
                        .map(Profile::getIdentifier)
                        .toList();

                List<String> secondTeam = profiles.subList(2, 4).stream()
                        .map(Profile::getIdentifier)
                        .toList();

                Duel2vs2 duel = new Duel2vs2(finalId, firstTeam, secondTeam, world, worldData, kit);
                duels.put(finalId, duel);
            } catch (Exception e) {
                System.out.println("Error when creating the duel 2vs2 " + finalId + ", Type: " + type.getName());
                System.out.println(e.getMessage());

                profiles.forEach(this::resetProfile);
            }
        });
    }

    private void resetProfile(Profile profile) {
        if (!profile.isOnline()) return;

        try {
            profile.clear();

            Player firstPlayer = profile.getPlayer();
            firstPlayer.sendMessage(TextFormat.colorize("&cDuel not available."));
            PlayerUtil.getLobbyKit(firstPlayer);
        } catch (Exception ignored) {}
    }

    public void sendDuelForm(Profile target, Profile profile) {
        Player player;
        try {
            player = target.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("duel_request", "Duel Request to " + profile.getName())
                .addHandler(h -> {
                    if (!h.isFormValid("duel_request")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        DuelType type = DuelType.get(button.getName());

                        if (type != null) {
                            List<DuelWorld> worlds = DuelWorldManager.getInstance().getWorldsByType(type.getName());

                            if (worlds.isEmpty()) {
                                player.sendMessage(TextFormat.colorize("&cNo duel available"));
                                return;
                            }

                            sendSetupDuelForm(target, profile, type);
                        }
                    }
                });

        Arrays.stream(DuelType.values()).toList().forEach(duelType -> form.addButton(duelType.getName(), duelType.getCustomName(), ImageType.PATH,duelType.getType().getIcon()));

        form.sendTo(player);
    }

    private void sendSetupDuelForm(Profile target, Profile profile, DuelType type) {
        Player player;
        try {
            player = target.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        List<String> worlds = DuelWorldManager.getInstance().getWorldsByType(type.getName()).stream()
                .map(DuelWorld::getName)
                .toList();

        CustomWindowForm form = FormAPI.customWindowForm("duel_request", TextFormat.colorize(("&6Duel Request to " + profile.getName())));
        boolean notValidRounds = !type.equals(DuelType.BRIDGE) && !type.equals(DuelType.BEDFIGHT) && !type.equals(DuelType.FIREBALL) && !type.equals(DuelType.BATTLERUSH);

        if (notValidRounds) {
            form.addSlider("round_option", "Rounds: ", 1, 7, 2, 1);
        }

        try {
            if (player.hasPermission("selector.permission")) {
                form.addDropdown("map_selector", "Map selector: ", worlds);
            } else if (!notValidRounds) {
                if (!profile.isOnline() || !profile.getProfileData().isInLobby() || profile.getProfileData().getQueue() != null) {
                    player.sendMessage(TextFormat.colorize("&cCould not complete the duel"));
                    return;
                }

                profile.addDuelRequest(target, type, 1, null);
                return;
            }
        } catch (Exception ignored) {}

        form.addHandler(h -> {
            if (!h.isFormValid("duel_request")) return;

            int rounds = 1;
            Slider roundOption = h.getForm().getElement("round_option");
            if (roundOption != null) {
                rounds = (int) roundOption.getValue();
            }

            DuelWorld world = null;
            Dropdown mapSelector = h.getForm().getElement("map_selector");

            if (mapSelector != null) {
                world = DuelWorldManager.getInstance().getWorld(mapSelector.getName());
            }

            if (!profile.isOnline() || !profile.getProfileData().isInLobby() || profile.getProfileData().getQueue() != null) {
                player.sendMessage(TextFormat.colorize("&cCould not complete the duel"));
                return;
            }

            profile.addDuelRequest(target, type, rounds, world);
        });

        form.sendTo(player);
    }

    public void removeDuel(int id) {
        duels.remove(id);
    }
}
