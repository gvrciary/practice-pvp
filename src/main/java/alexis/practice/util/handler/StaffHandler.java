package alexis.practice.util.handler;

import alexis.practice.Practice;
import alexis.practice.profile.Profile;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static cn.nukkit.entity.Entity.DATA_FLAGS;

public class StaffHandler {
    @Getter
    private final static StaffHandler instance = new StaffHandler();
    private final Map<String, StaffHandler.Data> staff = new HashMap<>();

    public StaffHandler.Data get(Profile profile) {
        return staff.get(profile.getIdentifier());
    }

    public void add(Profile profile) {
        staff.put(profile.getIdentifier(), new Data(profile));
    }

    public void remove(Profile profile) {
        staff.remove(profile.getIdentifier());
    }

    public void sendMessage(String message) {
        Practice.getInstance().getServer().getOnlinePlayers().values().forEach(player -> {
            if (player.hasPermission("staffmode.permission")) {
                player.sendMessage(TextFormat.colorize(message));
            }
        });
    }

    @Getter
    public static class Data {
        private final Profile profile;

        @Nullable
        @Setter
        private Profile follow = null;
        private boolean vanish = true;
        private boolean chat = false;

        public Data(Profile profile) {
            this.profile = profile;

            try {
                profile.clear();
                PlayerUtil.getStaffKit(profile.getPlayer());
                profile.getPlayer().setGamemode(Player.CREATIVE);
            } catch (Exception ignored) {}
        }

        public boolean isFollowing() {
            return follow != null && follow.isOnline();
        }

        public void toggleVanish() {
            vanish = !vanish;

            update();
        }

        public void toggleChat() {
            chat = !chat;

            update();
        }

        public void tick() {
            if (vanish) {
                try {
                    Player player = profile.getPlayer();

                    player.getServer().getOnlinePlayers().values().forEach(p -> {
                        if (p.hasPermission("staffmode.permission")) {
                            p.showPlayer(player);
                        } else p.hidePlayer(player);
                    });
                } catch (Exception ignored) {}
            }

            if (follow != null) {
                try {
                    Player player = profile.getPlayer();

                    if (!follow.isOnline()) {
                        player.sendMessage(TextFormat.colorize("&c[Follow] &f" + follow.getName() + " &7has left"));
                        follow = null;
                        return;
                    }

                    Player followPlayer = follow.getPlayer();

                    if (!player.getLevel().equals(followPlayer.getLevel())) {
                        player.teleport(followPlayer.getPosition());
                    }
                } catch (Exception ignored) {}
            }
        }

        public void stop() {
            StaffHandler.getInstance().remove(profile);

            try {
                Player player = profile.getPlayer();

                profile.clear();
                player.setCanPickupXP(true);
                player.setDataFlag(DATA_FLAGS, Entity.DATA_FLAG_SILENT, false);

                PlayerUtil.getLobbyKit(player);
                player.getServer().getOnlinePlayers().values().forEach(p -> p.showPlayer(player));
            } catch (Exception ignored) {}
        }

        public void update() {
            try {
                Player player = profile.getPlayer();

                if (vanish) {
                    player.setDataFlag(DATA_FLAGS, Entity.DATA_FLAG_SILENT, true);
                    player.setCanPickupXP(false);
                }
                else player.getServer().getOnlinePlayers().values().forEach(p -> p.showPlayer(player));
            } catch (Exception ignored) {}
        }

    }
}
