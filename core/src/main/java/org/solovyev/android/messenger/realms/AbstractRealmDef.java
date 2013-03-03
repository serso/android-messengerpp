package org.solovyev.android.messenger.realms;

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
    private final Class<? extends BaseRealmConfigurationFragment> configurationActivityClass;

    @Nonnull
    private final Class<? extends RealmConfiguration> configurationClass;

    protected AbstractRealmDef(@Nonnull String id,
                               int nameResId,
                               int iconResId,
                               @Nonnull Class<? extends BaseRealmConfigurationFragment> configurationActivityClass,
                               @Nonnull Class<? extends RealmConfiguration> configurationClass) {
        this.id = id;
        this.nameResId = nameResId;
        this.iconResId = iconResId;
        this.configurationActivityClass = configurationActivityClass;
        this.configurationClass = configurationClass;
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
}
