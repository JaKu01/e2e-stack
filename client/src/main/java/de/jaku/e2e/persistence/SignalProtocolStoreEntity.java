package de.jaku.e2e.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SessionRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "signal_protocol_store")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class SignalProtocolStoreEntity {
    @Id
    private String name;

    @Column(nullable = false, length = 8192)
    byte[] serializedIdentityKey;

    @Column(nullable = false)
    int registrationId;

    @Column(nullable = false)
    int deviceId;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(nullable = false)
    List<SignedPreKeyEntity> signedPreKeys;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(nullable = false)
    List<KyberPreKeyEntity> kyberPreKeys;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(nullable = false)
    List<SessionRecordEntity> sessionRecords;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(nullable = false)
    List<PreKeyEntity> preKeys;

    public Set<SignalProtocolAddress> getSessions() {
        return sessionRecords.stream().map(sre ->
                new SignalProtocolAddress(sre.getName(), sre.getDeviceId())).collect(Collectors.toSet());
    }

    public InMemorySignalProtocolStore toSignalProtocolStore() {
        InMemorySignalProtocolStore signalProtocolStore = new InMemorySignalProtocolStore(
                new IdentityKeyPair(serializedIdentityKey),
                registrationId);

        signedPreKeys.stream().map(signedPreKey -> {
            try {
                return new SignedPreKeyRecord(signedPreKey.getSerializedSignedPreKey());
            } catch (InvalidMessageException e) {
                throw new RuntimeException(e);
            }
        }).forEach(signedPreKeyRecord ->
                signalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord));

        kyberPreKeys.stream().map(kyberPreKey -> {
            try {
                return new KyberPreKeyRecord(kyberPreKey.getSerializedKyberPreKey());
            } catch (InvalidMessageException e) {
                throw new RuntimeException(e);
            }
        }).forEach(kyberPreKeyrecord ->
                signalProtocolStore.storeKyberPreKey(kyberPreKeyrecord.getId(), kyberPreKeyrecord));

        sessionRecords.forEach(sessionRecordentity -> {
            try {
                SessionRecord sessionRecord = new SessionRecord(sessionRecordentity.getSerializedSessionRecord());
                SignalProtocolAddress address = new SignalProtocolAddress(sessionRecordentity.getName(), sessionRecordentity.getDeviceId());
                signalProtocolStore.storeSession(address, sessionRecord);
            } catch (InvalidMessageException e) {
                throw new RuntimeException(e);
            }
        });

        preKeys.stream().map(preKey -> {
            try {
                return new PreKeyRecord(preKey.getSerializedPreKey());
            } catch (InvalidMessageException e) {
                throw new RuntimeException(e);
            }
        }).forEach(preKeyRecord ->
                signalProtocolStore.storePreKey(preKeyRecord.getId(), preKeyRecord));
        return signalProtocolStore;
    }
}
