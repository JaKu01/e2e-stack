package de.jaku.e2e.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.signal.libsignal.protocol.IdentityKey;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.ecc.ECPublicKey;
import org.signal.libsignal.protocol.kem.KEMPublicKey;
import tools.jackson.databind.ObjectMapper;
import org.signal.libsignal.protocol.state.PreKeyBundle;

import java.util.Base64;

public class PreKeyBundleJsonSerializer {

    public static String serializeToJson(PreKeyBundle preKeyBundle) {
        ObjectMapper objectMapper = new ObjectMapper();
        PreKeyBundleJson preKeyBundleJson = new PreKeyBundleJson(preKeyBundle);
        return objectMapper.writeValueAsString(preKeyBundleJson);
    }

    private static class PreKeyBundleJson {
        @JsonProperty
        private  int registrationId;
        @JsonProperty
        private  int deviceId;
        @JsonProperty
        private  int preKeyId;
        @JsonProperty
        private  String preKeyPublic;
        @JsonProperty
        private  int signedPreKeyId;
        @JsonProperty
        private  String signedPreKeyPublic;
        @JsonProperty
        private  String signedPreKeySignature;
        @JsonProperty
        private  String identityKey;
        @JsonProperty
        private  int kyberPreKeyId;
        @JsonProperty
        private  String kyberPreKeyPublic;
        @JsonProperty
        private  String kyberPreKeySignature;

        public PreKeyBundleJson(PreKeyBundle preKeyBundle) {
            this.registrationId = preKeyBundle.getRegistrationId();
            this.deviceId = preKeyBundle.getDeviceId();
            this.preKeyId = preKeyBundle.getPreKeyId();
            this.preKeyPublic = Base64.getEncoder().encodeToString(preKeyBundle.getPreKey().getPublicKeyBytes());
            this.signedPreKeyId = preKeyBundle.getSignedPreKeyId();
            this.signedPreKeyPublic = Base64.getEncoder().encodeToString(preKeyBundle.getSignedPreKey().getPublicKeyBytes());
            this.signedPreKeySignature = Base64.getEncoder().encodeToString(preKeyBundle.getSignedPreKeySignature());
            this.identityKey = Base64.getEncoder().encodeToString(preKeyBundle.getIdentityKey().serialize());
            this.kyberPreKeyId = preKeyBundle.getKyberPreKeyId();
            this.kyberPreKeyPublic = Base64.getEncoder().encodeToString(preKeyBundle.getKyberPreKey().serialize());
            this.kyberPreKeySignature = Base64.getEncoder().encodeToString(preKeyBundle.getKyberPreKeySignature());
        }

        public PreKeyBundleJson() {}
    }


    public static PreKeyBundle deserializeFromJson(String json) throws InvalidKeyException {
        ObjectMapper objectMapper = new ObjectMapper();
        PreKeyBundleJson preKeyBundleJson = objectMapper.readValue(json, PreKeyBundleJson.class);

        return new PreKeyBundle(
                preKeyBundleJson.registrationId,
                preKeyBundleJson.deviceId,
                preKeyBundleJson.preKeyId,
                ECPublicKey.fromPublicKeyBytes(Base64.getDecoder().decode(preKeyBundleJson.preKeyPublic)),
                preKeyBundleJson.signedPreKeyId,
                ECPublicKey.fromPublicKeyBytes(Base64.getDecoder().decode(preKeyBundleJson.signedPreKeyPublic)),
                Base64.getDecoder().decode(preKeyBundleJson.signedPreKeySignature),
                new IdentityKey(Base64.getDecoder().decode(preKeyBundleJson.identityKey)),
                preKeyBundleJson.kyberPreKeyId,
                new KEMPublicKey(Base64.getDecoder().decode(preKeyBundleJson.kyberPreKeyPublic)),
                Base64.getDecoder().decode(preKeyBundleJson.kyberPreKeySignature)
        );
    }
}
