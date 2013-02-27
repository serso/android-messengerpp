package org.solovyev.android.messenger.xmpp;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.solovyev.android.messenger.realms.RealmConfiguration;

public class XmppRealmConfiguration implements RealmConfiguration {

    private static final int DEFAULT_PORT = 5222;

    @NotNull
    private static final String DEFAULT_RESOURCE = "Messenger++";

    @NotNull
    private String server;

    @NotNull
    private String login;

    @NotNull
    private String password;

    @NotNull
    private String resource = DEFAULT_RESOURCE;

    @NotNull
    private Integer port = DEFAULT_PORT;

    // for gson
    public XmppRealmConfiguration() {
    }

    public XmppRealmConfiguration(@NotNull String server, @NotNull String login, @NotNull String password) {
        this.server = server;
        this.login = login;
        this.password = password;
    }

    @NotNull
    public String getServer() {
        return server;
    }

    @NotNull
    public String getLogin() {
        return login;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    void setResource(@NotNull String resource) {
        this.resource = resource;
    }

    void setPort(@NotNull Integer port) {
        this.port = port;
    }

    @NotNull
    public ConnectionConfiguration toXmppConfiguration() {
        final ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(this.server, this.port, this.resource);

        return connectionConfiguration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XmppRealmConfiguration)) return false;

        XmppRealmConfiguration that = (XmppRealmConfiguration) o;

        if (!login.equals(that.login)) return false;
        if (!port.equals(that.port)) return false;
        if (!server.equals(that.server)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = server.hashCode();
        result = 31 * result + login.hashCode();
        result = 31 * result + port.hashCode();
        return result;
    }

    @NotNull
    public static XmppRealmConfiguration fromJson(@NotNull String json) {
        return new Gson().fromJson(json, XmppRealmConfiguration.class);
    }
}
