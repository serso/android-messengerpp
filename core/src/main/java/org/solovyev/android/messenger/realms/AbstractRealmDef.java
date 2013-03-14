package org.solovyev.android.messenger.realms;

import android.content.Context;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 1:05 AM
 */
public abstract class AbstractRealmDef implements RealmDef {

    @Nonnull
    private final String id;

    private final int nameResId;

    private final int iconResId;

    @Nonnull
    private final Class<? extends BaseRealmConfigurationFragment<?>> configurationActivityClass;

    @Nonnull
    private final Class<? extends RealmConfiguration> configurationClass;
    private final boolean notifySentMessagesImmediately;

    protected AbstractRealmDef(@Nonnull String id,
                               int nameResId,
                               int iconResId,
                               @Nonnull Class<? extends BaseRealmConfigurationFragment<?>> configurationActivityClass,
                               @Nonnull Class<? extends RealmConfiguration> configurationClass,
                               boolean notifySentMessagesImmediately) {
        this.id = id;
        this.nameResId = nameResId;
        this.iconResId = iconResId;
        this.configurationActivityClass = configurationActivityClass;
        this.configurationClass = configurationClass;
        this.notifySentMessagesImmediately = notifySentMessagesImmediately;
    }

    @Nonnull
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getNameResId() {
        return this.nameResId;
    }

    @Override
    public int getIconResId() {
        return this.iconResId;
    }

    @Override
    @Nonnull
    public Class<? extends RealmConfiguration> getConfigurationClass() {
        return configurationClass;
    }

    @Nonnull
    @Override
    public Class<? extends BaseRealmConfigurationFragment> getConfigurationFragmentClass() {
        return this.configurationActivityClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AbstractRealmDef)) {
            return false;
        }

        final AbstractRealmDef that = (AbstractRealmDef) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public void init(@Nonnull Context context) {
    }

    @Override
    public boolean notifySentMessagesImmediately() {
        return notifySentMessagesImmediately;
    }
}
