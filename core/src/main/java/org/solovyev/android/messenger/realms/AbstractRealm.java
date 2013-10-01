package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.solovyev.android.messenger.accounts.*;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.properties.Properties.newProperty;
import static org.solovyev.common.text.Strings.isEmpty;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 1:05 AM
 */
public abstract class AbstractRealm<C extends AccountConfiguration> implements Realm<C> {

	@Nonnull
	private final String id;

	private final int nameResId;

	private final int iconResId;

	@Nonnull
	private final Class<? extends BaseAccountConfigurationFragment<?>> configurationFragmentClass;

	@Nonnull
	private final Class<? extends C> configurationClass;

	private final boolean notifySentMessagesImmediately;

	@Nullable
	private final Class<? extends BaseCreateUserFragment<?>> createUserFragmentClass;

	protected AbstractRealm(@Nonnull String id,
							int nameResId,
							int iconResId,
							@Nonnull Class<? extends BaseAccountConfigurationFragment<?>> configurationFragmentClass,
							@Nonnull Class<? extends C> configurationClass,
							boolean notifySentMessagesImmediately,
							@Nullable Class<? extends BaseCreateUserFragment<?>> createUserFragmentClass) {
		this.id = id;
		this.nameResId = nameResId;
		this.iconResId = iconResId;
		this.configurationFragmentClass = configurationFragmentClass;
		this.configurationClass = configurationClass;
		this.notifySentMessagesImmediately = notifySentMessagesImmediately;
		this.createUserFragmentClass = createUserFragmentClass;
	}

	@Nonnull
	@Override
	public final String getId() {
		return this.id;
	}

	@Override
	public final int getNameResId() {
		return this.nameResId;
	}

	@Override
	public final int getIconResId() {
		return this.iconResId;
	}

	@Override
	@Nonnull
	public final Class<? extends C> getConfigurationClass() {
		return configurationClass;
	}

	@Nonnull
	@Override
	public final Class<? extends BaseAccountConfigurationFragment> getConfigurationFragmentClass() {
		return this.configurationFragmentClass;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof AbstractRealm)) {
			return false;
		}

		final AbstractRealm that = (AbstractRealm) o;

		return id.equals(that.id);

	}

	@Override
	public final int hashCode() {
		return id.hashCode();
	}

	protected final void addUserProperty(@Nonnull Context context, @Nonnull List<AProperty> properties, int propertyNameResId, @Nullable String propertyValue) {
		if (!isEmpty(propertyValue)) {
			properties.add(newProperty(context.getString(propertyNameResId), propertyValue));
		}
	}

	@Override
	public void init(@Nonnull Context context) {
	}

	@Override
	public final boolean notifySentMessagesImmediately() {
		return notifySentMessagesImmediately;
	}

	@Override
	public boolean handleException(@Nonnull Throwable e, @Nonnull Account account) {
		return false;
	}

	@Override
	public boolean canCreateUsers() {
		return createUserFragmentClass != null;
	}

	@Nullable
	@Override
	public Class<? extends BaseCreateUserFragment> getCreateUserFragmentClass() {
		return createUserFragmentClass;
	}
}
