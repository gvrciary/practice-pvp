package alexis.practice.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

public final class PingCommand extends Command {

    public PingCommand() {
        super("ping", "Use command to check ping");

        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{CommandParameter.newType("player", CommandParamType.TARGET)});
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (strings.length == 0 && sender instanceof Player player) {
            sender.sendMessage(TextFormat.colorize("&7Your ping is &6" + player.getPing() + "ms"));
            return true;
        }

        Player player = sender.getServer().getPlayer(strings[0]);

        if (player == null) {
            sender.sendMessage(TextFormat.colorize("&cPlayer offline"));
            return false;
        }

        sender.sendMessage(TextFormat.colorize("&6" + player.getName() + "&7's ping is " + player.getPing() + "ms"));
        return true;
    }
}
