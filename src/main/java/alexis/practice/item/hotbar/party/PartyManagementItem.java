package alexis.practice.item.hotbar.party;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.party.Party;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.profile.settings.SettingType;
import cn.nukkit.Player;
import cn.nukkit.item.ItemID;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.Input;
import com.denzelcode.form.element.Toggle;
import com.denzelcode.form.window.SimpleWindowForm;

import java.util.List;

public class PartyManagementItem extends ItemCustom {

    public PartyManagementItem() {
        super("&6Party Management", ItemID.CLOCK, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null || !profile.getProfileData().getParty().isInLobby()) return;

        if (!profile.getProfileData().getParty().isOwner(profile)) {
            player.sendMessage(TextFormat.colorize("&cYou can't because you are not an owner"));
            return;
        }

        sendManagerForm(profile);
    }

    private void sendManagerForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        Party party = profile.getProfileData().getParty();

        if (party == null) return;

        FormAPI.simpleWindowForm("party.management", "Party Management")
                .addButton("configuration.option", "Configuration")
                .addButton("kick.option", "Kick Member")
                .addButton("promote.option", "Promote Member")
                .addButton("invite.option", "Invite Player")
                .addHandler(h -> {
                    if (!h.isFormValid("party.management")) return;

                    Button button = h.getButton();
                    List<Profile> members = party.getMembers().stream().filter(member -> party.getOwner() != null && !member.getIdentifier().equals(party.getOwner().getIdentifier())).toList();

                    if (button.getName() != null) {
                        switch (button.getName()) {
                            case "configuration.option" -> sendConfigurationForm(profile);
                            case "kick.option" -> {
                                if (members.isEmpty()) {
                                    player.sendMessage(TextFormat.colorize("&cThere are not enough members"));
                                    return;
                                }

                                sendKickForm(profile);
                            }
                            case "promote.option" -> {
                                if (members.isEmpty()) {
                                    player.sendMessage(TextFormat.colorize("&cThere are not enough members"));
                                    return;
                                }

                                sendPromoteForm(profile);
                            }
                            case "invite.option" -> {
                                List<Profile> players = ProfileManager.getInstance().getProfiles().values().stream().filter(s -> s.getProfileData().getParty() == null).toList();

                                if (players.isEmpty()) {
                                    player.sendMessage(TextFormat.colorize("&cThere are not enough players online"));
                                    return;
                                }

                                sendInviteForm(profile);
                            }
                        }
                    }
                })
                .sendTo(player);
    }

    private void sendConfigurationForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        Party party = profile.getProfileData().getParty();

        if (party == null) return;

        FormAPI.customWindowForm("party.configuration", "Party Configuration")
                .addInput("party.name", "Party Name", party.getName())
                .addToggle("party.privacy", "Party Privacy", party.isOpen())
                .addHandler(h -> {
                    if (!h.isFormValid("party.configuration")) return;

                    Input partyName = h.getForm().getElement("party.name");

                    if (!partyName.getValue().isEmpty()) {
                         party.setName(partyName.getValue());
                    }

                    Toggle partyPrivacy = h.getForm().getElement("party.privacy");

                    if (partyPrivacy.getValue() != party.isOpen()) {
                        party.toggleOpen();
                    }
                })
                .sendTo(player);
    }

    private void sendKickForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        Party party = profile.getProfileData().getParty();

        if (party == null) return;

        SimpleWindowForm form = FormAPI.simpleWindowForm("party.kick", "Party Kick")
                .addHandler(h -> {
                    if (!h.isFormValid("party.kick")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        Profile member = party.getMemberById(button.getName());

                        if (member == null) return;

                        if (!party.isInLobby()) {
                            player.sendMessage(TextFormat.colorize("&cYou can't expel because there is a game."));
                            return;
                        }

                        if (!party.isMember(member) || !member.isOnline()) {
                            player.sendMessage(TextFormat.colorize("&cYou cannot expel him because he is no longer in the party."));
                            return;
                        }

                        try {
                            member.getPlayer().sendMessage(TextFormat.colorize("&cYou have been kicked out of the party"));
                        } catch (Exception ignored) {}
                        player.sendMessage(TextFormat.colorize("&aYou have expelled " + member.getName()));
                        party.removeMember(member);
                    }
                });

        List<Profile> members = party.getMembers().stream().filter(member -> party.getOwner() != null && !member.getIdentifier().equals(party.getOwner().getIdentifier())).toList();

        members.forEach(s -> form.addButton(s.getIdentifier(), s.getName()));

        form.sendTo(player);
    }

    private void sendPromoteForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }
        Party party = profile.getProfileData().getParty();

        if (party == null) return;

        SimpleWindowForm form = FormAPI.simpleWindowForm("party.promote", "Party Promote")
                .addHandler(h -> {
                    if (!h.isFormValid("party.promote")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        Profile member = party.getMemberById(button.getName());

                        if (member == null) return;

                        if (!party.isInLobby()) {
                            player.sendMessage(TextFormat.colorize("&cYou can't promote because there is a game."));
                            return;
                        }

                        if (!party.isMember(member) || !member.isOnline()) {
                            player.sendMessage(TextFormat.colorize("&cYou cannot promote him because he is no longer in the party."));
                            return;
                        }

                        party.broadcast("&a" + member.getName() + " Now own the party");
                        player.sendMessage(TextFormat.colorize("&aYou already promoted the owner to " + member.getName()));
                        party.promoteOwner(member);
                    }
                });

        List<Profile> members = party.getMembers().stream().filter(member -> party.getOwner() != null && !member.getIdentifier().equals(party.getOwner().getIdentifier())).toList();

        members.forEach(s -> form.addButton(s.getIdentifier(), s.getName()));

        form.sendTo(player);
    }

    private void sendInviteForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        Party party = profile.getProfileData().getParty();

        if (party == null) return;

        SimpleWindowForm form = FormAPI.simpleWindowForm("party.invite", "Party Invite")
                .addHandler(h -> {
                    if (!h.isFormValid("party.invite")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        Profile playerInvited = ProfileManager.getInstance().get(button.getName());

                        if (playerInvited == null) return;

                        if (!party.isInLobby()) {
                            player.sendMessage(TextFormat.colorize("&cYou can't invite because there is a game."));
                            return;
                        }

                        if (!playerInvited.isOnline() || playerInvited.getProfileData().getParty() != null) {
                            player.sendMessage(TextFormat.colorize("&cThe invitation was not completed."));
                            return;
                        }

                        if (playerInvited.getSettingsData().isEnabled(SettingType.NO_PARTY_INVITATIONS.toString())) {
                            player.sendMessage(TextFormat.colorize("&cThis player does not accept invitations."));
                            return;
                        }

                        party.getRequestData().addInviteRequest(playerInvited);
                    }
                });

        List<Profile> players = ProfileManager.getInstance().getProfiles().values().stream().filter(s -> s.getProfileData().getParty() == null).toList();

        players.forEach(s -> form.addButton(s.getIdentifier(), s.getName()));

        form.sendTo(player);
    }
}
