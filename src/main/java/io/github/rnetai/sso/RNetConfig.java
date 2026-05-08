package io.github.rnetai.sso;

public class RNetConfig {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private static final String issuer = "https://central-backend.rnetai.org";
    private static final String aiProvider = "https://ai-provider.rnetai.org";

    public RNetConfig(String clientId, String clientSecret, String redirectUri) {
        if (clientId == null || clientSecret == null || redirectUri == null) {
            throw new IllegalArgumentException("clientId, clientSecret, and redirectUri are required");
        }
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getAiProvider() {
        return aiProvider;
    }
}