package alexis.practice.util.server;

import alexis.practice.Practice;
import alexis.practice.util.CustomMechanics;
import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.ImageType;
import com.denzelcode.form.window.SimpleWindowForm;
import lombok.Getter;

@Getter
public class Mechanics {
    @Getter
    private static final Mechanics instance = new Mechanics();

    private CustomMechanics enderPearlMechanics;
    private CustomMechanics splashPotionMechanics;
    private CustomMechanics fishingHookMechanics;

    public void sendMechanicsForm(Player player) {
        SimpleWindowForm form = FormAPI.simpleWindowForm("mechanics", "Mechanics")
                .addButton("ender", "Ender Pearl", ImageType.PATH, "textures/items/ender_pearl.png")
                .addButton("splash", "Splash Potion", ImageType.PATH, "textures/items/potion_bottle_splash_heal.png")
                .addButton("fishing", "Fishing Hook", ImageType.PATH, "textures/items/fishing_rod_uncast.png")
                .addHandler(h -> {
                    if (!h.isFormValid("mechanics")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        switch (button.getName()) {
                            case "ender" -> enderPearlMechanics.sendForm(player);
                            case "splash" -> splashPotionMechanics.sendForm(player);
                            case "fishing" -> fishingHookMechanics.sendForm(player);
                        }
                    }
                });

        form.sendTo(player);
    }

    public void load() {
        ConfigSection mechanics = Practice.getInstance().getConfig().getSection("mechanics");

        if (mechanics.isEmpty()) {
            throw new IllegalStateException("Mechanics: Some of the necessary sections do not exist");
        }

        ConfigSection ender = mechanics.getSection("ender");
        ConfigSection splash = mechanics.getSection("splash");
        ConfigSection fishing = mechanics.getSection("fishing");

        float enderDrag = (float) ender.getDouble("drag", 0.0085);
        float enderGravity = (float) ender.getDouble("gravity", 0.065);
        enderPearlMechanics = new CustomMechanics("Ender Pearl", enderDrag, enderGravity);

        float splashDrag = (float) splash.getDouble("drag", 0.0025);
        float splashGravity = (float) splash.getDouble("gravity", 0.06);
        splashPotionMechanics = new CustomMechanics("Splash Potion", splashDrag, splashGravity);

        float fishingDrag = (float) fishing.getDouble("drag", 0.04);
        float fishingGravity = (float) fishing.getDouble("gravity", 0.1);
        fishingHookMechanics = new CustomMechanics("Fishing Hook", fishingDrag, fishingGravity);
    }

    public void save() {
        Config config = Practice.getInstance().getConfig();

        final ConfigSection mechanics = config.getSection("mechanics");

        ConfigSection ender = mechanics.getSection("ender");
        ConfigSection splash = mechanics.getSection("splash");
        ConfigSection fishing = mechanics.getSection("fishing");

        ender.set("drag", enderPearlMechanics.getDrag());
        ender.set("gravity", enderPearlMechanics.getGravity());

        splash.set("drag", splashPotionMechanics.getDrag());
        splash.set("gravity", splashPotionMechanics.getGravity());

        fishing.set("drag", fishingHookMechanics.getDrag());
        fishing.set("gravity", fishingHookMechanics.getGravity());

        config.set("mechanics", mechanics);

        Practice.getInstance().saveConfig();
    }

}
