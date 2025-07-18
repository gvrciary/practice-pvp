package alexis.practice.command;

import alexis.practice.arena.Arena;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public class RekitCommand extends Command {

    public RekitCommand() {
        super("rekit", "Use command to refill kit");

        commandParameters.clear();
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (!(sender instanceof Player player)) return false;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return false;

        if (profile.getProfileData().isInLobby() || profile.getProfileData().getArena() == null) {
            player.sendMessage(TextFormat.colorize("&cYou cannot use this command now"));
            return false;
        }

        Arena arena = profile.getProfileData().getArena();
        Profile lastHit = profile.getCacheData().getCombat().get();

        if (lastHit != null) {
            sender.sendMessage(TextFormat.colorize("&cYou have combat log"));
            return false;
        }

        if (profile.getCacheData().getEnderPearl().isInCooldown()) {
            sender.sendMessage(TextFormat.colorize("&cYou have Ender Pearl cooldown"));
            return false;
        }

        if (profile.getCacheData().getFireball().isInCooldown()) {
            sender.sendMessage(TextFormat.colorize("&cYou have Fireball cooldown"));
            return false;
        }

        arena.getKit().giveKit(profile);
        return true;
    }
}
