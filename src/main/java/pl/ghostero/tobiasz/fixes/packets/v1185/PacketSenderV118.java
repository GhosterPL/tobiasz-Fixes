package pl.ghostero.tobiasz.fixes.packets.v1185;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.ghostero.tobiasz.fixes.objects.CosmeticUser;
import pl.ghostero.tobiasz.fixes.objects.WingsRotation;
import pl.ghostero.tobiasz.fixes.packets.PacketSender;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PacketSenderV118 implements PacketSender {

    @Override
    public void prepareWingsForPlayer(CosmeticUser cosmeticUser) {
        Player p = cosmeticUser.getPlayer();

        WorldServer s = ((CraftWorld) p.getWorld()).getHandle();

        for (int x = 1; x <= 2; x++) {

            final EntityArmorStand stand = new EntityArmorStand(s, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
            stand.o(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
            stand.j(true);
            stand.t(true);
            stand.m(true);

            cosmeticUser.getWings().put(x, stand);
        }
    }

    @Override
    public void createWingsPlayerForPlayer(CosmeticUser cosmeticUser, Player toPlayer) {
        Player p = cosmeticUser.getPlayer();

        for (int x = 1; x <= 2; x++) {


            net.minecraft.world.item.ItemStack wings;
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

            stand.o(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());

            List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> equipmentList = new ArrayList<>();
            equipmentList.add(new Pair<>(EnumItemSlot.f, wings));
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(stand.ae(), equipmentList);

            ((CraftPlayer) toPlayer).getHandle().b.a(new PacketPlayOutSpawnEntityLiving(stand));
            ((CraftPlayer) toPlayer).getHandle().b.a(new PacketPlayOutEntityMetadata(stand.ae(), stand.ai(), true));
            ((CraftPlayer) toPlayer).getHandle().b.a(packet);
        }
    }

    @Override
    public void destroyWingsPlayerForPlayer(CosmeticUser cosmeticUser, Player forPlayer) {
        if (forPlayer != null && forPlayer.isOnline()) {
            EntityArmorStand armorStand1 = (EntityArmorStand) cosmeticUser.getWings().get(1);
            EntityArmorStand armorStand2 = (EntityArmorStand) cosmeticUser.getWings().get(2);
            ((CraftPlayer) forPlayer).getHandle().b.a(new PacketPlayOutEntityDestroy(armorStand1.ae(), armorStand2.ae()));
        }
    }

    @Override
    public void updateWingsForPlayer(CosmeticUser cosmeticUser, Player toPlayer, WingsRotation wingsRotation) {

        Player p = cosmeticUser.getPlayer();

        EntityArmorStand stand1 = (EntityArmorStand) cosmeticUser.getWings().get(1);
        EntityArmorStand stand2 = (EntityArmorStand) cosmeticUser.getWings().get(2);

        stand1.o(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
        stand2.o(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());

        ((CraftPlayer) toPlayer).getHandle().b.a(new PacketPlayOutEntityHeadRotation(stand1, (byte) (wingsRotation.getLastWing1() + (wingsRotation.getLastValue()))));
        ((CraftPlayer) toPlayer).getHandle().b.a(new PacketPlayOutEntityHeadRotation(stand2, (byte) (wingsRotation.getLastWing2() - (wingsRotation.getLastValue()))));

        PacketPlayOutMount packetMount = new PacketPlayOutMount(((CraftPlayer) p).getHandle());
        try {
            Field passengersField = packetMount.getClass().getDeclaredField("b");
            passengersField.setAccessible(true);
            passengersField.set(packetMount, new int[]{stand1.ae(), stand2.ae()});

            ((CraftPlayer) toPlayer).getHandle().b.a(packetMount);

        } catch (Exception exc) {

        }
    }

    @Override
    public void hidePlayerWings(CosmeticUser cosmeticUser) {
        if(cosmeticUser.hasWings()) {
            for (int x = 1; x <= 2; x++) {
                EntityArmorStand stand = (EntityArmorStand) cosmeticUser.getWings().get(x);

                List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> equipmentList = new ArrayList<>();

                equipmentList.add(new Pair<>(EnumItemSlot.f, CraftItemStack.asNMSCopy(new ItemStack(Material.AIR))));
                ((CraftPlayer) cosmeticUser.getPlayer()).getHandle().b.a(new PacketPlayOutEntityEquipment(stand.ae(), equipmentList));
            }
        }
    }

    @Override
    public void showPlayerWings(CosmeticUser cosmeticUser) {
        if(cosmeticUser.hasWings()) {
            for (int x = 1; x <= 2; x++) {
                EntityArmorStand stand = (EntityArmorStand) cosmeticUser.getWings().get(x);

                List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> equipmentList = new ArrayList<>();

                net.minecraft.world.item.ItemStack wings;
                ItemStack itemStack = new ItemStack(Material.PHANTOM_MEMBRANE);
                ItemMeta meta = itemStack.getItemMeta();
                if (x == 1) {
                    meta.setCustomModelData(cosmeticUser.getWing().getLeftWing());
                } else {
                    meta.setCustomModelData(cosmeticUser.getWing().getRightWing());
                }
                itemStack.setItemMeta(meta);
                wings = CraftItemStack.asNMSCopy(itemStack);
                equipmentList.add(new Pair<>(EnumItemSlot.f, wings));
                ((CraftPlayer) cosmeticUser.getPlayer()).getHandle().b.a(new PacketPlayOutEntityEquipment(stand.ae(), equipmentList));
            }
        }
    }
}