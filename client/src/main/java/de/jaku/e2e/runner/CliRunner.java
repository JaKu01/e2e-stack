package de.jaku.e2e.runner;

import de.jaku.e2e.api.ApiClient;
import de.jaku.e2e.libsignal.KeyStoreService;
import de.jaku.e2e.libsignal.MessagingService;
import lombok.RequiredArgsConstructor;
import org.signal.libsignal.protocol.state.PreKeyBundle;
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CliRunner implements CommandLineRunner {

    private  final KeyStoreService keyStoreService;
    private  final ApiClient apiClient;
    private  final MessagingService messagingService;

    @Override
    public void run(String... args) throws Exception {

        PreKeyBundle myPreKeyBundle;

        if (!apiClient.hasPreKeyBundle("alice")) {
            myPreKeyBundle = keyStoreService.generatePreKeyBundle();
            apiClient.uploadPreKeyBundle("alice", myPreKeyBundle);
        }

        messagingService.sendMessage("bob", 1, "Hello world");

    }
}
