package alexis.practice.item.hotbar.lobby;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.party.Party;
import alexis.practice.party.PartyManager;
import alexis.practice.party.request.RequestData;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.window.SimpleWindowForm;

import java.util.List;
import java.util.Objects;

public class PartyItem extends ItemCustom {

    public PartyItem() {
        super("&6Party", Item.NAMETAG, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || !profile.getProfileData().isInLobby()) return;

        sendPartyForm(profile);
    }

    private void sendPartyForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("party_menu", "Party Menu")
                .addButton("create", "Create")
                .addButton("join", "Join")
                .addButton("invitations", "Invitations")
                .addHandler(h -> {
                    if (!h.isFormValid("party_menu")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        switch (button.getName()) {
                            case "create" -> PartyManager.getInstance().createParty(profile);
                            case "join" -> {
                                if (PartyManager.getInstance().getParties().isEmpty()) {
                                    player.sendMessage(TextFormat.colorize("&cNo parties available"));
                                    return;
                                }

                                sendParties(profile);
                            }
                            case "invitations" -> {
                                List<RequestData.InviteRequest> inviteRequestList = PartyManager.getInstance().getParties().values().stream()
                                        .map(party -> party.getRequestData().getInviteRequest(profile.getIdentifier()))
                                        .filter(Objects::nonNull)
                                        .toList();

                                if (inviteRequestList.isEmpty()) {
                                    player.sendMessage(TextFormat.colorize("&cYou don't have invitations"));
                                    return;
                                }

                                sendInvitations(profile);
                            }
                        }
                    }
                });

        form.sendTo(player);
    }

    private void sendParties(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("party_join", "Parties")
                .addHandler(h -> {
                    if (!h.isFormValid("party_join")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        int id = Integer.parseInt(button.getName());
                        Party party = PartyManager.getInstance().getParty(id);

                        if (party == null) {
                            player.sendMessage(TextFormat.colorize("&cParty no available"));
                            return;
                        }

                        if (party.isHasDisbanded()) {
                            player.sendMessage(TextFormat.colorize("&cThe action could not be completed"));
                            return;
                        }

                        if (!party.isOpen()) {
                            player.sendMessage(TextFormat.colorize("&cThe party is not open"));
                            return;
                        }

                        party.addMember(profile);
                    }
                });

        PartyManager.getInstance().getParties().values().forEach(party -> form.addButton(String.valueOf(party.getId()), party.getName() + "\n Members: " + party.getMembers().size()));

        form.sendTo(player);
    }

    private void sendInvitations(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("party_invitations", "Party Invitations")
                .addHandler(h -> {
                    if (!h.isFormValid("party_invitations")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        int id = Integer.parseInt(button.getName());
                        Party party = PartyManager.getInstance().getParty(id);

                        if (party == null) {
                            player.sendMessage(TextFormat.colorize("&cError"));
                            return;
                        }

                        RequestData.InviteRequest partyRequest = party.getRequestData().getInviteRequest(profile.getIdentifier());

                        if (partyRequest == null) {
                            player.sendMessage(TextFormat.colorize("&cError"));
                            return;
                        }

                        party.getRequestData().removeInviteRequest(profile);

                        if (!partyRequest.isValid()) {
                            player.sendMessage(TextFormat.colorize("&cThe action could not be completed"));
                            return;
                        }

                        partyRequest.getParty().addMember(profile);
                    }
                });

        List<RequestData.InviteRequest> inviteRequestList = PartyManager.getInstance().getParties().values().stream()
                .map(party -> party.getRequestData().getInviteRequest(profile.getIdentifier()))
                .filter(Objects::nonNull)
                .toList();

        inviteRequestList.forEach(request -> form.addButton(String.valueOf(request.getParty().getId()), request.getParty().getName() + "\n Members: " + request.getParty().getMembers().size()));

        form.sendTo(player);
    }

}
