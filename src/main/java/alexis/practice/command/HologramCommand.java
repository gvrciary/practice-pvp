package alexis.practice.command;

import alexis.practice.util.handler.HologramsHandler;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

public final class HologramCommand extends Command {

    public HologramCommand() {
        super("hologram", "Use command to holograms");
        setPermission("setup.command");

        commandParameters.clear();
        commandParameters.put("lobby", new CommandParameter[]{CommandParameter.newEnum("lobby", new String[]{"lobby"}), CommandParameter.newType("action", false, CommandParamType.STRING)});
        commandParameters.put("lb", new CommandParameter[]{CommandParameter.newEnum("lb", new String[]{"lb"}), CommandParameter.newType("action", false, CommandParamType.STRING)});
        commandParameters.put("personal", new CommandParameter[]{CommandParameter.newEnum("personal", new String[]{"personal"}), CommandParameter.newType("action", false, CommandParamType.STRING)});
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (!(sender instanceof Player player)) return false;

        if (strings.length == 0) {
            sender.sendMessage(TextFormat.colorize("&cUse /hologram <lobby/lb>."));
            return false;
        }

        HologramsHandler hologramsHandler = HologramsHandler.getInstance();
        switch (strings[0].toLowerCase()) {
            case "lobby" -> {
                if (strings.length >= 2 && strings[1] != null && strings[1].equalsIgnoreCase("remove")) {
                    hologramsHandler.setLobbyPosition(null);
                    hologramsHandler.setLobbyEnabled(false);

                    player.sendMessage(TextFormat.colorize("&cThe lobby hologram has been removed"));
                } else {
                    hologramsHandler.setLobbyPosition(player.getPosition().add(0, 1, 0));
                    hologramsHandler.setLobbyEnabled(true);

                    player.sendMessage(TextFormat.colorize("&aLobby hologram has been added"));
                }

                hologramsHandler.execute(HologramsHandler.HologramType.LOBBY);
            }
            case "lb" -> {
                if (strings.length >= 2 && strings[1] != null && strings[1].equalsIgnoreCase("remove")) {
                    hologramsHandler.setLeaderboardPosition(null);
                    hologramsHandler.setLeaderboardEnabled(false);

                    player.sendMessage(TextFormat.colorize("&cThe leaderboard hologram has been removed"));
                } else {
                    hologramsHandler.setLeaderboardPosition(player.getPosition().add(0, 1, 0));
                    hologramsHandler.setLeaderboardEnabled(true);

                    player.sendMessage(TextFormat.colorize("&aLeaderboard hologram has been added"));
                }

                hologramsHandler.execute(HologramsHandler.HologramType.LEADERBOARD);
            }
            case "personal" -> {
                if (strings.length >= 2 && strings[1] != null && strings[1].equalsIgnoreCase("remove")) {
                    hologramsHandler.setPersonalPosition(null);
                    hologramsHandler.setPersonalEnabled(false);

                    player.sendMessage(TextFormat.colorize("&cThe personal hologram has been removed"));
                } else {
                    hologramsHandler.setPersonalPosition(player.getPosition().add(0, 1, 0));
                    hologramsHandler.setPersonalEnabled(true);

                    player.sendMessage(TextFormat.colorize("&aPersonal hologram has been added"));
                }

                hologramsHandler.execute(HologramsHandler.HologramType.PERSONAL_STATISTICS);
            }
        }
        return true;
    }
}
