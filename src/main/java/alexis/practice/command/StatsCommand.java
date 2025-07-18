package alexis.practice.command;

import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

public class StatsCommand extends Command {

    public StatsCommand() {
        super("stats", "Use command to view stats");

        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{CommandParameter.newType("player", CommandParamType.TARGET)});
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (!(sender instanceof Player player)) return false;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return false;

        if (strings.length == 0) {
            player.addWindow(PlayerUtil.getStatsMenu(profile));
            return true;
        }

        Player targetPlayer = sender.getServer().getPlayer(strings[0]);

        if (targetPlayer == null) {
            sender.sendMessage(TextFormat.colorize("&cPlayer offline"));
            return false;
        }

        Profile target = ProfileManager.getInstance().get(targetPlayer);

        if (target == null) {
            sender.sendMessage(TextFormat.colorize("&cPlayer not found"));
            return false;
        }

        player.addWindow(PlayerUtil.getStatsMenu(target));
        return true;
    }
}
