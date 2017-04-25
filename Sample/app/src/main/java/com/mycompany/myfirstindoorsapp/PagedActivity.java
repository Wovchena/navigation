package com.mycompany.myfirstindoorsapp;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.customlbs.library.IndoorsException;
import com.customlbs.library.IndoorsFactory;
import com.customlbs.library.IndoorsLocationListener;
import com.customlbs.library.LocalizationParameters;
import com.customlbs.library.callbacks.IndoorsServiceCallback;
import com.customlbs.library.callbacks.LoadingBuildingCallback;
import com.customlbs.library.callbacks.LoadingBuildingStatus;
import com.customlbs.library.callbacks.OnlineBuildingCallback;
import com.customlbs.library.callbacks.RoutingCallback;
import com.customlbs.library.callbacks.ZoneCallback;
import com.customlbs.library.model.Building;
import com.customlbs.library.model.Zone;
import com.customlbs.shared.Coordinate;
import com.customlbs.surface.library.DefaultSurfacePainterConfiguration;
import com.customlbs.surface.library.IndoorsSurface;
import com.customlbs.surface.library.IndoorsSurfaceFactory;
import com.customlbs.surface.library.IndoorsSurfaceFragment;
import com.customlbs.surface.library.SurfacePainterConfiguration;
import com.customlbs.surface.library.ViewMode;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;


public class PagedActivity extends AppCompatActivity implements IndoorsLocationListener {

    public static final int REQUEST_CODE_PERMISSIONS = 34168; //Random request code, use your own
    public static final int REQUEST_CODE_LOCATION = 58774; //Random request code, use your own
    public final int POINTMODE=0; // modes of error calculating
    public final int LINEMODE=1;
    public int numberOfSettedPoints;
    public SurfaceOverlay[] createdOverlays;
    public int mesureMode;
    public Coordinate singlePoint=null;
    public Coordinate[] twoPoints=new Coordinate[2];
    PagedActivity pagedActivity=null;
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager NonSwipeableViewPager;
    private IndoorsSurfaceFragment indoorsSurfaceFragment = null;
    private Toast progressToast;
    private static int lastProgress = 0;
    private static String LICENSEKEY = "80015283-2556-4e16-96e0-193c97ad660b";
    private boolean connected = false;
    IndoorsSurfaceFactory.Builder surfaceBuilder;
    private boolean firstCall = false; // определение готовности идор сервиса (был ли сделан вызов updatePosition)

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }

    public SurfaceOverlay[] refreshOverlay(SurfaceOverlay[] so)
    {
        Log.d("Overlaydragon", "asdfasdfasddf");
        for (SurfaceOverlay i : so)
        {
            if (i!=null) {
                Log.d("Overlaydragon", i.toString());
                boolean tmp=indoorsSurfaceFragment.removeOverlay(i);
                Log.d("Overlayfire", Boolean.toString(tmp));
            }
        }
        indoorsSurfaceFragment.updateSurface();
        return new SurfaceOverlay[2];
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.pointMode:
                item.setChecked(true);
                mesureMode=POINTMODE;
                createdOverlays=refreshOverlay(createdOverlays);
                twoPoints[0]=null;
                twoPoints[1]=null;
                singlePoint=null;
                return true;
            case R.id.lineMode:
                item.setChecked(true);
                mesureMode=LINEMODE;
                numberOfSettedPoints=0;
                createdOverlays=refreshOverlay(createdOverlays);
                twoPoints[0]=null;
                twoPoints[1]=null;
                return true;
            case R.id.menu_startMesurement:
                //TODO: begin mesurement
                return true;
            case R.id.menu_stopMesurement:
                //TODO stop mesurement
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
                        new String[] {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    public void ClickonDiscList(View view) {
        Intent intent = new Intent(this, DiscountsOfShop.class);
        startActivity(intent);

    }

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
                    List<Zone> zones =indoorsSurfaceFragment.getZones();
                    Log.d ("List of coords", zones.toString());
                    //indoorsSurfaceFragment.routeTo(coordinate, true);
                    /*IndoorsFactory.getInstance().getBuilding(Building building,
                            LoadingBuildingCallback listener)
                    IndoorsFactory.getInstance().getZones();*/



                }
            });

