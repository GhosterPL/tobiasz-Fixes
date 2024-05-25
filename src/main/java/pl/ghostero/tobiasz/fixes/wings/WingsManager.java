package pl.ghostero.tobiasz.fixes.wings;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.ghostero.tobiasz.fixes.Main;
import pl.ghostero.tobiasz.fixes.objects.CosmeticUser;
import pl.ghostero.tobiasz.fixes.objects.PlayerRotation;
import pl.ghostero.tobiasz.fixes.objects.WingsRotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WingsManager {

    private Map<Integer, Float> directionMultipliers = new HashMap<>();

    private Map<Integer, Integer> cordinatesCorrection = new HashMap<>();

    private final Map<String, Player> playersToRefresh = new HashMap<>();

    Player testPlayer;

    private Main main;

    public WingsManager(Main main) {
        this.main = main;
        runTimer();

        directionMultipliers.put(60, 1.03f);
        directionMultipliers.put(80, 1.05f);
        directionMultipliers.put(90, 1.07f);
        directionMultipliers.put(100, 1.08f);
        directionMultipliers.put(110, 1.09f);
        directionMultipliers.put(120, 1.11f);
        directionMultipliers.put(130, 1.13f);
        directionMultipliers.put(140, 1.14f);
        directionMultipliers.put(150, 1.15f);
        directionMultipliers.put(160, 1.17f);
        directionMultipliers.put(170, 1.18f);
        directionMultipliers.put(180, 1.19f);
        directionMultipliers.put(190, 1.20f);
        directionMultipliers.put(200, 1.22f);
        directionMultipliers.put(210, 1.23f);
        directionMultipliers.put(220, 1.24f);
        directionMultipliers.put(230, 1.25f);
        directionMultipliers.put(240, 1.27f);
        directionMultipliers.put(250, 1.28f);
        directionMultipliers.put(280, 1.30f);
        directionMultipliers.put(320, 1.32f);
        directionMultipliers.put(340, 1.34f);

        cordinatesCorrection.put(-320, -5);
        cordinatesCorrection.put(-330, -10);
        cordinatesCorrection.put(-340, -10);
        cordinatesCorrection.put(-350, -15);
        cordinatesCorrection.put(-360, -15);
        cordinatesCorrection.put(-40, -5);
        cordinatesCorrection.put(-30, -10);
        cordinatesCorrection.put(-20, -15);
        cordinatesCorrection.put(-10, -15);

        cordinatesCorrection.put(5, 25);
        cordinatesCorrection.put(10, 20);
        cordinatesCorrection.put(20, 15);
        cordinatesCorrection.put(30, 10);
        cordinatesCorrection.put(310, 5);
        cordinatesCorrection.put(320, 5);
        cordinatesCorrection.put(330, 10);
        cordinatesCorrection.put(340, 10);
        cordinatesCorrection.put(350, 10);
        cordinatesCorrection.put(360, 10);
    }

    public int getCorrection(float yaw) {
        if (yaw > 0) {
            for (int x = (int) yaw; x < yaw + 10; x++) {
                if (cordinatesCorrection.get(x) != null) {
                    return cordinatesCorrection.get(x);
                }
            }
        } else {
            for (int x = (int) yaw; x > yaw - 10; x--) {
                if (cordinatesCorrection.get(x) != null) {
                    return cordinatesCorrection.get(x);
                }
            }
        }
        return 0;
    }

    public float getMultiplier(float direction) {
        float positiveDirection = Math.abs(direction);
        for (int start = (int) positiveDirection; start >= 60; start -= 1) {
            if (directionMultipliers.get(start) != null) {
                return directionMultipliers.get(start);
            }
        }
        return 1.0f;
    }

    public void runTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (CosmeticUser cosmeticUser : main.getCosmeticStorage().getCosmeticUsers().values()) {
                    if (cosmeticUser.hasWings()) {

                        WingsRotation wingsRotation = generateRotation(cosmeticUser.getPlayer());

                        createForNearbyPlayers(cosmeticUser);

                        updateNearbyPlayers(cosmeticUser, wingsRotation);

                    }
                }
            }
        }.runTaskTimer(main, 1L, 1L);
    }

    public void destroyPlayerWings(Player p) {

        final CosmeticUser cosmeticUser = main.getCosmeticStorage().getCosmeticUsers().get(p.getName());
        if (cosmeticUser.hasWings()) {
            main.getPacketsManager().getPacketSender().destroyWingsPlayerForPlayer(cosmeticUser, p);

            for (String player : cosmeticUser.getNearbyPlayers().keySet()) {
                Player toPlayer = Bukkit.getPlayer(player);
                main.getPacketsManager().getPacketSender().destroyWingsPlayerForPlayer(cosmeticUser, toPlayer);
            }
            cosmeticUser.getWings().clear();
            cosmeticUser.getNearbyPlayers().clear();
        }
    }

    public void prepareWingsForPlayer(final Player p) {

        final CosmeticUser cosmeticUser = main.getCosmeticStorage().getCosmeticUsers().get(p.getName());

        main.getPacketsManager().getPacketSender().prepareWingsForPlayer(cosmeticUser);

        main.getPacketsManager().getPacketSender().createWingsPlayerForPlayer(cosmeticUser, p);
    }

    public WingsRotation generateRotation(Player p) {

        final CosmeticUser cosmeticUser = main.getCosmeticStorage().getCosmeticUsers().get(p.getName());

        WingsRotation wingsRotation = cosmeticUser.getWingsRotation();

        wingsRotation.setPlusPower2(0);

        float newYaw = (p.getLocation().getYaw());
        if (Math.abs(newYaw) >= 60f) {
            float multiplier = getMultiplier(newYaw);
            newYaw /= multiplier;
            newYaw -= 3;
        }

        int correction = getCorrection(p.getLocation().getYaw());

        //p.sendMessage("Current correction > " + correction);

        if (p.getLocation().getYaw() > 0.0) {
            int plusNum = 0;
            if (cosmeticUser.getRotation() == PlayerRotation.LEFT) {
                plusNum += 10;
            }
            wingsRotation.setPlusPower(Math.max(-20 + plusNum, wingsRotation.getPlusPower() - 4));
        } else {
            wingsRotation.setPlusPower(Math.min(20, wingsRotation.getPlusPower() + 4));
        }

        if (cosmeticUser.getRotation() == PlayerRotation.RIGHT) {
            if (newYaw > 0) {
                wingsRotation.setPlusPower2(correction);
            } else {
                wingsRotation.setPlusPower2(-5 + correction);
            }
        } else if (cosmeticUser.getRotation() == PlayerRotation.LEFT) {
            if (newYaw > 0) {
                wingsRotation.setPlusPower2(10 + correction);
            } else {
                wingsRotation.setPlusPower2(30 + correction);
            }
        } else {
            wingsRotation.setPlusPower2(wingsRotation.getPlusPower2() + 10 + correction);
        }


        if (cosmeticUser.getHardRotation() == PlayerRotation.LEFT) {
            if(System.currentTimeMillis() - cosmeticUser.getLastBackward() > 400){
                wingsRotation.setPlusPower2(wingsRotation.getPlusPower2() - 20);
            } else {
                wingsRotation.setPlusPower2(wingsRotation.getPlusPower2() + 20);
            }
        } else if (cosmeticUser.getHardRotation() == PlayerRotation.RIGHT) {
            if(System.currentTimeMillis() - cosmeticUser.getLastBackward() > 400) {
                wingsRotation.setPlusPower2(wingsRotation.getPlusPower2() + 20);
            } else {
                wingsRotation.setPlusPower2(wingsRotation.getPlusPower2() - 20);
            }
        }

        float changeSize = 0.5f;

        if (p.isSprinting() || !p.isOnGround()) {
            changeSize = 4f;
        }

        if (wingsRotation.isHigher()) {
            wingsRotation.setLastValue(wingsRotation.getLastValue() + changeSize);
        } else {
            wingsRotation.setLastValue(wingsRotation.getLastValue() - changeSize);
        }
        if (wingsRotation.isHigher() && wingsRotation.getLastValue() >= 24) {
            wingsRotation.setHigher(false);
        } else if (!wingsRotation.isHigher() && wingsRotation.getLastValue() <= -4) {
            wingsRotation.setHigher(true);
        }

        if (cosmeticUser.isNeedUpdate()) {

            for (int x = 1; x <= 2; x++) {
                cosmeticUser.update();

                if (x == 1) {
                    wingsRotation.setLastWing1(newYaw + (50f) + (wingsRotation.getPlusPower() + wingsRotation.getPlusPower2()));
                } else {
                    wingsRotation.setLastWing2(newYaw - (70f) + (wingsRotation.getPlusPower() + wingsRotation.getPlusPower2()));
                }

            }
        }

        return wingsRotation;
    }

    private void updateNearbyPlayers(CosmeticUser cosmeticUser, WingsRotation wingsRotation) {

        for (String player : cosmeticUser.getNearbyPlayers().keySet()) {

            Player toPlayer = Bukkit.getPlayer(player);

            if (toPlayer != null && toPlayer.isOnline()) {
                main.getPacketsManager().getPacketSender().updateWingsForPlayer(cosmeticUser, toPlayer, wingsRotation);
            }
        }
        main.getPacketsManager().getPacketSender().updateWingsForPlayer(cosmeticUser, cosmeticUser.getPlayer(), wingsRotation);
    }

    private void createForNearbyPlayers(CosmeticUser cosmeticUser) {

        for (Player all : Bukkit.getOnlinePlayers()) {

            if (!all.getName().equalsIgnoreCase(cosmeticUser.getPlayer().getName())) {

                if (cosmeticUser.getNearbyPlayers().get(all.getName()) == null) {

                    if (all.getLocation().distanceSquared(cosmeticUser.getPlayer().getLocation()) < 32 * 32 && all.canSee(cosmeticUser.getPlayer())) {

                        main.getPacketsManager().getPacketSender().createWingsPlayerForPlayer(cosmeticUser, all);

                        cosmeticUser.getNearbyPlayers().put(all.getName(), true);
                    }
                }
            }
        }

        List<String> toRemove = new ArrayList<>();

        for (String player : cosmeticUser.getNearbyPlayers().keySet()) {

            Player toPlayer = Bukkit.getPlayer(player);

            if (toPlayer == null || !toPlayer.isOnline() || toPlayer.getLocation().distanceSquared(cosmeticUser.getPlayer().getLocation()) >= 32 * 32 || !toPlayer.canSee(cosmeticUser.getPlayer())) {

                main.getPacketsManager().getPacketSender().destroyWingsPlayerForPlayer(cosmeticUser, toPlayer);

                toRemove.add(player);
            }
        }

        for (String player : toRemove) {
            cosmeticUser.getNearbyPlayers().remove(player);
        }

    }
}