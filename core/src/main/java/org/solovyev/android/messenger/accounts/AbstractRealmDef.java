package org.solovyev.android.messenger.accounts;

import android.content.Context;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 1:05 AM
 */
public abstract class AbstractRealmDef<C extends AccountConfiguration> implements RealmDef<C> {

	@Nonnull
	private final String id;

	private final int nameResId;

	private final int iconResId;

	@Nonnull
	private final Class<? extends BaseAccountConfigurationFragment<?>> configurationFragmentClass;

	@Nonnull
	private final Class<? extends C> configurationClass;

	private final boolean notifySentMessagesImmediately;

	protected AbstractRealmDef(@Nonnull String id,
							   int nameResId,
							   int iconResId,
							   @Nonnull Class<? extends BaseAccountConfigurationFragment<?>> configurationFragmentClass,
							   @Nonnull Class<? extends C> configurationClass,
							   boolean notifySentMessagesImmediately) {
		this.id = id;
		this.nameResId = nameResId;
		this.iconResId = iconResId;
		this.configurationFragmentClass = configurationFragmentClass;
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
	public Class<? extends C> getConfigurationClass() {
		return configurationClass;
	}

	@Nonnull
	@Override
	public Class<? extends BaseAccountConfigurationFragment> getConfigurationFragmentClass() {
		return this.configurationFragmentClass;
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

	protected void addUserProperty(@Nonnull Context context, @Nonnull List<AProperty> properties, int propertyNameResId, @Nullable String propertyValue) {
		if (!Strings.isEmpty(propertyValue)) {
			properties.add(Properties.newProperty(context.getString(propertyNameResId), propertyValue));
		}
	}

	@Override
	public void init(@Nonnull Context context) {
	}

	@Override
	public boolean notifySentMessagesImmediately() {
		return notifySentMessagesImmediately;
	}

	@Override
	public boolean handleException(@Nonnull Throwable e, @Nonnull Account account) {
		return false;
	}
}
