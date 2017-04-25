package com.mycompany.myfirstindoorsapp;

import java.util.List;

import android.Manifest;
import android.annotation.TargetApi;
import android.support.v7.app.ActionBar; // trololo
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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


import com.customlbs.surface.library.ViewMode;



/**
 * Sample Android project, powered by indoo.rs :)
 *
 * For an API reference go to:
 *    https://my.indoo.rs/javadoc/
 *
 * For an implementers guide go to:
 *    https://indoors.readme.io/docs/getting-started-with-indoors-android-guide
 *
 * @author indoo.rs
 *
 */
public class MainActivity extends AppCompatActivity implements IndoorsLocationListener {
	static long time;
	public static final int REQUEST_CODE_PERMISSIONS = 34168; //Random request code, use your own
	public static final int REQUEST_CODE_LOCATION = 58774; //Random request code, use your own
	private IndoorsSurfaceFragment indoorsSurfaceFragment;
	private Toast progressToast;
	private static int lastProgress = 0;
	private static String LICENSEKEY="80015283-2556-4e16-96e0-193c97ad660b";
	private boolean connected=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		// We start by requesting permissions from the user
		requestPermissionsFromUser(); // this also does indoors initialisation
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

	}

	private void requestPermissionsFromUser() {
		/**
		 * Since API level 23 we need to request permissions for so called dangerous permissions from the user.
		 *
		 * You can see a full list of needed permissions in the Manifest File.
		 */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int permissionCheckForLocation = ContextCompat.checkSelfPermission(
			                                     MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

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

	// At this point we can continue to load the Indoo.rs SDK as we did with previous android versions
	private void continueLoading() {
		IndoorsFactory.Builder indoorsBuilder = initializeIndoorsLibrary();
		//indoorsBuilder.setEvaluationMode(true);
		indoorsSurfaceFragment = initializeIndoorsSurface(indoorsBuilder);

		setSurfaceFragment(indoorsSurfaceFragment);
	}

	private IndoorsFactory.Builder initializeIndoorsLibrary() {
		/**
		 * This will initialize the builder for the Indoo.rs object
		 */
		LocalizationParameters parameters = new LocalizationParameters();
		parameters.setUseKalmanStrategy(false);
		parameters.setUseStabilizationFilter(false);

		IndoorsFactory.Builder indoorsBuilder = new IndoorsFactory.Builder();

		indoorsBuilder.setLocalizationParameters(parameters);
		indoorsBuilder.setContext(this);
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
		IndoorsSurfaceFactory.Builder surfaceBuilder = new IndoorsSurfaceFactory.Builder();
		surfaceBuilder.setIndoorsBuilder(indoorsBuilder);
		return surfaceBuilder.build();
	}

	private void setSurfaceFragment(final IndoorsSurfaceFragment indoorsFragment) {
		/**
		 * This will add the IndoorsSurfaceFragment to the current layout
		 */
		IndoorsFactory.createInstance(this, LICENSEKEY, new IndoorsServiceCallback(){
			@Override
			public void onError(IndoorsException arg0) {

			}
			@Override
			public void connected() {
				connected=true;
			}
		});
		indoorsFragment.registerOnSurfaceClickListener(new IndoorsSurface.OnSurfaceClickListener()
		{

			@Override
			public void onClick(Coordinate coordinate) {
				indoorsFragment.routeTo(coordinate, true);


			}
		});
		// http://stackoverflow.com/questions/33264031/calling-dialogfragments-show-from-within-onrequestpermissionsresult-causes/34204394#34204394
		// http://stackoverflow.com/questions/17184653/commitallowingstateloss-in-fragment-activities

		new Handler().post(new Runnable() {
			@Override
			public void run() {


				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				transaction.add(android.R.id.content, indoorsFragment, "indoors");

				transaction.commit();


			}
		});

		indoorsFragment.setViewMode(ViewMode.HIGHLIGHT_CURRENT_ZONE);// does not work
		//indoorsFragment.updateSurface();
		//indoorsFragment.setViewMode(ViewMode.HIGHLIGHT_ALL_ZONES); // this works
	}

	public void positionUpdated(Coordinate userPosition, int accuracy) {
		/**
		 * Is called each time the Indoors Library calculated a new position for the user
		 * If Lat/Lon/Rotation of your building are set correctly you can calculate a
		 * GeoCoordinate for your users current location in the building.
		 */

		Coordinate c=indoorsSurfaceFragment.getCurrentUserPosition();
		if (c != null) {
			long timeBeforeChange=SystemClock.uptimeMillis()-time;
			time=SystemClock.uptimeMillis();

			Toast.makeText(
			    this,
			    "Time before change " + Float.toString((timeBeforeChange)/1000f), Toast.LENGTH_SHORT).show();
		}
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
		Toast.makeText(
		    this,
		    "Building is located at " + building.getLatOrigin() / 1E6 + ","
		    + building.getLonOrigin() / 1E6, Toast.LENGTH_SHORT).show();

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
		// user entered one or more zones
	}

	@Override
	public void buildingLoadingCanceled() {
		// Loading of building was cancelled
	}
}
