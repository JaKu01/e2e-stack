package de.jaku.e2e.libsignal;

import de.jaku.e2e.api.ApiClient;
import lombok.RequiredArgsConstructor;
import org.signal.libsignal.protocol.*;
import org.signal.libsignal.protocol.message.CiphertextMessage;
import org.signal.libsignal.protocol.message.PreKeySignalMessage;
import org.signal.libsignal.protocol.state.PreKeyBundle;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CryptoService {

    private final KeyStoreService keyStoreService;
    private final ApiClient apiClient;

    public void ensureSession(SignalProtocolAddress remoteAddress)
            throws InvalidKeyException, UntrustedIdentityException {

        if (keyStoreService.getSignalProtocolStore().containsSession(remoteAddress)) {
            // If no session exists, fetch a PreKeyBundle from the server and build a new session.
            PreKeyBundle bundle = apiClient.fetchPreKeyBundle(remoteAddress);
            SessionBuilder builder = new SessionBuilder(keyStoreService.getSignalProtocolStore(), remoteAddress);
            builder.process(bundle);
        }
    }

    public CiphertextMessage encrypt(SignalProtocolAddress remoteAddress, String plaintext)
            throws UntrustedIdentityException, InvalidKeyException, NoSessionException {
        ensureSession(remoteAddress);
        SessionCipher cipher = new SessionCipher(keyStoreService.getSignalProtocolStore(), remoteAddress);
        return cipher.encrypt(plaintext.getBytes());
    }

    public String decrypt(SignalProtocolAddress remoteAddress, CiphertextMessage ciphertext)
            throws InvalidMessageException, DuplicateMessageException,
            LegacyMessageException, UntrustedIdentityException, InvalidKeyException, InvalidVersionException, InvalidKeyIdException {
        ensureSession(remoteAddress);
        SessionCipher cipher = new SessionCipher(keyStoreService.getSignalProtocolStore(), remoteAddress);
        byte[] plaintext = cipher.decrypt(new PreKeySignalMessage(ciphertext.serialize()));
        return new String(plaintext);
    }
}


