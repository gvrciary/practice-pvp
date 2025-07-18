package alexis.practice.profile;

import alexis.practice.Practice;
import alexis.practice.arena.Arena;
import alexis.practice.arena.world.ArenaWorldSetup;
import alexis.practice.duel.Duel;
import alexis.practice.duel.Duel2vs2;
import alexis.practice.duel.DuelState;
import alexis.practice.duel.DuelType;
import alexis.practice.duel.world.DuelWorld;
import alexis.practice.duel.world.DuelWorldSetup;
import alexis.practice.entity.DeathAnimation;
import alexis.practice.entity.FishingHookEntity;
import alexis.practice.event.games.EventArena;
import alexis.practice.event.world.EventWorldSetup;
import alexis.practice.kit.setup.KitEditor;
import alexis.practice.party.games.PartyGame;
import alexis.practice.profile.cache.DuelRequest;
import alexis.practice.profile.data.*;
import alexis.practice.profile.scoreboard.Scoreboard;
import alexis.practice.profile.settings.SettingType;
import alexis.practice.profile.settings.types.GameTime;
import alexis.practice.profile.settings.types.HiddenNonOpponents;
import alexis.practice.profile.settings.types.LobbyVisibility;
import alexis.practice.profile.settings.types.ScoreboardColor;
import alexis.practice.queue.QueueManager;
import alexis.practice.storage.SQLStorage;
import alexis.practice.util.PlayerUtil;
import alexis.practice.util.Utils;
import alexis.practice.util.handler.StaffHandler;
import alexis.practice.util.server.ServerEssential;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Profile {

    private final UUID uuid;
    private final String xuid;

    @Getter
    @Setter
    private String name;

    @Getter
    private final String realName;

    @Getter
    private final ProfileData profileData;
    @Getter
    private final StatisticsData statisticsData;
    @Getter
    private final SettingsData settingsData;
    @Getter
    private final KitData kitData;
    @Getter
    private final CosmeticData cosmeticData;
    @Getter
    private final Scoreboard scoreboard;
    @Getter
    private final CacheData cacheData;

    public final Map<Block, Position> glassCached = new HashMap<>();
    @Getter
    private final Map<String, DuelRequest> duelRequest = new HashMap<>();

    @Getter
    private FishingHookEntity fishing = null;

    @Getter
    @Setter
    private boolean initialKnockbackMotion = false;
    @Getter
    @Setter
    private boolean cancelKnockbackMotion = false;

    public Profile(Player player) {
        uuid = player.getUniqueId();
        xuid = player.getLoginChainData().getXUID();
        name = player.getName();
        realName = player.getName();

        profileData = new ProfileData(this);
        statisticsData = new StatisticsData(this);
        settingsData = new SettingsData(this);
        kitData = new KitData(this);

        cosmeticData = new CosmeticData(this);
        scoreboard = new Scoreboard(this);
        cacheData = new CacheData(this);

        join(player);
    }

    public String getIdentifier() {
        return xuid;
    }

    public Player getPlayer() throws Exception {
        Optional<Player> object = Practice.getInstance().getServer().getPlayer(uuid);

        if (object.isEmpty()) {
            throw new Exception("Player not found.");
        }

        return object.get();
    }

    public boolean isOnline() {
        try {
            return getPlayer().isOnline();
        } catch (Exception ignored) {
            return false;
        }
    }

    public void join(Player player) {
        final long time  = System.currentTimeMillis();

        player.teleport(player.getServer().getDefaultLevel().getSpawnLocation());
        clear();

        SQLStorage.getInstance().get(this, () -> {
            if (settingsData.isEnabled(SettingType.SCOREBOARD.toString())) scoreboard.spawn();

            if (!player.hasPermission("settings.permission")) {
                ScoreboardColor scoreboardColor = (ScoreboardColor) SettingType.SCOREBOARD_COLOR.getSetting();

                if (scoreboardColor.getCurrentColor(this) > 0) scoreboardColor.setCurrentColor(this, 0);

                GameTime gameTime = (GameTime) SettingType.GAME_TIME.getSetting();

                if (gameTime.getCurrentTime(this) > 0) gameTime.setCurrentTime(this, 0);

                if (settingsData.isEnabled(SettingType.DISGUISE.toString())) settingsData.toggleEnabled(SettingType.DISGUISE.toString());
            }

            if (!player.hasPermission("cosmetics.permission")) {
                if (cosmeticData.hasColorChat()) cosmeticData.clearColorChat();
                if (cosmeticData.hasJoinMessages()) cosmeticData.clearJoinMessages();
                if (cosmeticData.hasKillEffect()) cosmeticData.clearKillEffect();
                if (cosmeticData.hasKillMessage()) cosmeticData.clearKillMessage();
                if (cosmeticData.hasProjectileTrail()) cosmeticData.clearProjectileTrail();
            }

            PlayerUtil.getLobbyKit(player, false);
            PlayerUtil.playSound(player, "random.orb");

            AtomicReference<String> join = new AtomicReference<>("&6[+] &7" + name);

            if (cosmeticData.hasJoinMessages()) join.set(cosmeticData.getJoinMessages().getMessageFormat(name));

            ProfileManager.getInstance().getProfiles().values().forEach(profile -> {
                try {
                    Player p = profile.getPlayer();

                    if (profile.getSettingsData().isEnabled(SettingType.JOIN_QUIT_MESSAGES.toString()) || cosmeticData.hasJoinMessages()) p.sendMessage(TextFormat.colorize(join.get()));
                } catch (Exception ignored) {}
            });

            player.sendPopup(TextFormat.colorize("&aYour data has been loaded in " + Utils.formatTime(System.currentTimeMillis() - time)));

            String message = ServerEssential.getInstance().getJoinText();

            if (!message.isEmpty()) player.sendMessage(message);
        });
    }

    public void tick() {
        if (!isOnline()) return;

        try {
            Player player = getPlayer();

            if (profileData.inStaffMode()) StaffHandler.getInstance().get(this).tick();

            if (profileData.getArena() != null && settingsData.isEnabled(SettingType.HIDDEN_NON_OPPONENTS.toString())) {
                HiddenNonOpponents hiddenNonOpponents = (HiddenNonOpponents) SettingType.HIDDEN_NON_OPPONENTS.getSetting();
                Profile target = cacheData.getCombat().get();

                if (target != null && target.isOnline() && hiddenNonOpponents.isHidden(this)) {
                    player.getLevel().getPlayers().values().forEach(p -> {
                        try {
                            Player targetPlayer = target.getPlayer();

                            if (p.getId() != player.getId() && p.getId() != targetPlayer.getId()) {
                                player.hidePlayer(p);
                                player.showPlayer(targetPlayer);
                            }
                        } catch (Exception ignored) {}
                    });

                } else if (hiddenNonOpponents.isHidden(this)) {
                    hiddenNonOpponents.setHidden(this, false);

                    player.getLevel().getPlayers().values().forEach(player::showPlayer);
                    player.sendMessage(TextFormat.colorize("&aNow you can see the other players."));
                }
            }

            if (!settingsData.isEnabled(SettingType.LOBBY_VISIBILITY.toString())) {
                LobbyVisibility lobbyVisibility = (LobbyVisibility) SettingType.LOBBY_VISIBILITY.getSetting();

                if (profileData.isInLobby() || (profileData.getParty() != null && profileData.getParty().isInLobby())) {
                    ProfileManager.getInstance().getProfiles().values().forEach(profile -> {
                        if (!profile.getIdentifier().equals(xuid) && !profile.isOnline() || !profile.getProfileData().isInLobby() || (profile.getProfileData().getParty() != null && !profile.getProfileData().getParty().isInLobby())) return;

                        try {
                            player.hidePlayer(profile.getPlayer());
                        } catch (Exception ignored) {}
                    });

                    if (!lobbyVisibility.isHidden(this)) {
                        lobbyVisibility.setHidden(this, true);
                    }
                } else if (lobbyVisibility.isHidden(this)) {
                    player.getServer().getOnlinePlayers().values().forEach(player::showPlayer);
                    lobbyVisibility.setHidden(this, false);
                }
            }
        } catch (Exception ignored) {}
    }

    public void clear() {
        if (!isOnline()) return;

        try {
            Player player = getPlayer();

            player.setGamemode(Player.ADVENTURE);
            player.setHealth(player.getMaxHealth());
            player.getFoodData().reset();
            player.getInventory().setHeldItemIndex(0);

            player.getUIInventory().clearAll();
            player.getInventory().clearAll();
            player.getCursorInventory().clearAll();
            player.getCraftingGrid().clearAll();
            player.getOffhandInventory().clearAll();

            player.removeAllEffects();
            player.setExperience(0, 0);

            player.extinguish();
            player.fireTicks = 0;

            player.setMovementSpeed(Player.DEFAULT_SPEED);

            player.setScoreTag("");
            PlayerUtil.setNameTag(this);
        } catch (Exception ignored) {}
    }

    public void quit() {
        if (profileData.getQueue() != null) QueueManager.getInstance().removeQueue(this);

        if (!profileData.isInLobby()) {
            if (profileData.getParty() != null) {
                profileData.getParty().removeMember(this);
            }

            if (profileData.getEvent() != null) {
                profileData.getEvent().removePlayer(this);
            }

            if (profileData.getDuel() != null && !profileData.getDuel().getState().equals(DuelState.ENDING)) {
                if (profileData.getDuel() instanceof Duel2vs2 duel2vs2) duel2vs2.remove(this);
                else profileData.getDuel().stop(profileData.getDuel().getOpponentProfile(this));
            }

            if (profileData.getArena() != null) {
                profileData.getArena().removePlayer(this);
            }

            if (profileData.isInSetupMode()) {
                ArenaWorldSetup.getInstance().remove(this);
                DuelWorldSetup.getInstance().remove(this);
                EventWorldSetup.getInstance().remove(this);
                KitEditor.getInstance().remove(this);
            }

            if (profileData.inStaffMode()) {
                StaffHandler.getInstance().remove(this);
            }

            if (profileData.isSpectator()) {
                Object spectate = profileData.getSpectate();

                if (spectate instanceof Duel duel) duel.removeSpectator(this);
                else if (spectate instanceof Arena arena) arena.removeSpectator(this);
                else if (spectate instanceof PartyGame partyGame) partyGame.removeSpectator(this);
                else if (spectate instanceof EventArena eventArena) eventArena.removeSpectator(this);
            }
        }

        SQLStorage.getInstance().update(this);

        cacheData.getCombat().clear();
        cacheData.clearCooldown();

        final String leaveMessage = "&c[-] &7" + name;

        ProfileManager.getInstance().getProfiles().values().forEach(profile -> {
            try {
                Player p = profile.getPlayer();

                if (profile.getSettingsData().isEnabled(SettingType.JOIN_QUIT_MESSAGES.toString())) p.sendMessage(TextFormat.colorize(leaveMessage));
            } catch (Exception ignored) {}
        });

        ProfileManager.getInstance().remove(this);
    }

    public void knockback(Entity damager) {
        try {
            Player player = getPlayer();

            float horizontalKnockback = 0.4F;
            float verticalKnockback = 0.4F;
            float maxHeight = 0.2F;
            float limiter = 0.005F;

            if (profileData.getDuel() != null) {
                horizontalKnockback = profileData.getDuel().getKit().getHorizontalKnockback();
                verticalKnockback = profileData.getDuel().getKit().getVerticalKnockback();
                maxHeight = profileData.getDuel().getKit().getMaxHeight();
                limiter = profileData.getDuel().getKit().getLimiter();
            } else if (profileData.getArena() != null) {
                horizontalKnockback = profileData.getArena().getKit().getHorizontalKnockback();
                verticalKnockback = profileData.getArena().getKit().getVerticalKnockback();
                maxHeight = profileData.getArena().getKit().getMaxHeight();
                limiter = profileData.getArena().getKit().getLimiter();
            } else if (profileData.getParty() != null && profileData.getParty().getDuel() != null) {
                horizontalKnockback = profileData.getParty().getDuel().getKit().getHorizontalKnockback();
                verticalKnockback = profileData.getParty().getDuel().getKit().getVerticalKnockback();
                maxHeight = profileData.getParty().getDuel().getKit().getMaxHeight();
                limiter = profileData.getParty().getDuel().getKit().getLimiter();
            } else if (profileData.getEvent() != null) {
                horizontalKnockback = profileData.getEvent().getKit().getHorizontalKnockback();
                verticalKnockback = profileData.getEvent().getKit().getVerticalKnockback();
                maxHeight = profileData.getEvent().getKit().getMaxHeight();
                limiter = profileData.getEvent().getKit().getLimiter();
            }

            if (maxHeight > 0.0 && !player.isOnGround()) {
                float distance = (float) (player.getPosition().getY() - damager.getPosition().getY());

                if (distance >= maxHeight) verticalKnockback -= distance * limiter;
            }

            double x = player.getX() - damager.getX();
            double z = player.getZ() - damager.getZ();

            double f = Math.sqrt(x * x + z * z);

            if (f <= 0) return;

            Vector3 motion = player.getMotion().clone();
            f = 1 / f;

            motion.x /= 2d;
            motion.y /= 2d;
            motion.z /= 2d;

            motion.x += x * f * horizontalKnockback;
            motion.y += verticalKnockback;
            motion.z += z * f * horizontalKnockback;

            if (motion.y > verticalKnockback) motion.y = verticalKnockback;

            initialKnockbackMotion = true;
            player.resetFallDistance();
            player.setMotion(motion);
        } catch (Exception ignored) {}
    }

    public void startFishing(FishingHookEntity fishingHook) {
        this.fishing = fishingHook;
        fishingHook.spawnToAll();
    }

    public void stopFishing() {
        this.fishing.close();
        this.fishing = null;
    }

    public void addDuelRequest(Profile profile, DuelType type, int rounds, DuelWorld worldData) {
        duelRequest.put(profile.getIdentifier(), new DuelRequest(profile.getIdentifier(), type, rounds, worldData));

        try {
            profile.getPlayer().sendMessage(TextFormat.colorize("&aYou have sent a request duel to " + getName() + " in " + type.getCustomName() + "&7 (" + rounds + ")"));
            getPlayer().sendMessage(TextFormat.colorize("&aYou have received a " + type.getCustomName() + " &7(" + rounds + ")" + "&a request duel from " + profile.getName()));
        } catch (Exception ignored) {}
    }

    @Nullable
    public DuelRequest getDuelRequest(Profile profile) {
        return duelRequest.get(profile.getIdentifier());
    }

    public void removeDuelRequest(Profile profile) {
        duelRequest.remove(profile.getIdentifier());
    }

    public void setDeathAnimation(Profile damager) {
        try {
            Player player = getPlayer();

            if (damager != null) {
                if (damager.isOnline() && isOnline()) {
                    Player targetPlayer = damager.getPlayer();

                    if (damager.getCosmeticData().hasKillEffect()) damager.getCosmeticData().getKillEffect().getEffect().sendPlayer(targetPlayer, player);

                    SettingsData settingsData = damager.getSettingsData();

                    if (settingsData.isEnabled(SettingType.AUTO_GG.toString())) targetPlayer.chat("GG");

                    DeathAnimation death = new DeathAnimation(
                            player.chunk,
                            Utils.createCompoundTag(this),
                            (float) Math.max(-5, Math.min(5, player.getPosition().getX() - targetPlayer.getPosition().getX())),
                            (float) Math.max(-5, Math.min(5, player.getPosition().getZ() - targetPlayer.getPosition().getZ()))
                    );
                    death.spawnToAll();
                    death.killAnimation();
                }
            }
        } catch (Exception ignored) {}
    }

}
