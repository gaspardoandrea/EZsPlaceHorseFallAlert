package org.ezsplace.horsefallalert;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
    private SharedPreferences sharedPrefs;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateAlert(intent.getStringExtra(Broadcaster.ACTION_FROM_SERVICE_ALERT));
            updateStatus(intent.getStringExtra(Broadcaster.ACTION_FROM_SERVICE_STATUS));
            updateAngle(intent.getStringExtra(Broadcaster.ACTION_FROM_SERVICE_ANGLE));
        }

    };
    private Intent intent;
    private BroadcastReceiver mSettingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateDebugLayout();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD, WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON, WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
    }

    private void updateDebugLayout() {
        findViewById(R.id.debugSeekBar).setVisibility(isDebug() ? View.VISIBLE : View.INVISIBLE);
    }

    public boolean isDebug() {
        return sharedPrefs.getBoolean("debug", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_events) {
            Intent intent = new Intent(this, EventsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mReceiver, new IntentFilter(Broadcaster.ACTION_FROM_SERVICE));
        registerReceiver(mSettingsReceiver, new IntentFilter(Broadcaster.SETTINGS_STOPPED));
        updateDebugLayout();
        ((SeekBar) findViewById(R.id.debugSeekBar)).setOnSeekBarChangeListener(this);
        startService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mSettingsReceiver);
        ((SeekBar) findViewById(R.id.debugSeekBar)).setOnSeekBarChangeListener(null);
    }

    private void startService() {
        intent = new Intent(this, FallAlertService.class);
        startService(intent);
    }

    /**
     * @deprecated
     */
    private void stopService() {
        if (intent != null) {
            stopService(intent);
        }
    }

    private void updateAlert(String s) {
        TextView textView = (TextView) findViewById(R.id.secs);
        textView.setText(s);
    }

    private void updateAngle(String s) {
        if (!s.isEmpty()) {
            s = s + "°";
        }
        TextView textView = (TextView) findViewById(R.id.angle);
        textView.setText(s);
    }

    private void updateStatus(String s) {
        TextView textView = (TextView) findViewById(R.id.alert);
        textView.setText(s);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateDebugAngle(progress);
    }

    private void updateDebugAngle(int value) {
        Broadcaster.updateDebugAngle(getApplicationContext(), value);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
