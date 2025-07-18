package alexis.practice.event.team;

import alexis.practice.event.Event;
import alexis.practice.profile.Profile;
import alexis.practice.profile.ProfileManager;
import cn.nukkit.Player;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Team {
    @Getter
    private final int id;

    private final int size;

    @Getter
    private final Event event;
    private final List<String> members = new ArrayList<>();

    public Team(int id, Event event, int size) {
        this.id = id;
        this.event = event;
        this.size = size;
    }

    public boolean isAlive() {
        return !getMembersAlive().isEmpty();
    }

    public List<Profile> getMembersAlive() {
        return getMembers().stream().filter(member -> event.getEventArena().isAlive(member)).toList();
    }

    public int getKills() {
        return getMembers().stream()
                .mapToInt(profile -> event.getEventArena().getKills(profile))
                .sum();
    }

    public boolean isFull() {
        return members.size() >= size;
    }

    public void addMember(Profile profile) {
        members.add(profile.getIdentifier());
    }

    public boolean isMember(Profile profile) {
        return members.contains(profile.getIdentifier());
    }

    public void removeMember(Profile profile) {
        members.remove(profile.getIdentifier());
    }

    public List<Profile> getMembers() {
        return ProfileManager.getInstance().getProfiles().values().stream()
                .filter(profile -> this.members.contains(profile.getIdentifier()) &&
                        profile.isOnline() && event.isPlayer(profile))
                .collect(Collectors.toList());
    }

    public void setNameTags() {
        if (!isAlive()) return;

        List <Profile> membersAlive = getMembersAlive();

        if (membersAlive.size() == 1) return;

        membersAlive.forEach(profile -> {
            try {
                Player player = profile.getPlayer();

                Player[] viewers = player.getViewers().values().stream().filter(viewer -> {
                    Profile p = ProfileManager.getInstance().get(viewer);

                    if (p == null) return false;

                    return membersAlive.contains(p);
                }).toArray(Player[]::new);

                if (viewers.length == 0) return;

                EntityMetadata metadata = new EntityMetadata();
                player.sendData(viewers, metadata.put(new StringEntityData(4, TextFormat.colorize("&a" + profile.getName()))));
            } catch (Exception ignored) {}
        });
    }

    public void broadcast(String message) {
        getMembers().forEach(profile -> {
            try {
                profile.getPlayer().sendMessage(TextFormat.colorize(message));
            } catch (Exception ignored) {}
        });
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("&6Team Information\n");

        List<String> members = getMembersAlive().stream()
                .map(Profile::getName)
                .collect(Collectors.toList());
        sb.append("&7ID:&f ").append(id).append("\n");
        sb.append("&7Members:&f ").append(String.join(", ", members)).append("\n");

        return sb.toString();
    }
}