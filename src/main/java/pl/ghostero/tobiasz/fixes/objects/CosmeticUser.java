package pl.ghostero.tobiasz.fixes.objects;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import pl.ghostero.tobiasz.fixes.tempstorage.Wing;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Data
public class CosmeticUser {

    private final Player player;
    private float lastYaw = 0.0f;
    private int needUpdate = 0;
    private WingsRotation wingsRotation = new WingsRotation();
    private PlayerRotation rotation = PlayerRotation.NONE;
    private PlayerRotation hardRotation = PlayerRotation.NONE;
    private boolean swimming = false;
    private boolean temporaryHidden = false;
    private long lastBackward = 0L;

    private Wing wing;

    private Map<String, Boolean> nearbyPlayers = new HashMap<>();

    private Map<Integer, Object> wings = new HashMap<>();

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
