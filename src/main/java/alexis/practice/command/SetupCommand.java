package alexis.practice.command;

import alexis.practice.arena.ArenaManager;
import alexis.practice.arena.world.ArenaWorldSetup;
import alexis.practice.duel.world.DuelWorldManager;
import alexis.practice.duel.world.DuelWorldSetup;
import alexis.practice.event.world.EventWorldManager;
import alexis.practice.event.world.EventWorldSetup;
import alexis.practice.kit.setup.KnockbackEditor;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.server.Mechanics;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

public final class SetupCommand extends Command {

    public SetupCommand() {
        super("setup", "Use command to setup duels/arenas/event/kit/mechanics");
        setPermission("setup.command");

        commandParameters.clear();
        commandParameters.put("duel", new CommandParameter[]{CommandParameter.newEnum("duel", new String[]{"duel"})});
        commandParameters.put("arena", new CommandParameter[]{CommandParameter.newEnum("arena", new String[]{"arena"})});
        commandParameters.put("event", new CommandParameter[]{CommandParameter.newEnum("event", new String[]{"event"})});
        commandParameters.put("kit", new CommandParameter[]{CommandParameter.newEnum("kit", new String[]{"kit"})});
        commandParameters.put("mechanics", new CommandParameter[]{CommandParameter.newEnum("mechanics", new String[]{"mechanics"})});
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (!(sender instanceof Player)) return false;

        if (strings.length < 1) {
            sender.sendMessage(TextFormat.colorize("&cUse /setup <duel/arena/event/kit/mechanics>."));
            return false;
        }

        Profile profile = ProfileManager.getInstance().get((Player) sender);

        if (profile == null) return false;

        switch (strings[0].toLowerCase()) {
            case "duel" -> {
                if (!profile.getProfileData().isInLobby() || profile.getProfileData().getQueue() != null) {
                    sender.sendMessage(TextFormat.colorize("&cYou cannot use this command now."));
                    return false;
                }

                if (strings.length == 1) {
                    DuelWorldSetup.getInstance().sendSetupForm(profile);
                    return true;
                }

                if (strings[1] != null && strings[1].equalsIgnoreCase("remove")) {
                    if (strings.length < 3) {
                        sender.sendMessage(TextFormat.colorize("&cUse /setup duel remove <worldName>"));
                        return false;
                    }

                    String worldName = strings[2];

                    if (DuelWorldManager.getInstance().getWorld(worldName) == null) {
                        sender.sendMessage(TextFormat.colorize("&cDuel world not found."));
                        return false;
                    }

                    DuelWorldManager.getInstance().removeWorld(worldName);
                    sender.sendMessage(TextFormat.colorize("&aDuel world has been successfully removed"));
                    return true;
                }
            }
            case "kit" -> KnockbackEditor.sendKitForm(profile);
            case "mechanics" -> Mechanics.getInstance().sendMechanicsForm((Player) sender);
            case "arena" -> {
                if (!profile.getProfileData().isInLobby() || profile.getProfileData().getQueue() != null) {
                    sender.sendMessage(TextFormat.colorize("&cYou cannot use this command now."));
                    return false;
                }

                if (strings.length == 1) {
                    ArenaWorldSetup.getInstance().sendSetupForm(profile);
                    return true;
                }

                if (strings[1] != null && strings[1].equalsIgnoreCase("remove")) {
                    if (strings.length < 3) {
                        sender.sendMessage(TextFormat.colorize("&cUse /setup arena remove <worldName>"));
                        return false;
                    }

                    String worldName = strings[2];

                    if (ArenaManager.getInstance().getArenaWorld(worldName) == null) {
                        sender.sendMessage(TextFormat.colorize("&cArena world not found."));
                        return false;
                    }

                    ArenaManager.getInstance().removeArena(worldName);
                    sender.sendMessage(TextFormat.colorize("&aArena world has been successfully removed"));
                    return true;
                }
            }
            case "event" -> {
                if (!profile.getProfileData().isInLobby() || profile.getProfileData().getQueue() != null) {
                    sender.sendMessage(TextFormat.colorize("&cYou cannot use this command now."));
                    return false;
                }

                if (strings.length == 1) {
                    EventWorldSetup.getInstance().sendSetupForm(profile);
                    return true;
                }

                if (strings[1] != null && strings[1].equalsIgnoreCase("remove")) {
                    if (strings.length < 3) {
                        sender.sendMessage(TextFormat.colorize("&cUse /setup event remove <worldName>"));
                        return false;
                    }

                    String worldName = strings[2];

                    if (EventWorldManager.getInstance().getWorld(worldName) == null) {
                        sender.sendMessage(TextFormat.colorize("&cEvent world not found."));
                        return false;
                    }

                    EventWorldManager.getInstance().removeWorld(worldName);
                    sender.sendMessage(TextFormat.colorize("&aEvent world has been successfully removed"));
                    return true;
                }
            }
            default -> sender.sendMessage(TextFormat.colorize("&cThe action does not exist"));
        }

        return true;
    }
}
