package de.jaku.e2e.libsignal;

import de.jaku.e2e.persistence.*;
import de.jaku.e2e.runner.CliRunner;
import de.jaku.e2e.util.Utils;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.ecc.ECKeyPair;
import org.signal.libsignal.protocol.state.*;
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore;
import org.signal.libsignal.protocol.util.KeyHelper;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Getter
@Log4j2
@Transactional
public class KeyStoreService {

    private final String name;

    private int deviceId;

    private InMemorySignalProtocolStore signalProtocolStore;

    private Set<SignalProtocolAddress> sessions;

    private final SignalProtocolStoreRepository signalProtocolStoreRepository;

    private PreKeyBundle preKeyBundle;

    private final List<PreKeyRecord> preKeys;


    public KeyStoreService(SignalProtocolStoreRepository signalProtocolStoreRepository) {
        this.signalProtocolStoreRepository = signalProtocolStoreRepository;
        this.name = CliRunner.myName;
        this.preKeys = new ArrayList<>();
        retrieveOrCreateFromDatabase();
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

        SignedPreKeyRecord signedPreKey = Utils.generateSignedPreKey(nextSignedPreKeyId, signalProtocolStore.getIdentityKeyPair());
        KyberPreKeyRecord kyberPreKeyRecord = Utils.generateKyberPreKey(nextKyberPreSignedKey, signalProtocolStore.getIdentityKeyPair());

        signalProtocolStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
        signalProtocolStore.storeSignedPreKey(signedPreKey.getId(), signedPreKey);
        signalProtocolStore.storeKyberPreKey(kyberPreKeyRecord.getId(), kyberPreKeyRecord);

        preKeys.add(preKeyRecord);

        persistToDatabase();

        preKeyBundle = new PreKeyBundle(
                signalProtocolStore.getLocalRegistrationId(),
                deviceId,
                preKeyRecord.getId(),
                preKey.getPublicKey(),
                signedPreKey.getId(),
                signedPreKey.getKeyPair().getPublicKey(),
                signedPreKey.getSignature(),
                signalProtocolStore.getIdentityKeyPair().getPublicKey(),
                kyberPreKeyRecord.getId(),
                kyberPreKeyRecord.getKeyPair().getPublicKey(),
                kyberPreKeyRecord.getSignature());
        return preKeyBundle;
    }

    private void retrieveOrCreateFromDatabase() {
        Optional<SignalProtocolStoreEntity> optionalStoreEntity = signalProtocolStoreRepository.findById(name);
        if (optionalStoreEntity.isEmpty()) {
            log.info("No existing SignalProtocolStore found for name {}, creating new one", name);
            IdentityKeyPair identityKeyPair = IdentityKeyPair.generate();
            int registrationId = KeyHelper.generateRegistrationId(false);
            signalProtocolStore = new InMemorySignalProtocolStore(identityKeyPair, registrationId);
            deviceId = Utils.generateDeviceId();
            sessions = new HashSet<>();
            persistToDatabase();
        } else {
            log.info("Existing SignalProtocolStore found for name {}, loading from database", name);
            signalProtocolStore = optionalStoreEntity.get().toSignalProtocolStore();
            deviceId = optionalStoreEntity.get().getDeviceId();
            sessions = optionalStoreEntity.get().getSessions();
        }
    }

    private void persistToDatabase() {
        List<SessionRecordEntity> sessionRecordEntities = sessions.stream().map(sessionAddress -> {
            SessionRecord sessionRecord = signalProtocolStore.loadSession(sessionAddress);
            return SessionRecordEntity.builder()
                    .name(sessionAddress.getName())
                    .deviceId(sessionAddress.getDeviceId())
                    .serializedSessionRecord(sessionRecord.serialize())
                    .build();
        }).toList();

        List<PreKeyEntity> preKeyEntities = preKeys.stream().map(preKeyRecord -> PreKeyEntity.builder()
                .serializedPreKey(preKeyRecord.serialize())
                .build()).toList();

        SignalProtocolStoreEntity signalProtocolStoreEntity = SignalProtocolStoreEntity.builder()
                .name(name)
                .serializedIdentityKey(signalProtocolStore.getIdentityKeyPair().serialize())
                .registrationId(signalProtocolStore.getLocalRegistrationId())
                .deviceId(deviceId)
                .signedPreKeys(signalProtocolStore.loadSignedPreKeys().stream().map(signedPreKeyRecord -> SignedPreKeyEntity.builder()
                        .serializedSignedPreKey(signedPreKeyRecord.serialize())
                        .build()).toList())
                .kyberPreKeys(signalProtocolStore.loadKyberPreKeys().stream().map(kyberPreKeyRecord -> KyberPreKeyEntity.builder()
                        .serializedKyberPreKey(kyberPreKeyRecord.serialize()).build()).toList())
                .sessionRecords(sessionRecordEntities)
                .preKeys(preKeyEntities)
                .build();
        signalProtocolStoreRepository.save(signalProtocolStoreEntity);
    }
}
