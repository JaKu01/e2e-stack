package de.jaku.e2e.libsignal;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

class CryptoServiceTest {
    @MockitoBean
    private KeyStoreService keyStoreService;

}
