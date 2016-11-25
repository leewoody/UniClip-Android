package com.piyushagade.uniclip;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentPermission extends Fragment {
    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;
    private static final int RC_SIGN_IN = 1;
    View root;


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
                if (ActivityCompat.checkSelfPermission(
                        getContext(), Manifest.permission.GET_ACCOUNTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.GET_ACCOUNTS},
                            MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);

                    return;
                }



                ((TextView) root.findViewById(R.id.when_done)).setVisibility(View.VISIBLE);
            }
        });

        return root;
    }


    private void makeToast(Object data) {
        Toast.makeText(getActivity().getApplication().getApplicationContext(), String.valueOf(data), Toast.LENGTH_LONG).show();
    }


}