package alexis.practice.event.games.types.meetup.scenarios;

import alexis.practice.event.games.EventState;
import alexis.practice.event.games.types.meetup.Meetup;
import alexis.practice.event.games.types.meetup.scenarios.defaults.*;
import alexis.practice.util.Utils;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import me.iwareq.fakeinventories.FakeInventory;

import java.util.HashMap;
import java.util.Map;

public class ScenarioManager {
    public static final String TIMEBOMB = "Timebomb";
    public static final String WEBCAGE = "WebCage";
    public static final String SIPHON = "Siphon";
    public static final String FIRELESS = "Fireless";
    public static final String BOWLESS = "Bowless";
    public static final String RODLESS = "Rodless";
    public static final String DONOTDISTURB = "DoNotDisturb";
    public static final String SAFELOOT = "SafeLoot";
    public static final String NOCLEAN = "No Clean";

    @Getter
    private final Map<String, Scenario> scenarios = new HashMap<>();

    private final FakeInventory menu = new FakeInventory(InventoryType.CHEST, TextFormat.colorize("&r&6Scenarios Menu"));

    public ScenarioManager(Meetup eventArena) {
        scenarios.put(TIMEBOMB, new Timebomb(eventArena, TIMEBOMB, "When a player dies their\n things appear in a chest", Block.get(BlockID.CHEST).toItem(), false));
        scenarios.put(WEBCAGE, new WebCage(eventArena, WEBCAGE, "When a player dies, a sphere of\n cobwebs will spawn around them", Block.get(BlockID.COBWEB).toItem(), false));
        scenarios.put(DONOTDISTURB, new DoNotDisturb(eventArena, DONOTDISTURB, "No player or team intervenes\n in your fight", Item.get(ItemID.DIAMOND_SWORD), false));
        scenarios.put(NOCLEAN, new NoClean(eventArena, NOCLEAN, "When you kill a player, you will\n receive 30 seconds of invulnerability", Item.get(ItemID.TOTEM), false));
        scenarios.put(SIPHON, new Siphon(eventArena, SIPHON, "Eliminating a player gives the killer\n 4 hearts of Absorption, Speed I, and Strength I for 15 seconds", Item.get(ItemID.ARROW), false));
        scenarios.put(FIRELESS, new Scenario(eventArena, FIRELESS, "There will be no damage to the fire", Item.get(ItemID.BUCKET, 8), false));
        scenarios.put(BOWLESS, new Scenario(eventArena, BOWLESS, "The bow will not be allowed", Item.get(ItemID.BOW), false));
        scenarios.put(RODLESS, new Scenario(eventArena, RODLESS, "The fishing rod will not be allowed", Item.get(ItemID.FISHING_ROD), false));
        scenarios.put(SAFELOOT, new SafeLoot(eventArena, SAFELOOT, "Only the murderer can open the\n chest of his victim", Item.get(ItemID.FLINT), false));
    }

    public Scenario get(String name) {
        return scenarios.get(name);
    }

    public void destroy() {
        scenarios.forEach((s, scenario) -> scenario.destroy());
    }

    public FakeInventory getScenariosMenu() {
        menu.setContents(Utils.getDecorationLow());

        scenarios.values().forEach(scenario -> {
            Item scenarioItem = scenario.getItem().setLore(TextFormat.colorize("\n&r&7" + scenario.getDescription() + "\n\n&r&7Status:&f " + (scenario.isEnabled() ? "Enabled" : "Disabled")));
            menu.setItem(menu.firstEmpty(scenarioItem), scenarioItem, (item, event) -> {
                event.setCancelled();

                if (!scenario.eventArena.getCurrentState().equals(EventState.WAITING)) {
                    return;
                }

                scenario.toggle();
                menu.setContents(getScenariosMenu().getContents());
            });
        });

        menu.setDefaultItemHandler((item, event) -> event.setCancelled());

        return menu;
    }

}
