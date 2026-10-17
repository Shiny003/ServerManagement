package shiny.serverManagement.listener;

import org.bukkit.ChatColor;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Listener implements org.bukkit.event.Listener {
    private final JavaPlugin plugin;
    public Map<String, Instant> combatQueue = new HashMap<>();
    public List<String> killQueue = new ArrayList<>();

    public Listener(JavaPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        DamageSource damageSource = event.getDamageSource();
        if (damageSource.getCausingEntity() instanceof Player damager) {
            if (event.getEntity() instanceof Player target) {
                plugin.getLogger().log(Level.INFO, MessageFormat.format("{0} is in combat with {1}", damager.getName(), target.getName()));
                Instant now = Instant.now();
                Instant later = now.plus(Duration.ofSeconds(30));
                combatQueue.put(damager.getName(), later);
                combatQueue.put(target.getName(), later);
                //damager.sendMessage("You are now in combat for the next 30 seconds. Do not log out");
                //target.sendMessage("You are now in combat for the next 30 seconds. Do not log out");
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getReason() == PlayerQuitEvent.QuitReason.DISCONNECTED &&
                combatQueue.containsKey(event.getPlayer().getName())){
            plugin.getLogger().log(Level.INFO, "Player in combat logged out");
            killQueue.add(event.getPlayer().getName());
            combatQueue.remove(event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (killQueue.contains(event.getPlayer().getName())){
            event.getPlayer().kill();
            event.getPlayer().sendMessage("You combat logged!");
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player p){
            combatQueue.remove(p.getName());
            killQueue.remove(p.getName());
        }
    }
}
