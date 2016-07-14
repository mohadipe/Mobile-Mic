package de.mohadipe.mobilemic;

import android.support.v4.app.Fragment;

public final class FragmentAdapter {

    private static final String MOBILE_MIC = "MobileMic";
    private static final String PAYPAL_ME = "Spenden";
    private static final String FEEDBACK = "Feedback";

    public static Fragment getFragmentByName(final String fragmentName) {
        if (MOBILE_MIC.equals(fragmentName)) {
            return new MobileMicFragment();
        }
        if (PAYPAL_ME.equals(fragmentName)) {
            return new PaypalMeFragment();
        }
        if (FEEDBACK.equals(fragmentName)) {
            return new MobileMicFragment();
        }
        return null;
    }
}
