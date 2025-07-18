package alexis.practice.command;

import alexis.practice.duel.world.DuelWorld;
import alexis.practice.duel.world.DuelWorldManager;
import alexis.practice.event.EventManager;
import alexis.practice.event.games.EventType;
import alexis.practice.kit.Kit;
import alexis.practice.kit.KitManager;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.Dropdown;
import com.denzelcode.form.element.ImageType;
import com.denzelcode.form.window.CustomWindowForm;

import java.util.ArrayList;
import java.util.List;

public class HostCommand extends Command {

    public HostCommand() {
        super("host", "Use command to host event");
        setPermission("event.permission");

        commandParameters.clear();
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (!(sender instanceof Player player)) return false;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return false;

        sendHostForm(profile);
        return true;
    }

    private void sendHostForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        FormAPI.simpleWindowForm("event.host", "Create Host")
                .addButton("event.sumo", "Sumo", ImageType.PATH, EventType.SUMO.getImage())
                .addButton("event.tournament", "Tournament", ImageType.PATH, EventType.TOURNAMENT.getImage())
                .addButton("event.meetup", "Meetup", ImageType.PATH, EventType.MEETUP.getImage())
                .addButton("event.skywars", "Skywars", ImageType.PATH, EventType.SKYWARS.getImage())
                .addHandler(h -> {
                    if (!h.isFormValid("event.host")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        switch (button.getName()) {
                            case "event.sumo" -> sendSumoForm(profile);
                            case "event.meetup" -> sendMeetupForm(profile);
                            case "event.skywars" -> sendSkyWarsForm(profile);
                            case "event.tournament" -> sendTournamentForm(profile);
                        }
                    }
                })
                .sendTo(player);
    }

    private void sendTournamentForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        List<String> kitsAvailable = KitManager.getInstance().getKits().values().stream()
                .filter(kit ->
                        kit.getName().equals("nodebuff") ||
                        kit.getName().equals("finaluhc") ||
                        kit.getName().equals("caveuhc") ||
                        kit.getName().equals("sumo") ||
                        kit.getName().equals("combo") ||
                        kit.getName().equals("hg") ||
                        kit.getName().equals("tntsumo") ||
                        kit.getName().equals("midfight") ||
                        kit.getName().equals("builduhc"))
                .map(Kit::getName)
                .toList();

        List<String> sizeAvailable = new ArrayList<>() { {
            add("1");
            add("2");
            add("3");
            add("4");
        }};

        CustomWindowForm form = FormAPI.customWindowForm("event.sumo", "Event Sumo")
                .addDropdown("event.type", "Kit", kitsAvailable)
                .addDropdown("event.team", "Team Size", sizeAvailable)
                .addHandler(h -> {
                    if (!h.isFormValid("event.sumo")) return;

                    if (EventManager.getInstance().checkEventType(EventType.TOURNAMENT)) {
                        player.sendMessage(TextFormat.colorize("&cYou can't host because there is already an active event"));
                        return;
                    }

                    Dropdown type = h.getForm().getElement("event.type");
                    Dropdown team = h.getForm().getElement("event.team");

                    Kit kit = KitManager.getInstance().getKit(kitsAvailable.get(type.getValue()));

                    if (kit == null) return;

                    DuelWorld duelWorld = DuelWorldManager.getInstance().getRandomWorld(kitsAvailable.get(type.getValue()));

                    if (duelWorld == null) {
                        player.sendMessage(TextFormat.colorize("&cEvent not available."));
                        return;
                    }

                    EventManager.getInstance().createEvent(profile, EventType.TOURNAMENT, kit, team.getValue() + 1);
                });

        form.sendTo(player);
    }

    private void sendSumoForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        List<String> kitsAvailable = KitManager.getInstance().getKits().values().stream()
                .filter(kit ->
                        kit.getName().equals("tntsumo") ||
                        kit.getName().equals("sumo"))
                .map(Kit::getName)
                .toList();

        List<String> sizeAvailable = new ArrayList<>() { {
            add("1");
            add("2");
            add("3");
            add("4");
        }};

        CustomWindowForm form = FormAPI.customWindowForm("event.sumo", "Event Sumo")
                .addDropdown("event.type", "Kit", kitsAvailable)
                .addDropdown("event.team", "Team Size", sizeAvailable)
                .addHandler(h -> {
                    if (!h.isFormValid("event.sumo")) return;

                    if (EventManager.getInstance().checkEventType(EventType.SUMO)) {
                        player.sendMessage(TextFormat.colorize("&cYou can't host because there is already an active event"));
                        return;
                    }

                    Dropdown type = h.getForm().getElement("event.type");
                    Dropdown team = h.getForm().getElement("event.team");

                    Kit kit = KitManager.getInstance().getKit(kitsAvailable.get(type.getValue()));

                    if (kit == null) return;

                    EventManager.getInstance().createEvent(profile, EventType.SUMO, kit, team.getValue() + 1);
                });

        form.sendTo(player);
    }

    private void sendSkyWarsForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        List<String> sizeAvailable = new ArrayList<>() { {
            add("1");
            add("2");
            add("3");
            add("4");
        }};

        CustomWindowForm form = FormAPI.customWindowForm("event.skywars", "Event Skywars")
                .addDropdown("event.team", "Team Size", sizeAvailable)
                .addHandler(h -> {
                    if (!h.isFormValid("event.skywars")) return;

                    if (EventManager.getInstance().checkEventType(EventType.SKYWARS)) {
                        player.sendMessage(TextFormat.colorize("&cYou can't host because there is already an active event"));
                        return;
                    }

                    Dropdown team = h.getForm().getElement("event.team");

                    Kit kit = KitManager.getInstance().getKit("bridge");

                    if (kit == null) return;

                    EventManager.getInstance().createEvent(profile, EventType.SKYWARS, kit, team.getValue() + 1);
                });

        form.sendTo(player);
    }

    private void sendMeetupForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        List<String> sizeAvailable = new ArrayList<>() { {
            add("1");
            add("2");
            add("3");
            add("4");
        }};

        CustomWindowForm form = FormAPI.customWindowForm("event.meetup", "Event Meetup")
                .addDropdown("event.team", "Team Size", sizeAvailable)
                .addHandler(h -> {
                    if (!h.isFormValid("event.meetup")) return;

                    if (EventManager.getInstance().checkEventType(EventType.MEETUP)) {
                        player.sendMessage(TextFormat.colorize("&cYou can't host because there is already an active event"));
                        return;
                    }

                    Dropdown team = h.getForm().getElement("event.team");

                    Kit kit = KitManager.getInstance().getKit("finaluhc");

                    if (kit == null) return;

                    EventManager.getInstance().createEvent(profile, EventType.MEETUP, kit, team.getValue() + 1);
                });

        form.sendTo(player);
    }

}
