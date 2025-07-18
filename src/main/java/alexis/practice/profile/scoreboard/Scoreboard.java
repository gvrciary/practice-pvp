package alexis.practice.profile.scoreboard;

import alexis.practice.Practice;
import alexis.practice.arena.Arena;
import alexis.practice.duel.Duel;
import alexis.practice.event.games.EventArena;
import alexis.practice.kit.setup.KitEditor;
import alexis.practice.party.Party;
import alexis.practice.party.games.PartyGame;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.profile.settings.SettingType;
import alexis.practice.profile.settings.types.ScoreboardColor;
import alexis.practice.queue.Queue;
import alexis.practice.util.Utils;
import alexis.practice.util.handler.StaffHandler;
import cn.nukkit.Server;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.network.protocol.RemoveObjectivePacket;
import cn.nukkit.network.protocol.SetDisplayObjectivePacket;
import cn.nukkit.network.protocol.SetScorePacket;
import cn.nukkit.network.protocol.types.DisplaySlot;
import cn.nukkit.network.protocol.types.SortOrder;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.List;

public class Scoreboard {

    private final Profile profile;

    private final List<SetScorePacket.ScoreInfo> lines = new ArrayList<>();

    public Scoreboard(Profile profile) {
        this.profile = profile;
    }

    public void update() {
        if (!profile.getSettingsData().isEnabled(SettingType.SCOREBOARD.toString())) return;

        List<String> lines = new ArrayList<>();
        lines.add("&r&a\uE000");

        if (profile.getProfileData().isInLobby()) {
            List<Profile> players = ProfileManager.getInstance().getProfiles().values().stream().filter(Profile::isOnline).toList();
            List<Profile> playersInFight = ProfileManager.getInstance().getProfiles().values().stream().filter(s -> !s.getProfileData().isInLobby() && s.getProfileData().isInSetupMode()).toList();

            lines.add("&l&6|&r &fOnline:&6 " + players.size());
            lines.add("&l&6|&r &fFighting:&6 " + playersInFight.size());

            if (profile.getProfileData().getQueue() != null) {
                Queue queue = profile.getProfileData().getQueue();
                lines.add("&r&7");
                lines.add(" &l&6Queue");
                lines.add("&l&6|&r &fLadder:&6 " + queue.getType().getCustomName());
                lines.add("&l&6|&r &fTime:&6 " + Utils.formatTime(System.currentTimeMillis() - queue.getTime()));
                if (queue.isRanked()) lines.add("&l&6|&r &fRange:&6 " + queue.getEloMin() + " -> " + queue.getEloMax());
                else if (queue.is2vs2()) lines.add("&l&6|&r &fMode:&6 2vs2");
            }

        } else if (profile.getProfileData().getDuel() != null) {
            lines.addAll(profile.getProfileData().getDuel().scoreboard(profile));
        } else if (profile.getProfileData().getArena() != null) {
            lines.addAll(profile.getProfileData().getArena().scoreboard(profile));
        } else if (profile.getProfileData().isInSetupMode()) {
            KitEditor.Setup setup = KitEditor.getInstance().get(profile);

            if (setup != null) lines.add("&l&6|&r&f Editing Kit:&6 " + setup.getKit().getCustomName());
            else lines.add("&l&6|&r&f In Setup Mode");
        } else if (profile.getProfileData().getParty() != null) {
            Party party = profile.getProfileData().getParty();

            if (party.isInLobby()) {
                List<Profile> players = ProfileManager.getInstance().getProfiles().values().stream().filter(Profile::isOnline).toList();

                lines.add("&l&6|&r &fOnline:&6 " + players.size());
                lines.add("&r&9");
                lines.add(" &l&6Party");
                lines.add("&l&6|&r &fName:&6 " + party.getName());
                if (party.getOwner() != null) lines.add("&l&6|&r &fOwner:&6 " + party.getOwner().getName());
                lines.add("&l&6|&r &fMembers:&6 " + party.getMembers().size() + "/" + party.getMaxMembers());
            } else {
                lines.addAll(party.getDuel().scoreboard(profile));
            }
        } else if (profile.getProfileData().getEvent() != null && profile.getProfileData().getEvent().getEventArena() != null) {
            lines.addAll(profile.getProfileData().getEvent().getEventArena().scoreboard(profile));
        } else if (profile.getProfileData().isSpectator()) {
            lines.add(" &l&6Spectator Mode");
            Object spectate = profile.getProfileData().getSpectate();
            if (spectate instanceof Duel) {
                lines.addAll(((Duel) spectate).scoreboard(profile));
            } else if (spectate instanceof Arena) {
                lines.addAll(((Arena) spectate).scoreboard(profile));
            } else if (spectate instanceof PartyGame) {
                lines.addAll(((PartyGame) spectate).scoreboard(profile));
            } else if (spectate instanceof EventArena) {
                lines.addAll(((EventArena) spectate).scoreboard(profile));
            }
        } else if (profile.getProfileData().inStaffMode()) {
            StaffHandler.Data data = StaffHandler.getInstance().get(profile);

            List<Profile> players = ProfileManager.getInstance().getProfiles().values().stream().filter(Profile::isOnline).toList();

            lines.add("&l&6|&r &fOnline:&6 " + players.size());
            lines.add("&r&9");
            lines.add(" &l&6Staff Mode");

            try {
                if (profile.getPlayer().isOp()) {
                    Runtime runtime = Runtime.getRuntime();
                    double totalMB = NukkitMath.round(((double) runtime.totalMemory()) / 1024 / 1024, 1);
                    double usedMB = NukkitMath.round((double) (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024, 1);
                    Server server = Practice.getInstance().getServer();
                    lines.add("&l&6|&r &fUsage:&6 " + usedMB +  "/" + totalMB);
                    lines.add("&l&6|&r &fTPS:&6 " + server.getTicksPerSecond() + " &7(" + server.getTickUsage() + ")");
                    lines.add("&r&7");
                }
            } catch (Exception ignored) {}

            lines.add("&l&6|&r &fVanish:&6 " + (data.isVanish() ? "&aEnabled" : "&cDisabled"));
            lines.add("&l&6|&r &fChat:&6 " + (data.isChat() ? "&aEnabled" : "&cDisabled"));

            if (data.isFollowing()) {
                lines.add("&r&d");
                Profile follow = data.getFollow();

                if (follow != null) {
                    lines.add("&l&6|&r &fFollowing:&6 " + follow.getName());
                    if (follow.getProfileData().isInLobby()) {
                        lines.add("&r&f In Lobby");
                    } else if (follow.getProfileData().getDuel() != null) {
                        lines.add("&r&f In Duel:");
                        lines.add("&r&6 " + follow.getProfileData().getDuel().getWorldData().getDuelType());
                    } else if (follow.getProfileData().getArena() != null) {
                        lines.add("&r&f In Arena:");
                        lines.add("&r&6 " + follow.getProfileData().getArena().getArenaData().getName());
                    } else if (follow.getProfileData().getEvent() != null) {
                        lines.add("&r&f In Event:");
                        lines.add("&r&6 " + follow.getProfileData().getEvent().getType());
                    } else if (follow.getProfileData().getParty() != null) {
                        lines.add("&r&f In Party:");
                        lines.add("&r&6 " + follow.getProfileData().getParty().getName());
                    } else if (follow.getProfileData().getQueue() != null) {
                        lines.add("&r&f In Queue:&6 " + follow.getProfileData().getQueue().getType());
                        lines.add("&r&f Time:&6 " + Utils.formatTime(System.currentTimeMillis() - follow.getProfileData().getQueue().getTime()));
                    }
                }
            }
        }

        lines.add("&7&r  ");
        lines.add(" " + Utils.getScoreInformation().get((int) ((System.currentTimeMillis() / 4000) % Utils.getScoreInformation().size())));
        lines.add("&8&r\uE000");
        respawn();

        lines.replaceAll(s -> s.replaceAll("&6", getColor()));
        lines.forEach((s) -> addLine(TextFormat.colorize(s)));
    }

    public String getColor() {
        return ((ScoreboardColor) SettingType.SCOREBOARD_COLOR.getSetting()).getColor(profile);
    }

    public String getTitle() {
        String title = Utils.getScoreTitles().get((int) ((System.currentTimeMillis() / 200.0) % Utils.getScoreTitles().size()));
        return title.replaceAll("&6", getColor());
    }

    public void spawn() {
        SetDisplayObjectivePacket packet = new SetDisplayObjectivePacket();
        packet.displaySlot = DisplaySlot.SIDEBAR;
        packet.objectiveId = profile.getIdentifier();
        packet.displayName = TextFormat.colorize(getTitle());
        packet.criteria = "dummy";
        packet.sortOrder = SortOrder.ASCENDING;

        try {
            profile.getPlayer().dataPacket(packet);
        } catch (Exception ignored) {
        }
    }

    public void despawn() {
        RemoveObjectivePacket packet = new RemoveObjectivePacket();
        packet.objectiveId = profile.getIdentifier();

        try {
            profile.getPlayer().dataPacket(packet);
        } catch (Exception ignored) {}
    }

    public void respawn() {
        despawn();
        lines.clear();
        spawn();
    }

    public void addLine(String text) {
        int lineId = lines.size() + 1;

        SetScorePacket pk = new SetScorePacket();
        pk.action = SetScorePacket.Action.SET;
        SetScorePacket.ScoreInfo scoreInfo = new SetScorePacket.ScoreInfo(lineId, profile.getIdentifier(), lineId, text);
        pk.infos.add(scoreInfo);

        lines.add(scoreInfo);

        try {
            profile.getPlayer().dataPacket(pk);
        } catch (Exception ignored) {}
    }

}
