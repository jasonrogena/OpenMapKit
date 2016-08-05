package org.redcross.openmapkit;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.PositionAssertions;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.widget.Button;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by Jason Rogena - jrogena@ona.io on 7/27/16.
 */
@RunWith(AndroidJUnit4.class)
public class MapActivityTest {
    private static final String ROUNDED_BUTTON_LABEL = "Add Structure";
    private static final long GPS_DIALOG_TIMEOUT = 10100l;
    private Activity currentActivity;
    /*
    Launch the MapActivity with touch mode set to true (nothing is in focus and nothing is initially
    selected) and launchActivity set to false so that the activity is launched for each test
     */
    @Rule
    public ActivityTestRule<MapActivity> mapActivityTR = new ActivityTestRule<MapActivity>(MapActivity.class, true, false);

    @Before
    public void setup() {
        //get instrumented (not device) context so as to fetch files from androidTest/assets
        Context context = InstrumentationRegistry.getContext();
        copySettingsDir(context);
    }

    private void copySettingsDir(Context context) {
        //first remove the existing dir if already exists
        File dir = new File(ExternalStorage.getSettingsDir());
        if (dir.exists() && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }

        //add the fresh settings dir
        ExternalStorage.copyAssetsFileOrDirToExternalStorage(context, ExternalStorage.SETTINGS_DIR);
    }

