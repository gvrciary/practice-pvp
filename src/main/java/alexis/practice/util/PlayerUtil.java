package alexis.practice.util;

import alexis.practice.division.DivisionManager;
import alexis.practice.duel.DuelType;
import alexis.practice.event.games.EventType;
import alexis.practice.item.HotbarItem;
import alexis.practice.profile.Profile;
import alexis.practice.profile.data.StatisticsData;
import alexis.practice.queue.QueueManager;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Attribute;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.window.SimpleWindowForm;
import me.iwareq.fakeinventories.FakeInventory;

import java.util.*;
import java.util.stream.Collectors;

public final class PlayerUtil {

    private static final Item FREEZE = Block.get(Block.ICE).toItem().setCustomName(TextFormat.colorize("&r&6Freeze"));
    private static final Item INFO = Item.get(ItemID.BOOK).setCustomName(TextFormat.colorize("&r&6Info"));

    static {
        FREEZE.setNamedTag(FREEZE.getNamedTag().putString("staff_item", "freeze"));
        INFO.setNamedTag(INFO.getNamedTag().putString("staff_item", "info"));
    }

    public static void getLobbyKit(Player player) {
        getLobbyKit(player, true);
    }

    public static void getLobbyKit(Player player, boolean teleport) {
        if (!player.isOnline()) return;

        if (teleport) player.teleport(player.getServer().getDefaultLevel().getSpawnLocation());
        player.getInventory().setItem(0, HotbarItem.DUELS.getItem());
        player.getInventory().setItem(1, HotbarItem.ARENAS.getItem());
        player.getInventory().setItem(2, HotbarItem.KIT_EDITOR.getItem());
        player.getInventory().setItem(4, HotbarItem.PARTY.getItem());
        player.getInventory().setItem(5, HotbarItem.EVENT.getItem());
        player.getInventory().setItem(7, HotbarItem.COSMETICS.getItem());
        player.getInventory().setItem(8, HotbarItem.PROFILE.getItem());
    }

    public static void getPartyKit(Player player, boolean isOwner) {
        if (!player.isOnline()) return;

        if (isOwner) {
            player.getInventory().setItem(2, HotbarItem.PARTY_REQUEST.getItem());
            player.getInventory().setItem(3, HotbarItem.PARTY_EVENT.getItem());
            player.getInventory().setItem(6, HotbarItem.PARTY_MANAGEMENT.getItem());
        }

        player.getInventory().setItem(0, HotbarItem.PARTY_INFO.getItem());
        player.getInventory().setItem(8, HotbarItem.PARTY_LEAVE.getItem());
    }

    public static void getStaffKit(Player player) {
        if (!player.isOnline()) return;

        player.getInventory().setItem(0, HotbarItem.VANISH.getItem());
        player.getInventory().setItem(1, HotbarItem.CHAT.getItem());
        player.getInventory().setItem(3, HotbarItem.TELEPORT_RANDOM.getItem());
        player.getInventory().setItem(4, FREEZE);
        player.getInventory().setItem(5, INFO);
        player.getInventory().setItem(8, HotbarItem.LEAVE_MODE.getItem());
    }

    public static void getPartyKit(Player player) {
        getPartyKit(player, true);
    }

    public static FakeInventory getStatsMenu(Profile profile) {
        FakeInventory menu = new FakeInventory(InventoryType.CHEST, TextFormat.colorize("&6" + profile.getName() + "'s Statistics"));

        menu.setContents(Utils.getDecoration());

        StatisticsData data = profile.getStatisticsData();
        String rankedInfo = Arrays.stream(DuelType.values())
                .map(stats -> "&r&f" + stats.getCustomName() + ":&f " + DivisionManager.getInstance().getDivisionByElo(data.getElo(stats.getName())).getRankFormat() + " &7(" + data.getElo(stats.getName()) + ")")
                .collect(Collectors.joining("\n"));

        String eventsInfo = Arrays.stream(EventType.values())
                .map(eventType -> "&r&7" + eventType.getName() + " Wins:&6 " + profile.getStatisticsData().getEventWin(eventType.getName()))
                .collect(Collectors.joining("\n"));

        final Item matchesItem = Item.get(ItemID.SKULL, 3).setCustomName(TextFormat.colorize("&r&6Matches")).setLore(TextFormat.colorize("\n&r&7Matches: &6" + data.getMatches() + "\n&r&7Wins: &6" + data.getWins() + "\n&r&7Lost: &6" + data.getMatchesLost() + "\n\n&r&7M/W: &6" + data.getMWRate() + "\n\n&r&7Win Streak: &6" + data.getWinStreak() +  "\n&r&7Highest Win Streak: &6" + data.getHighestWinStreak()));
        final Item skillsItem = Item.get(ItemID.DIAMOND_SWORD).setCustomName(TextFormat.colorize("&r&6Skills")).setLore(TextFormat.colorize("\n&r&7Kills: &6" + data.getKills() + "\n&r&7Deaths: &6" + data.getDeaths() + "\n&r&7K/D: &6" + data.getKDRate() + "\n\n&r&7KillStreak: &6" + data.getKillStreak() + "\n&r&7Highest Kill Streak: &6" + data.getHighestKillStreak()));
        final Item eventsItem = Item.get(ItemID.EMERALD, 3).setCustomName(TextFormat.colorize("&r&6Events")).setLore(TextFormat.colorize("\n&r&7Events: &6" + data.getEvents() + "\n&r&7Events Wins: &6" + data.getEventsWins() + "\n\n" + eventsInfo));
        final Item rankedItem = Item.get(ItemID.NETHER_STAR).setCustomName(TextFormat.colorize("&r&6Ranked")).setLore(TextFormat.colorize("\n" + rankedInfo));

        menu.setItem(10, matchesItem);
        menu.setItem(12, skillsItem);
        menu.setItem(14, eventsItem);
        menu.setItem(16, rankedItem);

        menu.setDefaultItemHandler((item, event) -> event.setCancelled());

        return menu;
    }

