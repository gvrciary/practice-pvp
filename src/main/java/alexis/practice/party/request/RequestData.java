package alexis.practice.party.request;

import alexis.practice.party.Party;
import alexis.practice.party.PartyManager;
import alexis.practice.party.games.duel.PartyDuelTypes;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class RequestData {
    private final Party party;

    @Getter
    private final Map<Integer, DuelRequest> duelRequest = new HashMap<>();
    private final Map<String, InviteRequest> inviteRequest = new HashMap<>();

    public RequestData(Party party) {
        this.party = party;
    }

    public void addInviteRequest(Profile profile) {
        inviteRequest.put(profile.getIdentifier(), new InviteRequest(party, profile.getIdentifier()));
        try {
            if (profile.isOnline()) {
                Player player = profile.getPlayer();

                player.sendMessage(TextFormat.colorize("&aYou have received an invitation to join the party " + party.getName()));
            }

            Profile leader = this.party.getOwner();

            if (leader != null && leader.isOnline()) {
                leader.getPlayer().sendMessage(TextFormat.colorize("&aYou have invited the player "+ profile.getName() + " to the party"));
            }

        } catch (Exception ignored) {}
    }

    public void addDuelRequest(Party party, PartyDuelTypes type) {
        duelRequest.put(party.getId(), new DuelRequest(party.getId(), type));

        try {
            Profile owner = party.getOwner();

            if (owner != null && owner.isOnline()) {
                owner.getPlayer().sendMessage(TextFormat.colorize("&aYou party have received a " + type.getName() + " request duel from " + party.getName()));
            }

            Profile leader = this.party.getOwner();

            if (leader != null && leader.isOnline()) {
                leader.getPlayer().sendMessage(TextFormat.colorize("&aYou party have sent a request duel to " + party.getName() + " in " + type.getName()));
            }
        } catch (Exception ignored) {}
    }

    public void removeDuelRequest(Party party) {
        duelRequest.remove(party.getId());
    }

    public DuelRequest getDuelRequest(int id) {
        return duelRequest.get(id);
    }

    public void removeInviteRequest(Profile profile) {
        inviteRequest.remove(profile.getIdentifier());
    }

    public InviteRequest getInviteRequest(String id) {
        return inviteRequest.get(id);
    }

    public static class DuelRequest {
        private static final long TIME_EXPIRE = 2 * 60 * 1000;

        private final int partyId;
        @Getter
        private final PartyDuelTypes type;
        private final long expire;

        DuelRequest(int id, PartyDuelTypes type) {
            this.partyId = id;
            this.type = type;
            this.expire = System.currentTimeMillis() + TIME_EXPIRE;
        }

        public boolean isValid() {
            Party partyOpponent = getPartyOpponent();

            if (partyOpponent == null) return false;

            if (partyOpponent.isHasDisbanded()) return false;

            if (!partyOpponent.isInLobby()) return false;

            return expire > System.currentTimeMillis();
        }

        public Party getPartyOpponent() {
            return PartyManager.getInstance().getParty(partyId);
        }
    }

    public static class InviteRequest {
        private static final long TIME_EXPIRE = 2 * 60 * 1000;

        @Getter
        private final Party party;
        private final String identifier;
        private final long expire;

        InviteRequest(Party party, String identifier) {
            this.party = party;
            this.identifier = identifier;

            this.expire = System.currentTimeMillis() + TIME_EXPIRE;
        }

        public boolean isValid() {
            if (party == null) return false;

            if (party.isFull()) return false;

            if (party.isHasDisbanded()) return false;

            return expire > System.currentTimeMillis();
        }

        public Profile getProfile() {
            return ProfileManager.getInstance().get(identifier);
        }
    }
}
