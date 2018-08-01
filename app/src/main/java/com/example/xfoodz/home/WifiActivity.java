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
    private Handler mScan = new Handler();
    private int level = 0;
    private Context context = this;
    private ImageView mImageView;
    private TextView date;
    private TextView time;
    private TextView wifiState;

    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private ListView wifiList;
    private Button buttonFlush;
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
    private int curID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_activity);

        mTime.post(mTimeRunnable);

        buttonFlush = findViewById(R.id.buttonFlush);
        buttonFlush.setOnClickListener(this);
        buttonEnable = findViewById(R.id.buttonEnable);
        buttonEnable.setOnClickListener(this);
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this);

        wifiList = findViewById(R.id.list);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled() == false) {
            wifiManager.setWifiEnabled(true);
        }

        this.adapter = new SimpleAdapter(this, arraylist, R.layout.row, new String[]{ITEM_KEY}, new int[]{R.id.list_value});
        wifiList.setAdapter(this.adapter);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (wifiManager.isWifiEnabled()) {
                }
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        mScan.post(mScanRunnable);

        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (wifiManager.isWifiEnabled()) {
                    boolean log = true;
                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                    for (WifiConfiguration j : list) {
                        if (j.SSID.equals("\"" + results.get(i).SSID + "\"")) {
                            log = false;
                            option(j);
                            break;
                        }
                    }
                    if (log) login(results.get(i).SSID);
                }
                else Toast.makeText(context, "Wifi disabled!", Toast.LENGTH_SHORT).show();
            }
        });

        mWifi.post(mWifiRunnable);
    }

    private void option(final WifiConfiguration conf) {
        final String SSID = conf.SSID;
        String BSSID = conf.BSSID;
        String networkId = String.valueOf(conf.networkId);
        final Dialog myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.wifi_option);
        myDialog.setCancelable(true);
        Button forget = myDialog.findViewById(R.id.buttonForget);
        Button back = myDialog.findViewById(R.id.buttonBack);
        Button connect = myDialog.findViewById(R.id.buttonConnect);

        TextView txtSSID = myDialog.findViewById(R.id.txtSSID);
        TextView txtBSSID = myDialog.findViewById(R.id.txtBSSID);
        TextView txtNetID = myDialog.findViewById(R.id.txtNetID);
        txtSSID.setText("SSID: " + SSID);
        txtBSSID.setText("BSSID: " + BSSID);
        txtNetID.setText("NetworkID: " + networkId);

        myDialog.setTitle("Option");
        myDialog.show();

        wifiInfo = wifiManager.getConnectionInfo();
        if(wifiInfo.getNetworkId() == conf.networkId) connect.setVisibility(View.GONE);
        else connect.setVisibility(View.VISIBLE);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                for (WifiConfiguration i : list) {
                    wifiManager.disableNetwork(i.networkId);
                }
                int netId = conf.networkId;
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();
                myDialog.cancel();
            }
        });
        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wifiManager.removeNetwork(conf.networkId))
                    Toast.makeText(context, "Successfully forget network!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, "Failed to forget network!", Toast.LENGTH_SHORT).show();
                myDialog.cancel();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.cancel();
            }
        });
    }

    private void login(final String SSID) {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
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

                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                for (WifiConfiguration i : list) {
                    wifiManager.disableNetwork(i.networkId);
                }

                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", SSID);
                wifiConfig.preSharedKey = String.format("\"%s\"", txtKey.getText().toString());

                int netId = wifiManager.addNetwork(wifiConfig);
                curID = netId;
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();

                check = true;
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
        if (view == buttonFlush) {
        } else if (view == buttonEnable) {
            if (wifiManager.isWifiEnabled() == false) {
                wifiManager.setWifiEnabled(true);
                buttonEnable.setText("Disable");
                wifiState.setText("Enabled");
            } else {
                wifiManager.setWifiEnabled(false);
                buttonEnable.setText("Enable");
                wifiState.setText("Disabled");
            }
        } else if (view == buttonBack) {
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
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int numberOfLevels = 5;
            wifiInfo = wifiManager.getConnectionInfo();
            level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            mImageView = findViewById(R.id.imageWifiState);
            wifiState = findViewById(R.id.wifiState);

            if(check) {
                if (wifiInfo.getSupplicantState() == SupplicantState.DISCONNECTED) {
                    if(i > 3) {
                        wifiState.setText("No connection");
                        Toast.makeText(context, "Fail to connect!", Toast.LENGTH_SHORT).show();
                        wifiManager.removeNetwork(curID);
                        check = false;
                        i = 0;
                    }
                    else {
                        wifiState.setText("Checking..." + i);
                        i++;
                    }
                } else {
                    wifiState.setText("Connecting...");
                    connecting();
                    check = false;
                }
            }
            else{
                if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                    int ipAddress = wifiInfo.getIpAddress();
                    String ipString = Formatter.formatIpAddress(ipAddress);
                    wifiState.setText(wifiInfo.getSSID() + " - " + ipString);
                    check = false;
                }
                else wifiState.setText("No connection");
            }
            if(wifiManager.isWifiEnabled() == false) level = -1;
            switch(level){
                case 0:
                    wifiManager.reconnect();
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

    public void connectTimerTask() {
        connecting_ = new TimerTask() {
            @Override
            public void run() {
                wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                    wifiState.setText(String.valueOf(wifiInfo.getSupplicantState()));
                    connecting_.cancel();
                    stopTimer();

                } else {
                    wifiManager.reconnect();
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
        connectTimerTask();
        timer.scheduleAtFixedRate(connecting_, 10000, 1000);
    }

    private Runnable mScanRunnable = new Runnable() {
        @Override
        public void run() {
            results = wifiManager.getScanResults();
            size = results.size();
            arraylist.clear();
            wifiManager.startScan();
            int i = 0;
            while (i < size) {
                HashMap<String, String> item = new HashMap<String, String>();
                item.put(ITEM_KEY, results.get(i).SSID + "  " + results.get(i).capabilities);

                arraylist.add(item);
                adapter.notifyDataSetChanged();
                i++;
            }
            mScan.postDelayed(mScanRunnable, 10000);
        }
    };


}
