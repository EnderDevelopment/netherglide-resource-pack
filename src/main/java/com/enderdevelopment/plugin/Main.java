package com.enderdevelopment.plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;

public final class Main extends JavaPlugin implements Listener, CommandExecutor {
    private final Set<UUID> enabledPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        if (getCommand("gildedblackstone") != null) {
            getCommand("gildedblackstone").setExecutor(this);
        }
        Bukkit.getScheduler().runTaskTimer(this, this::showVisibleTarget, 1L, 5L);
    }

    @Override
    public void onDisable() {
        enabledPlayers.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by a player.", NamedTextColor.RED));
            return true;
        }
        if (!player.hasPermission("gildedblackstone.use")) {
            player.sendMessage(Component.text("You do not have permission to use this.", NamedTextColor.RED));
            return true;
        }
        UUID playerId = player.getUniqueId();
        boolean enabled = enabledPlayers.remove(playerId);
        if (!enabled) {
            enabledPlayers.add(playerId);
        }
        String state = enabled ? "disabled" : "enabled";
        player.sendMessage(Component.text("Gilded Blackstone focus " + state + ".", NamedTextColor.GOLD));
        return true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        enabledPlayers.remove(event.getPlayer().getUniqueId());
    }

    private void showVisibleTarget() {
        for (UUID playerId : enabledPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                continue;
            }
            RayTraceResult result = player.getWorld().rayTraceBlocks(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    32.0,
                    org.bukkit.FluidCollisionMode.NEVER,
                    true);
            if (result == null || result.getHitBlock() == null) {
                continue;
            }
            Block block = result.getHitBlock();
            if (block.getType() != Material.GILDED_BLACKSTONE) {
                continue;
            }
            Location center = block.getLocation().add(0.5, 0.5, 0.5);
            player.spawnParticle(Particle.FLAME, center, 8, 0.35, 0.35, 0.35, 0.01);
            player.spawnParticle(Particle.GLOW, center, 4, 0.3, 0.3, 0.3, 0.0);
        }
    }
}
