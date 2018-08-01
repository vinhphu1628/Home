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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
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

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private TextView date;
    private TextView time;
    private Handler mTime = new Handler();
    private int level = 0;
    private Context context = this;
    private Handler mWifi = new Handler();
    private WifiManager wifi;
    private ImageView mImageView;
    private Button buttonSetting;
    private Button buttonList;

    private ListView appList;
    private String ITEM_KEY = "key";
    private ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    private SimpleAdapter adapter;

    //private Button buttonExit;
    private List<App> applist;
    private int appSize = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mWifi.post(mWifiRunnable);
        mTime.post(mTimeRunnable);

        buttonSetting = findViewById(R.id.buttonSetting);
        buttonSetting.setOnClickListener(this);
        //buttonExit = findViewById(R.id.buttonExit);
        //buttonExit.setOnClickListener(this);
        buttonList = findViewById(R.id.buttonList);
        buttonList.setOnClickListener(this);

        appList = findViewById(R.id.appList);

        this.adapter = new SimpleAdapter(this, arraylist, R.layout.app_list, new String[]{ITEM_KEY}, new int[]{R.id.list_value});
        appList.setAdapter(this.adapter);

        appList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(context, "App name is: " + applist.get(i).getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view == buttonSetting){
            startActivity(new Intent(this, WifiActivity.class));
        }
        else if(view == buttonList){
            arraylist.clear();
            applist = new ArrayList<App>(getAllApplications(context, false));
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
        }
        /*if(view == buttonExit){
            finish();
            System.exit(0);
        }*/
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

    private List getAllApplications(Context context, boolean includeSystemApps) {
        PackageManager packageManager = context.getPackageManager();
        List packages = packageManager.getInstalledPackages(0);

        List installedApps = new ArrayList<>();

        for (int i = 0;i<packages.size();i++) {
            PackageInfo pkgInfo = (PackageInfo) packages.get(i);
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

    class App {
        private String packageName;
        private String name;
        private Drawable icon;

        String getPackageName() {
            return packageName;
        }

        void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        public Drawable getIcon() {
            return icon;
        }

        public void setIcon(Drawable icon) {
            this.icon = icon;
        }
    }
}
