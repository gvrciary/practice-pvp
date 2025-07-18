package alexis.practice.command;

import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.handler.StaffHandler;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

public class StaffCommand extends Command {

    public StaffCommand() {
        super("staff", "Use command to staff");
        setPermission("staffmode.permission");

        commandParameters.clear();
        commandParameters.put("toggle", new CommandParameter[]{CommandParameter.newEnum("toggle", new String[]{"toggle"})});
        commandParameters.put("follow", new CommandParameter[]{CommandParameter.newEnum("follow", new String[]{"follow"}), CommandParameter.newType("player", CommandParamType.TARGET)});
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (!(sender instanceof Player player)) return false;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return false;

        if (strings.length == 0) {
            player.sendMessage(TextFormat.colorize("&cUse /staff <toggle/follow>"));
            return false;
        }

        StaffHandler staffHandler = StaffHandler.getInstance();

        switch (strings[0].toLowerCase()) {
            case "toggle" -> {
                if (!profile.getProfileData().isInLobby() && !profile.getProfileData().inStaffMode()) {
                    player.sendMessage(TextFormat.colorize("&c&cYou can only activate it in the lobby"));
                    return false;
                }

                if (staffHandler.get(profile) == null) {
                    staffHandler.add(profile);
                    player.sendMessage(TextFormat.colorize("&aYou have activated staff mode"));
                } else {
                    staffHandler.get(profile).stop();
                    player.sendMessage(TextFormat.colorize("&cYou have deactivated staff mode"));
                }
            }
            case "follow" -> {
                if (strings.length < 2) {
                    sender.sendMessage(TextFormat.colorize("&cUse /staff follow <player>"));
                    return false;
                }

                if (!profile.getProfileData().inStaffMode()) {
                    player.sendMessage(TextFormat.colorize("&cYou need to be in staff mode."));
                    return false;
                }

                Player targetPlayer = sender.getServer().getPlayer(strings[1]);

                if (targetPlayer == null) {
                    sender.sendMessage(TextFormat.colorize("&cPlayer offline"));
                    return false;
                }

                Profile target = ProfileManager.getInstance().get(targetPlayer);

                if (target == null) {
                    sender.sendMessage(TextFormat.colorize("&cPlayer not found"));
                    return false;
                }

                staffHandler.get(profile).setFollow(target);
                player.sendMessage(TextFormat.colorize("&aYou are now following " + target.getName()));
            }
        }
        return true;
    }
}
