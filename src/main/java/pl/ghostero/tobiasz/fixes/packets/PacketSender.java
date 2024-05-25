package pl.ghostero.tobiasz.fixes.packets;

import org.bukkit.entity.Player;
import pl.ghostero.tobiasz.fixes.objects.CosmeticUser;
import pl.ghostero.tobiasz.fixes.objects.WingsRotation;

public interface PacketSender {
    void prepareWingsForPlayer(CosmeticUser cosmeticUser);
    void createWingsPlayerForPlayer(CosmeticUser cosmeticUser, Player toPlayer);
    void destroyWingsPlayerForPlayer(CosmeticUser cosmeticUser, Player forPlayer);
    void updateWingsForPlayer(CosmeticUser cosmeticUser, Player toPlayer, WingsRotation wingsRotation);
    void hidePlayerWings(CosmeticUser cosmeticUser);
    void showPlayerWings(CosmeticUser cosmeticUser);
}
