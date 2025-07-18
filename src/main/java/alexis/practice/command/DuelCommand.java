package alexis.practice.command;

import alexis.practice.duel.DuelManager;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.profile.cache.DuelRequest;
import alexis.practice.profile.settings.SettingType;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

public final class DuelCommand extends Command {

    public DuelCommand() {
        super("duel", "Use command to duel");

        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{CommandParameter.newType("player", CommandParamType.TARGET)});
        commandParameters.put("accept", new CommandParameter[]{CommandParameter.newEnum("accept", new String[]{"accept"}), CommandParameter.newType("player", CommandParamType.TARGET)});
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (strings.length == 0) {
            if (sender instanceof Player) {
                sender.sendMessage(TextFormat.colorize("&cUse /duel <name> or /duel accept <name>"));
            }
            return false;
        }

        Profile profile = ProfileManager.getInstance().get((Player) sender);

        if (profile == null || !profile.getProfileData().isInLobby() || profile.getProfileData().getQueue() != null) {
            sender.sendMessage(TextFormat.colorize("&cYou cannot use this command now"));
            return false;
        }

        if (strings[0].equalsIgnoreCase("accept")) {
            if (strings.length < 2) {
                sender.sendMessage(TextFormat.colorize("&cUse /duel accept [player]"));
                return false;
            }

            Player player = sender.getServer().getPlayer(strings[1]);

            if (player == null) {
                sender.sendMessage(TextFormat.colorize("&cPlayer offline"));
                return false;
            }

            Profile target = ProfileManager.getInstance().get(player);

            if (target == null) {
                sender.sendMessage(TextFormat.colorize("&cPlayer not found"));
                return false;
            }

            if (target.getIdentifier().equals(profile.getIdentifier())) return false;

            DuelRequest invitation = profile.getDuelRequest(target);

            if (invitation == null) {
                sender.sendMessage(TextFormat.colorize("&cYou don't have invitation from " + target.getName()));
                return false;
            }

            profile.removeDuelRequest(target);

            if (!invitation.isValid()) {
                sender.sendMessage(TextFormat.colorize("&ERROR"));
                return false;
            }

            DuelManager.getInstance().createDuel(profile, target, false, invitation.getType(), invitation.getRounds(), invitation.getWorldData(), true);
            return true;
        }

        Player player = sender.getServer().getPlayer(strings[0]);

        if (player == null) {
            sender.sendMessage(TextFormat.colorize("&cPlayer offline"));
            return false;
        }

        Profile target = ProfileManager.getInstance().get(player);

        if (target == null) {
            sender.sendMessage(TextFormat.colorize("&cPlayer not found"));
            return false;
        }

        if (target.getIdentifier().equals(profile.getIdentifier())) return false;

        if (target.getSettingsData().isEnabled(SettingType.NO_DUEL_INVITATIONS.toString())) {
            sender.sendMessage(TextFormat.colorize("&cThis player does not accept invitations."));
            return false;
        }

        DuelManager.getInstance().sendDuelForm(profile, target);
        return true;
    }
}
