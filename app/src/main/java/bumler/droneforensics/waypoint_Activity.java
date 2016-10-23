package bumler.droneforensics;

import android.app.Activity;

/**
 * Created by d10 on 8/22/2016.
 */
import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.Math;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.FlightController.DJIFlightControllerDataType;
import dji.sdk.FlightController.DJIFlightControllerDelegate;
import dji.sdk.MissionManager.DJIMission;
import dji.sdk.MissionManager.DJIMissionManager;
import dji.sdk.MissionManager.DJIWaypoint;
import dji.sdk.MissionManager.DJIWaypointMission;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIError;

public class waypoint_Activity extends FragmentActivity implements View.OnClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback, DJIMissionManager.MissionProgressStatusCallback, DJIBaseComponent.DJICompletionCallback/*, LocationListener*/ {

    protected static final String TAG = "GSDemoActivity";

    private GoogleMap gMap;

    private Button locate, add, clear;
    private Button config, prepare;
    private ImageButton start, stop, camera;

    private boolean isAdd = false;

    private double droneLocationLat = 181, droneLocationLng = 181;
    private final Map<Integer, Marker> mMarkers = new ConcurrentHashMap<Integer, Marker>();
    private Marker droneMarker = null;

    private float altitude = 100.0f;
    private float mSpeed = 10.0f;

    private DJIWaypointMission mWaypointMission;
    private DJIMissionManager mMissionManager;
    private DJIFlightController mFlightController;

    private DJIWaypointMission.DJIWaypointMissionFinishedAction mFinishedAction = DJIWaypointMission.DJIWaypointMissionFinishedAction.NoAction;
    private DJIWaypointMission.DJIWaypointMissionHeadingMode mHeadingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.Auto;

    private double userLat;
    private double userLong;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BTW_UPDATES = 1000 * 60;

    protected LocationManager locationManager;

