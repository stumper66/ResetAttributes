import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Listeners implements Listener {
    private final ResetAttributes instance;

    public Listeners(final ResetAttributes instance){
        this.instance = instance;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onPlayerJoin(final PlayerJoinEvent event){
        if (!instance.isAutoResetEnabled) return;

        new BukkitRunnable() {
            public void run() {
                instance.resetPlayer(event.getPlayer(), true);
            }
        }.runTaskLater(instance, 2L);
    }
}
