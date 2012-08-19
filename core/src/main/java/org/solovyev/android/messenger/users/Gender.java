package org.solovyev.android.messenger.users;

import org.solovyev.android.Labeled;
import org.solovyev.android.messenger.R;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:46 PM
 */
public enum Gender implements Labeled {
    male(R.string.male),
    female(R.string.female);

    private int captionResId;

    private Gender(int captionResId) {
        this.captionResId = captionResId;
    }


    @Override
    public int getCaptionResId() {
        return this.captionResId;
    }
}
