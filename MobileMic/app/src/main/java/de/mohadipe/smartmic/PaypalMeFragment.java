package de.mohadipe.smartmic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import roboguice.fragment.RoboFragment;

public class PaypalMeFragment extends RoboFragment implements FragmentInterface {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_paypalme, container, false);
        return rootView;
    }

    public void callPaypalMe() {
        Uri uri = Uri.parse("http://paypal.me/mohadipe");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void disableFunction() {
        //Do nothing
    }

    @Override
    public void enableFunction() {
        //Do nothing
    }

    @Override
    public void activateMic(View view) {
        //Do nothing
    }
}
