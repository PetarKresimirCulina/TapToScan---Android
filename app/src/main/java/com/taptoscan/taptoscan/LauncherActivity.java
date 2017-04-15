package com.taptoscan.taptoscan;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LauncherActivity extends AppCompatActivity {

    private View mContentView;
    public static Activity launcherAct;
    private boolean firstTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launcher);
        mContentView = findViewById(R.id.fullscreen_content);

        /*mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
*/
        SharedPreferences pref;
        launcherAct = this;

        pref = LauncherActivity.this.getSharedPreferences("tts-pref-1", 0);
        if(pref.getBoolean("first_time", true)) {
            firstTime = true;
            Intent intent = new Intent(this, TutorialActivity.class);
            startActivity(intent);
        }

        if(!firstTime) {
            checkNFC();
        }

    }

    public void checkNFC() {
        NfcAdapter mAdapterNFC = NfcAdapter.getDefaultAdapter(this);
        if (mAdapterNFC == null) {
            //nfc not support your device.
            Toast.makeText(this, "Your device doesn't have NFC support built-in and therefor is not supported.", Toast.LENGTH_LONG).show();
            return;
        } else {
            if (!mAdapterNFC.isEnabled()) {
                final Dialog dialog = new Dialog(LauncherActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_yes_no);

                final TextView title = (TextView) dialog.findViewById(R.id.titleText);
                title.setText(R.string.nfc_not_active_title);

                final TextView message = (TextView) dialog.findViewById(R.id.bodyText);
                message.setText(R.string.nfc_open_settings);

                //message.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));

                Button btnYes = (Button) dialog.findViewById(R.id.btnYes);

                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                        Toast.makeText(getApplicationContext(), R.string.activate_nfc, Toast.LENGTH_LONG).show();
                    }
                });

                Button btnNo = (Button) dialog.findViewById(R.id.btnNo);

                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(firstTime) {
            firstTime = false;
            checkNFC();
        }

    }

    @Override
    public void onPause(){
        super.onPause();
    }

}
