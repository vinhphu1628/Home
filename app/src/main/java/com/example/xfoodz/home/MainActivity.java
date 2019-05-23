package com.example.xfoodz.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.CardView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.xfoodz.home.MVVM.VM.NPNHomeViewModel;
import com.example.xfoodz.home.MVVM.View.NPNHomeView;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

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
public class MainActivity extends Activity implements View.OnClickListener, NPNHomeView {
    private TextView date, time;
    private Handler mTime = new Handler();
    private int level = 0;
    private Context context = this;
    private Handler mWifi = new Handler();
    private WifiManager wifi;
    private ImageView imageWifiState;
    private CardView numberCard, qrCard, rfidCard, faceCard;
    private ImageButton wifiButton;
    private List<App> installedApps;
    private String wifiAppName = "com.example.xfoodz.wifiactivity";
    private String numberAppName = "com.example.number";
    private String qrAppName = "com.example.xfoodz.scancode";
    private String rfidAppName = "com.example.xfoodz.rfid";
    private String faceAppName = "com.example.androidthings.imageclassifier";
    private TextView ip_address;
    private TextView wifi_status;
    private VideoView myVideoView;
    private int idleCount = 0;
    private boolean activities = true;
    private Handler mIdle = new Handler();
    private boolean idle = false;

    private Button buttonExit;

    private Handler mHandler = new Handler();
    private boolean mLedState = false;
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 1000;
    TextView text;

    private static final String TAG = "NPNIoTs";
    private int DATA_CHECKING = 0;
    private TextToSpeech niceTTS;

    //GPIO Configuration Parameters
    private static final String LED_PIN_NAME = "BCM26"; // GPIO port wired to the LED
    private Gpio mLedGpio;

    //SPI Configuration Parameters
    private static final String SPI_DEVICE_NAME = "SPI0.1";
    private SpiDevice mSPIDevice;
    private static final String CS_PIN_NAME = "BCM12"; // GPIO port wired to the LED
    private Gpio mCS;


    // UART Configuration Parameters
    private static final int BAUD_RATE = 115200;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private UartDevice mUartDevice;

    byte[] test_data = new byte[]{0, (byte) 0x8b, 0, 0};


    private String DOOR_OPEN = "1";
    private String DOOR_CLOSE = "0";


    public enum DOOR_STATE {
        NONE, WAIT_DOOR_OPEN, WAIT_DOOR_CLOSE, DOOR_OPENED, DOOR_CLOSED
    }

    DOOR_STATE door_state = DOOR_STATE.NONE;
    private int door_timer = 0;
    private int TIME_OUT_DOOR_OPEN = 3;

    private static final int CHUNK_SIZE = 512;

    NPNHomeViewModel mHomeViewModel; //Request server object
    Timer mBlinkyTimer;             //Timer
    private boolean isAllowProcess = true;
    int testCounter = 0;
    String name = "KSD";
    String link = "http://192.168.0.188:3000/api/test/android?code=1";

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            niceTTS.setLanguage(Locale.forLanguageTag("VI"));
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.text);

        mHomeViewModel = new NPNHomeViewModel();
        mHomeViewModel.attach(this, this);

        Log.i(TAG, "Starting BlinkActivity");

        PeripheralManager manager = PeripheralManager.getInstance();
        Log.i(TAG, "Start blinking LED GPIO pin");
        // Post a Runnable that continuously switch the state of the GPIO, blinking the
        // corresponding LED
        mHandler.post(mBlinkRunnable);
        initUart();
        initSPI();
        setupBlinkyTimer();

        //create an Intent
        Intent checkData = new Intent();
        //set it up to check for tts data
        checkData.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        //start it so that it returns the result
        startActivityForResult(checkData, DATA_CHECKING);

        //visibleAllControls(false);


        Ultis.writeToInternalFile("test.txt", "abcdefgh");
        String read = Ultis.readFromInternalFile("test.txt");
        Log.d(TAG, "Data is: " + read);

        mWifi.post(mWifiRunnable);
        mTime.post(mTimeRunnable);
        mIdle.post(mIdleRunnable);

        buttonExit = findViewById(R.id.buttonExit);
        buttonExit.setOnClickListener(this);

        numberCard = findViewById(R.id.number_card);
        numberCard.setOnClickListener(this);
        qrCard = findViewById(R.id.qr_card);
        qrCard.setOnClickListener(this);
        rfidCard = findViewById(R.id.rfid_card);
        rfidCard.setOnClickListener(this);
        faceCard = findViewById(R.id.face_card);
        faceCard.setOnClickListener(this);

        wifiButton = findViewById(R.id.imageWifiState);
        wifiButton.setOnClickListener(this);
        ip_address = findViewById(R.id.ip_address);
        wifi_status = findViewById(R.id.wifi_status);
