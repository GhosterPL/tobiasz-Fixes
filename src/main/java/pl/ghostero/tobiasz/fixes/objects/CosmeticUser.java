package pl.ghostero.tobiasz.fixes.objects;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Data
public class CosmeticUser {

    private final Player player;
    private final long joinTime = System.currentTimeMillis();
    private float lastYaw = 0.0f;
    private int needUpdate = 0;
    private WingsRotation wingsRotation = new WingsRotation();
    private PlayerRotation rotation = PlayerRotation.NONE;

    private int modelLeft = 1005;
    private int modelRight = 1005;

    private int testValue1 = 0;
    private int testValue2 = 0;

    private Map<String, Boolean> nearbyPlayers = new HashMap<>();

    private Map<Integer, EntityArmorStand> wings = new HashMap<>();

    public boolean hasWings(){
        return wings.size() > 0;
    }

    public void setNeedUpdate(boolean state){
        if(state){
            needUpdate = 0;
        } else {
            needUpdate = 2;
        }
    }

    public boolean isNeedUpdate(){
        return needUpdate < 2;
    }

    public void update(){
        this.needUpdate++;
    }

}
