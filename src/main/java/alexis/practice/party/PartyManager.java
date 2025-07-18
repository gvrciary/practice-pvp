package alexis.practice.party;

import alexis.practice.profile.Profile;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

public final class PartyManager {
    @Getter
    private static final PartyManager instance = new PartyManager();

    @Getter
    private final ConcurrentHashMap<Integer, Party> parties = new ConcurrentHashMap<>();

    public void createParty(Profile profile) {
        int id = 1;

        while (parties.containsKey(id)) {
            id++;
        }

        Party party = new Party(id, profile);
        parties.put(id, party);
    }

    public Party getParty(int id) {
        return parties.get(id);
    }

    public void removeParty(int id) {
        parties.remove(id);
    }

}
