package com.example.xfoodz.home;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingActivity extends MainActivity implements View.OnClickListener{
    private Context context = this;
    private SettingAdapter adapter;
    private String setting[] = {"Wifi", "Bluetooth", "Sound", "Time", "Storage", "Device Information"};
    private int img[] = {R.drawable.ic_wifi_black_24dp, R.drawable.ic_bluetooth_black_24dp, R.drawable.ic_volume_up_black_24dp, R.drawable.ic_access_time_black_24dp, R.drawable.ic_storage_black_24dp, R.drawable.ic_devices_black_24dp};
    private ListView listSetting;
    private ImageView buttonBack;
    private WifiManager wifi;
    private int level = 0;
    private ImageView imageWifiState;
    private TextView date;
    private TextView time;
    private Handler mWifi = new Handler();
    private Handler mTime = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);

        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this);

        imageWifiState = findViewById(R.id.imageWifiState);
        imageWifiState.setOnClickListener(this);
        
        listSetting = findViewById(R.id.settingList);

        adapter = new SettingAdapter(this, setting, img);
        listSetting.setAdapter(adapter);

        listSetting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i){
                    case 0:
                        startActivity(new Intent(context, WifiActivity.class));
                        break;
                    default:
                        Toast.makeText(context, setting[i], Toast.LENGTH_SHORT).show();
                }
            }
        });

        mTime.post(mTimeRunnable);
        mWifi.post(mWifiRunnable);
    }

    @Override
    public void onClick(View view) {
        if(view == buttonBack){
            finish();
        }
    }

    private Runnable mWifiRunnable = new Runnable() {
        @Override
        public void run() {
            wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int numberOfLevels = 5;
            WifiInfo wifiInfo = wifi.getConnectionInfo();
            level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            imageWifiState = findViewById(R.id.imageWifiState);
            if(wifi.isWifiEnabled() == false) level = -1;
            switch(level){
                case 0:
                    imageWifiState.setImageResource(R.drawable.ic_signal_wifi_0_bar_black_48dp);
                    break;
                case 1:
                    imageWifiState.setImageResource(R.drawable.ic_signal_wifi_1_bar_black_48dp);
                    break;
                case 2:
                    imageWifiState.setImageResource(R.drawable.ic_signal_wifi_2_bar_black_48dp);
                    break;
                case 3:
                    imageWifiState.setImageResource(R.drawable.ic_signal_wifi_3_bar_black_48dp);
                    break;
                case 4:
                    imageWifiState.setImageResource(R.drawable.ic_signal_wifi_4_bar_black_48dp);
                    break;
                case -1:
                    imageWifiState.setImageResource(R.drawable.ic_signal_wifi_off_bar_black_48dp);
                    break;
            }
            mWifi.postDelayed(mWifiRunnable,1000);
        }
    };

    private Runnable mTimeRunnable = new Runnable() {
        @Override
        public void run() {
            date = findViewById(R.id.date);
            time = findViewById(R.id.time);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            date.setText(format.format(new Date()));
            format = new SimpleDateFormat("hh:mm");
            time.setText(format.format(new Date()));
            mTime.postDelayed(mTimeRunnable, 1000);
        }
    };
}
