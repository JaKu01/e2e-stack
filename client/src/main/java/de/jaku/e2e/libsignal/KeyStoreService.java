package de.jaku.e2e.libsignal;

import de.jaku.e2e.util.Utils;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.ecc.ECKeyPair;
import org.signal.libsignal.protocol.kem.KEMKeyPair;
import org.signal.libsignal.protocol.kem.KEMKeyType;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyBundle;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore;
import org.signal.libsignal.protocol.util.KeyHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class KeyStoreService {

    @Value("${user.name}")
    private String name;

    private final IdentityKeyPair identityKeyPair;

    private final int registrationId;

    private final int deviceId;

    @Getter
    private final InMemorySignalProtocolStore signalProtocolStore;

    @Getter
    private SignalProtocolAddress address;

    private PreKeyBundle preKeyBundle;

    public KeyStoreService() {
        identityKeyPair = IdentityKeyPair.generate();
        registrationId = KeyHelper.generateRegistrationId(false);
        signalProtocolStore = new InMemorySignalProtocolStore(identityKeyPair, registrationId);
        deviceId = Utils.generateDeviceId();
    }

    @PostConstruct
    private void init() {
        address = new SignalProtocolAddress(name, deviceId);
    }

    public PreKeyBundle generatePreKeyBundle() throws InvalidKeyException {
        if (preKeyBundle != null) {
            return preKeyBundle;
        }

        int nextPreKeyId = getNextPreKeyId();
        int nextSignedPreKeyId = getNextSignedPreKeyId();
        int nextKyberPreSignedKey = getNextKyberPreKeyId();

        ECKeyPair preKey = ECKeyPair.generate();
        PreKeyRecord preKeyRecord = new PreKeyRecord(nextPreKeyId, preKey);

        SignedPreKeyRecord signedPreKey = generateSignedPreKey(nextSignedPreKeyId);
        KyberPreKeyRecord kyberPreKeyRecord = generateKyberPreKey(nextKyberPreSignedKey);

        signalProtocolStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
        signalProtocolStore.storeSignedPreKey(signedPreKey.getId(), signedPreKey);
        signalProtocolStore.storeKyberPreKey(kyberPreKeyRecord.getId(), kyberPreKeyRecord);

        preKeyBundle = new PreKeyBundle(
                registrationId,
                deviceId,
                preKeyRecord.getId(),
                preKey.getPublicKey(),
                signedPreKey.getId(),
                signedPreKey.getKeyPair().getPublicKey(),
                signedPreKey.getSignature(),
                identityKeyPair.getPublicKey(),
                kyberPreKeyRecord.getId(),
                kyberPreKeyRecord.getKeyPair().getPublicKey(),
                kyberPreKeyRecord.getSignature());
        return preKeyBundle;
    }

    private SignedPreKeyRecord generateSignedPreKey(int signedPreKeyId) {
        ECKeyPair keyPair = ECKeyPair.generate();
        byte[] signature =
                identityKeyPair.getPrivateKey().calculateSignature(keyPair.getPublicKey().serialize());

        return new SignedPreKeyRecord(signedPreKeyId, System.currentTimeMillis(), keyPair, signature);
    }

    private KyberPreKeyRecord generateKyberPreKey(int kyberPreKeyId) {
        KEMKeyPair keyPair = KEMKeyPair.generate(KEMKeyType.KYBER_1024);
        byte[] signature =
                identityKeyPair.getPrivateKey().calculateSignature(keyPair.getPublicKey().serialize());

        return new KyberPreKeyRecord(kyberPreKeyId, System.currentTimeMillis(), keyPair, signature);
    }

    private int getNextSignedPreKeyId() {
        return signalProtocolStore.loadSignedPreKeys().stream()
                .mapToInt(SignedPreKeyRecord::getId)
                .max()
                .orElse(0) + 1;
    }

    private int getNextKyberPreKeyId() {
        return signalProtocolStore.loadKyberPreKeys().stream()
                .mapToInt(KyberPreKeyRecord::getId)
                .max()
                .orElse(0) + 1;
    }

    private int getNextPreKeyId() {
        int randomId = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        while (signalProtocolStore.containsPreKey(randomId)) {
            randomId = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        }
        return randomId;
    }
}
