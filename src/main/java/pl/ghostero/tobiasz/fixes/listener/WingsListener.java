package pl.ghostero.tobiasz.fixes.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pl.ghostero.tobiasz.fixes.Main;
import pl.ghostero.tobiasz.fixes.objects.CosmeticUser;
import pl.ghostero.tobiasz.fixes.objects.PlayerRotation;

public class WingsListener implements Listener {

    private Main main;

    public WingsListener(Main main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent e){
        if(e.getTo() != null && e.getFrom().distanceSquared(e.getTo()) >= 24 * 24) {
            main.getWingsManager().destroyPlayerWings(e.getPlayer());
            final CosmeticUser cosmeticUser = main.getCosmeticStorage().getCosmeticUsers().get(e.getPlayer().getName());
            new BukkitRunnable() {
                @Override
                public void run() {
                    main.getWingsManager().prepareWingsForPlayer(e.getPlayer(), cosmeticUser.getModelLeft(), cosmeticUser.getModelRight());
                }
            }.runTaskLater(main, 1L);
        }
    }

    @EventHandler
    public void onFullLoad(PlayerJoinEvent e){
        CosmeticUser cosmeticUser = new CosmeticUser(e.getPlayer());
        main.getCosmeticStorage().getCosmeticUsers().put(e.getPlayer().getName(), cosmeticUser);
        //main.getWingsManager().prepareWingsForPlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        main.getWingsManager().destroyPlayerWings(e.getPlayer());
        main.getCosmeticStorage().getCosmeticUsers().remove(e.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent e){
        CosmeticUser cosmeticUser = main.getCosmeticStorage().getCosmeticUsers().get(e.getPlayer().getName());
        float nYaw = e.getPlayer().getLocation().getYaw();
        if(e.getTo() != null) {
            if ((e.getTo().getX() != e.getFrom().getX() || e.getTo().getZ() != e.getFrom().getZ()) && e.getFrom().distance(e.getTo()) > 0.06) {
                cosmeticUser.setLastYaw(nYaw);
                cosmeticUser.setRotation(PlayerRotation.NONE);
                cosmeticUser.setNeedUpdate(true);
            } else {
                if(e.getPlayer().isOp()) {
                   // e.getPlayer().sendTitle(ChatColor.RED + " ", ChatColor.AQUA + "Raw Rotation: " + e.getTo().getYaw() + " / Frot: " + e.getPlayer().getLocation().getYaw() + " / " + "Current Rotation: " + nYaw + " / Previous Rotation: " + cosmeticUser.getRotation() + " / Last Yaw: " + cosmeticUser.getLastYaw());
                }

                float lastYaw = cosmeticUser.getLastYaw();
                float yawDiff = yawDifference(lastYaw, nYaw);

                if (Math.abs(yawDiff) >= 50) {
                    if (yawDiff > 0) {
                        cosmeticUser.setRotation(PlayerRotation.RIGHT);
                        cosmeticUser.setLastYaw(nYaw - 40f);
                    } else {
                        cosmeticUser.setRotation(PlayerRotation.LEFT);
                        cosmeticUser.setLastYaw(nYaw + 40f);
                    }
                    cosmeticUser.setNeedUpdate(true);
                }
            }
        }
    }

    private float yawDifference(float yaw1, float yaw2) {
        float difference = normalizeYaw(yaw2 - yaw1);
        if (difference > 180) {
            difference -= 360;
        } else if (difference < -180) {
            difference += 360;
        }
        return difference;
    }

    public float normalizeYaw(float yaw) {
        yaw = yaw % 360;
        if (yaw > 180) {
            yaw -= 360;
        } else if (yaw < -180) {
            yaw += 360;
        }
        return yaw;
    }

}
