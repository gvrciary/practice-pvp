package alexis.practice.kit;

import alexis.practice.Practice;
import alexis.practice.util.SerializerUtil;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class KitManager {
    @Getter
    private static final KitManager instance = new KitManager();
    @Getter
    private final Map<String, Kit> kits = new HashMap<>();

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public void load() {
        final Config kitConfig = new Config(Practice.getInstance().getDataFolder().toString() + File.separator + "kits.yml", Config.YAML);

        kitConfig.getSections().getKeys(false).forEach(key ->
                kits.put(key, SerializerUtil.parseKit(key, kitConfig.getSection(key)))
        );
    }

    public void save() {
        final Config kitConfig = new Config(Practice.getInstance().getDataFolder().toString() + File.separator + "kits.yml", Config.YAML);

        kitConfig.getSections().getKeys(false).forEach(key -> {
            Kit kit = kits.get(key);

            ConfigSection knockbackSection =  kitConfig.getSections(key).getSection("knockback");

            knockbackSection.set("vertical", kit.getVerticalKnockback());
            knockbackSection.set("horizontal", kit.getHorizontalKnockback());
            knockbackSection.set("max-height", kit.getMaxHeight());
            knockbackSection.set("attack-cooldown", kit.getAttackCooldown());
        });

        kitConfig.save();
    }
}

