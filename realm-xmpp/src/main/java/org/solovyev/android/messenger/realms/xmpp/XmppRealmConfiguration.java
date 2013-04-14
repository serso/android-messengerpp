package org.solovyev.android.messenger.realms.xmpp;

import com.google.gson.Gson;
import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.solovyev.android.messenger.realms.RealmConfiguration;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;

public class XmppRealmConfiguration extends JObject implements RealmConfiguration {

    private static final boolean DEBUG = true;

    private static final int DEFAULT_PORT = 5222;

    @Nonnull
    private static final String DEFAULT_RESOURCE = "Messenger++";

    @Nonnull
    private String server;

    @Nonnull
    private String login;

    @Nonnull
    private String password;

    @Nonnull
    private String resource = DEFAULT_RESOURCE;

    @Nonnull
    private Integer port = DEFAULT_PORT;

    // for gson
    public XmppRealmConfiguration() {
    }

    public XmppRealmConfiguration(@Nonnull String server, @Nonnull String login, @Nonnull String password) {
        this.server = server;
        this.login = login;
        this.password = password;
    }

    @Nonnull
    public String getServer() {
        return server;
    }

    @Nonnull
    public String getLogin() {
        return login;
    }

    @Nonnull
    public String getPassword() {
        return password;
    }

    void setResource(@Nonnull String resource) {
        this.resource = resource;
    }

    void setPort(@Nonnull Integer port) {
        this.port = port;
    }

    @Nonnull
    public String getResource() {
        return resource;
    }

    @Nonnull
    public Integer getPort() {
        return port;
    }

    @Nonnull
    public AndroidConnectionConfiguration toXmppConfiguration() {
        final AndroidConnectionConfiguration connectionConfiguration = new AndroidConnectionConfiguration(this.server, this.port, null);

        connectionConfiguration.setDebuggerEnabled(DEBUG);

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

    @Nonnull
    public static XmppRealmConfiguration fromJson(@Nonnull String json) {
        return new Gson().fromJson(json, XmppRealmConfiguration.class);
    }

    @Nonnull
    @Override
    public XmppRealmConfiguration clone() {
        return (XmppRealmConfiguration) super.clone();
    }

    public void setPassword(@Nonnull String password) {
        this.password = password;
    }
}
