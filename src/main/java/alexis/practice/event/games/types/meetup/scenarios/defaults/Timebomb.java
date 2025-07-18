package alexis.practice.event.games.types.meetup.scenarios.defaults;

import alexis.practice.Practice;
import alexis.practice.event.games.types.meetup.Meetup;
import alexis.practice.event.games.types.meetup.scenarios.Scenario;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockChest;
import cn.nukkit.block.BlockEntityHolder;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.item.Item;
import cn.nukkit.level.Explosion;
import cn.nukkit.level.Position;
import cn.nukkit.scheduler.Task;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Timebomb extends Scenario {

    private final List<TaskHandler> tasks = new ArrayList<>();

    public Timebomb(Meetup eventArena, String name, String description, Item item, boolean enabled) {
        super(eventArena, name, description, item, enabled);
    }

    public void destroy() {
        tasks.forEach(TaskHandler::cancel);
    }

    public void add(Player player) {
        Item[] drops = player.getInventory().getContents().values().toArray(new Item[0]);
        Item[] newDrops = Arrays.copyOf(drops, drops.length + 3);
        newDrops[drops.length-2] = Item.get(Item.GOLDEN_APPLE, 10, 1).setCustomName(TextFormat.colorize("&r&6Golden Head"));
        newDrops[drops.length-1] = Block.get(BlockID.ANVIL).toItem();
        Item experience = Item.get(Item.EXPERIENCE_BOTTLE);
        experience.setCount(16);
        newDrops[drops.length] = experience;

        Position position = player.getPosition().clone().floor();

        BlockChest firstChest = new BlockChest();
        firstChest.position(position);
        BlockChest secondChest = new BlockChest();
        secondChest.position(position.add(-1));

        BlockEntityChest firstEntity = BlockEntityHolder.setBlockAndCreateEntity(firstChest);
        BlockEntityChest secondEntity = BlockEntityHolder.setBlockAndCreateEntity(secondChest);

        if (firstEntity != null && secondEntity != null) {
            firstEntity.setName(TextFormat.colorize("&6" + player.getName() + " Corpse"));
            secondEntity.setName(TextFormat.colorize("&6" + player.getName() + " Corpse"));

            firstEntity.pairWith(secondEntity);
            secondEntity.pairWith(firstEntity);

            firstEntity.getInventory().addItem(newDrops);
        }

        player.getLevel().dropExpOrb(player.getLocation(), player.getExperience());
        tasks.add(Practice.getInstance().getServer().getScheduler().scheduleRepeatingTask(Practice.getInstance(), new TimebombTask(player.getLocation().clone()), 20, true));
    }

    public static class TimebombTask extends Task {
        private final Position position;
        private final long countdown;

        TimebombTask(Position position) {
            this.position = position.floor();
            countdown = System.currentTimeMillis() + 20 * 1000L;
        }

        @Override
        public void onRun(int i) {
            if (countdown <= System.currentTimeMillis()) {
                position.getLevelBlock().getLevel().setBlock(position, Block.get(BlockID.AIR));

                Explosion explosion = new Explosion(position, 3D, position.getLevelBlock());
                explosion.explodeA();
                explosion.explodeB();

                Practice.getInstance().getServer().getScheduler().cancelTask(this.getTaskId());
            }
        }
    }

}
