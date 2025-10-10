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
import org.signal.libsignal.protocol.state.SessionRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore;

import java.util.List;

@Entity
@Table(name = "signal_protocol_store")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class SignalProtocolStoreEntity {
    @Id
    private String name;

    @Column(nullable = false)
    byte[] serializedIdentityKey;

    @Column(nullable = false)
    int registrationId;

    @Column(nullable = false)
    int deviceId;

    @OneToMany
    @Column(nullable = false)
    List<SignedPreKeyEntity> signedPreKeys;

    @OneToMany
    @Column(nullable = false)
    List<KyberPreKeyEntity> kyberPreKeys;

    @OneToMany
    @Column(nullable = false)
    List<SessionRecordEntity> sessionRecords;

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

        return signalProtocolStore;
    }
}
