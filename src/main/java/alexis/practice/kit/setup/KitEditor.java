package alexis.practice.kit.setup;

import alexis.practice.kit.Kit;
import alexis.practice.kit.KitLoadout;
import alexis.practice.kit.KitManager;
import alexis.practice.profile.Profile;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.ImageType;
import com.denzelcode.form.window.SimpleWindowForm;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class KitEditor {

    @Getter
    private final static KitEditor instance = new KitEditor();
    private final Map<String, KitEditor.Setup> setup = new HashMap<>();

    public KitEditor.Setup get(Profile profile) {
        return setup.get(profile.getIdentifier());
    }

    public void sendKitForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("kit_editor", "Kit Editor")
                .addHandler(h -> {
                    if (!h.isFormValid("kit_editor")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        Kit kit = KitManager.getInstance().getKit(button.getName());

                        if (kit != null) {
                            KitLoadout.getInstance().getLoadInventory(profile, kit);

                            setup.putIfAbsent(profile.getIdentifier(), new KitEditor.Setup(profile, kit));
                        }
                    }
                });

        KitManager.getInstance().getKits().values().forEach(kit -> form.addButton(kit.getName(), kit.getCustomName() + "\nClick to Select", ImageType.PATH, kit.getIcon()));

        form.sendTo(player);
    }

    public void remove(Profile profile) {
        setup.remove(profile.getIdentifier());
    }

    @Getter
    public class Setup {
        private final Profile profile;
        private final Kit kit;

        public Setup(Profile profile, Kit kit) {
            this.profile = profile;
            this.kit = kit;

            init();
        }

        public void init() {
            try {
                profile.clear();
                Player player = profile.getPlayer();

                player.getInventory().setContents(profile.getKitData().getData(kit.getName()).getInventory());
                player.getInventory().setArmorContents(new Item[]{});
                sendMessages();
            } catch (Exception ignored) {}
        }

        public void save() {
            KitLoadout.getInstance().saveLoad(profile, kit);

            destroy();
        }

        public void sendMessages() {
            if (!profile.isOnline()) return;

            try {
                Player player = profile.getPlayer();

                player.sendMessage(TextFormat.colorize("&7 - To confirm changes, write &a\"save\"&7 in the chat"));
                player.sendMessage(TextFormat.colorize("&7 - To cancel changes, write &c\"cancel\"&7 in the chat"));
                player.sendMessage(TextFormat.colorize("&7 - To reset kit, write &c\"reset\"&7 in the chat"));
            } catch (Exception ignored) {}

        }

        public void destroy() {
            remove(profile);

            profile.clear();

            try {
                Player player = profile.getPlayer();

                player.sendMessage(TextFormat.colorize("&aYou have finished the kit setup!"));
                PlayerUtil.getLobbyKit(player);
            } catch (Exception ignored) {}
        }

    }

}
