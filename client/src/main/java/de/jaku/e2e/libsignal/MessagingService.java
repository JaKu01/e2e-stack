package de.jaku.e2e.libsignal;

import de.jaku.e2e.api.ApiClient;
import de.jaku.e2e.runner.CliRunner;
import lombok.RequiredArgsConstructor;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.message.CiphertextMessage;
import org.signal.libsignal.protocol.message.PreKeySignalMessage;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MessagingService {

    private final CryptoService crypto;
    private final ApiClient apiClient;

    public void sendMessage(String toUser, int toDevice, String message)
            throws Exception {
        SignalProtocolAddress remoteAddress = new SignalProtocolAddress(toUser, toDevice);
        CiphertextMessage ciphertext = crypto.encrypt(remoteAddress, message);
        apiClient.sendMessage(remoteAddress, ciphertext.serialize());
    }

    public String receiveMessage(String fromUser, int fromDevice)
            throws Exception {
        byte[] encryptedBytes = apiClient.receiveMessage(CliRunner.myName);
        SignalProtocolAddress remoteAddress = new SignalProtocolAddress(fromUser, fromDevice);
        CiphertextMessage ciphertext = new PreKeySignalMessage(encryptedBytes);
        return crypto.decrypt(remoteAddress, ciphertext);
    }
}
