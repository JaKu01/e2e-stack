package de.jaku.e2e.api;

import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.state.PreKeyBundle;
import org.springframework.stereotype.Component;

@Component
public class ApiClient {

    public PreKeyBundle fetchPreKeyBundle(SignalProtocolAddress address) {
        // TODO

        return null;
    }

    public void sendMessage(SignalProtocolAddress to, byte[] message) {
        // TODO
    }
}
