package alexis.practice.item.hotbar.lobby;

import alexis.practice.item.object.ItemCustom;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import alexis.practice.queue.Queue;
import alexis.practice.queue.QueueManager;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import com.denzelcode.form.FormAPI;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.element.ImageType;
import com.denzelcode.form.window.SimpleWindowForm;

public class QueueItem extends ItemCustom {

    public QueueItem() {
        super("&6Duel", Item.DIAMOND_SWORD, 0);
    }

    public void handleUse(Player player) {
        Profile profile = ProfileManager.getInstance().get(player);

        if (profile == null) return;

        if (!profile.getProfileData().isInLobby()) return;

        sendQueuesForm(profile);
    }

    private void sendQueuesForm(Profile profile) {
        Player player;
        try {
            player = profile.getPlayer();
        } catch (Exception ignored) {
            return;
        }

        QueueManager queueManager = QueueManager.getInstance();

        SimpleWindowForm form = FormAPI.simpleWindowForm("queues", "Duels")
                .addButton("ranked", "Ranked\nIn-Queue: " + queueManager.getQueues().stream().filter(Queue::isRanked).count(), ImageType.PATH, "textures/items/diamond_sword.png")
                .addButton("unranked", "Unranked\nIn-Queue: " + queueManager.getQueues().stream().filter(queue -> !queue.isRanked()).count(), ImageType.PATH, "textures/items/iron_sword.png")
                .addButton("2vs2", "2vs2 Unranked\nIn-Queue: " + queueManager.getQueues().stream().filter(Queue::is2vs2).count(), ImageType.PATH, "textures/items/diamond_axe.png")
                .addHandler(h -> {
                    if (!h.isFormValid("queues")) return;

                    Button button = h.getButton();

                    if (button.getName() != null) {
                        switch (button.getName()) {
                            case "unranked" -> queueManager.sendQueueForm(profile, false);
                            case "ranked" -> queueManager.sendQueueForm(profile,  true);
                            case "2vs2" -> queueManager.sendQueue2vs2Form(profile);
                        }
                    }
                });

        form.sendTo(player);
    }

}