    @Override
    protected void onResume() {
        super.onResume();
        initFlightController();
        initMissionManager();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * @Description : RETURN Button RESPONSE FUNCTION
     */
    public void onReturn(View view) {
        Log.d(TAG, "onReturn");
        this.finish();
    }

    private void setResultToToast(final String string) {
        waypoint_Activity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(waypoint_Activity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initUI() {

        locate = (Button) findViewById(R.id.locate);
        add = (Button) findViewById(R.id.add);
        clear = (Button) findViewById(R.id.clear);
        config = (Button) findViewById(R.id.config);
        prepare = (Button) findViewById(R.id.prepare);
        start = (ImageButton) findViewById(R.id.start);
        stop = (ImageButton) findViewById(R.id.stop);
        camera = (ImageButton) findViewById(R.id.camera);

        locate.setOnClickListener(this);
        locate.setBackgroundColor(Color.YELLOW);
        add.setOnClickListener(this);
        clear.setOnClickListener(this);
        config.setOnClickListener(this);
        prepare.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        camera.setOnClickListener(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.activity_waypoints);

        IntentFilter filter = new IntentFilter();
        filter.addAction(FPVDemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        initUI();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    private void onProductConnectionChange() {
        initMissionManager();
        initFlightController();
    }

    private void initMissionManager() {
        DJIBaseProduct product = FPVDemoApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            setResultToToast("Disconnected");
            mMissionManager = null;
            return;
        } else {

            setResultToToast("Product connected");
            mMissionManager = product.getMissionManager();
            mMissionManager.setMissionProgressStatusCallback(this);
            mMissionManager.setMissionExecutionFinishedCallback(this);
        }

        mWaypointMission = new DJIWaypointMission();
    }

    private void initFlightController() {

        DJIBaseProduct product = FPVDemoApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof DJIAircraft) {
                mFlightController = ((DJIAircraft) product).getFlightController();
            }
        }

        if (mFlightController != null) {
            mFlightController.setUpdateSystemStateCallback(new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {

                @Override
                public void onResult(DJIFlightControllerDataType.DJIFlightControllerCurrentState state) {
                    droneLocationLat = state.getAircraftLocation().getLatitude();
                    droneLocationLng = state.getAircraftLocation().getLongitude();
                    updateDroneLocation();
                }
            });
        }
    }

    /**
     * DJIMissionManager Delegate Methods
     */
    @Override
    public void missionProgressStatus(DJIMission.DJIMissionProgressStatus progressStatus) {

    }

    /**
     * DJIMissionManager Delegate Methods
     */
    @Override
    public void onResult(DJIError error) {
        setResultToToast("Execution finished: " + (error == null ? "Success!" : error.getDescription()));
    }

    private void setUpMap() {
        gMap.setOnMapClickListener(this);// add the listener for click for amap object

    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    // Update the drone location based on states from MCU.
    private void updateDroneLocation() {

        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);

        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }

                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                    droneMarker = gMap.addMarker(markerOptions);
                }
            }
        });
    }

    private void markWaypoint(LatLng point, String pointColor) {
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        if (pointColor.equals("blue")) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }
        else if (pointColor.equals("green")){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }
        else if (pointColor.equals("yellow")){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        }
        else if (pointColor.equals("red")){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        }
        else{
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        }

        Marker marker = gMap.addMarker(markerOptions);
        mMarkers.put(mMarkers.size(), marker);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate: {
                updateDroneLocation();
                cameraUpdate(); // Locate the drone's place
                break;
            }
            case R.id.add: {
                addAtPosition();
                //enableDisableAdd();
                break;
            }
            case R.id.clear: {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gMap.clear();
                        waypoints = new ArrayList<LatLng>();
                        add.setEnabled(true);
                    }

                });
                if (mWaypointMission != null) {
                    mWaypointMission.removeAllWaypoints(); // Remove all the waypoints added to the task
                }
                break;
            }
            case R.id.config: {
                showSettingDialog();
                break;
            }
            case R.id.prepare: {
                prepareWayPointMission();
                break;
            }
            case R.id.start: {
                startWaypointMission();
                break;
            }
            case R.id.stop: {
                stopWaypointMission();
                break;
            }
            case R.id.camera: {
                Intent i = new Intent(waypoint_Activity.this, camera_Activity.class);
                startActivity(i);
            }
            default:
                break;
        }
    }

    Location loc;
    public Location getLocation(){
//        try{
//            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BTW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//            if (locationManager != null) {
//                loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        return loc;
    }

    ArrayList<LatLng> waypoints = new ArrayList<>();

    @Override
    public void onMapClick(LatLng point) {
        if(add.isEnabled()){
            addToWaypoints(point);
        }
//        if (isAdd == true) {
//            markWaypoint(point);
//            DJIWaypoint mWaypoint = new DJIWaypoint(point.latitude, point.longitude, altitude);
//            //Add Waypoints to Waypoint arraylist;
//            if (mWaypointMission != null) {
//                mWaypointMission.addWaypoint(mWaypoint);
//            }
//        } else {
//            setResultToToast("Cannot Add Waypoint");
//        }
    }

    private void addAtPosition(){
//        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        double longitude = location.getLongitude();
//        double latitude = location.getLatitude();
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng point = new LatLng(latitude, longitude);

        addToWaypoints(point);
    }

    private void addToWaypoints(LatLng point){
        waypoints.add(point);
        markWaypoint(point, "blue");
        createDJIWaypoint(point);

        if(waypoints.size() == 2){
            mirrorWaypoints();
            buildFlightPath();
        }
    }

    private void createDJIWaypoint(LatLng point){
        DJIWaypoint mWaypoint = new DJIWaypoint(point.latitude, point.longitude, altitude);
        //Add Waypoints to Waypoint arraylist;
        if (mWaypointMission != null) {
            mWaypointMission.addWaypoint(mWaypoint);
        } else {
            //setResultToToast("Cannot Add Waypoint");
        }
    }

    private void mirrorWaypoints(){
        LatLng pointA = waypoints.get(0);
        LatLng pointB = waypoints.get(1);
        LatLng mirror = new LatLng(pointB.latitude, pointA.longitude);
        waypoints.add(mirror);
        //markWaypoint(mirror, "green");
        createDJIWaypoint(mirror);

        mirror = new LatLng(pointA.latitude, pointB.longitude);
        //markWaypoint(mirror, "green");
        waypoints.add(mirror);
        createDJIWaypoint(mirror);

        add.setEnabled(false);
    }

    double height;
    double width;
    double cameraHeight;
    double cameraWidth;
    int heightCells;
    int widthCells;
    double move_vertical;
    double move_horizontal;
    final double r_earth = 6378000;
    LatLng initialPoint;
    LatLng currentPoint;
    private void buildFlightPath(){
        //TODO order points SouthWest, NorthWest, NorthEast, SouthEast
        orientWaypoints();

        //this finds the distance of the points non diagonally
        height = calculateDistance(waypoints.get(0), waypoints.get(1));
        width = calculateDistance(waypoints.get(0), waypoints.get(3));

        setResultToToast("Height "+height+ " Width "+width);

        //find the camera dimensions based on altitude. Here we are assuming an altitude of 30m
        //TODO dynamically calculate altitude
        //TODO set altitude to 30
        cameraHeight = 22.5;
        cameraWidth  = 30.0;

        //once we have created a space width by height we will break up that space into cells
        //these cells will be calculated by the dimensions of our camera
        heightCells = findFlightCells(height, cameraHeight);
        widthCells  = findFlightCells(width, cameraWidth);

        setResultToToast("Height Cells "+heightCells+" Width Cells "+widthCells);

        //divides the vertical and horizontal distances by the number of cells
        move_vertical = setMove(height, heightCells);
        move_horizontal = setMove(width, widthCells);

        //generates the initial lat and long point based on the vertical and horizontal movement applied to the southwestern point
        double initialPointLat = waypoints.get(0).latitude  + ((move_vertical/2) / r_earth) * (180 / Math.PI);
        double initialPointLong = waypoints.get(0).longitude  + ((-1 * move_horizontal/2) / r_earth) * (180 / Math.PI) / Math.cos(waypoints.get(0).longitude * Math.PI/180);
        initialPoint = new LatLng(initialPointLat, initialPointLong);
        waypoints.add(initialPoint);
        markWaypoint(initialPoint, "default");

        generateWaypoints();
    }

    private void generateWaypoints(){
        boolean up = true;
        currentPoint = initialPoint;
        for (int i = 0; i < widthCells-1; i++){
            for (int j = 0; j < heightCells-1; j++){
                if (up){
                    makePoint("up");
                }
                else{
                    makePoint("down");
                }
            }

            if (i != widthCells - 2){
                makePoint("right");
            }
            if (up){
                up = false;
            }
            else{
                up = true;
            }
        }
    }

    private void makePoint(String direction){
        LatLng point;
        if (direction.equals("up")){
            //generates the initial lat and long point based on the vertical and horizontal movement applied to the southwestern point
            double PointLat = currentPoint.latitude  + ((move_vertical) / r_earth) * (180 / Math.PI);
            point = new LatLng(PointLat, currentPoint.longitude);
        }
        else if(direction.equals("down")){
            double PointLat = currentPoint.latitude  + ((-1 * move_vertical) / r_earth) * (180 / Math.PI);
            point = new LatLng(PointLat, currentPoint.longitude);
        }
        else{
            double PointLong = currentPoint.longitude  + ((-1 * move_horizontal) / r_earth) * (180 / Math.PI) / Math.cos(waypoints.get(0).longitude * Math.PI/180);
            point = new LatLng(currentPoint.latitude, PointLong);
        }

        waypoints.add(point);
        markWaypoint(point, "default");
        currentPoint = point;
    }

    private void orientWaypoints(){
        //the southwest point will have the smallest lat and longitude
        LatLng southwest = waypoints.get(0);
        for (int i = 1; i < waypoints.size(); i++){
            if (waypoints.get(i).latitude <= southwest.latitude && waypoints.get(i).longitude <= southwest.longitude){
                southwest = waypoints.get(i);
            }
        }

        //northwest will have the lowest long and highest lat
        LatLng northwest = waypoints.get(0);
        for (int i = 1; i < waypoints.size(); i++){
            if (waypoints.get(i).latitude >= northwest.latitude && waypoints.get(i).longitude <= northwest.longitude){
                northwest = waypoints.get(i);
            }
        }

        //northeast should have the highest lat and long
        LatLng northeast = waypoints.get(0);
        for (int i = 1; i < waypoints.size(); i++){
            if (waypoints.get(i).latitude >= northeast.latitude && waypoints.get(i).longitude >= northeast.longitude){
                northeast = waypoints.get(i);
            }
        }

        //ssoutheast should have the lowest lat and lowest long
        LatLng southeast = waypoints.get(0);
        for (int i = 1; i < waypoints.size(); i++){
            if (waypoints.get(i).latitude <= southeast.latitude && waypoints.get(i).longitude >= southeast.longitude){
                southeast = waypoints.get(i);
            }
        }

        waypoints = new ArrayList<>();
        waypoints.add(southwest);
        markWaypoint(southwest, "green");

        waypoints.add(northwest);
        markWaypoint(northwest, "blue");

        waypoints.add(northeast);
        markWaypoint(northeast, "red");

        waypoints.add(southeast);
        markWaypoint(southeast, "yellow");
    }

    private double calculateDistance(LatLng a, LatLng b){
                /*
                http://www.movable-type.co.uk/scripts/latlong.html
            Haversine
            formula:	a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
            c = 2 ⋅ atan2( √a, √(1−a) )
            d = R ⋅ c
                 */

        double R = 6371e3;
        double omega1 = Math.toRadians(a.latitude);
        double omega2 = Math.toRadians(b.latitude);
        double changeInOmega = Math.toRadians(b.latitude - a.latitude);
        double changeInLamda = Math.toRadians(b.longitude - a.longitude);

        double A = Math.sin((changeInOmega/2)) * Math.sin((changeInOmega/2)) +
                Math.cos(omega1) * Math.cos(omega2) * Math.sin((changeInLamda/2)) *Math.sin((changeInLamda/2));

        double c = 2 * Math.atan2(Math.sqrt(A), Math.sqrt(1-A));
        double d = R * c;

        return d;
    }

    private void cameraUpdate() {
        DJIBaseProduct product = FPVDemoApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
            float zoomlevel = (float) 18.0;
            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
            gMap.moveCamera(cu);
        }
        else{
            setResultToToast("Drone not found");
        }
    }

    private void swapPoints(int a, int b){
        LatLng temp = waypoints.get(b);
        waypoints.set(b, waypoints.get(a));
        waypoints.set(a, temp);
    }

    private void displayWaypoints(){
        gMap.clear();
        for (int i = 0; i < waypoints.size(); i++){
            markWaypoint(waypoints.get(i), "yellow");
        }
    }

    //breaks the total measurement into the number of cells the camera would need to capture it all
    private int findFlightCells(double m, double dimens){
        int numCells;

        //if our measurement is smaller than our base dimensions there can only be one cell
        //this is not optimal use. Although it allows it the user is given a warning
        //this removes any worries about dividing by zero
        if (m < dimens){
            setResultToToast("This is an extremely small area and may not provide optimal results");
            return 1;
        }

        else if (m%dimens != 0){
            numCells = (int)(m/dimens) + 1;
        }
        else{
            numCells = (int)(m/dimens);
        }

        return numCells;
    }

    private double setMove(double m, int cells){
        return m/cells;
    }

    private void enableDisableAdd() {
        if (isAdd == false) {
            isAdd = true;
            add.setText("Exit");
        } else {
            isAdd = false;
            add.setText("Add");
        }
    }

    private void showSettingDialog() {
        LinearLayout wayPointSettings = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_waypointsetting, null);

        final TextView wpAltitude_TV = (TextView) wayPointSettings.findViewById(R.id.altitude);
        RadioGroup speed_RG = (RadioGroup) wayPointSettings.findViewById(R.id.speed);
        RadioGroup actionAfterFinished_RG = (RadioGroup) wayPointSettings.findViewById(R.id.actionAfterFinished);
        RadioGroup heading_RG = (RadioGroup) wayPointSettings.findViewById(R.id.heading);

        speed_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.lowSpeed) {
                    mSpeed = 3.0f;
                } else if (checkedId == R.id.MidSpeed) {
                    mSpeed = 5.0f;
                } else if (checkedId == R.id.HighSpeed) {
                    mSpeed = 10.0f;
                }
            }

        });

        actionAfterFinished_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select finish action");
                if (checkedId == R.id.finishNone) {
                    mFinishedAction = DJIWaypointMission.DJIWaypointMissionFinishedAction.NoAction;
                } else if (checkedId == R.id.finishGoHome) {
                    mFinishedAction = DJIWaypointMission.DJIWaypointMissionFinishedAction.GoHome;
                } else if (checkedId == R.id.finishAutoLanding) {
                    mFinishedAction = DJIWaypointMission.DJIWaypointMissionFinishedAction.AutoLand;
                } else if (checkedId == R.id.finishToFirst) {
                    mFinishedAction = DJIWaypointMission.DJIWaypointMissionFinishedAction.GoFirstWaypoint;
                }
            }
        });

        heading_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select heading");

                if (checkedId == R.id.headingNext) {
                    mHeadingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.Auto;
                } else if (checkedId == R.id.headingInitDirec) {
                    mHeadingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.UsingInitialDirection;
                } else if (checkedId == R.id.headingRC) {
                    mHeadingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.ControlByRemoteController;
                } else if (checkedId == R.id.headingWP) {
                    mHeadingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.UsingWaypointHeading;
                }
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("")
                .setView(wayPointSettings)
                .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String altitudeString = wpAltitude_TV.getText().toString();
                        altitude = Integer.parseInt(nulltoIntegerDefalt(altitudeString));
                        Log.e(TAG, "altitude " + altitude);
                        Log.e(TAG, "speed " + mSpeed);
                        Log.e(TAG, "mFinishedAction " + mFinishedAction);
                        Log.e(TAG, "mHeadingMode " + mHeadingMode);
                        configWayPointMission();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }

                })
                .create()
                .show();
    }

    String nulltoIntegerDefalt(String value) {
        if (!isIntValue(value)) value = "0";
        return value;
    }

    boolean isIntValue(String val) {
        try {
            val = val.replace(" ", "");
            Integer.parseInt(val);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void configWayPointMission() {

        if (mWaypointMission != null) {
            mWaypointMission.finishedAction = mFinishedAction;
            mWaypointMission.headingMode = mHeadingMode;
            mWaypointMission.autoFlightSpeed = mSpeed;

            if (mWaypointMission.waypointsList.size() > 0) {
                for (int i = 0; i < mWaypointMission.waypointsList.size(); i++) {
                    mWaypointMission.getWaypointAtIndex(i).altitude = altitude;
                }

                setResultToToast("Set Waypoint attitude successfully");

            }
        }
    }

    private void prepareWayPointMission() {

        if (mMissionManager != null && mWaypointMission != null) {

            DJIMission.DJIMissionProgressHandler progressHandler = new DJIMission.DJIMissionProgressHandler() {
                @Override
                public void onProgress(DJIMission.DJIProgressType type, float progress) {
                }
            };

            mMissionManager.prepareMission(mWaypointMission, progressHandler, new DJIBaseComponent.DJICompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    setResultToToast(error == null ? "Mission Prepare Successfully" : error.getDescription());
                }
            });
        }

    }

    private void startWaypointMission() {

        if (mMissionManager != null) {

            mMissionManager.startMissionExecution(new DJIBaseComponent.DJICompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    setResultToToast("Mission Start: " + (error == null ? "Successfully" : error.getDescription()));
                }
            });

        }
    }

    private void stopWaypointMission() {

        if (mMissionManager != null) {
            mMissionManager.stopMissionExecution(new DJIBaseComponent.DJICompletionCallback() {

                @Override
                public void onResult(DJIError error) {
                    setResultToToast("Mission Stop: " + (error == null ? "Successfully" : error.getDescription()));
                }
            });

            if (mWaypointMission != null) {
                mWaypointMission.removeAllWaypoints();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (gMap == null) {
            gMap = googleMap;
            //this gets the permissions needed to view our location on the map
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            gMap.setMyLocationEnabled(true);
            setUpMap();

        }

        //fix so that it goes to the user
        LatLng thousandOaks = new LatLng(34.223344, -118.880932);
        //gMap.addMarker(new MarkerOptions().position(thousandOaks).title("Marker in Thousand Oaks"));
        gMap.moveCamera(CameraUpdateFactory.newLatLng(thousandOaks));
    }

}