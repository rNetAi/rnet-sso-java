package io.github.rnetai.sso;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RNetSsoTest {

    @Test
    public void testPKCEGeneration() {
        PKCEUtil.PKCE pkce = PKCEUtil.generate();
        assertNotNull(pkce.getVerifier());
        assertNotNull(pkce.getChallenge());
        assertNotEquals(pkce.getVerifier(), pkce.getChallenge());
    }

    @Test
    public void testLoginUrlGeneration() {
        RNetConfig config = new RNetConfig("test-client", "test-secret", "http://localhost/callback");
        RNetSso sso = new RNetSso(config);
        
        String url = sso.getLoginUrl("test-challenge", "openid profile");
        assertTrue(url.contains("client_id=test-client"));
        assertTrue(url.contains("code_challenge=test-challenge"));
        assertTrue(url.contains("scope=openid+profile"));
    }
}
