package pl.ghostero.tobiasz.fixes.objects;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CosmeticStorage {

    private Map<String, CosmeticUser> cosmeticUsers = new HashMap<>();
}
