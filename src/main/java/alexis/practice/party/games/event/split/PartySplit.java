package alexis.practice.party.games.event.split;

import alexis.practice.duel.world.DuelWorld;
import alexis.practice.kit.Kit;
import alexis.practice.party.Party;
import alexis.practice.party.games.PartyGame;
import alexis.practice.party.games.PartyGameState;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.Fireworks;
import alexis.practice.util.Utils;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
public class PartySplit extends PartyGame {
    protected int firstKills = 0, secondKills = 0;

    protected final List<String> blueTeam = new ArrayList<>();
    protected final List<String> redTeam = new ArrayList<>();

    protected final Party party;

    public PartySplit(int id, Party party, DuelWorld worldData, Level level, Kit kit) {
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

        List<Profile> membersOnline = new ArrayList<>(party.getMembers());

        Collections.shuffle(membersOnline);

        AtomicInteger counter = new AtomicInteger(0);
        membersOnline.forEach(member -> {
            if (counter.get() % 2 == 0) counter.set(0);

            getKit(member);
            member.getCacheData().getCombat().clear();
            member.getCacheData().clearCooldown();

            try {
                Player player = member.getPlayer();

                player.setImmobile();
                if (counter.get() == 1) {
                    redTeam.add(member.getIdentifier());

                    player.teleport(world.getBlock(worldData.getFirstPosition()).getLocation());
                    player.setNameTag(TextFormat.colorize("&c" + member.getName()));
                } else {
                    blueTeam.add(member.getIdentifier());

                    player.teleport(world.getBlock(worldData.getSecondPosition()).getLocation());
                    player.setNameTag(TextFormat.colorize("&9" + member.getName()));
                }
            } catch (Exception ignored) {}

            counter.getAndIncrement();
        });
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

    public void start() {
        if (getAlivesTeam(redTeam) == 0 || getAlivesTeam(blueTeam) == 0 || party.getMembers().isEmpty()) {
            stop((getAlivesTeam(redTeam) == 0 ? blueTeam : redTeam));
            return;
        }

        currentState = PartyGameState.RUNNING;

        party.getMembers().forEach(member -> {
            try {
                Player player = member.getPlayer();

                player.getLevel().getPlayers().values().forEach(player::showPlayer);
                player.setImmobile(false);
            } catch (Exception ignored) {}
        });

        broadcast("&aMatch started.");
    }

    public void checkWinner() {
        if (getAlivesTeam(redTeam) == 0 || getAlivesTeam(blueTeam) == 0 || party.getMembers().isEmpty()) {
            stop((getAlivesTeam(redTeam) == 0 ? blueTeam : redTeam));
        }
    }

    public boolean isSameTeam(Profile profile, Profile target) {
        return (redTeam.contains(profile.getIdentifier()) && redTeam.contains(target.getIdentifier())) || (blueTeam.contains(profile.getIdentifier()) && blueTeam.contains(target.getIdentifier()));
    }

    public int getAlivesTeam(List<String> team) {
        return (int) getTeam(team).stream()
                .filter(profile -> !isSpectator(profile) && party.isMember(profile))
                .count();
    }

    public int getKills(Profile profile) {
        if (blueTeam.contains(profile.getIdentifier())) {
            return firstKills;
        }

        return secondKills;
    }

    public int getOpponentKills(Profile profile) {
        if (blueTeam.contains(profile.getIdentifier())) {
            return secondKills;
        }

        return firstKills;
    }

    public void removeProfile(Profile profile) {
        blueTeam.remove(profile.getIdentifier());
        redTeam.remove(profile.getIdentifier());
    }

    public void increaseKills(Profile profile) {
        if (blueTeam.contains(profile.getIdentifier())) {
            firstKills++;
            return;
        }

        secondKills++;
    }

    public void increaseOpponentKills(Profile profile) {
        if (blueTeam.contains(profile.getIdentifier())) {
            secondKills++;
            return;
        }

        firstKills++;
    }

    public List<Profile> getOpponentTeam(Profile profile) {
        if (blueTeam.contains(profile.getIdentifier())) {
            return getTeam(redTeam);
        }

        return getTeam(blueTeam);
    }

    public boolean isFirstTeam(Profile profile) {
        return blueTeam.contains(profile.getIdentifier());
    }

    public List<Profile> getTeam(Profile profile) {
        if (blueTeam.contains(profile.getIdentifier())) {
            return getTeam(blueTeam);
        }

        return getTeam(redTeam);
    }

    public List<String> scoreboard(Profile profile) {
        List<String> lines = new ArrayList<>();

        switch (currentState) {
            case STARTING -> lines.add("&l&6|&r&f Match Starting in: &6" + startTime);
            case RUNNING -> {
                if (isSpectator(profile) && profile.getProfileData().isSpectator()) {
                    lines.add("&l&6|&r&f Duration: &6" + Utils.formatTime(runTime));
                    lines.add("&r&7");
                    lines.add("&l&6|&r&f Red Alives: &6" + getAlivesTeam(redTeam));
                    lines.add("&l&6|&r&f Blue Alives: &6" + getAlivesTeam(blueTeam));
                    return lines;
                }

                lines.add("&l&6|&r&f Duration: &6" + Utils.formatTime(runTime));
                lines.add("&l&6|&r&f Your Team:&6 " +  getAlivesTeam(isFirstTeam(profile) ? blueTeam : redTeam) + " &7(" + getKills(profile) + ")");
                lines.add("&l&6|&r&f Their Team:&6 " + getAlivesTeam(isFirstTeam(profile) ? redTeam : blueTeam) + " &7(" + (isFirstTeam(profile) ? secondKills : firstKills) + ")");
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

    protected void broadcastWinMessage(List<Profile> team, List<String> winner) {
        team.stream().filter(party::isMember).forEach(member -> {

            try {
                Player player = member.getPlayer();

                if (winner.contains(member.getIdentifier())) {
                    player.sendTitle(TextFormat.colorize("&l&aVICTORY&r"), TextFormat.colorize("&aYour team won the fight!"));
                    Fireworks.spawnFirework(player);
                } else {
                    player.sendTitle(TextFormat.colorize("&l&cDEFEAT&r"), TextFormat.colorize("&7Another Team won the fight!"));
                }

                player.setImmobile(false);
            } catch (Exception ignored) {}

            member.clear();
            member.getCacheData().getCombat().clear();
            member.getCacheData().clearCooldown();
        });
    }

    public void stop(List<String> winner) {
        currentState = PartyGameState.ENDING;

        broadcastWinMessage(getTeam(redTeam), winner);
        broadcastWinMessage(getTeam(blueTeam), winner);
    }

    public void broadcast(String message) {
        party.broadcast(message);
    }

    public List<Profile> getTeam(List<String> team) {
        return ProfileManager.getInstance().getProfiles().values().stream()
                .filter(profile -> team.contains(profile.getIdentifier()) && profile.isOnline() && party.isMember(profile))
                .collect(Collectors.toList());
    }

}