//            NonSwipeableViewPager.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    if (motionEvent.ACTION_DOWN)
//                    return false;
//                }
//            });

            indoorsSurfaceFragment.registerOnSurfaceLongClickListener(new IndoorsSurface.OnSurfaceLongClickListener() {
                public void onLongClick(final Coordinate mapPoint) {
                    if (POINTMODE==mesureMode)
                    {
                        final SurfaceOverlay overlay = new SurfaceOverlay(mapPoint);
                        indoorsSurfaceFragment.addOverlay(overlay);
                        indoorsSurfaceFragment.updateSurface();

                        View view = getLayoutInflater().inflate(R.layout.dialog_create_discount, null);
                        final EditText editText = (EditText) view.findViewById(R.id.editTextDiscount);
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
                                        createdOverlays[0]=overlay;
                                        singlePoint=mapPoint;
                                        fragment.addItem(pagedActivity, indoorsSurfaceFragment, editText.getText().toString(), mapPoint);
                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else if (LINEMODE==mesureMode)
                    {
                        Log.d("linemodezomb", "asfffhy");
                        if ((null!=twoPoints[0])&&(null!=twoPoints[1])) {
                            Toast.makeText(PagedActivity.this, "Refresh points by reselecting mode", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if ((null==twoPoints[0])&&(null==twoPoints[1]))
                        {
                            final SurfaceOverlay overlay = new SurfaceOverlay(mapPoint);
                            indoorsSurfaceFragment.addOverlay(overlay);
                            indoorsSurfaceFragment.updateSurface();

                            View view = getLayoutInflater().inflate(R.layout.dialog_create_discount, null);
                            final EditText editText = (EditText) view.findViewById(R.id.editTextDiscount);
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
                                            createdOverlays[0]=overlay;
                                            twoPoints[0]=mapPoint;
                                            fragment.addItem(pagedActivity, indoorsSurfaceFragment, editText.getText().toString(), mapPoint);
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();
                            return;
                        }

                        if ((null!=twoPoints[0])&&(null==twoPoints[1]))
                        {
                            final SurfaceOverlay overlay = new SurfaceOverlay(mapPoint);
                            indoorsSurfaceFragment.addOverlay(overlay);
                            indoorsSurfaceFragment.updateSurface();

                            View view = getLayoutInflater().inflate(R.layout.dialog_create_discount, null);
                            final EditText editText = (EditText) view.findViewById(R.id.editTextDiscount);
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
                                            createdOverlays[1]=overlay;
                                            twoPoints[1]=mapPoint;
                                            fragment.addItem(pagedActivity, indoorsSurfaceFragment, editText.getText().toString(), mapPoint);
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();
                            return;
                        }
                        Toast.makeText(PagedActivity.this, "mesureMode=LINEMODE, but strange values of twoPoints[]", Toast.LENGTH_LONG).show();


                    }
                    Toast.makeText(PagedActivity.this, "All points are efined", Toast.LENGTH_LONG).show();




                }
            });
            // http://stackoverflow.com/questions/33264031/calling-dialogfragments-show-from-within-onrequestpermissionsresult-causes/34204394#34204394
            // http://stackoverflow.com/questions/17184653/commitallowingstateloss-in-fragment-activities

//            new Handler().post(new Runnable() {
//                @Override
//                public void run() {
//
//                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                    transaction.add(android.R.id.content, indoorsSurfaceFragment);
//
//                    transaction.commit();
//
//
//                }
//            });
        }
        //indoorsSurfaceFragment.setViewMode(ViewMode.HIGHLIGHT_CURRENT_ZONE);// does not work
        //indoorsSurfaceFragment.updateSurface();
        indoorsSurfaceFragment.setViewMode(ViewMode.HIGHLIGHT_ALL_ZONES); // this works


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
         * TODO: replace this with your API-key
         * This is your API key as set on https://api.indoo.rs
         */
        indoorsBuilder.setApiKey(LICENSEKEY);
        /**
         * TODO: replace 12345 with the id of the building you uploaded to our cloud using the MMT
         * This is the ID of the Building as shown in the desktop Measurement Tool (MMT)
         */
        indoorsBuilder.setBuildingId((long) 967880259);
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

        /*SurfacePainterConfiguration.PaintConfiguration paintConfiguration = new SurfacePainterConfiguration.PaintConfiguration();
        paintConfiguration.setTextSize(new Float (0.1));
        SurfacePainterConfiguration config = DefaultSurfacePainterConfiguration.getConfiguration();
        config.setZoneNamePaintConfiguration(paintConfiguration);
        paintConfiguration.setColor(Color.GREEN);
        String hexColor = String.format("#%06X", (0xFFFFFF & paintConfiguration.getColor()));
        Log.d ("colorcolor", hexColor);
        config.setZonePaintInnerPaintConfiguration (paintConfiguration);
        surfaceBuilder.setSurfacePainterConfiguration(config);*/
        SurfacePainterConfiguration configuration = DefaultSurfacePainterConfiguration
                .getConfiguration();

        /*configuration.getUserPositionCircleInlinePaintConfiguration().setColor(Color.RED);
        configuration.getLargeCircleOutlinePaintConfiguration().setColor(Color.RED);
        configuration.getRoutingPathPaintConfiguration().setColor(Color.RED);*/
        SurfacePainterConfiguration.PaintConfiguration c = configuration.getZoneNamePaintConfiguration();

        c.setColor(Color.BLUE);
        c.setDimensionPixelSize(350);

        configuration.getZonePaintInnerPaintConfiguration().setColor(Color.GREEN);
        surfaceBuilder.setSurfacePainterConfiguration(configuration);

        return surfaceBuilder.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pagedActivity=this;
        super.onCreate(savedInstanceState);
        createdOverlays=new SurfaceOverlay[2];



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

        // Add 3 tabs, specifying the tab's text and TabListener
        actionBar.addTab(actionBar.newTab().setText("Map")
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Discounts")
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
/*        indoorsSurfaceFragment.getView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d("Touched", motionEvent.toString());
                return false;
            }
        });*/

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("key");
            if (value.equals("Disc")) {
                NonSwipeableViewPager.setCurrentItem(1);
                Toast.makeText(
                        this,
                        "Shop list will appear soon", Toast.LENGTH_SHORT).show();
            }
            Log.d("extras", value);
            //The key argument here must match that used in the other activity
        }

    }

    public void positionUpdated(Coordinate userPosition, int accuracy) {
        Log.i ("positionUpdated", "start of function");
        /**
         * Is called each time the Indoors Library calculated a new position for the user
         * If Lat/Lon/Rotation of your building are set correctly you can calculate a
         * GeoCoordinate for your users current location in the building.
         */

        // if first try, itialize list of zones
        if (connected==true && firstCall == false) // if first try, itialize list of zones
        {
            firstCall = true;

            List<Zone> zoneList = indoorsSurfaceFragment.getZones();
            for (Zone z : zoneList)
            {
                int x=0, y=0;
                for (Coordinate c : z.getZonePoints()) {
                    x = x + c.x;
                    y+=c.y;
                }
                Coordinate center = new Coordinate (x/z.getZonePoints().size(), y/z.getZonePoints().size(), z.getZonePoints().get(0).z);
                fragment.addItem(pagedActivity, indoorsSurfaceFragment, z.getName(), center);
            }




        }
       /* Coordinate c = indoorsSurfaceFragment.getCurrentUserPosition();
        if (c != null) {
            Toast.makeText(
                    this,
                    "User is located at " + c.toString(), Toast.LENGTH_SHORT).show();
        }*/
        /*GeoCoordinate geoCoordinate = indoorsSurfaceFragment.getCurrentUserGpsPosition(); // для маленьких перемещений изменения незаметны

		if (geoCoordinate != null) {
			Toast.makeText(
			    this,
			    "User is located at " + geoCoordinate.getLatitude() + ","
			    + geoCoordinate.getLongitude(), Toast.LENGTH_SHORT).show();
		}*/
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
        // indoo.rs SDK successfully loaded the building you requested and
        // calculates a position now
        /*Toast.makeText(
                this,
                "Building is located at " + building.getLatOrigin() / 1E6 + ","
                        + building.getLonOrigin() / 1E6, Toast.LENGTH_SHORT).show();*/

		/*if (connected==true) { // путь от точки до точки из гайда indoo.rs
			try {
				sleep(3000); //  да, я знаю, но приложуха не успевает загрузить каике-то waypoints, и вообще я просто так попробовал, путь же должен простраиваться по запросу пользователя, а это будет значительно позже вызываться из другого метода
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Coordinate start = new Coordinate(200, 200, 0);
			Coordinate end = new Coordinate(47000, 25000, 0);

			Indoors indoors = indoorsSurfaceFragment.getIndoors();
			indoors.getRouteAToB(start, end, new RoutingCallback() {
				@Override
				public void onError(IndoorsException arg0) {
					Log.i("ERROR", "Error in getRouteAToB()" + arg0.toString());
				}

				@Override
				public void setRoute(ArrayList<Coordinate> route) {
					indoorsSurfaceFragment.getSurfaceState().setRoutingPath(route);
					// this is how to enable route snapping starting with version 3.8
					IndoorsFactory.getInstance().enableRouteSnapping(route);
					indoorsSurfaceFragment.updateSurface();
				}
			});
		}*/


    }

    private void showDownloadProgressToUser(int progress) {
        if (progress % 10 == 0) { // Avoid showing too many values.
            if (progress > lastProgress) {
                lastProgress = progress; // Avoid showing same value multiple times.

                if (progressToast != null) {
                    progressToast.cancel();
                }

                progressToast = Toast.makeText(this, "Building downloading : "+progress+"%", Toast.LENGTH_SHORT);
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
        Log.d ("enteredZones", "start of enteredZones(List<Zone> zones)");
        if (zones.size()>=1) {
            final Toast t=Toast.makeText(this, "You are in " + zones.get(0).getName(), Toast.LENGTH_SHORT);
            t.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    t.cancel();
                }
            }, 1000);
        }
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
                .setName("Paged Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
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
}