package ru.eclipsedev.frostRtp;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import ru.eclipsedev.frostRtp.util.LocationFinder;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RtpCommand implements CommandExecutor, TabCompleter {
    private final FrostRtp plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");

    public RtpCommand(FrostRtp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return commandreload(sender);
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("player-only-command"));
            return true;
        }
        Player player = (Player) sender;
        String rtpType = (args.length > 0) ? args[0].toLowerCase() : "default";
        if (!plugin.getConfig().contains("rtp-types." + rtpType)) {
            if (plugin.getConfig().contains("rtp-types.default")) {
                rtpType = "default";
            } else {
                player.sendMessage(getMessage("rtp-type-not-found").replace("%тип%", rtpType));
                return true;
            }
        }

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("rtp-types." + rtpType);

        String permission = section.getString("permission");
        if (permission != null && !player.hasPermission(permission)) {
            player.sendMessage(getMessage("no-permission-rtp-type").replace("%тип%", rtpType));
            return true;
        }

        long cooldownTime = section.getInt("cooldown");
        if (!player.hasPermission("rtp.bypass") && cooldowns.containsKey(player.getUniqueId())) {
            long secondsLeft = ((cooldowns.get(player.getUniqueId()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                player.sendMessage(getMessage("cooldown").replace("%секунд%", String.valueOf(secondsLeft)));
                return true;
            }
        }

        String worldName = section.getString("world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            player.sendMessage(getMessage("world-not-found"));
            return true;
        }

        if (rtpType.equals("near")) {
            int minPlayers = section.getInt("min-players", 2);
            if (world.getPlayers().size() < minPlayers) {
                player.sendMessage(getMessage("no-players-for-near"));
                return true;
            }
        }

        player.sendMessage(getMessage("finding-safe-location"));

        searchAndTeleport(player, world, section);

        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        return true;
    }

    private void searchAndTeleport(Player player, World world, ConfigurationSection section) {
        double maxRadius = section.getInt("max-teleport.x", 1000);
        double minRadius = section.getInt("min-radius", 0);

        CompletableFuture.supplyAsync(() -> LocationFinder.findSafeLocation(world, minRadius, maxRadius))
                .thenAccept(location -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!player.isOnline()) return;
                        if (location != null) {
                            player.teleport(location);
                            player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                            String successMessage = getMessage("teleport-success");
                            String coords = "&#B1C3DB" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
                            player.sendMessage(colorize(successMessage.replace("%координаты%", coords)));
                        } else {
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if (player.isOnline()) {
                                    searchAndTeleport(player, world, section);
                                }
                            }, 5L);
                        }
                    });
                });
    }

    private boolean commandreload(CommandSender sender) {
        if (!sender.hasPermission("rtp.admin.reload")) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }
        plugin.rreload();
        sender.sendMessage(getMessage("config-reloaded"));
        return true;
    }

    private String colorize(String message) {
        if (message == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    private String getMessage(String key) {
        String message = plugin.getConfig().getString("messages." + key, "&cСообщение не найдено: " + key);
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        if (key.equals("usage")) {
            return colorize(plugin.getConfig().getString("messages." + key));
        }
        return colorize(prefix + message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            ConfigurationSection typesSection = plugin.getConfig().getConfigurationSection("rtp-types");
            if (typesSection != null) {
                Set<String> rtpTypes = typesSection.getKeys(false);
                for (String type : rtpTypes) {
                    String permission = plugin.getConfig().getString("rtp-types." + type + ".permission");
                    if (permission == null || sender.hasPermission(permission)) {
                        completions.add(type);
                    }
                }
            }
            if (sender.hasPermission("rtp.admin.reload")) {
                completions.add("reload");
            }
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}