package alexis.practice.party.games.duel;

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
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PartyDuel extends PartyGame {
    protected int firstKills = 0, secondKills = 0;

    protected final Party firstTeam;
    protected final Party secondTeam;

    public PartyDuel(int id, Party firstTeam, Party secondTeam, DuelWorld worldData, Level level, Kit kit) {
        this.id = id;
        this.firstTeam = firstTeam;
        this.secondTeam = secondTeam;
        this.kit = kit;

        this.world = level;
        this.worldData = worldData;

        firstTeam.setDuel(this);
        secondTeam.setDuel(this);

        setup();
    }

    public void setup() {
        super.setup();

        firstTeam.getMembers().forEach(member -> {
            getKit(member);

            member.getCacheData().getCombat().clear();
            member.getCacheData().clearCooldown();

            try {
                Player player = member.getPlayer();

                player.setImmobile();
                player.teleport(world.getBlock(worldData.getFirstPosition()).getLocation());
                player.setNameTag(TextFormat.colorize("&9" + member.getName()));
            } catch (Exception ignored) {}
        });

        secondTeam.getMembers().forEach(member -> {
            getKit(member);

            member.getCacheData().getCombat().clear();
            member.getCacheData().clearCooldown();

            try {
                Player player = member.getPlayer();

                player.setImmobile();
                player.teleport(world.getBlock(worldData.getSecondPosition()).getLocation());
                player.setNameTag(TextFormat.colorize("&c" + member.getName()));
            } catch (Exception ignored) {}

        });
    }

    public void start() {
        if (firstTeam.getMembers().isEmpty() || secondTeam.getMembers().isEmpty()) {
            stop((firstTeam.getMembers().isEmpty() ? secondTeam : firstTeam));
            return;
        }

        currentState = PartyGameState.RUNNING;
        firstTeam.getMembers().forEach(member -> {
            try {
                Player player = member.getPlayer();

                player.getLevel().getPlayers().values().forEach(player::showPlayer);
                player.setImmobile(false);
            } catch (Exception ignored) {}
        });

        secondTeam.getMembers().forEach(member -> {
            try {
                Player player = member.getPlayer();

                player.getLevel().getPlayers().values().forEach(player::showPlayer);
                player.setImmobile(false);
            } catch (Exception ignored) {}
        });

        broadcast("&aMatch started.");
    }

    public void getKit(Profile profile) {
        if (!profile.isOnline()) return;

        try {
            Player player = profile.getPlayer();

            profile.clear();
            player.setGamemode(Player.SURVIVAL);
            player.setMaxHealth(player.getMaxHealth());

            kit.giveKit(profile);
        } catch (Exception ignored) {}
    }

    public void checkWinner() {
        if (getPartyAlive(firstTeam) == 0 || getPartyAlive(secondTeam) == 0) {
            stop((getPartyAlive(firstTeam) == 0 ? secondTeam : firstTeam));
        }
    }

    public int getKills(Party party) {
        if (firstTeam.getId() == party.getId()) {
            return firstKills;
        }

        return secondKills;
    }

    public void increaseKills(Party party) {
        if (firstTeam.getId() == party.getId()) {
            firstKills++;
            return;
        }

        secondKills++;
    }

    public Party getOpponentParty(Party party) {
        if (firstTeam.getId() == party.getId()) {
            return secondTeam;
        }

        return firstTeam;
    }

    public void stop(Party winner) {
        currentState = PartyGameState.ENDING;

        if (winner != null) {
            broadcastWinMessage(firstTeam, winner);
            broadcastWinMessage(secondTeam, winner);

            String spectators = getSpectators().stream()
                    .filter(s -> s.getProfileData().isSpectator())
                    .map(Profile::getName)
                    .collect(Collectors.joining(", "));

            if (!spectators.isEmpty()) {
                broadcast("&aSpectators: " + spectators);
            }
        }
    }

    protected void broadcastWinMessage(Party team, Party winner) {
        team.getMembers().forEach(member -> {
            try {
                Player player = member.getPlayer();

                if (team.getId() == winner.getId()) {
                    player.sendTitle(TextFormat.colorize("&l&cFinish&r"), TextFormat.colorize("&7Your party won the fight!"));
                    Fireworks.spawnFirework(player);
                } else {
                    player.sendTitle(TextFormat.colorize("&l&cFinish&r"), TextFormat.colorize("&a" + winner.getName() + " &7won the fight!"));
                }
                player.setImmobile(false);
            } catch (Exception ignored) {}

            member.clear();
            member.getCacheData().getCombat().clear();
            member.getCacheData().clearCooldown();
        });
    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();

        switch (currentState) {
            case STARTING -> lines.add("&l&6|&r&f Match Starting in:&6 " + startTime);
            case RUNNING -> {
                if (isSpectator(profile) && profile.getProfileData().isSpectator()) {
                    lines.add(" &f" + firstTeam.getName());
                    lines.add(" &6VS");
                    lines.add(" &f" + secondTeam.getName());
                    lines.add(" &r&6");
                    lines.add("&l&6|&r&f Duration: &6" + Utils.formatTime(runTime));
                    lines.add("&r&7");
                    return lines;
                }

                if (currentState.equals(PartyGameState.STARTING)) {
                    lines.add("&l&6|&r&f Opponent Party:&6 " + getOpponentParty(profile.getProfileData().getParty()).getName());
                }

                lines.add("&l&6|&r&f Your Party Kills:&6 " + getKills(profile.getProfileData().getParty()));
                lines.add("&l&6|&r&f Their Party Kills:&6 " + getKills(getOpponentParty(profile.getProfileData().getParty())));
            }

            case ENDING -> {
                if (!isSpectator(profile)) {
                    lines.add("&l&6| &aVICTORY");
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
                firstTeam.setDuel();
                secondTeam.setDuel();

                getSpectators().stream().filter(profile -> profile.getProfileData().isSpectator()).forEach(spectator -> spectator.getProfileData().setSpectate());

                delete();
            }
        }
    }

    public boolean isFirstParty(Party party) {
        return party.getId() == firstTeam.getId();
    }

    public int getPartyAlive(Party party) {
        return (int) party.getMembers().stream().filter(member -> !isSpectator(member)).count();
    }

    public void broadcast(String message) {
        secondTeam.broadcast(message);
        firstTeam.broadcast(message);
    }

}
