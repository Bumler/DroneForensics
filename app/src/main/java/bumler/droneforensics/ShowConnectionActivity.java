package bumler.droneforensics;

        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.hardware.usb.UsbManager;
        import android.os.Build;
        import android.os.Handler;
        import android.os.Message;
        import android.preference.PreferenceManager;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.app.TaskStackBuilder;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.support.v7.app.NotificationCompat;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;
        import android.Manifest;

        import java.text.SimpleDateFormat;
        import java.util.Date;

        import dji.sdk.Products.DJIAircraft;
        import dji.sdk.SDKManager.DJISDKManager;
        import dji.sdk.base.DJIBaseProduct;

public class ShowConnectionActivity extends AppCompatActivity {

    public static final String TAG = ShowConnectionActivity.class.getName();

    private TextView connectionStatus;

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_connection);

        //initUI();


        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        initUI();
        updateTitleBar();
        check();
        }

    private  void check(){
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check();
            }
        });
    }

    private void initUI(){
        connectionStatus = (TextView) findViewById(R.id.tv_connection_status);
    }

    private void updateTitleBar() {
        if(connectionStatus == null) return;
        boolean ret = false;
        DJIBaseProduct product = practice_practice_Activity.getProductInstance();
        if (product != null) {
            if(product.isConnected()) {
                //The product is connected
                connectionStatus.setText(practice_practice_Activity.getProductInstance().getModel() + " Connected");
                ret = true;
            } else {
                if(product instanceof DJIAircraft) {
                    DJIAircraft aircraft = (DJIAircraft)product;
                    if(aircraft.getRemoteController() != null && aircraft.getRemoteController().isConnected()) {
                        // The product is not connected, but the remote controller is connected
                        connectionStatus.setText("only RC Connected");
                        ret = true;
                    }
                }
            }
        }

        if(!ret) {
            // The product or the remote controller are not connected.
            connectionStatus.setText("Disconnected");
        }
    }

    public void onClick(View v){

    }


}