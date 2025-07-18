package alexis.practice.party;

import alexis.practice.party.games.PartyGame;
import alexis.practice.party.games.duel.PartyDuel;
import alexis.practice.party.games.event.ffa.PartyFFA;
import alexis.practice.party.games.event.split.PartySplit;
import alexis.practice.party.request.RequestData;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Party {

    @Getter
    private final int id;

    @Getter
    private int maxMembers = 12;
    @Getter
    private boolean open = true;
    private String owner;

    @Setter @Getter
    private String name;
    @Setter @Getter
    private PartyGame duel = null;

    private final List<String> members = new ArrayList<>();

    @Getter
    private final RequestData requestData;

    @Getter
    private boolean hasDisbanded = false;

    public Party(int id, Profile owner) {
        this.id = id;
        this.owner = owner.getIdentifier();
        this.name = owner.getName() + "'s Party";
        this.requestData = new RequestData(this);

        try {
            if (owner.getPlayer().hasPermission("partypremium.permission")) {
                this.maxMembers = 32;
            }
        } catch (Exception ignored) {}

        addMember(owner);
    }

    @Nullable
    public Profile getOwner() {
        return ProfileManager.getInstance().get(owner);
    }

    public void toggleOpen() {
        open = !open;

        broadcast(isOpen() ? "&aThe party has been opened" : "&cThe party has been closed");
    }

    public void setDuel() {
        duel = null;

        if (hasDisbanded) {
            disband();
        } else {
            getMembers().forEach(member -> {
                member.clear();

                try {
                    Player player = member.getPlayer();

                    if (isOwner(member)) PlayerUtil.getPartyKit(player);
                    else PlayerUtil.getPartyKit(player, false);

                    player.teleport(player.getServer().getDefaultLevel().getSpawnLocation());
                } catch (Exception ignored) {}
            });
        }
    }

    public void addMember(Profile profile) {
        members.add(profile.getIdentifier());
        profile.clear();
        profile.getProfileData().setParty(this);

        broadcast("&a" + profile.getName() + " has joined the party");

        if (isInLobby()) {
            try {
                if (isOwner(profile)) PlayerUtil.getPartyKit(profile.getPlayer());
                else PlayerUtil.getPartyKit(profile.getPlayer(), false);
            } catch (Exception ignored) {}

            return;
        }

        duel.addSpectator(profile);
    }

    public boolean isOwner(Profile profile) {
        return owner.equals(profile.getIdentifier());
    }

    public List<Profile> getMembers() {
        return ProfileManager.getInstance().getProfiles().values().stream()
                .filter(profile -> this.members.contains(profile.getIdentifier()) &&
                        profile.isOnline() &&
                        profile.getProfileData().getParty() != null &&
                        profile.getProfileData().getParty().getId() == id)
                .collect(Collectors.toList());
    }

    @Nullable
    public Profile getMemberById(String id) {
        if (!members.contains(id)) return null;

        return ProfileManager.getInstance().get(id);
    }

    public boolean isMember(Profile profile) {
        return members.contains(profile.getIdentifier());
    }

    public boolean isFull() {
        return getMembers().size() >= maxMembers;
    }

    public boolean isInLobby() {
        return duel == null;
    }

    public void promoteOwner(Profile profile) {
        String oldOwner = owner;
        owner = profile.getIdentifier();

        if (isInLobby()) {
            try {
                profile.clear();
                Player player = profile.getPlayer();

                player.sendMessage(TextFormat.colorize("&aNow you own the party"));
                PlayerUtil.getPartyKit(player);
            } catch (Exception ignored) {}

            try {
                Profile leaderOld = ProfileManager.getInstance().get(oldOwner);

                if (leaderOld != null) {
                    leaderOld.clear();
                    PlayerUtil.getPartyKit(leaderOld.getPlayer(), false);
                }
            } catch (Exception ignored) {}
        }

        broadcast("&a" + profile.getName() + " Now he owns the party");
    }

    public void removeMember(Profile profile) {
        if (!isInLobby()) {
            if (duel.isSpectator(profile)) duel.removeSpectator(profile);
            else setDeath(profile, false);
        }

        broadcast("&c" + profile.getName() + " has leave the party");
        members.remove(profile.getIdentifier());
        profile.clear();
        profile.getProfileData().setParty();

        try {
            PlayerUtil.getLobbyKit(profile.getPlayer());
        } catch (Exception ignored) {}

        if (isOwner(profile)) {
            if (isInLobby()) {
                disband();
                return;
            } else hasDisbanded = true;
        }

        if (getMembers().isEmpty()) disband();
    }

    public void setDeath(Profile profile) {
        setDeath(profile, true);
    }

    public void setDeath(Profile profile, boolean addSpectator) {
        Profile lastHit = profile.getCacheData().getCombat().get();

        if (lastHit != null) {
            profile.setDeathAnimation(lastHit);
            duel.broadcast("&6" + profile.getName() + " &7was slain by&6 " + lastHit.getName());

            if (duel instanceof PartyDuel partyDuel) partyDuel.increaseKills(partyDuel.getOpponentParty(this));
            else if (duel instanceof PartySplit partySplit) partySplit.increaseKills(profile);
            else if (duel instanceof PartyFFA partyFFA) partyFFA.increaseKills(profile);
        }

        if (addSpectator) duel.addSpectator(profile);
        else {
            if (duel instanceof PartySplit partySplit) partySplit.removeProfile(profile);
        }

        duel.checkWinner();
    }

    public void broadcast(String message) {
        getMembers().forEach(member -> {
            try {
                member.getPlayer().sendMessage(TextFormat.colorize(message));
            } catch (Exception ignored) {}
        });
    }

    public void disband() {
        hasDisbanded = true;
        broadcast("&cParty was disband");

        getMembers().forEach(this::removeMember);

        PartyManager.getInstance().removeParty(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("&6Party Information\n");

        List<String> members = getMembers().stream()
                .map(Profile::getName)
                .collect(Collectors.toList());
        sb.append("&7Members:&f ").append(String.join(", ", members)).append("\n");
        sb.append("&7Party Privacy:&f ").append((isOpen() ? "Open" : "Close"));

        return sb.toString();
    }
}
