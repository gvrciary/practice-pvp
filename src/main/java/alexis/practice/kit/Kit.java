package alexis.practice.kit;

import alexis.practice.profile.Profile;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Kit {
    @Getter
    private final String name;
    @Getter
    private final String customName;

    @Getter
    private final String icon;

    @Getter @Setter
    private float verticalKnockback;
    @Getter @Setter
    private float horizontalKnockback;
    @Getter @Setter
    private float maxHeight;
    @Getter @Setter
    private float limiter;
    @Getter @Setter
    private int attackCooldown;

    private final List<Effect> effects;
    private final Item[] armor;
    @Getter
    private final Map<Integer, Item> inventory;

    public Kit(String name, String customName, String icon, List<Effect> effects, Item[] armor, Map<Integer, Item> inventory, float verticalKnockback, float horizontalKnockback, float maxHeight, float limiter, int attackCooldown) {
        this.effects = effects;
        this.customName = customName;
        this.icon = icon;
        this.armor = armor;
        this.inventory = new TreeMap<>(inventory);
        this.name = name;
        this.horizontalKnockback = horizontalKnockback;
        this.verticalKnockback = verticalKnockback;
        this.maxHeight = maxHeight;
        this.attackCooldown = attackCooldown;
        this.limiter = limiter;
    }

    public boolean isNeedScoreTag() {
        return name.equals("finaluhc") || name.equals("builduhc") || name.equals("caveuhc");
    }

    public void giveKit(Profile profile) {
        if (!profile.isOnline()) return;

        try {
            Player player = profile.getPlayer();

            player.getInventory().setContents(profile.getKitData().getData(name).getInventory());
            player.getInventory().setArmorContents(armor);
            effects.forEach(effect -> player.addEffect(effect.setVisible(false)));

            if (isNeedScoreTag()) {
                player.setScoreTag(TextFormat.colorize("&f" + String.format("%.1f", (player.getHealth() + player.getAbsorption()))));
            }
        } catch (Exception ignored) {}
    }

}
