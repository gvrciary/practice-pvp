package alexis.practice.command;

import alexis.practice.util.server.ServerEssential;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public final class GlobalMuteCommand extends Command {

    public GlobalMuteCommand() {
        super("globalmute", "Use command to enable/disable globalmute");
        setPermission("globalmute.command");

        commandParameters.clear();
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (!(sender instanceof Player player)) return false;

        ServerEssential serverEssential = ServerEssential.getInstance();
        serverEssential.setGlobalMute(!serverEssential.isGlobalMute());
        player.getServer().broadcastMessage(TextFormat.colorize(serverEssential.isGlobalMute() ? "&cGlobal chat has been muted" : "&aGlobal chat has been unmuted"));
        return true;
    }
}
