package de.jaku.e2e.libsignal;

import de.jaku.e2e.persistence.*;
import de.jaku.e2e.util.Utils;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.ecc.ECKeyPair;
import org.signal.libsignal.protocol.state.*;
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore;
import org.signal.libsignal.protocol.util.KeyHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Getter
public class KeyStoreService {

    @Value("${user.name}")
    private String name;

    private final IdentityKeyPair identityKeyPair;

    private final int registrationId;

    private final int deviceId;

    private final InMemorySignalProtocolStore signalProtocolStore;

    private final Set<SignalProtocolAddress> sessions;

    private final SignalProtocolStoreRepository signalProtocolStoreRepository;

    private SignalProtocolAddress address;

    private PreKeyBundle preKeyBundle;


    public KeyStoreService(SignalProtocolStoreRepository signalProtocolStoreRepository) {
        identityKeyPair = IdentityKeyPair.generate();
        registrationId = KeyHelper.generateRegistrationId(false);
        signalProtocolStore = new InMemorySignalProtocolStore(identityKeyPair, registrationId);
        deviceId = Utils.generateDeviceId();
        sessions = new HashSet<>();
        this.signalProtocolStoreRepository = signalProtocolStoreRepository;
    }

    @PostConstruct
    private void init() {
        address = new SignalProtocolAddress(name, deviceId);
    }

    public PreKeyBundle generatePreKeyBundle() throws InvalidKeyException {
        if (preKeyBundle != null) {
            return preKeyBundle;
        }

        int nextPreKeyId = Utils.getNextPreKeyId(signalProtocolStore);
        int nextSignedPreKeyId = Utils.getNextSignedPreKeyId(signalProtocolStore);
        int nextKyberPreSignedKey = Utils.getNextKyberPreKeyId(signalProtocolStore);

        ECKeyPair preKey = ECKeyPair.generate();
        PreKeyRecord preKeyRecord = new PreKeyRecord(nextPreKeyId, preKey);

        SignedPreKeyRecord signedPreKey = Utils.generateSignedPreKey(nextSignedPreKeyId, identityKeyPair);
        KyberPreKeyRecord kyberPreKeyRecord = Utils.generateKyberPreKey(nextKyberPreSignedKey, identityKeyPair);

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

    public void persistKeyStore() {
        List<SessionRecordEntity> sessionRecordEntities = sessions.stream().map(sessionAddress -> {
            SessionRecord sessionRecord = signalProtocolStore.loadSession(sessionAddress);
            return SessionRecordEntity.builder()
                    .name(sessionAddress.getName())
                    .deviceId(sessionAddress.getDeviceId())
                    .serializedSessionRecord(sessionRecord.serialize())
                    .build();
        }).toList();

        SignalProtocolStoreEntity signalProtocolStoreEntity = SignalProtocolStoreEntity.builder()
                .name(name)
                .serializedIdentityKey(identityKeyPair.serialize())
                .registrationId(registrationId)
                .deviceId(deviceId)
                .signedPreKeys(signalProtocolStore.loadSignedPreKeys().stream().map(signedPreKeyRecord -> SignedPreKeyEntity.builder()
                        .serializedSignedPreKey(signedPreKeyRecord.serialize())
                        .build()).toList())
                .kyberPreKeys(signalProtocolStore.loadKyberPreKeys().stream().map(kyberPreKeyRecord -> KyberPreKeyEntity.builder()
                        .serializedKyberPreKey(kyberPreKeyRecord.serialize()).build()).toList())
                .sessionRecords(sessionRecordEntities)
                .build();
        signalProtocolStoreRepository.save(signalProtocolStoreEntity);
    }
}
