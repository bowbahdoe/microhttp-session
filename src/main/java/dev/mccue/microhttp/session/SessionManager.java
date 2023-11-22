package dev.mccue.microhttp.session;

import dev.mccue.microhttp.cookies.Cookies;
import dev.mccue.microhttp.setcookie.SetCookieHeader;
import org.microhttp.Header;
import org.microhttp.Request;
import org.microhttp.Response;

import java.net.HttpCookie;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class SessionManager {
    private final SessionStore store;
    private final String root;
    private final String cookieName;
    private final Consumer<SetCookieHeader.Builder> customizeCookie;
    private SessionManager(Builder builder) {
        this.store = builder.store;
        this.root = builder.root;
        this.cookieName = builder.cookieName;
        this.customizeCookie = builder.customizeCookie;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Reads session data from a {@link Request}.
     * @param request The {@link Request} which contains session data.
     * @return The active session, if one exists.
     */
    public Optional<Session> read(
            Request request
    ) {
        var cookies = Cookies.parse(request);
        var sessionCookie = cookies.get(cookieName)
                .orElse(null);
        if (sessionCookie == null){
            return Optional.empty();
        }

        var key = new SessionKey(sessionCookie);
        var value = this.store.read(key);
        return value.map(data -> new Session(key, data));
    }

    /**
     * Reads session data from a {@link Response}. This is useful
     * for code that generically wraps handlers that wants to co-operate
     * in updating the session data.
     *
     * <p>
     *     This necessarily does some extra work. The alternatives are to
     *     thread session data through the call stack or to use mechanisms like
     *     {@link ScopedValue} or {@link ThreadLocal} to have nested code have
     *     participate in building up {@link SessionData}.
     * </p>
     *
     * @param response The response to parse
     * @return The active session, if one exists.
     */
    public Optional<Session> read(
            Response response
    ) {
        for (var header : response.headers()) {
            if (header.name().equalsIgnoreCase("set-cookie")) {
                var parsedSetCookies = HttpCookie.parse(header.value());
                for (var setCookie : parsedSetCookies) {
                    if (setCookie.getName().equals(this.cookieName)) {
                        var key = new SessionKey(setCookie.getValue());
                        var value = this.store.read(key);
                        return value.map(data -> new Session(key, data));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Writes the given session into an appropriate {@code Set-Cookie}
     * header.
     *
     * <p>
     *     After doing this, the given {@link Session} should be considered invalid.
     * </p>
     *
     * @param session The session to write.
     * @return The header to put in the {@link Response}.
     */
    public Header write(Session session) {
        var newSessionKey = store.write(session.key(), session.data());
        var header = SetCookieHeader.builder(cookieName, newSessionKey.value())
                .path(root);
        customizeCookie.accept(header);
        return header.build();
    }

    public static final class Builder {
        SessionStore store;
        String root;
        String cookieName;
        Consumer<SetCookieHeader.Builder> customizeCookie;

        Builder() {
            this.store = SessionStore.inMemory();
            this.root = "/";
            this.cookieName = "__session_cookie";
            this.customizeCookie = (cookie) -> cookie.httpOnly(true);
        }

        public Builder store(SessionStore store) {
            this.store = Objects.requireNonNull(store);
            return this;
        }

        public Builder root(String root) {
            this.root = Objects.requireNonNull(root);
            return this;
        }

        public Builder cookieName(String cookieName) {
            this.cookieName = Objects.requireNonNull(cookieName);
            return this;
        }

        public Builder customizeCookie(Consumer<SetCookieHeader.Builder> customizeCookie) {
            this.customizeCookie = Objects.requireNonNull(customizeCookie);
            return this;
        }

        public SessionManager build() {
            return new SessionManager(this);
        }
    }
}
