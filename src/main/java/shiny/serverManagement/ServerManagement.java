package shiny.serverManagement;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import shiny.serverManagement.listener.Listener;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class ServerManagement extends JavaPlugin {
    public Listener listener;


    @Override
    public void onEnable() {
        listener = new Listener(this);
        getServer().getPluginManager().registerEvents(listener, this);

        getServer().getScheduler().runTaskTimer(this, () -> {
            Map<String, Instant> mapClone = new HashMap<>(listener.combatQueue);
            for (Map.Entry<String, Instant> entry : mapClone.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null){
                    continue;
                }

                Duration remaining = Duration.between(Instant.now(), entry.getValue());

                long totalSeconds = Math.max(0, Math.round(remaining.toMillis() / 1000.0));
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;

                String text = String.format("%d:%02d", minutes, seconds);   // "1:05"

                player.sendActionBar(Component.text(MessageFormat.format("You are in combat for {0} seconds", text), NamedTextColor.RED));
                if (Instant.now().isAfter(entry.getValue())){
                    listener.combatQueue.remove(entry.getKey());
                    getLogger().log(Level.INFO, MessageFormat.format("{0} is not longer in combat", entry.getKey()));
                }
            }
        }, 0L, 1L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
