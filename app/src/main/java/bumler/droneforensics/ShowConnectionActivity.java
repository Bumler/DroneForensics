package bumler.droneforensics;

        import android.app.Activity;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.hardware.usb.UsbManager;
        import android.os.Build;
        import android.os.Handler;
        import android.os.Looper;
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

        import dji.sdk.Camera.DJICamera;
        import dji.sdk.Products.DJIAircraft;
        import dji.sdk.Products.DJIHandHeld;
        import dji.sdk.SDKManager.DJISDKManager;
        import dji.sdk.base.DJIBaseProduct;

        import dji.sdk.Products.DJIAircraft;
        import dji.sdk.Products.DJIHandHeld;
        import dji.sdk.SDKManager.DJISDKManager;
        import dji.sdk.base.DJIBaseComponent;
        import dji.sdk.base.DJIBaseProduct;
        import dji.sdk.base.DJIError;
        import dji.sdk.base.DJISDKError;

public class ShowConnectionActivity extends Activity {

    public static final String TAG = ShowConnectionActivity.class.getName();

    private TextView connectionStatus;

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_connection);

        initUI();
        updateTitleBar();

        }

    private  void check(){

    }

    private void initUI(){
        DJISDKManager.getInstance().initSDKManager(this, mDJISDKManagerCallback);

        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTitleBar();
            }
        });

        connectionStatus = (TextView) findViewById(R.id.tv_connection_status);
    }

    private void updateTitleBar() {
        if(connectionStatus == null) return;
        boolean ret = false;
        DJIBaseProduct product = mProduct;
        if (product != null) {
            if(product.isConnected()) {
                //The product is connected
                connectionStatus.setText(mProduct.getModel() + " Connected");
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

    //----------------------------------------------------------------------------
    //Code from FPVDemo
    //----------------------------------------------------------------------------
    private static DJIBaseProduct mProduct;

    private Handler mHandler;

    public static final String FLAG_CONNECTION_CHANGE = "fpv_tutorial_connection_change";

    /**
     * This function is used to get the instance of DJIBaseProduct.
     * If no product is connected, it returns null.
     */
    public static synchronized DJIBaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getDJIProduct();
        }
        return mProduct;
    }

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof DJIAircraft;
    }

    public static boolean isHandHeldConnected() {
        return getProductInstance() != null && getProductInstance() instanceof DJIHandHeld;
    }

    public static synchronized DJICamera getCameraInstance() {

        if (getProductInstance() == null) return null;
        return getProductInstance().getCamera();

    }

    /**
     * When starting SDK services, an instance of interface DJISDKManager.DJISDKManagerCallback will be used to listen to
     * the SDK Registration result and the product changing.
     */
    private DJISDKManager.DJISDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.DJISDKManagerCallback() {

        //Listens to the SDK registration result
        @Override
        public void onGetRegisteredResult(DJIError error) {
            if(error == DJISDKError.REGISTRATION_SUCCESS) {
                DJISDKManager.getInstance().startConnectionToProduct();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "register sdk fails, check network is available", Toast.LENGTH_LONG).show();
                    }
                });

            }
            Log.e("TAG", error.toString());
        }

        //Listens to the connected product changing, including two parts, component changing or product connection changing.
        @Override
        public void onProductChanged(DJIBaseProduct oldProduct, DJIBaseProduct newProduct) {

            mProduct = newProduct;
            if(mProduct != null) {
                mProduct.setDJIBaseProductListener(mDJIBaseProductListener);
            }

            notifyStatusChange();
        }
    };

    private DJIBaseProduct.DJIBaseProductListener mDJIBaseProductListener = new DJIBaseProduct.DJIBaseProductListener() {

        @Override
        public void onComponentChange(DJIBaseProduct.DJIComponentKey key, DJIBaseComponent oldComponent, DJIBaseComponent newComponent) {

            if(newComponent != null) {
                newComponent.setDJIComponentListener(mDJIComponentListener);
            }
            notifyStatusChange();
        }

        @Override
        public void onProductConnectivityChanged(boolean isConnected) {

            notifyStatusChange();
        }

    };

    private DJIBaseComponent.DJIComponentListener mDJIComponentListener = new DJIBaseComponent.DJIComponentListener() {

        @Override
        public void onComponentConnectivityChanged(boolean isConnected) {
            notifyStatusChange();
        }

    };

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };
    //----------------------------------------------------------------------------
    //End of code from FPV demo
    //----------------------------------------------------------------------------

    public void onClick(View v){

    }


}