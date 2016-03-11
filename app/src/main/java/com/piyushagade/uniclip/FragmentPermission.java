package com.piyushagade.uniclip;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentPermission extends Fragment {
    View root;

    private Handler handler = new Handler();
    private Runnable toastHandler = new Runnable() {
        @Override
        public void run() {
            makeToast("Select 'App permissions'.");

        }
    };


    public static FragmentPermission newInstance() {
        return new FragmentPermission();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {
            root = inflater.inflate(R.layout.fragment_permission, container, false);
        } catch (InflateException e) {
        }

        Button b_grant_permission = (Button) root.findViewById(R.id.b_grant_permission);

        b_grant_permission.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getActivity().getPackageName(), null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                ((TextView) root.findViewById(R.id.when_done)).setVisibility(View.VISIBLE);
            }
        });


        return root;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(toastHandler);
        super.onDestroy();
    }

    private void makeToast(Object data) {
        Toast.makeText(getActivity().getApplication().getApplicationContext(), String.valueOf(data), Toast.LENGTH_LONG).show();
    }


}