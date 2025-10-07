package de.jaku.e2e.util;

import java.util.UUID;

public class Utils {
    public static int generateDeviceId() {
        UUID uuid = UUID.randomUUID();
        return Math.abs((int) uuid.getMostSignificantBits()) % 127 + 1;
    }
}
