package com.example.xfoodz.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AppActivity extends MainActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView date, time;
    private Handler mTime = new Handler();
    private int level = 0;
    private Context context = this;
    private Handler mWifi = new Handler();
    private WifiManager wifi;
    private ImageView mImageView;
    private Button buttonList;
    private ImageView buttonBack;

    private ListView appList;
    private String ITEM_KEY = "key";
    private ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    private SimpleAdapter adapter;

    //private Button buttonExit;
    private List<App> applist;
    private int appSize = 0;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.app_activity);

        mWifi.post(mWifiRunnable);
        mTime.post(mTimeRunnable);

        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this);

        recyclerView = findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        //buttonExit = findViewById(R.id.buttonExit);
        //buttonExit.setOnClickListener(this);

       // appList = findViewById(R.id.appList);

  //      this.adapter = new SimpleAdapter(this, arraylist, R.layout.app_list, new String[]{ITEM_KEY}, new int[]{R.id.list_value});
//        appList.setAdapter(this.adapter);

//        appList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(context, "App name is: " + applist.get(i).getName(), Toast.LENGTH_SHORT).show();
//            }
//        });

        //listApp();
    }

    @Override
    public void onClick(View view) {
        if(view == buttonBack){
            finish();
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
            WifiInfo wifiInfo = wifi.getConnectionInfo();
            level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            mImageView = findViewById(R.id.imageWifiState);
            if(wifi.isWifiEnabled() == false) level = -1;
            switch(level){
                case 0:
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
            mWifi.postDelayed(mWifiRunnable,1000);
        }
    };

    @Override
    public void onResume(){
        super.onResume();

        List<App> installedApps = getAllApplications(getApplicationContext(), false);
        AppAdapter appAdapter = new AppAdapter(installedApps);

        for (App app: installedApps) {
            Log.d(TAG, "Installed package: " + app.getPackageName());
            Log.d(TAG, "Installed App    : " + app.getName());
        }

        recyclerView.setAdapter(appAdapter);
        recyclerView.setVisibility(View.VISIBLE);
    }

 /*   private void listApp(){
        arraylist.clear();
        applist = new ArrayList<AppActivity.App>(getAllApplications(context, false));
        int i = 0;
        appSize = applist.size();
        Toast.makeText(context, String.valueOf(applist.size()) + " applications loaded!", Toast.LENGTH_SHORT).show();

        while(appSize > 0){
            HashMap<String, String> item = new HashMap<String, String>();
            item.put(ITEM_KEY, applist.get(i).getName());

            arraylist.add(item);
            appSize--;
            adapter.notifyDataSetChanged();
            i++;
        }
    }*/

    private List getAllApplications(Context context, boolean includeSystemApps) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);

        List<App> installedApps = new ArrayList<>();

        for (PackageInfo pkgInfo : packages) {
            if (pkgInfo.versionName == null) {
                continue;
            }

            App newApp = new App();
            boolean isSystemApp = ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);

            newApp.setPackageName(pkgInfo.packageName);
            newApp.setName(pkgInfo.applicationInfo.loadLabel(packageManager).toString());
            newApp.setIcon(pkgInfo.applicationInfo.loadIcon(packageManager));

            if (includeSystemApps || !isSystemApp) {
                installedApps.add(newApp);
            }
        }
        return installedApps;
    }
}