//        myVideoView = findViewById(R.id.myVideoView);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        imageWifiState = findViewById(R.id.imageWifiState);

        installedApps = getAllApplications(getApplicationContext(), false);
        for (App app: installedApps) {
           // Log.d(TAG, "Installed package: " + app.getPackageName());
            //Log.d(TAG, "Installed App    : " + app.getName());
        }
//
//        myVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.kitkat));
//
//        myVideoView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                visibleAllControls(true);
//                activities = true;
//                idle = false;
//                idleCount = 0;
//                mIdle.post(mIdleRunnable);
//            }
//        });
//        myVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
//                myVideoView.start();
//            }
//        });
//        myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mediaPlayer) {
//                mediaPlayer.setVolume(100f, 100f);
//            }
//        });
        visibleAllControls(true);

        activities = true;
        idle = false;
        idleCount = 0;
        mIdle.post(mIdleRunnable);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove pending blink Runnable from the handler.
        mHandler.removeCallbacks(mBlinkRunnable);
        // Close the Gpio pin.
        Log.i(TAG, "Closing LED GPIO pin");
        try {
            //mLedGpio.close();
            closeUart();
            closeSPI();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
            Log.e(TAG, "Error closing UART device:", e);
        } finally {
            //mLedGpio = null;
        }
    }

    private String door_status = "";
    private UartDeviceCallback mCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            //read data from Rx buffer
            try {
                byte[] buffer = new byte[CHUNK_SIZE];
                int noBytes = -1;
                while ((noBytes = mUartDevice.read(buffer, buffer.length)) > 0) {
                    Log.d(TAG,"Number of bytes: " + Integer.toString(noBytes));

                    String str = new String(buffer,0,noBytes, "UTF-8");

                    Log.d(TAG,"Buffer is: " + str);
                    door_status = str;

                }
            } catch (IOException e) {
                Log.w(TAG, "Unable to transfer data over UART", e);
            }
            return true;
        }
        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };



    public void talkToMe(String sentence) {
        String speakWords = sentence;
        niceTTS.speak(speakWords, TextToSpeech.QUEUE_FLUSH, null);
    }


    public void request(){
        String urlSalinity = "";

        String url = link;
        mHomeViewModel.updateToServer(url);
        //urlSalinity = "http://9e53aa04.ngrok.io/api/test/android";
        /*String params = "{\n" +
                "\t\"name\"😕"trung\",\n" +
                "\t\"locker\":4,\n" +
                "\t\"code\"😕"123\"\n" +
                "}";
        // Log.d(TAG, "VALUEEEEEEEEEEE: " + urlSalinity);
        //mHomeViewModel.updateToServer(urlSalinity);
        mHomeViewModel.updatePost(urlSalinity,params);
        //url = "http://httpbin.org/post";

*/

    }


    private void setupBlinkyTimer()
    {
        mBlinkyTimer = new Timer();
        TimerTask blinkyTask = new TimerTask() {
            @Override
            public void run() {
                switch (door_state){
                    case NONE:
                        if(door_timer > 0)
                        {
                            door_timer--;
                            if(door_timer == 0){
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //visibleAllControls(false);
                                        isAllowProcess = true;
                                    }
                                });

                            }
                        }
                        break;
                    case WAIT_DOOR_OPEN:
                        door_timer--;
                        if(door_status.equals(DOOR_OPEN) == true){
                            door_state = DOOR_STATE.DOOR_OPENED;
                        }else {
                            readStatus(currentDoor);
                        }
                        if(door_timer == 0)
                        {
                            Log.d("NPNIoTs", "Open again the door: " + currentDoor);
                            writeUartData(currentDoor);
                            door_timer = 3;
                        }
                        break;
                    case DOOR_OPENED:
                        door_timer = 10;
                        readStatus(currentDoor);
                        door_state = DOOR_STATE.WAIT_DOOR_CLOSE;
                        break;
                    case WAIT_DOOR_CLOSE:
                        door_timer--;
                        readStatus(currentDoor);
                        if(door_status.equals(DOOR_CLOSE)){
                            door_state = DOOR_STATE.DOOR_CLOSED;
                        }
                        if(door_timer <= 0)
                        {
                            talkToMe("Xin vui lòng đóng cửa số " + currentDoor);
                            door_timer = 5;
                        }
                        break;
                    case DOOR_CLOSED:
                        talkToMe("Xin cám ơn quý khách");
                        door_state = DOOR_STATE.NONE;
                        door_timer = 5;
                        break;
                    default:
                        break;
                }
            }
        };
        mBlinkyTimer.schedule(blinkyTask, 5000, 1000);
    }



    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the GPIO is already closed

            // Toggle the GPIO stat
            // Log.d(TAG, "State set to " + mLedState);
            request();

            Log.e(TAG, "saaaaaaaaaaaaaa");
            //request1();
            // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds
            mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
        }
    };

    public void writeUartData(String message) {
        try {
            byte[] buffer = {'W',' ',' '};
            buffer[2] =  (byte)(Integer.parseInt(message));
            int count = mUartDevice.write(buffer, buffer.length);
            Log.d(TAG, "Send: "   + buffer[2]);
        }catch (IOException e)
        {
            Log.d(TAG, "Error on UART");
        }
    }

    public void readStatus(String ID)
    {
        try {
            byte[] buffer = {'R',' ',' '};
            buffer[2] =  (byte)(Integer.parseInt(ID));
            int count = mUartDevice.write(buffer, buffer.length);
            //Log.d(TAG, "Wrote " + count + " bytes to peripheral  "  + buffer[2]);
        }catch (IOException e)
        {
            Log.d(TAG, "Error on UART");
        }
    }

    private String currentDoor = "";
    @Override
    public void onSuccessUpdateServer(String message) {
        Log.d(TAG, "Request server is successful " + message);
        writeUartData(message);
        String speakWords = "Xin vui lòng đến ô số " + message;
        //niceTTS.speak(speakWords, TextToSpeech.QUEUE_FLUSH, null);
        door_state = DOOR_STATE.WAIT_DOOR_OPEN;
        door_timer = TIME_OUT_DOOR_OPEN;
        currentDoor = message;
        readStatus(currentDoor);
    }

    @Override
    public void onErrorUpdateServer(String message) {
        Log.d(TAG, "Request server is fail");
    }


    private void openUart(String name, int baudRate) throws IOException {
        mUartDevice = PeripheralManager.getInstance().openUartDevice(name);
        // Configure the UART
        mUartDevice.setBaudrate(baudRate);
        mUartDevice.setDataSize(DATA_BITS);
        mUartDevice.setParity(UartDevice.PARITY_NONE);
        mUartDevice.setStopBits(STOP_BITS);

        mUartDevice.registerUartDeviceCallback(mCallback);
    }

    private void closeUart() throws IOException {
        if (mUartDevice != null) {
            mUartDevice.unregisterUartDeviceCallback(mCallback);
            try {
                mUartDevice.close();
            } finally {
                mUartDevice = null;
            }
        }
    }

    private void initGPIO()
    {
        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            mLedGpio = manager.openGpio(LED_PIN_NAME);
            // Step 2. Configure as an output.
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            mCS = manager.openGpio(CS_PIN_NAME);
            mCS.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);


        } catch (IOException e) {
            Log.d(TAG, "Error on PeripheralIO API");
        }
    }

    private void initUart()
    {
        try {
            openUart("UART0", BAUD_RATE);
        }catch (IOException e) {
            Log.d(TAG, "Error on UART API");
        }
    }

    private void initSPI()
    {
        PeripheralManager manager = PeripheralManager.getInstance();
        List<String> deviceList = manager.getSpiBusList();
        if(deviceList.isEmpty())
        {
            Log.d(TAG,"No SPI bus is not available");
        }
        else
        {
            Log.d(TAG,"SPI bus available: " + deviceList);
            //check if SPI_DEVICE_NAME is in list
            try {
                mSPIDevice = manager.openSpiDevice(SPI_DEVICE_NAME);

                mSPIDevice.setMode(SpiDevice.MODE1);
                mSPIDevice.setFrequency(1000000);
                mSPIDevice.setBitsPerWord(8);
                mSPIDevice.setBitJustification(SpiDevice.BIT_JUSTIFICATION_MSB_FIRST);


                Log.d(TAG,"SPI: OK... ");


            }catch (IOException e)
            {
                Log.d(TAG,"Open SPI bus fail... ");
            }
        }
    }


    private void closeSPI() throws IOException {
        if(mSPIDevice != null)
        {
            try {
                mSPIDevice.close();
            }finally {
                mSPIDevice = null;
            }

        }
    }

    @Override
    public void onClick(View view) {
        if(view == numberCard){
            activities = true;
            if(isNetworkAvailable()) {
                Intent LaunchIntent = this.getPackageManager().getLaunchIntentForPackage(numberAppName);
                if (LaunchIntent != null) {
                    this.startActivity(LaunchIntent);
                    finish();
                    System.exit(0);
                } else
                    Toast.makeText(context, "This application is not available!", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(context, "No internet connection!", Toast.LENGTH_SHORT).show();
        }
        else if(view == qrCard){
            activities = true;
            if(isNetworkAvailable()) {
                Intent LaunchIntent = this.getPackageManager().getLaunchIntentForPackage(qrAppName);
                if (LaunchIntent != null) {
                    this.startActivity(LaunchIntent);
                    finish();
                    System.exit(0);
                } else
                    Toast.makeText(context, "This application is not available!", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(context, "No internet connection!", Toast.LENGTH_SHORT).show();
        }
        else if(view == rfidCard){
            activities = true;
            if(isNetworkAvailable()) {
                Intent LaunchIntent = this.getPackageManager().getLaunchIntentForPackage(rfidAppName);
                if (LaunchIntent != null) {
                    this.startActivity(LaunchIntent);
                    finish();
                    System.exit(0);
                } else
                    Toast.makeText(context, "This application is not available!", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(context, "No internet connection!", Toast.LENGTH_SHORT).show();
        }
        else if(view == faceCard){
            activities = true;
            if(isNetworkAvailable()) {
                Intent LaunchIntent = this.getPackageManager().getLaunchIntentForPackage(faceAppName);
                if (LaunchIntent != null) {
                    this.startActivity(LaunchIntent);
                    finish();
                    System.exit(0);
                } else
                    Toast.makeText(context, "This application is not available!", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(context, "No internet connection!", Toast.LENGTH_SHORT).show();
        }
        else if(view == wifiButton) {
            activities = true;
            Intent LaunchIntent = this.getPackageManager().getLaunchIntentForPackage(wifiAppName);
            if(LaunchIntent != null) {
                this.startActivity( LaunchIntent );
                finish();
                System.exit(0);
            }
            else Toast.makeText(context, "This application is not available!", Toast.LENGTH_SHORT).show();
        }
        if(view == buttonExit){
            finish();
            System.exit(0);
        }
    }

    private Runnable mTimeRunnable = new Runnable() {
        @Override
        public void run() {
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
            if(wifi.isWifiEnabled() == false) level = -1;
            if(isEthernetConnected()) level = -2;
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
                case -2:
                    imageWifiState.setImageResource(R.drawable.ic_computer_black_24dp);
                    break;
            }
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                int ipAddress = wifiInfo.getIpAddress();
                String ipString = Formatter.formatIpAddress(ipAddress);
                ip_address.setText(ipString);
            } else ip_address.setText("No connection");
            mWifi.postDelayed(mWifiRunnable,1000);
        }
    };

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

    private void visibleAllControls(boolean isVisible){
        if(isVisible == false) {
            time.setVisibility(View.GONE);
            date.setVisibility(View.GONE);
            wifi_status.setVisibility(View.GONE);
            ip_address.setVisibility(View.GONE);
            imageWifiState.setVisibility(View.GONE);
            numberCard.setVisibility(View.GONE);
            qrCard.setVisibility(View.GONE);
            rfidCard.setVisibility(View.GONE);
//            faceCard.setVisibility(View.GONE);

//            myVideoView.setVisibility(View.VISIBLE);
//            myVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.kitkat));
//
//            //myVideoView.setVideoURI(Uri.parse("https://hjyjrvmlsk.vcdn.com.vn/hls/elgfjdh/index.m3u8"));
//
//            myVideoView.start();

        }else {
            time.setVisibility(View.VISIBLE);
            date.setVisibility(View.VISIBLE);
            wifi_status.setVisibility(View.VISIBLE);
            ip_address.setVisibility(View.VISIBLE);
            imageWifiState.setVisibility(View.VISIBLE);
            numberCard.setVisibility(View.VISIBLE);
            qrCard.setVisibility(View.VISIBLE);
            rfidCard.setVisibility(View.VISIBLE);
//            faceCard.setVisibility(View.VISIBLE);

//            myVideoView.setVisibility(View.GONE);
//            myVideoView.stopPlayback();

        }
    }
    private Runnable mIdleRunnable = new Runnable() {
        @Override
        public void run() {
            if(idleCount == 30) {
                idle = true;
                idleCount = 0;
            }
            else {
                if(!activities) idleCount++;
                else {
                    activities = false;
                    idleCount = 0;
                }
            }
            if(!idle) mIdle.postDelayed(mIdleRunnable, 1000);
        }
    };

    private Boolean isNetworkAvailable() {
        ConnectivityManager cm
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public Boolean isEthernetConnected(){
        if(isNetworkAvailable()){
            ConnectivityManager cm
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            return (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_ETHERNET);
        }
        return false;
    }
}

