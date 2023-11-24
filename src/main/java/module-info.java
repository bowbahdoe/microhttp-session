module dev.mccue.microhttp.session {
    requires dev.mccue.microhttp.setcookie;
    requires dev.mccue.microhttp.cookies;
    requires dev.mccue.microhttp.handler;
    requires dev.mccue.async;
    requires transitive dev.mccue.json;

    exports dev.mccue.microhttp.session;
}