package org.solovyev.android.messenger.users;

import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 3:17 PM
 */
public final class Users {

    private Users() {
    }

    @Nonnull
    public static String getDisplayNameFor(@Nonnull User user) {
        final StringBuilder result = new StringBuilder();

        final String firstName = user.getPropertyValueByName(User.PROPERTY_FIRST_NAME);
        final String lastName = user.getPropertyValueByName(User.PROPERTY_LAST_NAME);

        boolean firstNameExists = !Strings.isEmpty(firstName);
        boolean lastNameExists = !Strings.isEmpty(lastName);

        if ( !firstNameExists && !lastNameExists ) {
            // first and last names are empty
            result.append(user.getRealmEntity().getRealmEntityId());
        } else {

            if (firstNameExists) {
                result.append(firstName);
            }

            if (firstNameExists && lastNameExists) {
                result.append(" ");
            }

            if (lastNameExists) {
                result.append(lastName);
            }
        }

        return result.toString();
    }
}
