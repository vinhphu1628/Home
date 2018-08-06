package com.example.xfoodz.home;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

public class WifiAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String, String>> list;
    private List<ScanResult> results;

    @Override
    public int getCount()
    {
        return list.size();
    }

    @Override
    public Object getItem(int position)
    {
        return list.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return list.indexOf(position);
    }


    public WifiAdapter(Context context, ArrayList<HashMap<String, String>> list, List<ScanResult> results) {
        this.context = context;
        this.list = list;
        this.results = results;
    }

    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(context).inflate(R.layout.wifi_list, parent, false);

        TextView txtTitle = view.findViewById(R.id.list_value);
        ImageView imageView = view.findViewById(R.id.signal);

        txtTitle.setText(list.get(position).get("key"));

        ScanResult scanResult = results.get(position);

        String capabilities =  scanResult.capabilities;

        int level = WifiManager.calculateSignalLevel(scanResult.level, 5);
        String txtLevel = String.valueOf(level);

        if (capabilities.contains("PSK")){
            txtLevel = txtLevel + "LOCK";
        }
        else if(capabilities.contains("WEP")){
            txtLevel = txtLevel + "LOCK";
        }
        else if(capabilities.contains("EAP")){
            txtLevel = txtLevel + "LOCK";
        }

        switch (txtLevel) {
            case "0":
                imageView.setImageResource(R.drawable.ic_signal_wifi_0_bar_black_48dp);
                break;
            case "1":
                imageView.setImageResource(R.drawable.ic_signal_wifi_1_bar_black_48dp);
                break;
            case "2":
                imageView.setImageResource(R.drawable.ic_signal_wifi_2_bar_black_48dp);
                break;
            case "3":
                imageView.setImageResource(R.drawable.ic_signal_wifi_3_bar_black_48dp);
                break;
            case "4":
                imageView.setImageResource(R.drawable.ic_signal_wifi_4_bar_black_48dp);
                break;
            case "1LOCK":
                imageView.setImageResource(R.drawable.ic_signal_wifi_1_bar_lock_black_48dp);
                break;
            case "2LOCK":
                imageView.setImageResource(R.drawable.ic_signal_wifi_2_bar_lock_black_48dp);
                break;
            case "3LOCK":
                imageView.setImageResource(R.drawable.ic_signal_wifi_3_bar_lock_black_48dp);
                break;
            case "4LOCK":
                imageView.setImageResource(R.drawable.ic_signal_wifi_4_bar_lock_black_48dp);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_signal_wifi_off_bar_black_48dp);
                break;
        }
        return view;
    }
}
