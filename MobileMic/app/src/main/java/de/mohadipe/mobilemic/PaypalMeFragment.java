package de.mohadipe.mobilemic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class PaypalMeFragment extends RoboFragment {

    @InjectView(R.id.paypalme_Button)
    Button paypalMeButton;

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
}
