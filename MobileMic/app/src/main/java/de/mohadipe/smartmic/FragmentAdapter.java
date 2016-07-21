package de.mohadipe.smartmic;

import android.support.v4.app.Fragment;

public final class FragmentAdapter {

    private static final String MOBILE_MIC = "SmartMic";
    private static final String PAYPAL_ME = "Spenden";

    public static Fragment getFragmentByName(final String fragmentName) {
        if (MOBILE_MIC.equals(fragmentName)) {
            return new SmartMicFragment();
        }
        if (PAYPAL_ME.equals(fragmentName)) {
            return new PaypalMeFragment();
        }
        return new SmartMicFragment();
    }
}
