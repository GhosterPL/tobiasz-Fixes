package pl.ghostero.tobiasz.fixes.wings;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import pl.ghostero.tobiasz.fixes.Main;
import pl.ghostero.tobiasz.fixes.objects.CosmeticUser;
import pl.ghostero.tobiasz.fixes.objects.PlayerRotation;
import pl.ghostero.tobiasz.fixes.objects.WingsRotation;

import java.lang.reflect.Field;
import java.util.*;

public class WingsManager {

    private Map<Integer, Float> directionMultipliers = new HashMap<>();

    private Map<Integer, Integer> cordinatesCorrection = new HashMap<>();

    private final Map<String, Player> playersToRefresh = new HashMap<>();

    Player testPlayer;

    private final double addYWithDummy = 2.45;
    private final double addY = 2.05;

    private Main main;

    private Map<Integer, String> text = new HashMap<>();

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

    public int getCorrection(float yaw){
        if(yaw > 0){
            for(int x = (int) yaw; x < yaw+10; x++){
                if(cordinatesCorrection.get(x) != null){
                    return cordinatesCorrection.get(x);
                }
            }
        } else {
            for(int x = (int) yaw; x > yaw-10; x--){
                if(cordinatesCorrection.get(x) != null){
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

    public void runTimer(){
        new BukkitRunnable(){
            @Override
            public void run() {
                for(CosmeticUser cosmeticUser : main.getCosmeticStorage().getCosmeticUsers().values()){
                    if(cosmeticUser.hasWings()){

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
        if(cosmeticUser.hasWings()) {
            destroyWingsPlayerForPlayer(p, p);

            for (String player : cosmeticUser.getNearbyPlayers().keySet()) {
                Player toPlayer = Bukkit.getPlayer(player);
                destroyWingsPlayerForPlayer(cosmeticUser.getPlayer(), toPlayer);
            }
            cosmeticUser.getWings().clear();
            cosmeticUser.getNearbyPlayers().clear();
        }
    }

    public void prepareWingsForPlayer(final Player p, int modelDataLeft, int modelDataRight){

        final CosmeticUser cosmeticUser = main.getCosmeticStorage().getCosmeticUsers().get(p.getName());

        WorldServer s = ((CraftWorld) p.getWorld()).getHandle();

        for (int x = 1; x <= 2; x++) {

            final EntityArmorStand stand = new EntityArmorStand(EntityTypes.ARMOR_STAND, s);
            stand.setPosition(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setNoGravity(true);

            cosmeticUser.getWings().put(x, stand);
        }

        cosmeticUser.setModelLeft(modelDataLeft);
        cosmeticUser.setModelRight(modelDataRight);

        createWingsPlayerForPlayer(p, p);
    }

    public void createWingsPlayerForPlayer(Player p, Player toPlayer){

        final CosmeticUser cosmeticUser = main.getCosmeticStorage().getCosmeticUsers().get(p.getName());

        for(int x = 1; x <= 2; x++){

            net.minecraft.server.v1_16_R3.ItemStack wings;
            ItemStack itemStack = new ItemStack(Material.PHANTOM_MEMBRANE);
            ItemMeta meta = itemStack.getItemMeta();
            if(x == 1) {
                meta.setCustomModelData(cosmeticUser.getModelLeft());
            } else {
                meta.setCustomModelData(cosmeticUser.getModelRight());
            }
            itemStack.setItemMeta(meta);
            wings = CraftItemStack.asNMSCopy(itemStack);

            EntityArmorStand stand = cosmeticUser.getWings().get(x);

            stand.setPosition(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());

            List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> equipmentList = new ArrayList<>();
            equipmentList.add(new Pair<>(EnumItemSlot.HEAD, wings));
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(stand.getId(), equipmentList);

            ((CraftPlayer) toPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(stand));
            ((CraftPlayer) toPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(stand.getId(), stand.getDataWatcher(), true));
            ((CraftPlayer) toPlayer).getHandle().playerConnection.sendPacket(packet);

            //((CraftPlayer) p).getHandle().passengers.add(stand);

            if(p.getName().equalsIgnoreCase(toPlayer.getName())){
            } else {
                //put passenger on player by packets
            }
        }
    }

    public void destroyWingsPlayerForPlayer(Player p, Player toPlayer){
        if(toPlayer != null && toPlayer.isOnline()){
            final CosmeticUser cosmeticUser = main.getCosmeticStorage().getCosmeticUsers().get(p.getName());
            ((CraftPlayer) toPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(cosmeticUser.getWings().get(1).getId()));
            ((CraftPlayer) toPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(cosmeticUser.getWings().get(2).getId()));
        }
    }

    public WingsRotation generateRotation(Player p){

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
            wingsRotation.setPlusPower(Math.max(-20 + plusNum, wingsRotation.getPlusPower()-4));
        } else {
            wingsRotation.setPlusPower(Math.min(20, wingsRotation.getPlusPower()+4));
        }

        if (cosmeticUser.getRotation() == PlayerRotation.RIGHT) {
            if(newYaw > 0){
                wingsRotation.setPlusPower2(correction);
            } else {
                wingsRotation.setPlusPower2(-5+correction);
            }
        } else if (cosmeticUser.getRotation() == PlayerRotation.LEFT) {
            if(newYaw > 0){
                wingsRotation.setPlusPower2(10+correction);
            } else {
                wingsRotation.setPlusPower2(30+correction);
            }
        } else {
            wingsRotation.setPlusPower2(wingsRotation.getPlusPower2()+10+correction);
        }

        float changeSize = 0.5f;

        if (p.isSprinting() || !p.isOnGround()) {
            changeSize = 4f;
        }

        if (wingsRotation.isHigher()) {
            wingsRotation.setLastValue(wingsRotation.getLastValue()+changeSize);
        } else {
            wingsRotation.setLastValue(wingsRotation.getLastValue()-changeSize);
        }
        if (wingsRotation.isHigher() && wingsRotation.getLastValue() >= 30) {
            wingsRotation.setHigher(false);
        } else if (!wingsRotation.isHigher() && wingsRotation.getLastValue() <= 0) {
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

        EntityArmorStand stand1 = cosmeticUser.getWings().get(1);
        EntityArmorStand stand2 = cosmeticUser.getWings().get(2);

        for (String player : cosmeticUser.getNearbyPlayers().keySet()) {

            Player toPlayer = Bukkit.getPlayer(player);

            if (toPlayer != null && toPlayer.isOnline()) {
                updateWingsForPlayer(cosmeticUser.getPlayer(), toPlayer, wingsRotation, stand1, stand2);
            }
        }

        updateWingsForPlayer(cosmeticUser.getPlayer(), cosmeticUser.getPlayer(), wingsRotation, stand1, stand2);
    }

    private void updateWingsForPlayer(Player p, Player toPlayer, WingsRotation wingsRotation, EntityArmorStand stand1, EntityArmorStand stand2){

        stand1.setPosition(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
        stand2.setPosition(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());

        ((CraftPlayer) toPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation(stand1, (byte) (wingsRotation.getLastWing1() + (wingsRotation.getLastValue()))));
        ((CraftPlayer) toPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation(stand2, (byte) (wingsRotation.getLastWing2() - (wingsRotation.getLastValue()))));

        PacketPlayOutMount packetMount = new PacketPlayOutMount(((CraftPlayer) p).getHandle());
        try {
            Field passengersField = packetMount.getClass().getDeclaredField("b");
            passengersField.setAccessible(true);
            passengersField.set(packetMount, new int[]{stand1.getId(), stand2.getId()});

            ((CraftPlayer) toPlayer).getHandle().playerConnection.sendPacket(packetMount);

        } catch (Exception exc){

        }
    }

    private void createForNearbyPlayers(CosmeticUser cosmeticUser){

        for(Player all : Bukkit.getOnlinePlayers()) {

            if (!all.getName().equalsIgnoreCase(cosmeticUser.getPlayer().getName())) {

                if (cosmeticUser.getNearbyPlayers().get(all.getName()) == null) {

                    if (all.getLocation().distanceSquared(cosmeticUser.getPlayer().getLocation()) < 32 * 32 && all.canSee(cosmeticUser.getPlayer())) {

                        createWingsPlayerForPlayer(cosmeticUser.getPlayer(), all);

                        cosmeticUser.getNearbyPlayers().put(all.getName(), true);
                    }
                }
            }
        }

        List<String> toRemove = new ArrayList<>();

        for(String player : cosmeticUser.getNearbyPlayers().keySet()){

            Player toPlayer = Bukkit.getPlayer(player);

            if(toPlayer == null || !toPlayer.isOnline() || toPlayer.getLocation().distanceSquared(cosmeticUser.getPlayer().getLocation()) >= 32 * 32 || !toPlayer.canSee(cosmeticUser.getPlayer())){

                destroyWingsPlayerForPlayer(cosmeticUser.getPlayer(), toPlayer);

                toRemove.add(player);
            }
        }

        for(String player : toRemove){
            cosmeticUser.getNearbyPlayers().remove(player);
        }

    }

    public void createWings(final Player p) {

        final CosmeticUser cosmeticUser = main.getCosmeticStorage().getCosmeticUsers().get(p.getName());

        for (int x = 1; x <= 2; x++) {

            WorldServer s = ((CraftWorld) p.getWorld()).getHandle();

            final EntityArmorStand stand = new EntityArmorStand(EntityTypes.ARMOR_STAND, s);
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setNoGravity(true);

            net.minecraft.server.v1_16_R3.ItemStack wings;
            ItemStack itemStack = new ItemStack(Material.PHANTOM_MEMBRANE);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setCustomModelData(1005);
            itemStack.setItemMeta(meta);
            wings = CraftItemStack.asNMSCopy(itemStack);

            List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> equipmentList = new ArrayList<>();
            equipmentList.add(new Pair<>(EnumItemSlot.HEAD, wings));
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(stand.getId(), equipmentList);

            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(stand));
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(stand.getId(), stand.getDataWatcher(), true));
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
            ((CraftPlayer) p).getHandle().passengers.add(stand);

            final int finalX = x;

            new BukkitRunnable() {
                float lastValue = 0;
                int plusPower = 0;
                int plusPower2 = 0;
                boolean higher = false;

                float lastWing1 = 0.0f;
                float lastWing2 = 0.0f;

                @Override
                public void run() {

                    plusPower2 = 0;

                    if (p.getLocation().getYaw() > 0.0) {
                        int plusNum = 0;
                        if (cosmeticUser.getRotation() == PlayerRotation.LEFT) {
                            plusNum += 10;
                        }
                        plusPower = Math.max(-20 + plusNum, plusPower - 4);
                    } else {
                        plusPower = Math.min(20, plusPower + 4);
                    }

                    if (cosmeticUser.getRotation() == PlayerRotation.RIGHT) {
                        plusPower2 = -30 + cosmeticUser.getTestValue1();
                    } else if (cosmeticUser.getRotation() == PlayerRotation.LEFT) {
                        plusPower2 = 10 + cosmeticUser.getTestValue2();
                    } else {
                        plusPower2 += 10;
                    }

                    float changeSize = 0.5f;

                    if (p.isSprinting() || !p.isOnGround()) {
                        changeSize = 4f;
                    }

                    if (higher) {
                        lastValue += changeSize;
                    } else {
                        lastValue -= changeSize;
                    }
                    if (higher && lastValue >= 30) {
                        higher = false;
                    } else if (!higher && lastValue <= 0) {
                        higher = true;
                    }

                    float newYaw = (p.getLocation().getYaw());
                    if (Math.abs(newYaw) >= 60f) {
                        float multiplier = getMultiplier(newYaw);
                        newYaw /= multiplier;
                        newYaw -= 3;
                    }

                    if (cosmeticUser.isNeedUpdate()) {

                        cosmeticUser.update();

                        if (finalX == 1) {

                            if (plusPower != 0) {
                                p.sendMessage("Plus power > " + plusPower + " Power2 > " + plusPower2);
                            }
                            lastWing1 = (float) (newYaw + (50f) + (plusPower + plusPower2));
                            p.sendMessage("ArmorStand Yaw > " + newYaw);
                            p.sendMessage("Player Yaw > " + p.getLocation().getYaw());
                        } else {
                            if (plusPower != 0) {
                                p.sendMessage("Plus power > " + plusPower + " Power2 > " + plusPower2);
                            }
                            p.sendMessage("ArmorStand Yaw > " + newYaw);
                            p.sendMessage("Player Yaw > " + p.getLocation().getYaw());
                            lastWing2 = (float) (newYaw - (70f) + (plusPower + plusPower2));
                        }
                    }

                    if (finalX == 1) {
                        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation(stand, (byte) (lastWing1 + (lastValue))));
                    } else {
                        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation(stand, (byte) (lastWing2 - (lastValue))));
                    }
                }
            }.runTaskTimer(main, 1L, 1L);
        }
    }
}
