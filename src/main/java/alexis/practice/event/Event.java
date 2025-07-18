package alexis.practice.event;

import alexis.practice.Practice;
import alexis.practice.event.games.EventArena;
import alexis.practice.event.games.EventState;
import alexis.practice.event.games.EventType;
import alexis.practice.event.games.types.meetup.Meetup;
import alexis.practice.event.games.types.meetup.scenarios.Scenario;
import alexis.practice.event.games.types.meetup.scenarios.ScenarioManager;
import alexis.practice.event.games.types.meetup.scenarios.defaults.*;
import alexis.practice.event.games.types.sumo.Sumo;
import alexis.practice.event.games.types.tournament.Tournament;
import alexis.practice.event.games.types.tournament.match.Match;
import alexis.practice.event.team.Team;
import alexis.practice.event.team.TeamManager;
import alexis.practice.event.world.EventWord;
import alexis.practice.item.HotbarItem;
import alexis.practice.kit.Kit;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Event {
    @Getter @Setter
    private int maxPlayers = 50;

    @Getter
    private final int id;
    @Getter
    private final Profile profile;
    @Getter
    private final Kit kit;
    @Getter
    private final EventType type;
    private final int team;

    @Getter
    private final EventArena eventArena;

    @Getter
    private TeamManager teamManager = null;

    private final List<String> players = new ArrayList<>();

    public Event(int id, Profile profile, Kit kit, EventType type, int team, @Nullable EventWord worldData) {
        this.id = id;
        this.profile = profile;
        this.kit = kit;
        this.type = type;
        this.team = team;

        try {
            if (this.type.equals(EventType.TOURNAMENT)) this.eventArena = (EventArena) type.getEventClass().getDeclaredConstructors()[0].newInstance(id, this);
            else {
                Level world = Practice.getInstance().getServer().getLevelByName("event-" + id);
                this.eventArena = (EventArena) type.getEventClass().getDeclaredConstructors()[0].newInstance(id, this, worldData, world);
            }
        } catch (Exception e) {
            System.out.println("There was an error creating the event arena");
            System.out.println(e.getMessage());

            try {
                profile.getPlayer().sendMessage(TextFormat.colorize("&cError"));
            } catch (Exception ignored) {}

            throw new RuntimeException();
        }

        if (team > 1) {
            teamManager = new TeamManager(this, team);
        }

        if (worldData != null && type.equals(EventType.SKYWARS)) {
            maxPlayers = worldData.getSpawns().size() * team;
        }

        sendMessage();
        addPlayer(profile);
    }

    public boolean isTeam() {
        return team > 1 && teamManager != null;
    }

    public int getCountTeam() {
        return team;
    }

    public boolean isCreator(Profile profile) {
        return profile.getIdentifier().equals(this.profile.getIdentifier());
    }

    public void executeForceStart() {
        if (eventArena.getCurrentState().equals(EventState.WAITING) && eventArena.getWaitingTime() > 30 && getPlayers().size() > 1) {
            eventArena.setWaitingTime(30);
            broadcast("&aThe creator has forced the start of the event to 30 seconds");
        }
    }

    public void sendMessage() {
        Practice.getInstance().getServer().broadcastMessage(TextFormat.colorize("&l&6" + getType().getName()
                + "\n&l&6|&r &fMode:&6 " + (isTeam() ? "TO" + getCountTeam() : "FFA")
                + "\n&l&6|&r &fHost:&6 " + getProfile().getName()
                + "\n&l&6|&r &fStarting in:&6 " + getEventArena().getWaitingTime() + "s"));
    }

    public void addPlayer(Profile profile) {
        profile.clear();

        profile.getProfileData().setEvent(this);
        players.add(profile.getIdentifier());
        eventArena.addPlayer(profile);

        try {
            Player player = profile.getPlayer();

            player.getInventory().setItem(0, HotbarItem.EVENT_INFO.getItem());
            player.getInventory().setItem(8, HotbarItem.EVENT_LEAVE.getItem());

            if (isTeam()) {
                player.getInventory().setItem(4, HotbarItem.TEAM_SELECTOR.getItem());
            }

            if (isCreator(profile)) {
                player.getInventory().setItem(1, HotbarItem.EVENT_MANAGEMENT.getItem());
            }
        } catch (Exception ignored) {}

        broadcast("&f" + profile.getName() + " &6has joined the event");
    }

    public void removePlayer(Profile profile) {
        if (!eventArena.getCurrentState().equals(EventState.WAITING) && !eventArena.isSpectator(profile)) {
            if (!type.equals(EventType.TOURNAMENT)) {
                broadcast("&c" + profile.getName() + " has been disqualified for leaving");
            }
            setDeath(profile, false);
        }

        if (isTeam()) {
            Team team = teamManager.getTeam(profile);

            if (team != null) {
                team.removeMember(profile);
            }
        }

        profile.clear();
        profile.getProfileData().setEvent();
        players.remove(profile.getIdentifier());

        try {
            PlayerUtil.getLobbyKit(profile.getPlayer());
        } catch (Exception ignored) {}

        if (getPlayers().isEmpty()) stop();
    }

    public void setDeath(Profile profile) {
        setDeath(profile, true);
    }

    public void setDeath(Profile profile, boolean addSpectator) {
        if (eventArena instanceof Meetup meetup) {
            Scenario webCage = meetup.getScenarioManager().get(ScenarioManager.WEBCAGE);

            if (webCage.isEnabled() && webCage instanceof WebCage) {
                try {
                    webCage.add(profile.getPlayer());
                } catch (Exception ignored) {}
            }

            Scenario siphon = meetup.getScenarioManager().get(ScenarioManager.SIPHON);

            if (siphon.isEnabled() && siphon instanceof Siphon) {
                try {
                    siphon.add(profile.getPlayer());
                } catch (Exception ignored) {}
            }

            Scenario timebomb = meetup.getScenarioManager().get(ScenarioManager.TIMEBOMB);

            if (timebomb.isEnabled() && timebomb instanceof Timebomb) {
                try {
                    timebomb.add(profile.getPlayer());
                } catch (Exception ignored) {}
            } else {
                try {
                    Player player = profile.getPlayer();

                    player.getInventory().getContents().values().forEach(item -> player.getLevel().dropItem(player.getPosition(), item));

                    Item experience = Item.get(Item.EXPERIENCE_BOTTLE);
                    experience.setCount(16);

                    player.getLevel().dropItem(player.getPosition(), experience);
                    player.getLevel().dropItem(player.getPosition(), Block.get(BlockID.ANVIL).toItem());

                    player.getLevel().dropExpOrb(player.getPosition(), player.getExperience());
                } catch (Exception ignored) {}
            }
        } else if (eventArena instanceof Tournament tournament) {
            Match match = tournament.inAnyMatch(profile);

            if (match != null && match.isSpectatorInMatch(profile)) return;
        }

        Profile killer = profile.getCacheData().getCombat().get();

        if (killer != null) {
            profile.setDeathAnimation(killer);

            if (killer.isOnline()) {
                eventArena.increaseKills(killer);

                if (eventArena instanceof Meetup meetup) {
                    Scenario timebomb = meetup.getScenarioManager().get(ScenarioManager.TIMEBOMB);
                    Scenario safeLoot = meetup.getScenarioManager().get(ScenarioManager.SAFELOOT);

                    if (safeLoot.isEnabled() && timebomb.isEnabled() && safeLoot instanceof SafeLoot sL) {
                        try {
                            Position position = profile.getPlayer().getPosition().clone();

                            sL.lock(killer, position.getLocation());
                            sL.lock(killer, position.add(-1).getLocation());
                        } catch (Exception ignored) {}
                    }

                    Scenario dnd = meetup.getScenarioManager().get(ScenarioManager.DONOTDISTURB);

                    if (dnd.isEnabled() && dnd instanceof DoNotDisturb doNotDisturb) {
                        doNotDisturb.removeData(profile);
                        doNotDisturb.removeData(killer);
                    }

                    Scenario noClean = meetup.getScenarioManager().get(ScenarioManager.NOCLEAN);

                    if (noClean.isEnabled() && noClean instanceof NoClean nC) {
                        nC.setNoClean(killer);

                        try {
                            killer.getPlayer().sendMessage(TextFormat.colorize("&aYou have NoClean for 30 seconds"));
                        } catch (Exception ignored) {}
                    }
                }
            }

            if (type.equals(EventType.SKYWARS) || type.equals(EventType.MEETUP)) {
                broadcast("&c" + profile.getName() + "[" + eventArena.getKills(profile) + "] &7was slain by &6" + killer.getName() + "[" + eventArena.getKills(killer) + "]");
            } else if (type.equals(EventType.SUMO) || type.equals(EventType.TOURNAMENT)) {
                broadcast("&c" + profile.getName() + " &7has been eliminated by&6 " + killer.getName());
            }
        }

        if (addSpectator) {
            if (eventArena instanceof Tournament tournament) {
                Match match = tournament.inAnyMatch(profile);

                if (match != null && match.isAlive(profile)) {
                    match.addSpectator(profile);
                }
            } else eventArena.addSpectator(profile);
        }

        profile.getCacheData().getCombat().clear();
        profile.getCacheData().clearCooldown();

        if (eventArena instanceof Sumo sumo && sumo.inFight(profile)) {
            sumo.assignFight();
        } else if (eventArena instanceof Tournament tournament) {
            Match match = tournament.inAnyMatch(profile);

            if (match != null) {
                match.checkWinner();
            }
        }

        eventArena.checkWinner();
    }

    public boolean isPlayer(Profile profile) {
        return players.contains(profile.getIdentifier());
    }

    public List<Profile> getPlayers() {
        return ProfileManager.getInstance().getProfiles().values().stream()
                .filter(profile -> this.players.contains(profile.getIdentifier()) &&
                        profile.isOnline() &&
                        profile.getProfileData().getEvent() != null &&
                        profile.getProfileData().getEvent().getId() == id)
                .collect(Collectors.toList());
    }

    public void broadcast(String message) {
        getPlayers().forEach(profile -> {
            try {
                profile.getPlayer().sendMessage(TextFormat.colorize(message));
            } catch (Exception ignored) {}
        });
    }

    public void stop() {
        broadcast("&cEvent has finished");
        getPlayers().forEach(profile ->{
            profile.getProfileData().setEvent();
            profile.clear();

            try {
                PlayerUtil.getLobbyKit(profile.getPlayer());
            } catch (Exception ignored) {}
        });

        eventArena.delete();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("&6Event Information\n");

        List<String> players = getPlayers().stream()
                .map(Profile::getName)
                .collect(Collectors.toList());

        sb.append("&7Mode: &f").append(type.getName()).append("\n");
        sb.append("&7Players:&f ").append(String.join(", ", players)).append("\n");

        return sb.toString();
    }

}
