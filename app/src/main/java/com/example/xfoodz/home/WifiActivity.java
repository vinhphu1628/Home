package com.example.xfoodz.home;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WifiActivity extends MainActivity implements View.OnClickListener {
    private Handler mWifi = new Handler();
    private Handler mTime = new Handler();
    private int level = 0;
    private Context context = this;
    private ImageView mImageView;
    private TextView date;
    private TextView time;
    private TextView wifiState;

    private WifiManager wifi;
    private WifiInfo wifiInfo;
    private ListView wifiList;
    private Button buttonScan;
    private Button buttonEnable;
    private Button buttonBack;
    private int size = 0;
    private List<ScanResult> results;

    private String ITEM_KEY = "key";
    private ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    private SimpleAdapter adapter;

    private boolean check = false;
    private TimerTask connecting_;
    private Timer timer;
    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_activity);

        mTime.post(mTimeRunnable);

        buttonScan = findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(this);
        buttonEnable = findViewById(R.id.buttonEnable);
        buttonEnable.setOnClickListener(this);
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this);

        wifiList = findViewById(R.id.list);

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false) {
            wifi.setWifiEnabled(true);
        }

        this.adapter = new SimpleAdapter(this, arraylist, R.layout.row, new String[]{ITEM_KEY}, new int[]{R.id.list_value});
        wifiList.setAdapter(this.adapter);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {

            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifi.startScan();

        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("Debug", results.get(i).SSID);
                if(wifi.isWifiEnabled()) {
                    login(results.get(i).SSID);
                    int size = wifi.getConfiguredNetworks().size();
                    while(size >= 0){
                        Log.d("size", String.valueOf(size));
                        size--;
                    }
                }
                else Toast.makeText(context, "Wifi disabled!", Toast.LENGTH_SHORT).show();
            }
        });

        mWifi.post(mWifiRunnable);
    }

    private void option(){

    }

    private void login(final String SSID){
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifi.getConnectionInfo();
        final Dialog myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.wifi_login);
        myDialog.setCancelable(true);
        Button login = myDialog.findViewById(R.id.buttonLogin);
        Button back = myDialog.findViewById(R.id.buttonBack);

        myDialog.setTitle("Login");
        myDialog.show();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText txtKey = myDialog.findViewById(R.id.txtKey);
                Log.d("ssid", SSID);
                Log.d("key", txtKey.getText().toString());
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", SSID);
                wifiConfig.preSharedKey = String.format("\"%s\"", txtKey.getText().toString());

                int netId = wifi.addNetwork(wifiConfig);
                wifi.disconnect();
                wifi.enableNetwork(netId, true);
                wifi.reconnect();

                myDialog.cancel();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.cancel();
                return;
            }
        });
    }

    public void onClick(View view) {
        if (view == buttonScan) {
            if(wifi.isWifiEnabled() == false){
                Toast.makeText(this, "Wifi disabled, unable to scan!", Toast.LENGTH_SHORT).show();
            }
            else {
                results = wifi.getScanResults();
                size = results.size();
                arraylist.clear();
                wifi.startScan();
                Toast.makeText(this, "Scanning...." + size, Toast.LENGTH_SHORT).show();
                int i = 0;
                try {
                    size = size - 1;
                    while (size >= 0) {
                        HashMap<String, String> item = new HashMap<String, String>();
                        item.put(ITEM_KEY, results.get(i).SSID + "  " + results.get(i).capabilities);

                        arraylist.add(item);
                        size--;
                        adapter.notifyDataSetChanged();
                        i++;
                    }
                } catch (Exception e) {
                }
            }
        }
        else if(view == buttonEnable){
            if(wifi.isWifiEnabled() == false) {
                wifi.setWifiEnabled(true);
                buttonEnable.setText("Disable");
                wifiState.setText("Enabled");
            }
            else{
                wifi.setWifiEnabled(false);
                buttonEnable.setText("Enable");
                wifiState.setText("Disabled");
            }
        }
        else if(view == buttonBack){
            startActivity(new Intent(this, MainActivity.class));
        }
    }

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

    private Runnable mWifiRunnable = new Runnable() {
        @Override
        public void run() {
            wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int numberOfLevels = 5;
            wifiInfo = wifi.getConnectionInfo();
            level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            mImageView = findViewById(R.id.imageWifiState);
            wifiState = findViewById(R.id.wifiState);
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                int ipAddress = wifiInfo.getIpAddress();
                String ipString = Formatter.formatIpAddress(ipAddress);
                wifiState.setText(wifiInfo.getSSID() + " - " + ipString);
            }
            /*else if (wifiInfo.getSupplicantState() == SupplicantState.DISCONNECTED){wifiState.setText(String.valueOf(wifiInfo.getSupplicantState()));}
            else {
                wifiState.setText(String.valueOf(wifiInfo.getSupplicantState()));
                connecting();
            }*/
            if(wifi.isWifiEnabled() == false) level = -1;
            switch(level){
                case 0:
                    wifi.reconnect();
                    mImageView.setImageResource(R.drawable.ic_signal_wifi_0_bar_black_48dp);
                    break;
                case 1:
                    mImageView.setImageResource(R.drawable.ic_signal_wifi_1_bar_black_48dp);
                    break;
                case 2:
                    mImageView.setImageResource(R.drawable.ic_signal_wifi_2_bar_black_48dp);
                    break;
                case 3:
                    mImageView.setImageResource(R.drawable.ic_signal_wifi_3_bar_black_48dp);
                    break;
                case 4:
                    mImageView.setImageResource(R.drawable.ic_signal_wifi_4_bar_black_48dp);
                    break;
                case -1:
                    mImageView.setImageResource(R.drawable.ic_signal_wifi_off_bar_black_48dp);
                    break;
            }
            mWifi.postDelayed(mWifiRunnable, 1000);
        }
    };

    public void initializeTimerTask() {
        connecting_ = new TimerTask() {
            @Override
            public void run() {
                wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                wifiInfo = wifi.getConnectionInfo();
                if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                    wifiState.setText(String.valueOf(wifiInfo.getSupplicantState()));
                    connecting_.cancel();
                    stopTimer();

                } else {
                    wifi.reconnect();
                }
            }
        };
    }

    public void stopTimer(){
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    void connecting(){
        Timer timer = new Timer();
        initializeTimerTask();
        timer.scheduleAtFixedRate(connecting_, 10000, 1000);
    }

}
