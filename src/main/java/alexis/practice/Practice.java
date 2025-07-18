package alexis.practice;

import alexis.practice.arena.ArenaManager;
import alexis.practice.command.*;
import alexis.practice.division.DivisionManager;
import alexis.practice.duel.world.DuelWorldManager;
import alexis.practice.entity.*;
import alexis.practice.event.world.EventWorldManager;
import alexis.practice.kit.KitLoadout;
import alexis.practice.kit.KitManager;
import alexis.practice.listener.*;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.queue.QueueManager;
import alexis.practice.storage.SQLStorage;
import alexis.practice.util.handler.HologramsHandler;
import alexis.practice.util.server.Mechanics;
import alexis.practice.util.server.ServerEssential;
import cn.nukkit.command.Command;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.Listener;
import cn.nukkit.item.food.Food;
import cn.nukkit.item.food.FoodEffective;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.potion.Effect;
import lombok.Getter;

import java.util.Arrays;

public class Practice extends PluginBase {

    @Getter
    private static Practice instance;

    @Override
    public void onLoad() {
        instance = this;

        saveDefaultConfig();
        saveResource("kits.yml");
    }

    @Override
    public void onEnable() {
        unregisterCommands();

        registerListeners();
        registerCommands();
        registerFoods();
        registerEntities();

        KitManager.getInstance().load();
        SQLStorage.getInstance().load();
        Mechanics.getInstance().load();
        KitLoadout.getInstance().load();
        HologramsHandler.getInstance().load();

        DuelWorldManager.getInstance().load();
        ArenaManager.getInstance().load();
        EventWorldManager.getInstance().load();

        DivisionManager.getInstance().load();
        ServerEssential.getInstance().load();

        registerThreads();
    }

    @Override
    public void onDisable() {
        KitManager.getInstance().save();
        KitLoadout.getInstance().save();
        Mechanics.getInstance().save();
        HologramsHandler.getInstance().save();

        DuelWorldManager.getInstance().save();
        ArenaManager.getInstance().save();
        EventWorldManager.getInstance().save();
    }

    private void registerListeners() {
        final Listener[] listeners = {
                new ProfileListener(),
                new DuelListener(),
                new ArenaListener(),
                new PartyListener(),
                new EventListener()
        };

        Arrays.stream(listeners).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
    }

    private void registerEntities() {
        Entity.registerEntity("ThrownPotion", SplashPotionEntity.class, true);
        Entity.registerEntity("Hologram", HologramEntity.class, true);
        Entity.registerEntity("EnderPearl", EnderPearlEntity.class, true);
        Entity.registerEntity("DeathAnimation", DeathAnimation.class, true);
        Entity.registerEntity("FishingHook", FishingHookEntity.class, true);
        Entity.registerEntity("Arrow", ArrowEntity.class, true);
        Entity.registerEntity("Fireball", FireballEntity.class, true);
        Entity.registerEntity("TNT", TntEntity.class, true);
    }

    private void registerThreads() {
        final Thread profileThread = new Thread(() -> {
            while (Thread.currentThread().isAlive()) {
                try {
                    ProfileManager.getInstance().getProfiles().values().forEach(Profile::tick);
                    Thread.sleep(1000);
                } catch (InterruptedException exception) {
                    System.out.println("Profile Thread Error: " + exception.getMessage());
                }
            }
        });

        final Thread visualThread = new Thread(() -> {
            while (Thread.currentThread().isAlive()) {
                try {
                    ProfileManager.getInstance().getProfiles().values().forEach(profile -> {
                        profile.getScoreboard().update();
                        profile.getCacheData().update();
                    });
                    Thread.sleep(100);
                } catch (InterruptedException exception) {
                    System.out.println("Visual Thread Error: " + exception.getMessage());
                }
            }
        });

        final Thread queueThread = new Thread(() -> {
            while (Thread.currentThread().isAlive()) {
                try {
                    QueueManager.getInstance().tick();
                    Thread.sleep(500);
                } catch (InterruptedException exception) {
                    System.out.println("Queue Thread Error: " + exception.getMessage());
                }
            }
        });

        profileThread.start();
        visualThread.start();
        queueThread.start();
    }

    private void unregisterCommands() {
        final String[] commands = {"kill", "version", "plugins", "me", "stop"};

        getServer().getCommandMap().getCommands().entrySet().removeIf(entry ->
                Arrays.asList(commands).contains(entry.getValue().getName().toLowerCase())
        );
    }

    public void registerCommands() {
        final Command[] commands = {
                new PingCommand(),
                new SetupCommand(),
                new DuelCommand(),
                new HubCommand(),
                new DivisionCommand(),
                new HostCommand(),
                new StatsCommand(),
                new ResponseCommand(),
                new StaffCommand(),
                new SpectateCommand(),
                new HologramCommand(),
                new RekitCommand(),
                new LeaderboardCommand(),
                new StopCommand(),
                new DisguiseCommand(),
                new GlobalMuteCommand()
        };

        Arrays.stream(commands).forEach(command -> getServer().getCommandMap().register(getName(), command));
    }

    private void registerFoods() {
        Food.registerFood(new FoodEffective(4, 9.6F)
                        .addEffect(Effect.getEffect(Effect.REGENERATION).setAmplifier(1).setDuration(9 * 20))
                        .addEffect(Effect.getEffect(Effect.ABSORPTION).setDuration(120 * 20))
                        .addRelative(322, 10)
                , this);
    }

}
