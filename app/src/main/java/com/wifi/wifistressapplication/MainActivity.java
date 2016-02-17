package com.wifi.wifistressapplication;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    final String TAG = "WifiStress";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(this)) {
            Log.d(TAG, "Send intent to grant");
            Intent grantIntent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            startActivity(grantIntent);
        }

        ItemsFragment mItemsFragment = new ItemsFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, mItemsFragment);
        ft.commit();
    }
}
