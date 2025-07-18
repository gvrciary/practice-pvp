package alexis.practice.event.games.types.meetup.scenarios.defaults;

import alexis.practice.event.games.types.meetup.Meetup;
import alexis.practice.event.games.types.meetup.scenarios.Scenario;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;

public class WebCage extends Scenario {
    private static final int RADIUS = 4;
    private static final int HEIGHT = 6;

    public WebCage(Meetup eventArena, String name, String description, Item item, boolean enabled) {
        super(eventArena, name, description, item, enabled);
    }

    public void add(Player player) {
        Level world = player.getLevel();
        Location location = player.getLocation();

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = 0; y <= HEIGHT; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    if ((x * x + y * y + z * z <= RADIUS * RADIUS) && !(x >= -2 && x <= 2 && y <= 1 && z >= -2 && z <= 2)) {
                        Block webBlock = Block.get(Block.COBWEB);
                        world.setBlock(location.add(x, y, z), webBlock, true, true);
                    }
                }
            }
        }
    }
}
