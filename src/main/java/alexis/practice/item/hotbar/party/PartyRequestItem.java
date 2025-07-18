package alexis.practice.item.hotbar.party;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.party.Party;
import alexis.practice.party.PartyManager;
import alexis.practice.party.games.PartyGamesManager;
import alexis.practice.party.games.duel.PartyDuelTypes;
import alexis.practice.party.request.RequestData;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.ImageType;
import com.denzelcode.form.window.SimpleWindowForm;

import java.util.Arrays;

public class PartyRequestItem extends ItemCustom {

    public PartyRequestItem() {
        super("&6Party Duel", 279, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null || !profile.getProfileData().getParty().isInLobby()) return;

        if (!profile.getProfileData().getParty().isOwner(profile)) {
            player.sendMessage(TextFormat.colorize("&cYou can't because you are not an owner"));
            return;
        }

        sendPartyDuelForm(profile);
    }

    private void sendPartyDuelForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        Party party = profile.getProfileData().getParty();

        if (party == null) return;

        FormAPI.simpleWindowForm("party.duels", "Party Duel")
                .addButton("party.duel", "Duel Party")
                .addButton("party.request", "View Requests")
                .addHandler(h -> {
                    if (!h.isFormValid("party.duels")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        switch (button.getName()) {
                            case "party.duel" -> {
                                if (PartyManager.getInstance().getParties().size() - 1 <= 0) {
                                    player.sendMessage(TextFormat.colorize("&cNo parties available"));
                                    return;
                                }

                                sendDuelForm(profile);
                            }
                            case "party.request" -> {
                                if (party.getRequestData().getDuelRequest().isEmpty()) {
                                    player.sendMessage(TextFormat.colorize("&cDuel party request is empty"));
                                    return;
                                }

                                sendRequest(profile);
                            }
                        }
                    }
                })
                .sendTo(player);
    }

    private void sendDuelForm(Profile profile) {
        if (profile.getProfileData().getParty() == null) return;

        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("party.duel", "Party Duel")
                .addHandler(h -> {
                    if (!h.isFormValid("party.duel")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        Party party = PartyManager.getInstance().getParty(Integer.parseInt(button.getName()));
                        finishForm(profile, party);
                    }
                });

        PartyManager.getInstance().getParties().values().stream().filter(party -> party.getId() != profile.getProfileData().getParty().getId() && party.isInLobby() && !party.isHasDisbanded()).forEach(parties -> form.addButton(String.valueOf(parties.getId()), parties.getName()));

        form.sendTo(player);
    }

    private void sendRequest(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }
        Party party = profile.getProfileData().getParty();

        if (party == null) return;

        SimpleWindowForm form = FormAPI.simpleWindowForm("party.duel", "Party Duel")
                .addHandler(h -> {
                    if (!h.isFormValid("party.duel")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        RequestData.DuelRequest request = party.getRequestData().getDuelRequest(Integer.parseInt(button.getName()));

                        if (request == null) return;

                        Party partyOpponent = request.getPartyOpponent();

                        if (partyOpponent == null) return;

                        party.getRequestData().removeDuelRequest(partyOpponent);

                        if (!request.isValid()) {
                            player.sendMessage(TextFormat.colorize("&cDuel request is invalid or has been expired"));
                            return;
                        }

                        PartyGamesManager.getInstance().createPartyDuel(party, partyOpponent, request.getType(), null);
                    }
                });

        party.getRequestData().getDuelRequest().values().forEach(parties -> form.addButton(String.valueOf(parties.getPartyOpponent().getId()), parties.getPartyOpponent().getName(), ImageType.PATH, parties.getType().getType().getIcon()));

        form.sendTo(player);
    }

    private void finishForm(Profile profile, Party party) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }
        Party partyProfile = profile.getProfileData().getParty();

        if (partyProfile == null) return;

        SimpleWindowForm form = FormAPI.simpleWindowForm("party.duel", "Party Duel")
                .addHandler(h -> {
                    if (!h.isFormValid("party.duel")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        PartyDuelTypes type = PartyDuelTypes.get(button.getName());

                        if (type == null) return;

                        if (party.isHasDisbanded() || !party.isInLobby()) {
                            player.sendMessage("&cThe request could not be completed");
                            return;
                        }

                        party.getRequestData().addDuelRequest(partyProfile, type);
                    }
                });

        Arrays.stream(PartyDuelTypes.values()).forEach(type -> form.addButton(type.getName(), type.getCustomName(), ImageType.PATH, type.getType().getIcon()));

        form.sendTo(player);
    }

}
