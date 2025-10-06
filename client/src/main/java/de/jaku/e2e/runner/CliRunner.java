package de.jaku.e2e.runner;

import de.jaku.e2e.service.SignalProtocolService;
import lombok.RequiredArgsConstructor;
import org.signal.libsignal.protocol.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CliRunner implements CommandLineRunner {

    private final SignalProtocolService signalProtocolService;

    @Override
    public void run(String... args) throws InvalidKeyException, NoSessionException, InvalidMessageException, UntrustedIdentityException, DuplicateMessageException, InvalidVersionException, InvalidKeyIdException, LegacyMessageException {

        signalProtocolService.init();
    }
}
