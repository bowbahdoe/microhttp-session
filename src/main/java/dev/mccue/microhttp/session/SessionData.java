package dev.mccue.microhttp.session;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SessionData {
    private final Map<String, String> data;
    private SessionData(Map<String, String> data) {
        this.data = Map.copyOf(data);
    }

    public static SessionData of(Map<String, String> data) {
        return new SessionData(data);
    }

    public Map<String, String> asMap() {
        return data;
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(data.get(key));
    }

    public SessionData with(String key, String value) {
        var data = new HashMap<>(this.data);
        data.put(key, value);
        return of(data);
    }

    @Override
    public String toString() {
        return "SessionData[data=" + data + "]";
    }
}
