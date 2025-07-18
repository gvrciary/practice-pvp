package alexis.practice.command;

import alexis.practice.duel.DuelType;
import alexis.practice.kit.Kit;
import alexis.practice.util.server.ServerEssential;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.ImageType;
import com.denzelcode.form.window.SimpleWindowForm;

import java.util.Arrays;

public class LeaderboardCommand extends Command {

    public LeaderboardCommand() {
        super("leaderboard", "Use command to check the leaderboards");
        setAliases(new String[]{"lb"});

        commandParameters.clear();
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (sender instanceof Player player) {
            sendLeaderboardForm(player);
        }

        return true;
    }

     private void sendLeaderboardForm(Player player) {
        SimpleWindowForm form = FormAPI.simpleWindowForm("types", "Leaderboards")
                .addHandler(h -> {
                    if (!h.isFormValid("types")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        DuelType type = DuelType.get(button.getName());

                        if (type == null) return;

                        SimpleWindowForm formLb = FormAPI.simpleWindowForm("leaderboard", "Leaderboard  " + type.getCustomName());
                        formLb.setContent(TextFormat.colorize(ServerEssential.getInstance().getLeaderboard(type.getName())));
                        formLb.sendTo(player);
                    }
                });

         Arrays.stream(DuelType.values()).forEach(type -> {
             Kit kit = type.getType();
             form.addButton(kit.getName(), kit.getCustomName() + "\nClick to view", ImageType.PATH, kit.getIcon());
         });

        form.sendTo(player);
    }

}
