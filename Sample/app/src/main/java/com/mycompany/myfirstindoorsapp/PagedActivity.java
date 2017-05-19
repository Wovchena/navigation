package com.mycompany.myfirstindoorsapp;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.customlbs.library.IndoorsException;
import com.customlbs.library.IndoorsFactory;
import com.customlbs.library.IndoorsLocationListener;
import com.customlbs.library.LocalizationParameters;
import com.customlbs.library.callbacks.IndoorsServiceCallback;
import com.customlbs.library.callbacks.LoadingBuildingStatus;
import com.customlbs.library.model.Building;
import com.customlbs.library.model.Zone;
import com.customlbs.shared.Coordinate;
import com.customlbs.surface.library.IndoorsSurface;
import com.customlbs.surface.library.IndoorsSurfaceFactory;
import com.customlbs.surface.library.IndoorsSurfaceFragment;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.List;


public class PagedActivity extends AppCompatActivity implements IndoorsLocationListener {

    public static final int REQUEST_CODE_PERMISSIONS = 34168; //Random request code, use your own
    public static final int REQUEST_CODE_LOCATION = 58774; //Random request code, use your own
    public final int POINTMODE = 0; // modes of error calculating
    public final int LINEMODE = 1;
    public SurfaceOverlay[] createdOverlays;
    public int mesureMode;
    public Coordinate singlePoint = null;
    public Coordinate[] twoPoints = new Coordinate[2];
    PagedActivity pagedActivity = null;
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager NonSwipeableViewPager;
    private IndoorsSurfaceFragment indoorsSurfaceFragment = null;
    private Toast progressToast;
    private static int lastProgress = 0;
    private static String LICENSEKEY = "58d4a963-98a5-411b-8c30-a4659cc8d3d6";
    private boolean connected = false;
    IndoorsSurfaceFactory.Builder surfaceBuilder;
    private boolean firstCall = false; // определение готовности индор сервиса (был ли сделан
    // вызов updatePosition)
    Kalman kalman = null;
    Accel accel = null;
    Data data = null;
    int FLOORLVL = 0;
    float rotation = 0; //Real world orientation of this building in degrees. 0 is north.
    Building building = null;
    double accelNoise = 10.357431855947539;
    double measurementNoise = 5000;
    Coordinate currentPosition = null;
    RedSurfaceOverlay overlayKalman = null;

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }

    public SurfaceOverlay[] refreshOverlay(SurfaceOverlay[] so) {
        for (SurfaceOverlay i : so) {
            if (i != null) {
                boolean tmp = indoorsSurfaceFragment.removeOverlay(i);
            }
        }
        indoorsSurfaceFragment.updateSurface();
        return new SurfaceOverlay[2];
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pointMode:
                item.setChecked(true);
                mesureMode = POINTMODE;
                createdOverlays = refreshOverlay(createdOverlays);
                twoPoints[0] = null;
                twoPoints[1] = null;
                singlePoint = null;
                return true;
            case R.id.lineMode:
                item.setChecked(true);
                mesureMode = LINEMODE;
                createdOverlays = refreshOverlay(createdOverlays);
                twoPoints[0] = null;
                twoPoints[1] = null;
                singlePoint = null;
                return true;
            case R.id.menu_startMesurement:
                // begin mesurement
                if ((currentPosition == null) || ((singlePoint == null) && (twoPoints[0] == null))) {
                    Toast.makeText(this, "Set pts", Toast.LENGTH_SHORT).show();
                } else {

                    double[] coord = new double[4];
                    coord[0] = currentPosition.x;
                    coord[1] = currentPosition.y;
                    coord[2] = 0;
                    coord[3] = 0;
                    RealVector x = new ArrayRealVector(coord);
                    kalman = new Kalman(accelNoise, measurementNoise, x);
                    accel.start(mesureMode);
                    if ((mesureMode == POINTMODE) && (singlePoint != null)) {
                        data = new Data(singlePoint);
                        Toast.makeText(this, "new Data (singlePoint)", Toast.LENGTH_SHORT).show();
                    }
                    if ((mesureMode == LINEMODE) && (twoPoints[0] != null) && (twoPoints[1] != null)) {
                        data = new Data(twoPoints[0], twoPoints[1], SystemClock.uptimeMillis());
                        Toast.makeText(this, "new Data (twoPoints)", Toast.LENGTH_SHORT).show();
                    }
                    if ((twoPoints[0] != null) && (twoPoints[1] != null) && (singlePoint != null)) {
                        Toast.makeText(this, "singlePoint!=!=!null!=!=!twoPoints[0][1]", Toast
                                .LENGTH_SHORT).show();
                    }
                    if ((twoPoints[0] == null) && (twoPoints[1] == null) && (singlePoint == null)) {
                        Toast.makeText(this, "singlePoint==null==twoPoints[0][1]", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
                return true;
            case R.id.menu_stopMesurement:
                if (null != data) {
                    double[] standartDeviations = data.calc(SystemClock.uptimeMillis());
                    accel.stop();
                    kalman = null;
                    fragment.addItem(pagedActivity, indoorsSurfaceFragment, "Deviation for indoors=" +
                            standartDeviations[0] + "; and for Kalman=" + standartDeviations[1], new Coordinate(0, 0, 0));


                    Toast.makeText(this, "Deviation for indoors=" + standartDeviations[0] + "; and for " +
                            "Kalman=" + standartDeviations[1], Toast.LENGTH_LONG).show();
                    data = null;
                    if (overlayKalman != null) {
                        indoorsSurfaceFragment.removeOverlay(overlayKalman);
                        indoorsSurfaceFragment.updateSurface();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void continueLoading() {
// everything is ok
    }

    private void requestPermissionsFromUser() {
        /**
         * Since API level 23 we need to request permissions for so called dangerous permissions from the user.
         *
         * You can see a full list of needed permissions in the Manifest File.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheckForLocation = ContextCompat.checkSelfPermission(
                    PagedActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permissionCheckForLocation != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        REQUEST_CODE_PERMISSIONS);
            } else {
                //If permissions were already granted,
                // we can go on to check if Location Services are enabled.
                checkLocationIsEnabled();
            }
        } else {
            //Continue loading Indoors if we don't need user-settable-permissions.
            // In this case we are pre-Marshmallow.
            continueLoading();
        }
    }

    /**
     * The Android system calls us back
     * after the user has granted permissions (or denied them)
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // Since we have requested multiple permissions,
            // we need to check if any were denied
            for (int grant : grantResults) {
                if (grant == PackageManager.PERMISSION_DENIED) {
                    if (shouldShowRequestPermissionRationale(
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        // User has *NOT* allowed us to use ACCESS_COARSE_LOCATION
                        // permission on first try. This is the last chance we get
                        // to ask the user, so we explain why we want this permission
                        Toast.makeText(this,
                                "Location is used for Bluetooth location",
                                Toast.LENGTH_SHORT).show();
                        // Re-ask for permission
                        requestPermissionsFromUser();
                        return;
                    }

                    // The user has finally denied us permissions.
                    Toast.makeText(this,
                            "Cannot continue without permissions.",
                            Toast.LENGTH_SHORT).show();
                    this.finishAffinity();
                    return;
                }
            }

            checkLocationIsEnabled();
        }
    }

    private void checkLocationIsEnabled() {
        // On android Marshmallow we also need to have active Location Services (GPS or Network based)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean isNetworkLocationProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean isGPSLocationProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isGPSLocationProviderEnabled && !isNetworkLocationProviderEnabled) {
                // Only if both providers are disabled we need to ask the user to do something
                Toast.makeText(this, "Location is off, enable it in system settings.", Toast.LENGTH_LONG).show();
                Intent locationInSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                this.startActivityForResult(locationInSettingsIntent, REQUEST_CODE_LOCATION);
            } else {
                continueLoading();
            }
        } else {
            continueLoading();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent msg) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            // Check if the user has really enabled Location services.
            checkLocationIsEnabled();
        }
    }

    final DemoObjectFragment fragment = new DemoObjectFragment();
    List<Fragment> fragmentList;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    public IndoorsSurfaceFragment createIndoorsFragment() {

        IndoorsFactory.Builder indoorsBuilder = initializeIndoorsLibrary();
        //indoorsBuilder.setEvaluationMode(true);

        {
            indoorsSurfaceFragment = initializeIndoorsSurface(indoorsBuilder);

            /**
             * This will add the IndoorsSurfaceFragment to the current layout
             */
            IndoorsFactory.createInstance(this, LICENSEKEY, new IndoorsServiceCallback() {
                @Override
                public void onError(IndoorsException arg0) {
                    Log.i("ERROR", indoorsSurfaceFragment.toString());
                }

                @Override
                public void connected() {
                    connected = true;

                }
            });
            indoorsSurfaceFragment.registerOnSurfaceClickListener(new IndoorsSurface.OnSurfaceClickListener() {

                @Override
                public void onClick(Coordinate coordinate) {

                }
            });

            indoorsSurfaceFragment.registerOnSurfaceLongClickListener(new IndoorsSurface.OnSurfaceLongClickListener() {
                public void onLongClick(final Coordinate mapPoint) {
                    if (POINTMODE == mesureMode) {
                        final SurfaceOverlay overlay = new SurfaceOverlay(mapPoint);
                        indoorsSurfaceFragment.addOverlay(overlay);
                        indoorsSurfaceFragment.updateSurface();
                        View view = getLayoutInflater().inflate(R.layout.dialog_create_point, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(PagedActivity.this);
                        builder.setTitle("Set one point")
                                .setMessage(mapPoint.toString())
                                .setCancelable(true)
                                .setView(view)
                                .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                indoorsSurfaceFragment.removeOverlay(overlay);
                                                indoorsSurfaceFragment.updateSurface();
                                            }
                                        })
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int arg1) {
                                        createdOverlays[0] = overlay;
                                        singlePoint = mapPoint;
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else if (LINEMODE == mesureMode) {
                        Log.d("linemodezomb", "asfffhy");
                        if ((null != twoPoints[0]) && (null != twoPoints[1])) {
                            Toast.makeText(PagedActivity.this, "Refresh points by reselecting mode", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if ((null == twoPoints[0]) && (null == twoPoints[1])) {
                            final SurfaceOverlay overlay = new SurfaceOverlay(mapPoint);
                            indoorsSurfaceFragment.addOverlay(overlay);
                            indoorsSurfaceFragment.updateSurface();
                            View view = getLayoutInflater().inflate(R.layout.dialog_create_point, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(PagedActivity.this);
                            builder.setTitle("Set first point")
                                    .setMessage(mapPoint.toString())
                                    .setCancelable(true)
                                    .setView(view)
                                    .setNegativeButton("Cancel",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    indoorsSurfaceFragment.removeOverlay(overlay);
                                                    indoorsSurfaceFragment.updateSurface();
                                                }
                                            })
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int arg1) {
                                            createdOverlays[0] = overlay;
                                            twoPoints[0] = mapPoint;
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                            return;
                        }

                        if ((null != twoPoints[0]) && (null == twoPoints[1])) {
                            final SurfaceOverlay overlay = new SurfaceOverlay(mapPoint);
                            indoorsSurfaceFragment.addOverlay(overlay);
                            indoorsSurfaceFragment.updateSurface();
                            View view = getLayoutInflater().inflate(R.layout.dialog_create_point, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(PagedActivity.this);
                            builder.setTitle("Set second point")
                                    .setMessage(mapPoint.toString())
                                    .setCancelable(true)

                                    .setView(view)
                                    .setNegativeButton("Cancel",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    indoorsSurfaceFragment.removeOverlay(overlay);
                                                    indoorsSurfaceFragment.updateSurface();
                                                }
                                            })
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int arg1) {
                                            createdOverlays[1] = overlay;
                                            twoPoints[1] = mapPoint;
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                            return;
                        }
                        Toast.makeText(PagedActivity.this, "mesureMode=LINEMODE, but strange values of twoPoints[]", Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(PagedActivity.this, "All points are defined", Toast
                            .LENGTH_LONG).show();

                }
            });

        }
        return indoorsSurfaceFragment;
    }

    private IndoorsFactory.Builder initializeIndoorsLibrary() {
        /**
         * This will initialize the builder for the Indoo.rs object
         */


        IndoorsFactory.Builder indoorsBuilder = new IndoorsFactory.Builder();
        indoorsBuilder.setContext(this);

        LocalizationParameters parameters = new LocalizationParameters();
        parameters.setUseKalmanStrategy(false);
        parameters.setUseStabilizationFilter(false);
        indoorsBuilder.setLocalizationParameters(parameters);
        /**
         * This is your API key as set on https://api.indoo.rs
         */
        indoorsBuilder.setApiKey(LICENSEKEY);
        /**
         * This is the ID of the Building as shown in the desktop Measurement Tool (MMT)
         */
        indoorsBuilder.setBuildingId((long) 991841068);
        // callback for indoo.rs-events
        indoorsBuilder.setUserInteractionListener(this);

        return indoorsBuilder;
    }

    private IndoorsSurfaceFragment initializeIndoorsSurface(IndoorsFactory.Builder indoorsBuilder) {
        /**
         * This will initialize the UI from Indoo.rs which is called IndoorsSurface.
         * The implementation is the IndoorsSurfaceFragment
         *
         * If you use your own map view implementation you don't need the Surface.
         * https://indoors.readme.io/docs/localisation-without-ui
         *
         */
        surfaceBuilder = new IndoorsSurfaceFactory.Builder();
        surfaceBuilder.setIndoorsBuilder(indoorsBuilder);

        return surfaceBuilder.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pagedActivity = this;
        super.onCreate(savedInstanceState);
        createdOverlays = new SurfaceOverlay[2];


        fragmentList = new ArrayList<>();

        indoorsSurfaceFragment = createIndoorsFragment();
        fragmentList.add(indoorsSurfaceFragment);
        fragmentList.add(fragment);

        setContentView(R.layout.activity_paged);
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mDemoCollectionPagerAdapter =
                new DemoCollectionPagerAdapter(
                        getSupportFragmentManager(), this, fragmentList);
        NonSwipeableViewPager = (ViewPager) findViewById(R.id.pager);


        NonSwipeableViewPager.setAdapter(mDemoCollectionPagerAdapter);
        final ActionBar actionBar = getSupportActionBar();
        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {


            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                NonSwipeableViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        // Add 2 tabs, specifying the tab's text and TabListener
        actionBar.addTab(actionBar.newTab().setText("Map")
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Logs")
                .setTabListener(tabListener));


        NonSwipeableViewPager = (ViewPager) findViewById(R.id.pager);
        NonSwipeableViewPager.addOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getSupportActionBar().setSelectedNavigationItem(position);
                    }
                });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("key");
            if (value.equals("Disc")) {
                NonSwipeableViewPager.setCurrentItem(1);
            }
            Log.d("extras", value);
            //The key argument here must match that used in the other activity
        }

    }

    public void positionUpdated(Coordinate userPosition, int accuracy) {
        /**
         * Is called each time the Indoors Library calculated a new position for the user
         * If Lat/Lon/Rotation of your building are set correctly you can calculate a
         * GeoCoordinate for your users current location in the building.
         */

        // if first try, itialize list of zones
        if (connected == true && firstCall == false) // if first try
        {
            this.rotation = building.getRotation();
            accel = new Accel(this, (SensorManager) getSystemService(Context.SENSOR_SERVICE), rotation);
            firstCall = true;
        }

        currentPosition = userPosition;

        if (kalman != null) {
            long time = SystemClock.uptimeMillis();
            double[] coord = new double[2];
            coord[0] = (double) userPosition.x;
            coord[1] = (double) userPosition.y;
            kalman.correct(new ArrayRealVector(coord));
            double[] estimatedState;
            estimatedState = kalman.getStateEstimationVector().toArray();
            data.addToFilter(time, new Coordinate((int) estimatedState[0], (int) estimatedState[1], FLOORLVL));

            data.addToIndoors(time, userPosition);
        }
    }

    public void loadingBuilding(LoadingBuildingStatus loadingBuildingStatus) {
        // indoo.rs is still downloading or parsing the requested building
        // Inform the User of Progress
        showDownloadProgressToUser(loadingBuildingStatus.getProgress());
    }

    @Override
    public void buildingLoaded(Building building) {
        // Fake a 100% progress to your UI when you receive info that the download is finished.
        showDownloadProgressToUser(100);
        this.building = building;
    }

    private void showDownloadProgressToUser(int progress) {
        if (progress % 10 == 0) { // Avoid showing too many values.
            if (progress > lastProgress) {
                lastProgress = progress; // Avoid showing same value multiple times.

                if (progressToast != null) {
                    progressToast.cancel();
                }

                progressToast = Toast.makeText(this, "Building downloading : " + progress + "%", Toast.LENGTH_SHORT);
                progressToast.show();
            }
        }
    }


    @Override
    public void onError(IndoorsException indoorsException) {
        Toast.makeText(this, indoorsException.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void changedFloor(int floorLevel, String name) {
        // user changed the floor
    }

    @Override
    public void leftBuilding(Building building) {
        // Deprecated
    }

    @Override
    public void buildingReleased(Building building) {
        // Another building was loaded, you can release any resources related to linked building
    }

    @Override
    public void orientationUpdated(float orientation) {
        // user changed the direction he's heading to


    }

    @Override
    public void enteredZones(List<Zone> zones) {
    }

    @Override
    public void buildingLoadingCanceled() {
        // Loading of building was cancelled
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Paged Page")
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void onBackPressed() {
    }

    void onAccelChanged(double[] accelData)
    //store accelData
    {
        if (kalman != null) {
            long time = SystemClock.uptimeMillis();
            kalman.predict(new ArrayRealVector(accelData));
            double[] estimatedState;
            estimatedState = kalman.getStateEstimationVector().toArray();
            data.addToFilter(time, new Coordinate((int) estimatedState[0], (int) estimatedState[1], FLOORLVL));
            Log.d("velocityvelocity", "" + estimatedState[2] + " | " + estimatedState[3]);
            if (null != overlayKalman) {
                indoorsSurfaceFragment.removeOverlay(overlayKalman);
                overlayKalman = null;
            }
            overlayKalman = new RedSurfaceOverlay(new Coordinate((int)
                    estimatedState[0], (int) estimatedState[1], FLOORLVL));
            indoorsSurfaceFragment.addOverlay(overlayKalman);
            indoorsSurfaceFragment.updateSurface();
        }
    }
}