package alexis.practice.util;

import alexis.practice.profile.Profile;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Utils {

    @Getter
    private static final Map<Integer, Item> decoration = new HashMap<>();
    @Getter
    private static final Map<Integer, Item> decorationLow = new HashMap<>();

    @Getter
    private static final List<String> scoreTitles = Arrays.asList(
            "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice",
            "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice", "&l&6Practice",
            "&l&6      P", "&l&6     Pr", "&l&6    Pra", "&l&6   Prac", "&l&6  Pract",  "&l&6 Practi", "&l&6Practic", "&l&6Practice", "&l&6Practice",
            "&l&6Practice", "&l&fPractice", "&l&6Practice", "&l&fPractice", "&l&6Practice"
    );

    @Getter
    private static final List<String> scoreInformation = Arrays.asList(
            "&r&7server.online",
            "&r&9dsc.gg/server"
    );

    static {
        final Item decorationItem = Item.get(Item.STAINED_GLASS_PANE, 4).setCustomName(TextFormat.colorize("&r&4Practice"));

        decoration.put(0, decorationItem);
        decoration.put(1, decorationItem);
        decoration.put(2, decorationItem);
        decoration.put(3, decorationItem);
        decoration.put(4, decorationItem);
        decoration.put(5, decorationItem);
        decoration.put(6, decorationItem);
        decoration.put(7, decorationItem);
        decoration.put(8, decorationItem);
        decoration.put(9, decorationItem);
        decoration.put(17, decorationItem);
        decoration.put(18, decorationItem);
        decoration.put(19, decorationItem);
        decoration.put(20, decorationItem);
        decoration.put(21, decorationItem);
        decoration.put(22, decorationItem);
        decoration.put(23, decorationItem);
        decoration.put(24, decorationItem);
        decoration.put(25, decorationItem);
        decoration.put(26, decorationItem);

        decorationLow.put(8, decorationItem);
        decorationLow.put(17, decorationItem);
        decorationLow.put(18, decorationItem);
        decorationLow.put(0, decorationItem);
        decorationLow.put(9, decorationItem);
        decorationLow.put(26, decorationItem);
    }

    public static int countItems(Collection<Item> items, int itemId) {
        return items.stream()
                .filter(item -> item.getId() == itemId)
                .mapToInt(Item::getCount)
                .sum();
    }

    public static Vector3 parseVector3(String str) {
        String[] parts = str.split(":");

        if (parts.length < 3) {
            throw new RuntimeException("Invalid vector data.");
        }

        return new Vector3(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    public static String repeat(final String str, final int count) {
        Objects.requireNonNull(str, "str");
        if (count < 0) {
            throw new IllegalArgumentException("count");
        }
        return str.repeat(count);
    }

    public static String formatTime(long time) {
        final long realTime = time / 1000;

        final int hours = (int) realTime / 3600;
        final int minutes = (int) (realTime % 3600) / 60;
        final int seconds = (int) realTime % 60;

        if (realTime < 60) {
            return String.format("%.1f", (double) time / 1000) + "s";
        } else if (hours <= 0) {
            return String.format("%02d:%02d", minutes, seconds);
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String formatTime(int time) {
        final int hours = time / 3600;
        final int minutes = (time % 3600) / 60;
        final int seconds = time % 60;

        if (hours <= 0) {
            return String.format("%02d:%02d", minutes, seconds);
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String intToRoman(int num) {
        if (num < 1 || num > 3999) {
            throw new IllegalArgumentException("index out");
        }

        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D",
                "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L",
                "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V",
                "VI", "VII", "VIII", "IX"};

        return thousands[num / 1000] +
                hundreds[(num % 1000) / 100] +
                tens[(num % 100) / 10] +
                ones[num % 10];
    }

    public static int randomInteger(int min, int max) {
        return (int) Math.floor(Math.random() * (max - min + 1) + min);
    }

    public static String cleanString(String input) {
        return input.replaceAll("&[a-zA-Z0-9]", "");
    }

    public static void deleteSource(File source) {
        if (source.isDirectory()) {
            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteSource(file);
                }
            }
        }

        if (!source.delete()) {
            System.out.println("Failed to delete: " + source.getAbsolutePath());
        }
    }

    public static CompoundTag createCompoundTag(Profile profile) {
        CompoundTag nbt = null;

        try {
            Player player = profile.getPlayer();

            nbt = createCompoundTag(player)
                    .putBoolean("Invulnerable", true)
                    .putString("NameTag", profile.getName())
                    .putList(new ListTag<StringTag>("Commands"))
                    .putList(new ListTag<StringTag>("PlayerCommands"))
                    .putBoolean("npc", true)
                    .putFloat("scale", 1);
            CompoundTag skinTag = new CompoundTag()
                    .putByteArray("Data", player.getSkin().getSkinData().data)
                    .putInt("SkinImageWidth", player.getSkin().getSkinData().width)
                    .putInt("SkinImageHeight", player.getSkin().getSkinData().height)
                    .putString("ModelId", player.getSkin().getSkinId())
                    .putString("CapeId", player.getSkin().getCapeId())
                    .putByteArray("CapeData", player.getSkin().getCapeData().data)
                    .putInt("CapeImageWidth", player.getSkin().getCapeData().width)
                    .putInt("CapeImageHeight", player.getSkin().getCapeData().height)
                    .putByteArray("SkinResourcePatch", player.getSkin().getSkinResourcePatch().getBytes(StandardCharsets.UTF_8))
                    .putByteArray("GeometryData", player.getSkin().getGeometryData().getBytes(StandardCharsets.UTF_8))
                    .putByteArray("AnimationData", player.getSkin().getAnimationData().getBytes(StandardCharsets.UTF_8))
                    .putBoolean("PremiumSkin", player.getSkin().isPremium())
                    .putBoolean("PersonaSkin", player.getSkin().isPersona())
                    .putBoolean("CapeOnClassicSkin", player.getSkin().isCapeOnClassic());
            nbt.putCompound("Skin", skinTag);
            nbt.putBoolean("ishuman", true);

        } catch (Exception ignored) {}

        return nbt;
    }

    public static CompoundTag createCompoundTag(Player player) {
        return new CompoundTag()
                .putList(new ListTag<>("Pos")
                        .add(new DoubleTag("", player.x))
                        .add(new DoubleTag("", player.y))
                        .add(new DoubleTag("", player.z)))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("", (float) player.getYaw()))
                        .add(new FloatTag("", (float) player.getPitch())));
    }

}
