package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;
import org.solovyev.common.JObject;

/**
 * User: serso
 * Date: 7/19/12
 * Time: 5:29 PM
 */
public class RealmIdImpl<I> extends JObject implements RealmId<I> {

    @NotNull
    private final String realm;

    @NotNull
    private final I id;

    public RealmIdImpl(@NotNull String realm, @NotNull I id) {
        this.id = id;
        this.realm = realm;
    }

    @NotNull
    @Override
    public String getRealm() {
        return this.realm;
    }

    @NotNull
    @Override
    public I getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RealmIdImpl)) return false;

        RealmIdImpl realmId = (RealmIdImpl) o;

        if (!id.equals(realmId.id)) return false;
        if (!realm.equals(realmId.realm)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = realm.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @NotNull
    @Override
    public RealmIdImpl<I> clone() {
        return (RealmIdImpl<I>) super.clone();
    }
}
