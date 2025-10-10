package de.jaku.e2e.util;

import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.ecc.ECKeyPair;
import org.signal.libsignal.protocol.kem.KEMKeyPair;
import org.signal.libsignal.protocol.kem.KEMKeyType;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.SignalProtocolStore;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    public static int generateDeviceId() {
        UUID uuid = UUID.randomUUID();
        return Math.abs((int) uuid.getMostSignificantBits()) % 127 + 1;
    }

    public static SignedPreKeyRecord generateSignedPreKey(int signedPreKeyId, IdentityKeyPair identityKeyPair) {
        ECKeyPair keyPair = ECKeyPair.generate();
        byte[] signature =
                identityKeyPair.getPrivateKey().calculateSignature(keyPair.getPublicKey().serialize());

        return new SignedPreKeyRecord(signedPreKeyId, System.currentTimeMillis(), keyPair, signature);
    }

    public static KyberPreKeyRecord generateKyberPreKey(int kyberPreKeyId, IdentityKeyPair identityKeyPair) {
        KEMKeyPair keyPair = KEMKeyPair.generate(KEMKeyType.KYBER_1024);
        byte[] signature =
                identityKeyPair.getPrivateKey().calculateSignature(keyPair.getPublicKey().serialize());

        return new KyberPreKeyRecord(kyberPreKeyId, System.currentTimeMillis(), keyPair, signature);
    }

    public static int getNextSignedPreKeyId(SignalProtocolStore signalProtocolStore) {
        return signalProtocolStore.loadSignedPreKeys().stream()
                .mapToInt(SignedPreKeyRecord::getId)
                .max()
                .orElse(0) + 1;
    }

    public static int getNextKyberPreKeyId(SignalProtocolStore signalProtocolStore) {
        return signalProtocolStore.loadKyberPreKeys().stream()
                .mapToInt(KyberPreKeyRecord::getId)
                .max()
                .orElse(0) + 1;
    }

    public static int getNextPreKeyId(SignalProtocolStore signalProtocolStore) {
        int randomId = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        while (signalProtocolStore.containsPreKey(randomId)) {
            randomId = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        }
        return randomId;
    }
}
