package dev.mccue.microhttp.session;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public record Session(SessionKey key, SessionData data) {
    public Session {
        Objects.requireNonNull(key);
        Objects.requireNonNull(data);
    }

    public Session() {
        this(SessionKey.random(), SessionData.of(Map.of()));
    }

    public Session update(Function<SessionData, SessionData> f) {
        return new Session(key, f.apply(data));
    }
}
