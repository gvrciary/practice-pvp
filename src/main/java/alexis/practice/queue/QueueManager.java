package alexis.practice.queue;

import alexis.practice.duel.Duel2vs2;
import alexis.practice.duel.DuelManager;
import alexis.practice.duel.DuelType;
import alexis.practice.item.HotbarItem;
import alexis.practice.kit.Kit;
import alexis.practice.kit.KitManager;
import alexis.practice.profile.Profile;
import alexis.practice.util.PlayerUtil;
import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.ImageType;
import com.denzelcode.form.window.SimpleWindowForm;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueManager {
    @Getter
    private static final QueueManager instance = new QueueManager();
    private boolean running = true;

    @Getter
    private final java.util.Queue<Queue> queues = new ConcurrentLinkedQueue<>();

    public void createQueue(Profile profile, boolean ranked, DuelType type) {
        Queue queue = new Queue(profile, ranked, type, false);
        queues.add(queue);

        if (profile.getProfileData().getDuel() == null) {
            profile.clear();

            try {
                profile.getPlayer().getInventory().setItem(8, HotbarItem.LEAVE_QUEUE.getItem());
            } catch (Exception ignored) {}
        }
    }

    public void createQueue2vs2(Profile profile, DuelType type) {
        Queue queue = new Queue(profile, false, type, true);
        queues.add(queue);

        if (profile.getProfileData().getDuel() == null) {
            profile.clear();

            try {
                profile.getPlayer().getInventory().setItem(8, HotbarItem.LEAVE_QUEUE.getItem());
            } catch (Exception ignored) {}
        }

        List<Profile> foundProfiles = new ArrayList<>(queues.stream()
                .filter(q -> q.isCompatible2vs2(queue))
                .limit(3)
                .map(Queue::getProfile)
                .toList());

        foundProfiles.add(profile);

        if (foundProfiles.size() == 4) {
            foundProfiles.forEach(this::removeQueue);
            DuelManager.getInstance().createDuel2vs2(foundProfiles, type);
        }
    }

    public void removeQueue(Profile profile) {
        queues.remove(profile.getProfileData().getQueue());
        profile.getProfileData().setQueue();
    }

    public void tick() {
        try {
            queues.forEach(Queue::increaseRange);

            if (queues.size() < 2) return;

            ArrayList<Queue> workingQueues = new ArrayList<>(queues);

            for (Queue firstQueue : workingQueues) {
                for (Queue secondQueue : workingQueues) {
                    if (firstQueue.isCompatible(secondQueue) && queues.contains(firstQueue) && queues.contains(secondQueue)) {
                        removeQueue(firstQueue.getProfile());
                        removeQueue(secondQueue.getProfile());

                        DuelManager.getInstance().createDuel(secondQueue.getProfile(), firstQueue.getProfile(), secondQueue.isRanked(), secondQueue.getType(), 1, null, false);
                    }
                }
            }
        } catch (Exception exception) {
            running = false;

            queues.forEach(queueProfile ->{
                removeQueue(queueProfile.getProfile());
                queueProfile.getProfile().clear();

                try {
                    PlayerUtil.getLobbyKit(queueProfile.getProfile().getPlayer());
                } catch (Exception ignored) {}
            });
        }
    }

    public void sendQueueForm(Profile profile, boolean isRanked) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        SimpleWindowForm form = FormAPI.simpleWindowForm("queues", (isRanked ? "Ranked Queue" : "Unranked Queue"))
                .addHandler(h -> {
                    if (!h.isFormValid("queues")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        if (!running) {
                            player.sendMessage(TextFormat.colorize("&cIt is disabled, report this error"));
                            return;
                        }

                        DuelType type = DuelType.get(button.getName());

                        if (type != null) {
                            createQueue(profile, isRanked, type);
                        }
                    }
                });

        Arrays.stream(DuelType.values()).toList().forEach(duelType -> {
            final int fights = (int) DuelManager.getInstance().getDuels().values().stream().filter(duel -> duel.getKit().getName().equals(duelType.getName()) && duel.isRanked() == isRanked).count();
            final int queue = (int) queues.stream().filter(q -> q.getType().equals(duelType) && q.isRanked() == isRanked).count();
            form.addButton(duelType.getName(), duelType.getCustomName() + "\nIn-Fight: " + fights + " | In-Queue: " + queue, ImageType.PATH, duelType.getType().getIcon());
        });

        form.sendTo(player);
    }

    public void sendQueue2vs2Form(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        List<Kit> kitsAvailable = KitManager.getInstance().getKits().values().stream()
                .filter(kit ->
                        kit.getName().equals("nodebuff") ||
                        kit.getName().equals("finaluhc") ||
                        kit.getName().equals("builduhc") ||
                        kit.getName().equals("caveuhc") ||
                        kit.getName().equals("sumo") ||
                        kit.getName().equals("combo") ||
                        kit.getName().equals("hg") ||
                        kit.getName().equals("midfight") ||
                        kit.getName().equals("tntsumo"))
                .toList();

        SimpleWindowForm form = FormAPI.simpleWindowForm("queues", "2vs2 Queue")
                .addHandler(h -> {
                    if (!h.isFormValid("queues")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        DuelType type = DuelType.get(button.getName());

                        if (type != null) {
                            createQueue2vs2(profile, type);
                        }
                    }
                });

        kitsAvailable.forEach(duelType -> {
            final int fights = (int) DuelManager.getInstance().getDuels().values().stream().filter(duel -> duel.getKit().getName().equals(duelType.getName()) && duel instanceof Duel2vs2).count();
            final int queue = (int) queues.stream().filter(q -> q.getType().getType().equals(duelType) && q.is2vs2()).count();
            form.addButton(duelType.getName(), duelType.getCustomName() + "\nIn-Fight: " + fights + " | In-Queue: " + queue, ImageType.PATH, duelType.getIcon());
        });

        form.sendTo(player);
    }
    
}
