package alexis.practice.command;

import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.profile.data.SettingsData;
import alexis.practice.profile.settings.SettingType;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public class DisguiseCommand extends Command {

    public DisguiseCommand() {
        super("disguise", "Use command To hide your name and skin during the game");
        setAliases(new String[]{"d"});
        setPermission("disguise.command");

        commandParameters.clear();
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (!(sender instanceof Player player)) return false;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return false;

        SettingsData settingsData = profile.getSettingsData();

        settingsData.toggleEnabled(SettingType.DISGUISE.toString());

        if (!settingsData.isEnabled(SettingType.DISGUISE.toString())) {
            player.sendMessage(TextFormat.colorize("&cYou have disabled the disguise"));
            return false;
        }

        player.sendMessage(TextFormat.colorize("&aYou have activated the disguise, your name is: " + profile.getName()));
        return true;
    }
}