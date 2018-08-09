package com.example.xfoodz.home;

import android.graphics.drawable.Drawable;

public class App {
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
