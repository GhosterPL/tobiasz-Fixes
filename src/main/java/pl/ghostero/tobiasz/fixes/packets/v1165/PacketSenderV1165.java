package pl.ghostero.tobiasz.fixes.packets.v1165;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.ghostero.tobiasz.fixes.objects.CosmeticUser;
import pl.ghostero.tobiasz.fixes.objects.WingsRotation;
import pl.ghostero.tobiasz.fixes.packets.PacketSender;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PacketSenderV1165 implements PacketSender {

    @Override
    public void prepareWingsForPlayer(CosmeticUser cosmeticUser) {
        Player p = cosmeticUser.getPlayer();

        WorldServer s = ((CraftWorld) p.getWorld()).getHandle();

        for (int x = 1; x <= 2; x++) {

            final EntityArmorStand stand = new EntityArmorStand(EntityTypes.ARMOR_STAND, s);
            stand.setPosition(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setInvulnerable(true);

            cosmeticUser.getWings().put(x, stand);
        }
    }

    @Override
    public void createWingsPlayerForPlayer(CosmeticUser cosmeticUser, Player toPlayer) {
        Player p = cosmeticUser.getPlayer();

        for (int x = 1; x <= 2; x++) {

            net.minecraft.server.v1_16_R3.ItemStack wings;
            ItemStack itemStack = new ItemStack(Material.PHANTOM_MEMBRANE);
            ItemMeta meta = itemStack.getItemMeta();
            if (x == 1) {
                meta.setCustomModelData(cosmeticUser.getWing().getLeftWing());
            } else {
                meta.setCustomModelData(cosmeticUser.getWing().getRightWing());
            }
            itemStack.setItemMeta(meta);
            wings = CraftItemStack.asNMSCopy(itemStack);

            EntityArmorStand stand = (EntityArmorStand) cosmeticUser.getWings().get(x);

            stand.setPosition(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());

            List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> equipmentList = new ArrayList<>();
            equipmentList.add(new Pair<>(EnumItemSlot.HEAD, wings));
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(stand.getId(), equipmentList);

            ((CraftPlayer) toPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(stand));
            ((CraftPlayer) toPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(stand.getId(), stand.getDataWatcher(), true));
            ((CraftPlayer) toPlayer).getHandle().playerConnection.sendPacket(packet);
        }
    }

    @Override
    public void destroyWingsPlayerForPlayer(CosmeticUser cosmeticUser, Player forPlayer) {
        if(forPlayer != null && forPlayer.isOnline()){
            EntityArmorStand armorStand1 = (EntityArmorStand) cosmeticUser.getWings().get(1);
            EntityArmorStand armorStand2 = (EntityArmorStand) cosmeticUser.getWings().get(2);
            ((CraftPlayer) forPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(armorStand1.getId()));
            ((CraftPlayer) forPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(armorStand2.getId()));
        }
    }

    @Override
    public void updateWingsForPlayer(CosmeticUser cosmeticUser, Player toPlayer, WingsRotation wingsRotation) {

        Player p = cosmeticUser.getPlayer();

        EntityArmorStand stand1 = (EntityArmorStand) cosmeticUser.getWings().get(1);
        EntityArmorStand stand2 = (EntityArmorStand) cosmeticUser.getWings().get(2);

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

    @Override
    public void hidePlayerWings(CosmeticUser cosmeticUser) {
        for (int x = 1; x <= 2; x++) {
            EntityArmorStand stand = (EntityArmorStand) cosmeticUser.getWings().get(x);
            List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> equipmentList = new ArrayList<>();
            equipmentList.add(new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.AIR))));
            ((CraftPlayer) cosmeticUser.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(stand.getId(), equipmentList));
        }
    }

    @Override
    public void showPlayerWings(CosmeticUser cosmeticUser) {
        for (int x = 1; x <= 2; x++) {
            EntityArmorStand stand = (EntityArmorStand) cosmeticUser.getWings().get(x);
            List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> equipmentList = new ArrayList<>();

            net.minecraft.server.v1_16_R3.ItemStack wings;
            ItemStack itemStack = new ItemStack(Material.PHANTOM_MEMBRANE);
            ItemMeta meta = itemStack.getItemMeta();
            if (x == 1) {
                meta.setCustomModelData(cosmeticUser.getWing().getLeftWing());
            } else {
                meta.setCustomModelData(cosmeticUser.getWing().getRightWing());
            }
            itemStack.setItemMeta(meta);
            wings = CraftItemStack.asNMSCopy(itemStack);

            equipmentList.add(new Pair<>(EnumItemSlot.HEAD, wings));
            ((CraftPlayer) cosmeticUser.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(stand.getId(), equipmentList));
        }
    }
}
