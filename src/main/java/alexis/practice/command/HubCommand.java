package alexis.practice.command;

import alexis.practice.arena.Arena;
import alexis.practice.duel.Duel;
import alexis.practice.event.Event;
import alexis.practice.event.games.EventArena;
import alexis.practice.party.Party;
import alexis.practice.party.games.PartyGame;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public final class HubCommand extends Command {

    public HubCommand() {
        super("hub", "Use command to teleport to hub");
        setAliases(new String[]{"lobby", "spawn"});

        commandParameters.clear();
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (!(sender instanceof Player player)) return false;

        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return false;

        if (profile.getProfileData().isInLobby() || profile.getProfileData().isInSetupMode() || profile.getProfileData().inStaffMode()) {
            sender.sendMessage(TextFormat.colorize("&cYou cannot use this command now"));
            return false;
        }

        if (profile.getProfileData().getArena() != null) {
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

            profile.getProfileData().setArena();
            arena.removePlayer(profile);
        } else if (profile.getProfileData().getDuel() != null) {
            sender.sendMessage(TextFormat.colorize("&cYou cannot use this command now"));
            return false;
        } else if (profile.getProfileData().getParty() != null && !profile.getProfileData().getParty().isInLobby()) {
            Party party = profile.getProfileData().getParty();

            if (!party.getDuel().isSpectator(profile)) {
                sender.sendMessage(TextFormat.colorize("&cYou cannot use this command now"));
                return false;
            }

            party.removeMember(profile);
        } else if (profile.getProfileData().getEvent() != null && profile.getProfileData().getEvent().getEventArena() != null) {
            Event event = profile.getProfileData().getEvent();

            if (!event.getEventArena().isSpectator(profile)) {
                sender.sendMessage(TextFormat.colorize("&cYou cannot use this command now"));
                return false;
            }

            event.removePlayer(profile);
        }

        if (profile.getProfileData().isSpectator()) {
            Object spectate = profile.getProfileData().getSpectate();
            if (spectate instanceof Duel duel) {
                duel.removeSpectator(profile);
            } else if (spectate instanceof Arena arena) {
                arena.removeSpectator(profile);
            } else if (spectate instanceof PartyGame partyGame) {
                partyGame.removeSpectator(profile);
            } else if (spectate instanceof EventArena eventArena) {
                eventArena.removeSpectator(profile);
            }
        }

        profile.clear();
        PlayerUtil.getLobbyKit(player);
        player.sendMessage(TextFormat.colorize("&aTeleport to hub"));
        return true;
    }
}
