package alexis.practice.item;

import alexis.practice.item.hotbar.duel.LeaveQueueItem;
import alexis.practice.item.hotbar.duel.RequestMatchItem;
import alexis.practice.item.hotbar.event.*;
import alexis.practice.item.hotbar.lobby.*;
import alexis.practice.item.hotbar.party.*;
import alexis.practice.item.hotbar.staff.LeaveModeItem;
import alexis.practice.item.hotbar.staff.TeleportRandomItem;
import alexis.practice.item.hotbar.staff.ToggleChatItem;
import alexis.practice.item.hotbar.staff.ToggleVanishItem;
import alexis.practice.item.object.ItemCustom;
import cn.nukkit.item.Item;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum HotbarItem {
    //LOBBY
    DUELS(new QueueItem()),
    LEAVE_QUEUE(new LeaveQueueItem()),
    PROFILE(new ProfileItem()),
    EVENT(new EventItem()),
    PARTY(new PartyItem()),
    KIT_EDITOR(new LayoutEditorItem()),
    COSMETICS(new CosmeticsItem()),
    ARENAS(new ArenaItem()),

    //DUEL
    REQUEST(new RequestMatchItem()),

    //STAFF
    VANISH(new ToggleVanishItem()),
    CHAT(new ToggleChatItem()),
    TELEPORT_RANDOM(new TeleportRandomItem()),
    LEAVE_MODE(new LeaveModeItem()),

    //Party
    PARTY_INFO(new PartyInfotItem()),
    PARTY_EVENT(new PartyEventItem()),
    PARTY_REQUEST(new PartyRequestItem()),
    PARTY_LEAVE(new PartyLeaveItem()),
    PARTY_MANAGEMENT(new PartyManagementItem()),

    //EVENT
    EVENT_INFO(new EvenInfoItem()),
    EVENT_MANAGEMENT(new EventManagementItem()),
    EVENT_LEAVE(new EventLeaveItem()),
    TEAM_SELECTOR(new TeamSelectorItem()),
    TEAM_INFO(new TeamInfoItem());

    private final Item item;
    private final String name;
    private final ItemCustom instance;

    HotbarItem(ItemCustom instance) {
        this.instance = instance;
        this.item = instance.getItem();
        this.name = instance.getName();
    }

    public static ItemCustom getItemCustom(String itemName) {
        return Arrays.stream(HotbarItem.values())
                .filter(hotbarItem -> hotbarItem.getName().equals(itemName))
                .map(HotbarItem::getInstance)
                .findFirst()
                .orElse(null);
    }

}
