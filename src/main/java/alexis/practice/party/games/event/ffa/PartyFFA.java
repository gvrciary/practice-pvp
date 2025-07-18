package alexis.practice.party.games.event.ffa;

import alexis.practice.duel.world.DuelWorld;
import alexis.practice.kit.Kit;
import alexis.practice.party.Party;
import alexis.practice.party.games.PartyGame;
import alexis.practice.party.games.PartyGameState;
import alexis.practice.profile.Profile;
import alexis.practice.util.Fireworks;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.level.Level;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PartyFFA extends PartyGame {

    protected final Map<String, Integer> kills = new HashMap<>();
    protected final Party party;

    public PartyFFA(int id, Party party, DuelWorld worldData, Level level, Kit kit) {
        this.id = id;
        this.kit = kit;
        this.world = level;
        this.party = party;
        this.worldData = worldData;

        party.setDuel(this);

        setup();
    }

    public void setup() {
        super.setup();

        AtomicInteger counter = new AtomicInteger(0);
        party.getMembers().forEach(member -> {
            try {
                Player player = member.getPlayer();

                if (counter.get() % 2 == 0) counter.set(0);

                member.clear();
                player.setGamemode(Player.SURVIVAL);
                player.setImmobile();
                kit.giveKit(member);

                if (counter.get() == 1) {
                    player.teleport(world.getBlock(worldData.getFirstPosition()).getLocation());
                } else {
                    player.teleport(world.getBlock(worldData.getSecondPosition()).getLocation());
                }

                counter.getAndIncrement();
            } catch (Exception ignored) {}

        });
    }

    public int getAlives() {
        return (int) party.getMembers().stream().filter(member -> !isSpectator(member)).count();
    }

    public void start() {
        if (party.getMembers().isEmpty() || getAlives()  <= 1) {
            stop();
            return;
        }

        currentState = PartyGameState.RUNNING;

        party.getMembers().forEach(member -> {
            try {
                Player player = member.getPlayer();

                kills.put(member.getIdentifier(), 0);

                player.setImmobile(false);
                player.getLevel().getPlayers().values().forEach(player::showPlayer);
            } catch (Exception ignored) {}

        });

        broadcast("&aMatch started.");
    }

    public void checkWinner() {
        if (party.getMembers().isEmpty() || getAlives() <= 1) {
            stop();
        }
    }

    public int getKills(Profile profile) {
        return kills.getOrDefault(profile.getIdentifier(), 0);
    }

    public void increaseKills(Profile profile) {
        kills.put(profile.getIdentifier(), getKills(profile) + 1);
    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();

        switch (currentState) {
            case STARTING -> lines.add("&l&6|&r&f Match Starting in: &6" + startTime);
            case RUNNING -> {
                if (isSpectator(profile) && profile.getProfileData().isSpectator()) {
                    lines.add("&l&6|&r&f Duration:&6 " + Utils.formatTime(runTime));
                    lines.add("&l&6|&r&f Players:&6 " +  getAlives() + " &7(" + party.getMembers().size() + ")");
                    return lines;
                }

                lines.add("&l&6|&r&f Duration:&6 " + Utils.formatTime(runTime));
                lines.add("&l&6|&r&f Kills:&6 " +  getKills(profile));
                lines.add("&l&6|&r&f Players:&6" +  getAlives() + "&7 (" + party.getMembers().size() + ")" );
            }
            case ENDING -> {
                if (!isSpectator(profile)) {
                    lines.add("&l&6|&a VICTORY");
                    lines.add("&6&r");
                }

                lines.add("&l&6|&r&c Match ended");
            }
        }
        return lines;
    }

    public void tick() {
        super.tick();
        if (currentState.equals(PartyGameState.ENDING)) {
            if (--endTime <= 0) {
                party.setDuel();

                getSpectators().stream().filter(profile -> profile.getProfileData().isSpectator()).forEach(spectator -> spectator.getProfileData().setSpectate());

                delete();
            }
        }
    }

    public void stop() {
        currentState = PartyGameState.ENDING;

        Optional<Profile> winner = party.getMembers().stream().filter(member -> !isSpectator(member)).findFirst();
        winner.ifPresent(profile -> {
            broadcast("&6" + profile.getName() + " has won!");

            try {
                Fireworks.spawnFirework(winner.get().getPlayer());
            } catch (Exception ignored) {}
        });

        party.getMembers().forEach(member -> {
            member.clear();
            member.getCacheData().getCombat().clear();
            member.getCacheData().clearCooldown();

            try {
                member.getPlayer().setImmobile(false);
            } catch (Exception ignored) {}
        });
    }

    public void broadcast(String message) {
        party.broadcast(message);
    }
}
