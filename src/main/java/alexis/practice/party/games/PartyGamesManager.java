package alexis.practice.party.games;

import alexis.practice.Practice;
import alexis.practice.duel.world.DuelWorld;
import alexis.practice.duel.world.DuelWorldManager;
import alexis.practice.kit.Kit;
import alexis.practice.party.Party;
import alexis.practice.party.games.duel.PartyDuel;
import alexis.practice.party.games.duel.PartyDuelTypes;
import alexis.practice.party.games.event.ffa.PartyFFA;
import alexis.practice.party.games.event.ffa.PartyFFATypes;
import alexis.practice.party.games.event.split.PartySplit;
import alexis.practice.party.games.event.split.PartySplitTypes;
import cn.nukkit.level.Level;
import lombok.Getter;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public final class PartyGamesManager {

    @Getter
    private final static PartyGamesManager instance = new PartyGamesManager();
    @Getter
    private final ConcurrentHashMap<Integer, PartyGame> partyGames = new ConcurrentHashMap<>();

    PartyGamesManager() {
        Practice.getInstance().getServer().getScheduler().scheduleRepeatingTask(Practice.getInstance(), () -> partyGames.values().forEach(PartyGame::tick), 20);
    }

    public void createPartyDuel(Party firstParty, Party secondParty, Object type, DuelWorld worldData) {
        int id = 0;

        while (partyGames.containsKey(id) || new File(Practice.getInstance().getServer().getDataPath() + "worlds" + File.separator + "party-" + id).exists()) {
            id++;
        }

        Kit kit;

        if (type instanceof PartyDuelTypes) {
            kit = ((PartyDuelTypes) type).getType();
            if (worldData == null) worldData = DuelWorldManager.getInstance().getRandomWorld(((PartyDuelTypes) type).getName());
        } else if (type instanceof PartySplitTypes) {
            kit = ((PartySplitTypes) type).getType();
            if (worldData == null) worldData = DuelWorldManager.getInstance().getRandomWorld(((PartySplitTypes) type).getName());
        } else if (type instanceof PartyFFATypes) {
            kit = ((PartyFFATypes) type).getType();
            if (worldData == null) worldData = DuelWorldManager.getInstance().getRandomWorld(((PartyFFATypes) type).getName());
        } else {
            kit = null;
        }

        if (worldData == null || kit == null) {
            firstParty.broadcast("&cDuel not available");
            if (secondParty != null) {
                secondParty.broadcast("&cDuel not available");
            }
            return;
        }

        int finalId = id;
        DuelWorld finalWorldData = worldData;
        worldData.copyWorld("party-" + id, Practice.getInstance().getServer().getDataPath() + File.separator + "worlds" + File.separator, () -> {
            try {
                Level world = Practice.getInstance().getServer().getLevelByName("party-" + finalId);
                PartyGame duel;

                if (type instanceof PartyDuelTypes) {
                   duel = (PartyDuel) ((PartyDuelTypes) type).getClassDuel().getDeclaredConstructors()[0].newInstance(finalId, firstParty, secondParty, finalWorldData, world, kit);
                } else if (type instanceof PartySplitTypes){
                    duel = (PartySplit) ((PartySplitTypes) type).getClassDuel().getDeclaredConstructors()[0].newInstance(finalId, firstParty, finalWorldData, world, kit);
                } else {
                    duel = (PartyFFA) ((PartyFFATypes) type).getClassDuel().getDeclaredConstructors()[0].newInstance(finalId, firstParty, finalWorldData, world, kit);
                }

                partyGames.put(finalId, duel);
            } catch (Exception e) {
                System.out.println("Error when creating the party duel " + finalId);
                System.out.println(e.getMessage());
            }
        });
    }

    public void removeDuel(int id) {
        partyGames.remove(id);
    }

}
