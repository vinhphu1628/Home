package com.example.xfoodz.home;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;


public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {
    private static final String TAG = AppAdapter.class.getSimpleName();
    final private List<App> appList;

    public AppAdapter(List<App> appList) {
        this.appList = appList;
    }

    @Override
    public AppAdapter.AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_row,parent,false));
    }

    @Override
    public void onBindViewHolder(AppAdapter.AppViewHolder holder, int position) {
        holder.image.setImageDrawable(appList.get(position).getIcon());
        holder.appName.setText(appList.get(position).getName());
        holder.packageName.setText(appList.get(position).getPackageName());
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView appName;
        final TextView packageName;

        public AppViewHolder(final View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.app_icon_id);
            appName = itemView.findViewById(R.id.text_app_name_id);
            packageName = itemView.findViewById(R.id.text_package_name_id);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog myDialog = new Dialog(itemView.getContext());
                    myDialog.setContentView(R.layout.app_option);
                    myDialog.setCancelable(true);
                    TextView name = myDialog.findViewById(R.id.name);
                    ImageView icon = myDialog.findViewById(R.id.icon);
                    final TextView appPackage = myDialog.findViewById(R.id.appPackage);
                    Button launch = myDialog.findViewById(R.id.buttonLaunch);
                    Button back = myDialog.findViewById(R.id.buttonBack);
                    Button delete = myDialog.findViewById(R.id.buttonDelete);

                    name.setText(appName.getText());
                    icon.setImageDrawable(image.getDrawable());
                    appPackage.setText(packageName.getText());
                    
                    myDialog.setTitle("Option");
                    myDialog.show();

                    launch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            myDialog.cancel();
                            Intent LaunchIntent = itemView.getContext().getPackageManager().getLaunchIntentForPackage(packageName.getText().toString());
                            itemView.getContext().startActivity( LaunchIntent );
                        }
                    });
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent DeleteIntent = new Intent(Intent.ACTION_DELETE);
                            DeleteIntent.setData(Uri.parse("package:" + appPackage.getText().toString()));
                            view.getContext().startActivity(DeleteIntent);
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
            });
        }
    }
}
