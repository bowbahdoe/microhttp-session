module dev.mccue.microhttp.session {
    requires dev.mccue.microhttp.setcookie;
    requires dev.mccue.microhttp.cookies;
    requires transitive dev.mccue.json;

    exports dev.mccue.microhttp.session;
}