package pl.ghostero.tobiasz.fixes.objects;

import lombok.Data;

@Data
public class WingsRotation {
    float lastValue = 0;
    int plusPower = 0;
    int plusPower2 = 0;
    boolean higher = false;

    float lastWing1 = 0.0f;
    float lastWing2 = 0.0f;
}
