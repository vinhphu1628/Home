package com.example.xfoodz.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingAdapter extends BaseAdapter{
    private Context context;
    private String list[];
    private int[] img;

    @Override
    public int getCount()
    {
        return list.length;
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }


    public SettingAdapter(Context context, String[] list, int[] img) {
        this.context = context;
        this.list = list;
        this.img = img;
    }

    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(context).inflate(R.layout.setting_list, parent, false);

        TextView name = view.findViewById(R.id.list_value);
        ImageView imageView = view.findViewById(R.id.setting);

        name.setText(list[position]);
        imageView.setImageResource(img[position]);

        return view;
    }
}
