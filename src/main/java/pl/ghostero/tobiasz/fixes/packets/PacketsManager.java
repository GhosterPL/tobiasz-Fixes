package pl.ghostero.tobiasz.fixes.packets;

import lombok.Getter;
import org.bukkit.Bukkit;
import pl.ghostero.tobiasz.fixes.packets.v1165.PacketSenderV1165;
import pl.ghostero.tobiasz.fixes.packets.v1185.PacketSenderV118;

public class PacketsManager {

    @Getter
    private PacketSender packetSender;

    public PacketsManager() {
        for(int x = 1; x <= 50; x++) {
            System.out.println("Server version -> " + Bukkit.getBukkitVersion());
        }
        switch(Bukkit.getBukkitVersion()){
            case "1.18.2-R0.1-SNAPSHOT":
                System.out.println("Load 1.18.2 Library");
                packetSender = new PacketSenderV118();
                break;
            default:
                System.out.println("Load Default Library");
                packetSender = new PacketSenderV1165();
                break;

        }

    }
}