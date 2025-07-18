package alexis.practice.command;

import alexis.practice.duel.DuelState;
import alexis.practice.event.games.EventState;
import alexis.practice.party.games.PartyGameState;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

public class SpectateCommand extends Command {

    public SpectateCommand() {
        super("spectate", "Use command to spectate player");

        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{CommandParameter.newType("player", CommandParamType.TARGET)});
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (!(sender instanceof Player player)) return false;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return false;

        if (!profile.getProfileData().isInLobby() || profile.getProfileData().isInSetupMode()) {
            player.sendMessage(TextFormat.colorize("&cYou cannot use this command now"));
            return false;
        }

        if (strings.length == 0) {
            player.sendMessage(TextFormat.colorize("&cUse /spectate <player>"));
            return true;
        }

        Player targetPlayer = sender.getServer().getPlayer(strings[0]);

        if (targetPlayer == null) {
            sender.sendMessage(TextFormat.colorize("&cPlayer offline"));
            return false;
        }

        if (targetPlayer.equals(player)) {
            sender.sendMessage(TextFormat.colorize("&cYou can't see yourself"));
            return false;
        }

        Profile target = ProfileManager.getInstance().get(targetPlayer);

        if (target == null) {
            sender.sendMessage(TextFormat.colorize("&cPlayer not found"));
            return false;
        }

        if (target.getProfileData().isInLobby() || target.getProfileData().isInSetupMode()) {
            sender.sendMessage(TextFormat.colorize("&cThe player is not in an arena."));
            return false;
        }

        if (target.getProfileData().getDuel() != null && target.getProfileData().getDuel().getState().equals(DuelState.RUNNING) && !target.getProfileData().getDuel().isSpectator(target)) {
            target.getProfileData().getDuel().addSpectator(profile);
            player.sendMessage(TextFormat.colorize("&aYou are seeing " + target.getName() + " in a Duel"));
        } else if (target.getProfileData().getArena() != null) {
            target.getProfileData().getArena().addSpectator(profile);
            player.sendMessage(TextFormat.colorize("&aYou are seeing " + target.getName() + " in a Arena"));
        } else if (target.getProfileData().getEvent() != null && target.getProfileData().getEvent().getEventArena().getCurrentState().equals(EventState.RUNNING) && !target.getProfileData().getEvent().getEventArena().isSpectator(target)) {
            target.getProfileData().getEvent().getEventArena().addSpectator(profile);
            player.sendMessage(TextFormat.colorize("&aYou are seeing " + target.getName() + " in a Event"));
        } else if (target.getProfileData().getParty() != null && !target.getProfileData().getParty().isInLobby() && target.getProfileData().getParty().getDuel().getCurrentState().equals(PartyGameState.RUNNING) && !target.getProfileData().getParty().getDuel().isSpectator(target)) {
            target.getProfileData().getEvent().getEventArena().addSpectator(profile);
            player.sendMessage(TextFormat.colorize("&aYou are seeing " + target.getName() + " in a Party Duel"));
        } else {
            sender.sendMessage(TextFormat.colorize("&cThe player is not available."));
        }

        return true;
    }
}
