package de.jaku.e2e.service;

import lombok.RequiredArgsConstructor;
import org.signal.libsignal.protocol.*;
import org.signal.libsignal.protocol.ecc.ECKeyPair;
import org.signal.libsignal.protocol.kem.KEMKeyPair;
import org.signal.libsignal.protocol.kem.KEMKeyType;
import org.signal.libsignal.protocol.message.CiphertextMessage;
import org.signal.libsignal.protocol.message.PreKeySignalMessage;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyBundle;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore;
import org.signal.libsignal.protocol.util.KeyHelper;
import org.signal.libsignal.protocol.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor

public class SignalProtocolService {

    public void init() throws InvalidKeyException, UntrustedIdentityException, NoSessionException, InvalidMessageException, InvalidVersionException, LegacyMessageException, DuplicateMessageException, InvalidKeyIdException {

        var aliceName = "alice";
        var aliceDeviceId = 1;

        var bobName = "bob";
        var bobDeviceId = 2;

        var aliceStuff = setupKeys(aliceDeviceId);
        SignalProtocolAddress aliceAddress = new SignalProtocolAddress(aliceName, aliceDeviceId);

        var bobStuff = setupKeys(bobDeviceId);
        SignalProtocolAddress bobAddress = new SignalProtocolAddress(bobName, bobDeviceId);

        SessionBuilder aliceSessionBuilder = new SessionBuilder(aliceStuff.second(), bobAddress);
        aliceSessionBuilder.process(bobStuff.first());

        SessionCipher aliceCipher = new SessionCipher(aliceStuff.second(), bobAddress);
        CiphertextMessage encryptedMessage = aliceCipher.encrypt("Hello Bob!".getBytes());

        // convert to base64 for better readability
        String base64Encoded = Base64.getEncoder().encodeToString(encryptedMessage.serialize());
        System.out.println("Alice sends: " + base64Encoded);

        SessionCipher bobCipher = new SessionCipher(bobStuff.second(), aliceAddress);
        byte[] plaintext = bobCipher.decrypt(new PreKeySignalMessage(encryptedMessage.serialize()));
        System.out.println("Bob receives: " + new String(plaintext));

    }

    private static SignedPreKeyRecord generateSignedPreKey(
            IdentityKeyPair identityKeyPair, int signedPreKeyId) {
        ECKeyPair keyPair = ECKeyPair.generate();
        byte[] signature =
                identityKeyPair.getPrivateKey().calculateSignature(keyPair.getPublicKey().serialize());

        return new SignedPreKeyRecord(signedPreKeyId, System.currentTimeMillis(), keyPair, signature);
    }

    private static KyberPreKeyRecord generateKyberPreKey(
            IdentityKeyPair identityKeyPair, int kyberPreKeyId) {
        KEMKeyPair keyPair = KEMKeyPair.generate(KEMKeyType.KYBER_1024);
        byte[] signature =
                identityKeyPair.getPrivateKey().calculateSignature(keyPair.getPublicKey().serialize());

        return new KyberPreKeyRecord(kyberPreKeyId, System.currentTimeMillis(), keyPair, signature);
    }

    private Pair<PreKeyBundle, InMemorySignalProtocolStore> setupKeys(int deviceId) throws InvalidKeyException {

        IdentityKeyPair identityKeyPair = IdentityKeyPair.generate();
        int regId = KeyHelper.generateRegistrationId(false);

        InMemorySignalProtocolStore signalProtocolStore = new InMemorySignalProtocolStore(identityKeyPair, regId);

        var nextPreKeyId = getNextPreKeyId(signalProtocolStore);
        var nextSignedPreKeyId = getNextSignedPreKeyId(signalProtocolStore);
        var nextKyberPreSignedKey =  getNextKyberPreKeyId(signalProtocolStore);

        ECKeyPair preKey = ECKeyPair.generate();
        PreKeyRecord preKeyRecord = new PreKeyRecord(nextPreKeyId, preKey);
        SignedPreKeyRecord signedPreKey = generateSignedPreKey(identityKeyPair, nextSignedPreKeyId);
        KyberPreKeyRecord kyberPreKeyRecord = generateKyberPreKey(identityKeyPair, nextKyberPreSignedKey);

        signalProtocolStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
        signalProtocolStore.storeSignedPreKey(signedPreKey.getId(), signedPreKey);
        signalProtocolStore.storeKyberPreKey(kyberPreKeyRecord.getId(), kyberPreKeyRecord);

        PreKeyBundle preKeyBundle = new PreKeyBundle(
                regId,
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

        return new Pair<>(preKeyBundle, signalProtocolStore);
    }

    private int getNextSignedPreKeyId(InMemorySignalProtocolStore store) {
        return store.loadSignedPreKeys().stream()
                .mapToInt(SignedPreKeyRecord::getId)
                .max()
                .orElse(0) + 1;
    }

    private int getNextKyberPreKeyId(InMemorySignalProtocolStore store) {
        return store.loadKyberPreKeys().stream()
                .mapToInt(KyberPreKeyRecord::getId)
                .max()
                .orElse(0) + 1;
    }

    private int getNextPreKeyId(InMemorySignalProtocolStore store) {
        int randomId = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        while (store.containsPreKey(randomId)) {
            randomId = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        }
        return randomId;
    }

}
