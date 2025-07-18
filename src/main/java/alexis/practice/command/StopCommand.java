package alexis.practice.command;

import alexis.practice.arena.ArenaManager;
import alexis.practice.arena.world.ArenaWorld;
import alexis.practice.duel.Duel;
import alexis.practice.duel.DuelManager;
import alexis.practice.event.Event;
import alexis.practice.event.EventManager;
import alexis.practice.party.games.PartyGame;
import alexis.practice.party.games.PartyGamesManager;
import alexis.practice.profile.ProfileManager;
import alexis.practice.storage.SQLStorage;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;

public class StopCommand extends Command {

    public StopCommand() {
        super("stop", "Use command to stop the server");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (sender instanceof Player) return false;

        sender.sendMessage("Stopping the server asynchronously");

        CompletableFuture<Void> arenaManager = CompletableFuture.runAsync(() -> ArenaManager.getInstance().getArenas().values().stream().filter(ArenaWorld::isCanBuild).forEach(arenaWorld -> arenaWorld.getArena().reset()));
        CompletableFuture<Void> duelManager = CompletableFuture.runAsync(() -> DuelManager.getInstance().getDuels().values().forEach(Duel::stop));
        CompletableFuture<Void> partyManager = CompletableFuture.runAsync(() -> PartyGamesManager.getInstance().getPartyGames().values().forEach(PartyGame::delete));
        CompletableFuture<Void> eventManager = CompletableFuture.runAsync(() -> EventManager.getInstance().getEvents().values().forEach(Event::stop));
        CompletableFuture<Void> sql = CompletableFuture.runAsync(() -> ProfileManager.getInstance().getProfiles().values().forEach(profile -> SQLStorage.getInstance().update(profile)));
        CompletableFuture<Void> shutdown = CompletableFuture.allOf(sql, duelManager, arenaManager, partyManager, eventManager);

        shutdown.thenRun(() -> sender.getServer().shutdown());
        return false;
    }

}
