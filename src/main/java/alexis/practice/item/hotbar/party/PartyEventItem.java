package alexis.practice.item.hotbar.party;

import alexis.practice.duel.world.DuelWorld;
import alexis.practice.duel.world.DuelWorldManager;
import alexis.practice.item.object.ItemCustom;
import alexis.practice.party.Party;
import alexis.practice.party.games.PartyGamesManager;
import alexis.practice.party.games.event.ffa.PartyFFATypes;
import alexis.practice.party.games.event.split.PartySplitTypes;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.item.ItemID;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.Dropdown;
import com.denzelcode.form.element.ImageType;
import com.denzelcode.form.window.CustomWindowForm;
import com.denzelcode.form.window.SimpleWindowForm;

import java.util.Arrays;
import java.util.List;

public class PartyEventItem extends ItemCustom {

    public PartyEventItem() {
        super("&6Party Event", ItemID.GOLD_AXE, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null || profile.getProfileData().getParty() == null || !profile.getProfileData().getParty().isInLobby()) return;

        if (!profile.getProfileData().getParty().isOwner(profile)) {
            player.sendMessage(TextFormat.colorize("&cYou can't because you are not an owner"));
            return;
        }

        sendPartyEventForm(profile);
    }

    private void sendPartyEventForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        Party party = profile.getProfileData().getParty();

        if (party == null) return;

        FormAPI.simpleWindowForm("party.event", "Party Event")
                .addButton("party.split", "Party Split")
                .addButton("party.ffa", "Party FFA")
                .addHandler(h -> {
                    if (!h.isFormValid("party.event")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        switch (button.getName()) {
                            case "party.split" -> {
                                if (party.getMembers().size() <= 1){
                                    player.sendMessage(TextFormat.colorize("&cYou need more players"));
                                    return;
                                }

                                selectKitSplitForm(profile);
                            }
                            case "party.ffa" -> {
                                if (party.getMembers().size() <= 1){
                                    player.sendMessage(TextFormat.colorize("&cYou need more players"));
                                    return;
                                }

                                selectKitFFAForm(profile);
                            }
                        }
                    }
                })
                .sendTo(player);
    }

    private void selectKitSplitForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        Party party = profile.getProfileData().getParty();

        if (party == null) return;

        SimpleWindowForm form = FormAPI.simpleWindowForm("party.split", "Party Split")
                .addHandler(h -> {
                    if (!h.isFormValid("party.split")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        PartySplitTypes type = PartySplitTypes.get(button.getName());

                        if (type == null) return;

                        if (party.getMembers().size() <= 1) {
                            player.sendMessage(TextFormat.colorize("&cYou need more players"));
                            return;
                        }

                        finishForm(profile, type, type.getName());
                    }
                });

        Arrays.stream(PartySplitTypes.values()).forEach(type -> form.addButton(type.getName(), type.getCustomName(), ImageType.PATH, type.getType().getIcon()));

        form.sendTo(player);
    }

    private void selectKitFFAForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        Party party = profile.getProfileData().getParty();

        if (party == null) return;

        SimpleWindowForm form = FormAPI.simpleWindowForm("party.ffa", "Party FFA")
                .addHandler(h -> {
                    if (!h.isFormValid("party.ffa")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        PartyFFATypes type = PartyFFATypes.get(button.getName());

                        if (type == null) return;

                        if (party.getMembers().size() <= 1) {
                            player.sendMessage(TextFormat.colorize("&cYou need more players"));
                            return;
                        }

                       finishForm(profile, type, type.getName());
                    }
                });

        Arrays.stream(PartyFFATypes.values()).forEach(type -> form.addButton(type.getName(), type.getCustomName(), ImageType.PATH,type.getType().getIcon()));

        form.sendTo(player);
    }

    private void finishForm(Profile profile, Object type, String name) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        Party party = profile.getProfileData().getParty();

        if (party == null) return;

        if (!player.hasPermission("selector.permission")) {
            PartyGamesManager.getInstance().createPartyDuel(party, null, type, null);
            return;
        }

        List<String> worlds = DuelWorldManager.getInstance().getWorldsByType(name).stream()
                .map(DuelWorld::getName)
                .toList();

        if (worlds.isEmpty()) {
            player.sendMessage(TextFormat.colorize("&cNo duel available"));
            return;
        }

        CustomWindowForm form = FormAPI.customWindowForm("party.selector", "Party Map Selector")
                .addHandler(h -> {
                    if (!h.isFormValid("party.selector")) return;

                    if (party.getMembers().size() <= 1) {
                        player.sendMessage(TextFormat.colorize("&cYou need more players"));
                        return;
                    }

                    Dropdown mapSelector = h.getForm().getElement("map_selector");

                    DuelWorld world = null;

                    if (mapSelector != null) {
                        world = DuelWorldManager.getInstance().getWorld(mapSelector.getName());
                    }

                    PartyGamesManager.getInstance().createPartyDuel(party, null, type, world);
                });

        form.addDropdown("map_selector", "Map selector: ", worlds);

        form.sendTo(player);
    }

}