    public static void setNameTag(Profile profile) {
        try {
            Player player = profile.getPlayer();

            String color;
            if (player.hasPermission("owner.nametag")) color = "&4";
            else if (player.hasPermission("admin.nametag")) color = "&6";
            else if (player.hasPermission("helper.nametag")) color = "&2";
            else if (player.hasPermission("mod.nametag")) color = "&1";
            else if (player.hasPermission("coordinator.nametag")) color = "&5";
            else if (player.hasPermission("developer.nametag")) color = "&9";
            else if (player.hasPermission("media.nametag")) color = "&5";
            else if (player.hasPermission("famous.nametag")) color = "&d";
            else if (player.hasPermission("vip.nametag"))  color = "&e";
            else if (player.hasPermission("mvp.nametag")) color = "&b";
            else if (player.hasPermission("booster.nametag")) color = "&d";
            else if (player.hasPermission("discord.nametag")) color = "&5";
            else color = "&7";

            player.setNameTag(TextFormat.colorize(color + profile.getName()));
        } catch (Exception ignored) {}
    }

    public static void sendFinishForm(Profile profile, Profile target, DuelType type, boolean isRanked, String statistics, Map<Integer, Item> enemyInventory) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("post_duel", "Post Duel")
                .addButton("play_again", "Play Again")
                .addButton("view_inventory", "View Enemy Inventory")
                .addButton("request", "Request Duel")
                .addHandler(h -> {
                    if (!h.isFormValid("post_duel")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        switch (button.getName()) {
                            case "play_again" -> QueueManager.getInstance().createQueue(profile, isRanked, type);
                            case "view_inventory" -> {
                                FakeInventory menu = new FakeInventory(InventoryType.CHEST, TextFormat.colorize("&r&c" + target.getName() + " Inv"));
                                final Item nextInventories = Item.get(ItemID.BED).setCustomName(TextFormat.colorize("&r&cNext"));
                                final Item backInventories = Item.get(ItemID.REDSTONE).setCustomName(TextFormat.colorize("&r&cBack"));

                                Map<Integer, Item> page_One = new HashMap<>(enemyInventory.entrySet().stream()
                                        .filter(entry -> entry.getKey() < 18)
                                        .sorted(Comparator.comparingInt(Map.Entry::getKey))
                                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new)));

                                Map<Integer, Item> page_Two = new HashMap<>(enemyInventory.entrySet().stream()
                                        .filter(entry -> entry.getKey() > 18)
                                        .sorted(Comparator.comparingInt(Map.Entry::getKey))
                                        .collect(Collectors.toMap(entry -> entry.getKey() - 18, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new)));

                                menu.setContents(page_One);
                                menu.setItem(26, nextInventories);

                                menu.setDefaultItemHandler((item, event) -> {
                                    event.setCancelled();

                                    if (item.equals(nextInventories, false, false)) {
                                        menu.setContents(page_Two);
                                        menu.setItem(26, backInventories);
                                    } else if (item.equals(backInventories, false, false)) {
                                        menu.setContents(page_One);
                                        menu.setItem(26, nextInventories);
                                    }
                                });

                                player.addWindow(menu);
                            }
                            case "request" -> {
                                if (!target.isOnline() || !target.getProfileData().isInLobby() || target.getProfileData().getQueue() != null) {
                                    player.sendMessage(TextFormat.colorize("&cCould not complete the duel"));
                                    return;
                                }

                                target.addDuelRequest(target, type, 1, null);
                            }
                        }
                    }
                });
        form.setContent(TextFormat.colorize(statistics));
        form.sendTo(player);
    }

    public static void playSound(Player player, String id) {
        PlaySoundPacket playSoundPacket = new PlaySoundPacket();
        playSoundPacket.name = id;
        playSoundPacket.volume = 1;
        playSoundPacket.pitch = 1;
        playSoundPacket.x = (int) player.x;
        playSoundPacket.y = (int) player.y;
        playSoundPacket.z = (int) player.z;

        player.dataPacket(playSoundPacket);
    }

    public static void setExperience(Player player, float level, float progress, int duration) {
        if (player.spawned) {
            player.setAttribute(Attribute.getAttribute(9).setValue(level));
            player.setAttribute(Attribute.getAttribute(10).setValue(Math.max(0.0F, Math.min(1.0F, progress / duration))));
        }
    }

}
