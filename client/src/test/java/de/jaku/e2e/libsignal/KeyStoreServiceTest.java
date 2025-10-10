package de.jaku.e2e.libsignal;

import de.jaku.e2e.persistence.SignalProtocolStoreRepository;
import de.jaku.e2e.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.state.*;
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = "user.name=TestUser")
class KeyStoreServiceTest {

    private KeyStoreService keyStoreService;

    @MockitoBean
    private SignalProtocolStoreRepository signalProtocolStoreRepository;

    @BeforeEach
    void setUp() {
        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
            mockedUtils.when(Utils::generateDeviceId).thenReturn(1);
            keyStoreService = new KeyStoreService(signalProtocolStoreRepository);
        }
    }

    @Test
    void testGeneratePreKeyBundle() throws InvalidKeyException {
        PreKeyBundle preKeyBundle = keyStoreService.generatePreKeyBundle();

        assertNotNull(preKeyBundle);
        assertNotNull(preKeyBundle.getIdentityKey());
        assertNotNull(preKeyBundle.getPreKey());
        assertNotNull(preKeyBundle.getSignedPreKey());
        assertNotNull(preKeyBundle.getKyberPreKey());
    }

    @Test
    void testGeneratePreKeyBundleCaching() throws InvalidKeyException {
        PreKeyBundle firstBundle = keyStoreService.generatePreKeyBundle();
        PreKeyBundle secondBundle = keyStoreService.generatePreKeyBundle();

        assertSame(firstBundle, secondBundle);
    }

    @Test
    void testGetNextPreKeyId() throws Exception {
        InMemorySignalProtocolStore store = keyStoreService.getSignalProtocolStore();
        int preKeyId = keyStoreService.generatePreKeyBundle().getPreKeyId();

        PreKeyRecord preKeyRecord = store.loadPreKey(preKeyId);
        assertNotNull(preKeyRecord);
        assertEquals(preKeyId, preKeyRecord.getId());
    }

    @Test
    void testGetNextSignedPreKeyId() throws Exception {
        InMemorySignalProtocolStore store = keyStoreService.getSignalProtocolStore();
        int signedPreKeyId = keyStoreService.generatePreKeyBundle().getSignedPreKeyId();

        SignedPreKeyRecord signedPreKeyRecord = store.loadSignedPreKey(signedPreKeyId);
        assertNotNull(signedPreKeyRecord);
        assertEquals(signedPreKeyId, signedPreKeyRecord.getId());
    }

    @Test
    void testGetNextKyberPreKeyId() throws Exception {
        InMemorySignalProtocolStore store = keyStoreService.getSignalProtocolStore();
        int kyberPreKeyId = keyStoreService.generatePreKeyBundle().getKyberPreKeyId();

        KyberPreKeyRecord kyberPreKeyRecord = store.loadKyberPreKey(kyberPreKeyId);
        assertNotNull(kyberPreKeyRecord);
        assertEquals(kyberPreKeyId, kyberPreKeyRecord.getId());
    }
}