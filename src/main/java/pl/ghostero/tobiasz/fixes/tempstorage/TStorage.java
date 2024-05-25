package pl.ghostero.tobiasz.fixes.tempstorage;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class TStorage {

    @Getter
    private Map<String, Wing> playerWings = new HashMap<>();

    public TStorage(){
        playerWings.put("Ghostero", new Wing(1006, 1006));
    }
}
