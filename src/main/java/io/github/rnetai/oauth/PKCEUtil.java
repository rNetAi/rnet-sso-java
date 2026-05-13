package io.github.rnetai.oauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PKCEUtil {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static class PKCE {
        private final String verifier;
        private final String challenge;

        public PKCE(String verifier, String challenge) {
            this.verifier = verifier;
            this.challenge = challenge;
        }

        public String getVerifier() {
            return verifier;
        }

        public String getChallenge() {
            return challenge;
        }
    }

    public static PKCE generate() {
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return new PKCE(verifier, challenge);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
