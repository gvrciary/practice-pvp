package alexis.practice.command;

import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.handler.StaffHandler;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public class ResponseCommand extends Command {

    public ResponseCommand() {
        super("response", "Use command to fast response");
        setAliases(new String[]{"r"});

        commandParameters.clear();
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (!(sender instanceof Player player)) return false;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return false;

        if (strings.length == 0) {
            player.sendMessage(TextFormat.colorize("&cUse /r <message>"));
            return false;
        }

        Profile target = profile.getCacheData().getLastMessage();

        if (target == null) {
            player.sendMessage(TextFormat.colorize("&cThe action could not be completed"));
            return false;
        }

        if (!target.isOnline()) {
            player.sendMessage(TextFormat.colorize("&cPlayer offline"));
            return false;
        }

        try {
            target.getPlayer().sendMessage(TextFormat.colorize("&6" + profile.getName() + " responded you >&f " + String.join(" ", strings)));
            player.sendMessage(TextFormat.colorize("&aYou have responded correctly to " + target.getName()));
            StaffHandler.getInstance().sendMessage("&r&7" + profile.getName() + " to " + target.getName() + ":&f " + String.join(" ", strings));
        } catch (Exception ignored) {}

        return true;
    }
}