    /**
     * This method tests the position of the 'locate-me' button in the activity
     */
    @Test
    public void locationButtonPosition() {
        startMapActivity(new OnPostLaunchActivity() {
            @Override
            public void run(Activity activity) {
                //check initial position
                try {
                    Thread.sleep(GPS_DIALOG_TIMEOUT);
                    Espresso.onView(ViewMatchers.withId(R.id.locationButton))
                            .check(PositionAssertions
                                    .isAbove(ViewMatchers.withId(R.id.nodeModeButton)));
                    Espresso.onView(ViewMatchers.withId(R.id.locationButton))
                            .check(PositionAssertions
                                    .isRightAlignedWith(ViewMatchers.withId(R.id.nodeModeButton)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Test
    public void roundedButtonLabel() {
        startMapActivity(new OnPostLaunchActivity() {
            @Override
            public void run(Activity activity) {
                MapActivity mapActivity = (MapActivity) activity;
                //show the add structure marker by clicking the '+' button
                try {
                    Thread.sleep(GPS_DIALOG_TIMEOUT);
                    Espresso.onView(ViewMatchers.withId(R.id.nodeModeButton)).perform(ViewActions.click());
                    Button addNodeButton = (Button)mapActivity.findViewById(R.id.addNodeBtn);
                    assertTrue(addNodeButton.getText().toString().contains(ROUNDED_BUTTON_LABEL));

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * This method tests whether the GPS countdown dialog is shown when the MapActivity is first
     * started and a GPS fix is not gotten
     */
    @Test
    public void testLoadingGpsDialogShown() {
        startMapActivity(new OnPostLaunchActivity() {
            @Override
            public void run(Activity activity) {
                MapActivity mapActivity = (MapActivity) activity;
                Location location = new Location("test_location");
                location.setLatitude(-0.3212321d);
                location.setLongitude(36.324324d);
                location.setAccuracy(30.0f);//accuracy in settings set to 10
                location.setTime(System.currentTimeMillis());
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                }

                mapActivity.changeTestProviderLocation(location);
                Espresso.onView(ViewMatchers.withText(R.string.getting_gps_fix))
                        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
            }
        });
    }

    /**
     * This method tests whether the GPS countdown dialog stops showing after a location with an
     * accuracy below the gps_proximity_accuracy is obtained
     */
    @Test
    public void testLoadingGpsDialogNotShownGpsAccurate() {
        startMapActivity(new OnPostLaunchActivity() {
            @Override
            public void run(Activity activity) {
                final MapActivity mapActivity = (MapActivity) activity;

                Location location = new Location(mapActivity.getPreferredLocationProvider());
                location.setLatitude(-0.3212321d);
                location.setLongitude(36.324324d);
                location.setAccuracy(9f);//accuracy in settings set to 10
                location.setTime(System.currentTimeMillis());
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                }
                mapActivity.changeTestProviderLocation(location);

                try {
                    Thread.sleep(MapActivity.TASK_INTERVAL_IN_MILLIS + 100);
                    Espresso.onView(ViewMatchers.withText(R.string.getting_gps_fix))
                            .check(ViewAssertions.doesNotExist());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * This method tests whether the GPS countdown dialog stops showing the timeout expires
     */
    @Test
    public void testLoadingGpsDialogNotShownAfterTimeout() {
        startMapActivity(new OnPostLaunchActivity() {
            @Override
            public void run(Activity activity) {
                final MapActivity mapActivity = (MapActivity) activity;

                Location location = new Location(mapActivity.getPreferredLocationProvider());
                location.setLatitude(-0.3212321d);
                location.setLongitude(36.324324d);
                location.setAccuracy(20f);//accuracy in settings set to 10
                location.setTime(System.currentTimeMillis());
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                }
                mapActivity.changeTestProviderLocation(location);

                try {
                    Thread.sleep(GPS_DIALOG_TIMEOUT);//wait for 10s timeout to expire
                    Espresso.onView(ViewMatchers.withText(R.string.getting_gps_fix))
                            .check(ViewAssertions.doesNotExist());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startMapActivity(OnPostLaunchActivity onPostLaunchActivity) {
        Intent intent = getLaunchOMKIntent();
        mapActivityTR.launchActivity(intent);
        Activity activity = getActivityInstance();
        if(activity instanceof MapActivity) {
            final MapActivity mapActivity = (MapActivity) activity;
            mapActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapActivity.clickMbtilesPositiveButton();
                    mapActivity.zoomToRecommendedLevel();
                }
            });
            onPostLaunchActivity.run(activity);
        } else {
            assertTrue("Current activity is not the MapActivity", false);
        }
    }

    private Activity getActivityInstance() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(Stage.RESUMED);
                if (resumedActivities.iterator().hasNext()) {
                    currentActivity = (Activity) resumedActivities.iterator().next();
                }
            }
        });
        return currentActivity;
    }

    private interface OnPostLaunchActivity {
        void run(Activity activity);
    }

    /**
     * This method creates an intent similar to the one used to launch OpenMapKit from OpenDataKit
     *
     * @return  Intent similar to the one used to launch OpenMapKit from OpenDataKit
     */
    private Intent getLaunchOMKIntent() {
        File odkInstanceDir = new File("/storage/emulated/0/odk/instances/omk_functional_test");
        odkInstanceDir.mkdirs();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra("FORM_FILE_NAME", "omk_functional_test");
        intent.putExtra("FORM_ID", "-1");
        intent.putExtra("INSTANCE_ID", "uuid:6004201f-9942-429d-bfa4-e65b683da37b");
        intent.putExtra("INSTANCE_DIR", "/storage/emulated/0/odk/instances/omk_functional_test");
        intent.putExtra("OSM_EDIT_FILE_NAME", "omk_functional_test.osm");
        ArrayList<String> tagKeys = new ArrayList<>();
        tagKeys.add("spray_status");
        intent.putExtra("TAG_KEYS", tagKeys);
        intent.putExtra("TAG_LABEL.spray_status", "Spray Status");
        intent.putExtra("TAG_VALUES.spray_status", "null");
        intent.putExtra("TAG_VALUE_LABEL.spray_status.undefined", "Undefined");
        intent.putExtra("TAG_VALUE_LABEL.spray_status.yes", "Yes");
        intent.putExtra("TAG_VALUE_LABEL.spray_status.no", "No");
        intent.putExtra(MapActivity.BUNDLE_KEY_IS_TESTING, true);

        return intent;
    }
}