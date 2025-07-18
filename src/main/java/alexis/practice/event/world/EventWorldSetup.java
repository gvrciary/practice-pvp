package alexis.practice.event.world;

import alexis.practice.event.games.EventType;
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
import com.denzelcode.form.window.CustomWindowForm;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EventWorldSetup {
    @Getter
    private static final EventWorldSetup instance = new EventWorldSetup();
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

        List<String> events = EventWorldManager.getInstance().getEventsWorlds().values().stream().map(EventWord::getName).toList();
        List<String> worlds = player.getServer().getLevels().values()
                .stream()
                .map(Level::getFolderName)
                .filter(world -> !events.contains(world) && !world.equals(player.getServer().getDefaultLevel().getFolderName()))
                .toList();

        if (worlds.isEmpty()) {
            player.sendMessage(TextFormat.colorize("&cNo worlds available"));
            return;
        }

        CustomWindowForm form = FormAPI.customWindowForm("event_setup", "Event Setup")
                .addHandler(h -> {
                    if (!h.isFormValid("event_setup")) return;

                    Dropdown eventWorld = h.getForm().getElement("event_world");
                    Dropdown eventType = h.getForm().getElement("event_type");

                    Level world = player.getServer().getLevelByName(worlds.get(eventWorld.getValue()));
                    EventType type = EventType.values()[eventType.getValue()];

                    if (type.equals(EventType.MEETUP)) {
                        player.sendMessage(TextFormat.colorize("&aEvent setup has finished"));
                        EventWorldManager.getInstance().createEventWorld(world.getFolderName(), null, null, null, type, null);
                        return;
                    }

                    create(profile, type, world);
                });

        List<String> eventsTypes = Stream.of(EventType.values()).map(EventType::getName).toList();

        form.addDropdown("event_world", "Event World", worlds);
        form.addDropdown("event_type", "Event Type", eventsTypes);

        form.sendTo(player);
    }

    public void create(Profile profile, EventType type, Level world) {
        setup.putIfAbsent(profile.getIdentifier(), new Setup(profile, type, world));
    }

    public void remove(Profile profile) {
        setup.remove(profile.getIdentifier());
    }

    @Getter
    public static final class Setup {
        @Setter
        private Vector3 lobbyPosition;

        @Setter
        private Vector3 firstPosition = null;
        @Setter
        private Vector3 secondPosition = null;

        private final Profile profile;
        private final EventType type;
        private final Level world;
        private final List<Vector3> eventArenaSpawns = new ArrayList<>();

        public Setup(Profile profile, EventType type, Level world) {
            this.profile = profile;
            this.type = type;
            this.world = world;

            init();
        }

        public void addArenaSpawn(Vector3 spawn) {
            eventArenaSpawns.add(spawn);
        }

        public void removeArenaSpawns() {
            eventArenaSpawns.clear();
        }

        public void init() {
            try {
                Player player = profile.getPlayer();
                profile.clear();
                player.setGamemode(Player.CREATIVE);
                Item selectLobby = Item.get(Item.EMERALD).setCustomName(TextFormat.colorize("&r&eSelect Lobby Spawn"));
                selectLobby.setNamedTag(selectLobby.getNamedTag().putString("setup_item", "select_lobby"));

                player.getInventory().setItem(0, selectLobby);

                if (type.equals(EventType.SKYWARS)) {
                    Item selectSpawns = Item.get(Item.STICK).setCustomName(TextFormat.colorize("&r&eSelect Spawns"));
                    selectSpawns.setNamedTag(selectSpawns.getNamedTag().putString("setup_item", "add_spawns"));

                    player.getInventory().setItem(1, selectSpawns);
                } else if (type.equals(EventType.SUMO)) {
                    Item firstPos = Block.get(Block.DIAMOND_BLOCK).toItem().setCustomName(TextFormat.colorize("&r&eSelect First Position"));
                    firstPos.setNamedTag(firstPos.getNamedTag().putString("setup_item", "first_pos"));

                    Item secondPos = Block.get(Block.GOLD_BLOCK).toItem().setCustomName(TextFormat.colorize("&r&eSelect Second Position"));
                    secondPos.setNamedTag(secondPos.getNamedTag().putString("setup_item", "second_pos"));

                    player.getInventory().setItem(2, firstPos);
                    player.getInventory().setItem(3, secondPos);
                }

                player.teleport(world.getSpawnLocation());
                player.setGamemode(Player.CREATIVE);
            } catch (Exception ignored) {}
        }

        public void save() {
            try {
                Player player = profile.getPlayer();

                if (EventWorldManager.getInstance().getWorld(world.getFolderName()) != null) {
                    player.sendMessage(TextFormat.colorize("&cWorld in Events already exists."));
                    return;
                }

                if (lobbyPosition == null) {
                    player.sendMessage(TextFormat.colorize("&cLobby position not defined."));
                    return;
                }

                if (type.equals(EventType.SKYWARS)) {
                    if (eventArenaSpawns.isEmpty()) {
                        player.sendMessage(TextFormat.colorize("&cNo spawns added."));
                        return;
                    }

                    if (eventArenaSpawns.size() < 6) {
                        player.sendMessage(TextFormat.colorize("&cThere must be at least 6 spawn."));
                        return;
                    }
                } else if (type.equals(EventType.SUMO)) {
                    if (firstPosition == null || secondPosition == null) {
                        player.sendMessage(TextFormat.colorize("&cFirst position or Second position not defined."));
                        return;
                    }
                }


                EventWorldManager.getInstance().createEventWorld(world.getFolderName(), lobbyPosition, firstPosition, secondPosition, type, eventArenaSpawns);
                destroy();
            } catch (Exception ignored) {}
        }

        public void destroy() {
            profile.clear();
            EventWorldSetup.getInstance().remove(profile);

            try {
                Player player = profile.getPlayer();

                player.sendMessage(TextFormat.colorize("&aYou have finished the event setup!"));
                PlayerUtil.getLobbyKit(player);
            } catch (Exception ignored) {}
        }
    }
}
