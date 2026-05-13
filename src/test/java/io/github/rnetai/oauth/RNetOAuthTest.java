package io.github.rnetai.oauth;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import static org.junit.jupiter.api.Assertions.*;

public class RNetOAuthTest {

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
        RNetAuth auth = new RNetAuth(config);

        String url = auth.getAuthorizationUrl("test-challenge");
        assertTrue(url.contains("client_id=test-client"));
        assertTrue(url.contains("code_challenge=test-challenge"));
        assertTrue(url.contains("scope=openid+profile"));
    }

    @Test
    public void testUserInfoRequest() throws Exception {
        FakeHttpClient client = new FakeHttpClient(new FakeResponse<>(200,
                "{\"sub\":\"user-123\",\"email\":\"user@example.com\",\"name\":\"Test User\",\"user_id\":123,\"role\":\"USER\",\"status\":\"ACTIVE\"}"));
        RNetConfig config = new RNetConfig("test-client", "test-secret", "http://localhost/callback");
        RNetAuth auth = new RNetAuth(config, client);

        Map<String, Object> userInfo = auth.getUserInfo("test-access-token");
        assertEquals(URI.create("https://central-backend.rnetai.org/userinfo"), client.lastRequest.uri());
        assertEquals("Bearer test-access-token", client.lastRequest.headers().firstValue("Authorization").orElse(""));
        assertEquals("user-123", userInfo.get("sub"));
        assertEquals("user@example.com", userInfo.get("email"));
        assertEquals("Test User", userInfo.get("name"));
        assertEquals("USER", userInfo.get("role"));
    }

    @Test
    public void testChatStreamRequest() throws Exception {
        InputStream body = new java.io.ByteArrayInputStream("chunk-one\nchunk-two\n".getBytes(StandardCharsets.UTF_8));
        FakeHttpClient client = new FakeHttpClient(new FakeResponse<>(200, body));
        RNetConfig config = new RNetConfig("test-client", "test-secret", "http://localhost/callback");
        RNetAi ai = new RNetAi(config, client);

        InputStream stream = ai.chatStream(Map.of("contents", List.of()), "test-access-token", "test-model");
        assertEquals(URI.create("https://ai-provider.rnetai.org/ai/stream?access_token=test-access-token&model=test-model"),
                client.lastRequest.uri());
        assertEquals("POST", client.lastRequest.method());
        assertEquals("chunk-one\nchunk-two\n", readAll(stream));
    }

    private static String readAll(InputStream stream) throws IOException {
        try (stream; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            stream.transferTo(output);
            return output.toString(StandardCharsets.UTF_8);
        }
    }

    private static final class FakeHttpClient extends HttpClient {
        private final HttpResponse<?> response;
        private HttpRequest lastRequest;

        private FakeHttpClient(HttpResponse<?> response) {
            this.response = response;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            this.lastRequest = request;
            return (HttpResponse<T>) response;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }
    }

    private static final class FakeResponse<T> implements HttpResponse<T> {
        private final int statusCode;
        private final T body;

        private FakeResponse(int statusCode, T body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional<HttpResponse<T>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(Map.of(), (first, second) -> true);
        }

        @Override
        public T body() {
            return body;
        }

        @Override
        public Optional<javax.net.ssl.SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return URI.create("https://example.test");
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
