package pl.ghostero.tobiasz.fixes.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pl.ghostero.tobiasz.fixes.Main;
import pl.ghostero.tobiasz.fixes.objects.CosmeticUser;
import pl.ghostero.tobiasz.fixes.objects.PlayerRotation;
import pl.ghostero.tobiasz.fixes.tempstorage.Wing;

public class WingsListener implements Listener {

    private Main main;

    public WingsListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerMoves(PlayerMoveEvent e) {

        if(e.getTo() != null) {

            if(e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()) {

                CosmeticUser cosmeticUser = main.getCosmeticStorage().getCosmeticUsers().get(e.getPlayer().getName());

                if (cosmeticUser != null) {
                    Location fromLoc = e.getFrom();
                    Location toLoc = e.getTo();

                    double deltaX = toLoc.getX() - fromLoc.getX();
                    double deltaZ = toLoc.getZ() - fromLoc.getZ();

                    float yaw = fromLoc.getYaw();
                    double radians = Math.toRadians(yaw);

                    double forward = -deltaX * Math.sin(radians) + deltaZ * Math.cos(radians);

                    Vector from = e.getFrom().toVector().setY(0);
                    Vector to = e.getTo().toVector().setY(0);
                    Vector direction = to.subtract(from);
                    Vector lookDirection = e.getPlayer().getLocation().getDirection().normalize();
                    double sideMotion = direction.crossProduct(lookDirection).getY();

                    if (forward < -0.1) {
                        //e.getPlayer().sendMessage("Poruszasz się do tyłu!");
                        cosmeticUser.setLastBackward(System.currentTimeMillis());
                    }

                    if (Math.abs(sideMotion) > 0.05) {
                        double newRequire = 0.17;
                        if(e.getPlayer().getLocation().getBlock().getType().name().contains("WATER")){
                            newRequire = 0.08;
                        }
                        if(e.getPlayer().getLocation().add(0, -0.05, 0).getBlock().getType().isOccluding() || Math.abs(sideMotion) > newRequire) {
                            if (sideMotion > 0) {
                                //e.getPlayer().sendMessage("poruszasz się w prawo (D) (mot:" + sideMotion + " dis:" + e.getFrom().distance(e.getTo()) + ")");
                                cosmeticUser.setHardRotation(PlayerRotation.RIGHT);
                            } else {
                                //e.getPlayer().sendMessage("poruszasz się w lewo (A) (mot:" + sideMotion + " dis:" + e.getFrom().distance(e.getTo()) + ")");
                                cosmeticUser.setHardRotation(PlayerRotation.LEFT);
                            }
                        }
                    } else {
                        if(Math.abs(sideMotion) < 0.05) {
                            //e.getPlayer().sendMessage("Nie ruszasz sie!");
                            cosmeticUser.setHardRotation(PlayerRotation.NONE);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent e){
        if(e.getTo() != null && e.getFrom().distanceSquared(e.getTo()) >= 24 * 24) {
            final CosmeticUser cosmeticUser = main.getCosmeticStorage().getCosmeticUsers().get(e.getPlayer().getName());
            if(cosmeticUser.hasWings()) {
                main.getWingsManager().destroyPlayerWings(e.getPlayer());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        main.getWingsManager().prepareWingsForPlayer(e.getPlayer());
                    }
                }.runTaskLater(main, 1L);
            }
        }
    }

    @EventHandler
    public void onFullLoad(PlayerJoinEvent e){
        CosmeticUser cosmeticUser = new CosmeticUser(e.getPlayer());
        main.getCosmeticStorage().getCosmeticUsers().put(e.getPlayer().getName(), cosmeticUser);
        Wing wing = main.getTStorage().getPlayerWings().get(e.getPlayer().getName());
        cosmeticUser.setWing(wing);
        if(wing != null){
            main.getWingsManager().prepareWingsForPlayer(e.getPlayer());
        }
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

            if(cosmeticUser.hasWings()) {
                if (e.getTo().getPitch() > 65) {
                    if (!cosmeticUser.isTemporaryHidden()) {
                        cosmeticUser.setTemporaryHidden(true);
                        main.getPacketsManager().getPacketSender().hidePlayerWings(cosmeticUser);
                    }
                } else if (cosmeticUser.isTemporaryHidden()) {
                    cosmeticUser.setTemporaryHidden(false);
                    main.getPacketsManager().getPacketSender().showPlayerWings(cosmeticUser);
                }
            }

            if ((e.getTo().getX() != e.getFrom().getX() || e.getTo().getZ() != e.getFrom().getZ()) && e.getFrom().distance(e.getTo()) > 0.06) {
                cosmeticUser.setLastYaw(nYaw);
                cosmeticUser.setRotation(PlayerRotation.NONE);
                cosmeticUser.setNeedUpdate(true);

                boolean hasElytra = e.getPlayer().getInventory().getChestplate() != null && e.getPlayer().getInventory().getChestplate().getType() == Material.ELYTRA;

                if((e.getPlayer().isSwimming() || hasElytra) && cosmeticUser.hasWings()){
                    if(!cosmeticUser.isSwimming()){
                        cosmeticUser.setSwimming(true);
                        main.getWingsManager().destroyPlayerWings(e.getPlayer());
                    }
                } else if(!e.getPlayer().isSwimming() && !hasElytra && cosmeticUser.isSwimming()) {
                    cosmeticUser.setSwimming(false);
                    main.getWingsManager().prepareWingsForPlayer(e.getPlayer());
                }

            } else {
                if(e.getPlayer().isOp()) {
                   // e.getPlayer().sendTitle(ChatColor.RED + " ", ChatColor.AQUA + "Raw Rotation: " + e.getTo().getYaw() + " / Frot: " + e.getPlayer().getLocation().getYaw() + " / " + "Current Rotation: " + nYaw + " / Previous Rotation: " + cosmeticUser.getRotation() + " / Last Yaw: " + cosmeticUser.getLastYaw());
                }

                float lastYaw = cosmeticUser.getLastYaw();
                float yawDiff = yawDifference(lastYaw, nYaw);

                if (Math.abs(yawDiff) >= 50) {
                    if (yawDiff > 0) {
                        cosmeticUser.setRotation(PlayerRotation.RIGHT);
                        cosmeticUser.setHardRotation(PlayerRotation.NONE);
                        cosmeticUser.setLastYaw(nYaw - 40f);
                    } else {
                        cosmeticUser.setRotation(PlayerRotation.LEFT);
                        cosmeticUser.setHardRotation(PlayerRotation.NONE);
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
