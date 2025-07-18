package alexis.practice.command;

import alexis.practice.division.DivisionManager;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

public class DivisionCommand extends Command {

    public DivisionCommand() {
        super("division", "Use command to check divisions");

        commandParameters.clear();
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (sender instanceof Player player) {
            DivisionManager.getInstance().sendForm(player);
        }

        return true;
    }
}
