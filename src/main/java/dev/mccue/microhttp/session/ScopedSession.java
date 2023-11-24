package dev.mccue.microhttp.session;

import dev.mccue.async.Atom;
import dev.mccue.microhttp.handler.Handler;
import org.microhttp.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;

public final class ScopedSession {
    private static final ScopedValue<Atom<Session>> SESSION_SCOPED_VALUE
            = ScopedValue.newInstance();

    public static SessionData get() {
        return SESSION_SCOPED_VALUE
                .get()
                .get()
                .data();
    }

    public static void set(SessionData data) {
        var atom = SESSION_SCOPED_VALUE.get();
        var session = atom.get();
        atom.reset(new Session(session.key(), data));
    }

    public static SessionData update(Function<SessionData, SessionData> f) {
        return SESSION_SCOPED_VALUE.get()
                .swap(session -> new Session(session.key(), f.apply(session.data())))
                .data();
    }

    public static Handler wrap(SessionManager sessionManager, Handler handler) {
        return request -> ScopedValue.where(SESSION_SCOPED_VALUE, Atom.of(sessionManager.read(request)
                        .orElse(new Session())))
                .call(() -> {
                    var intoResponse = handler.handle(request);
                    var response = intoResponse.intoResponse();
                    var headers = new ArrayList<>(response.headers());
                    headers.add(sessionManager.write(SESSION_SCOPED_VALUE.get().get()));
                    return () -> new Response(
                            response.status(),
                            response.reason(),
                            Collections.unmodifiableList(headers),
                            response.body()
                    );
                });
    }
}
