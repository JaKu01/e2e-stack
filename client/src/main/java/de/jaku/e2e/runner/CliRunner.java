package de.jaku.e2e.runner;

import de.jaku.e2e.api.ApiClient;
import de.jaku.e2e.libsignal.KeyStoreService;
import de.jaku.e2e.libsignal.MessagingService;
import lombok.RequiredArgsConstructor;
import org.signal.libsignal.protocol.state.PreKeyBundle;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;


@Component
@RequiredArgsConstructor
public class CliRunner implements CommandLineRunner {

    public static final String myName = "bob";

    private  final KeyStoreService keyStoreService;
    private  final ApiClient apiClient;
    private  final MessagingService messagingService;

    @Override
    public void run(String... args) throws Exception {

        try {
            apiClient.fetchPreKeyBundle(myName);
        } catch (NoSuchElementException e) {
            PreKeyBundle myPreKeyBundle = keyStoreService.generatePreKeyBundle();
            apiClient.uploadPreKeyBundle(myName, myPreKeyBundle);
        }

        messagingService.sendMessage("bob", 1, "Hello world");
        messagingService.receiveMessage("alice", 107);
    }
}
