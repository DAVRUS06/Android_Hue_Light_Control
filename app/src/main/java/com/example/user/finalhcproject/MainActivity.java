package com.example.user.finalhcproject;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.wrapper.Persistence;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnection;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateCacheType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.HeartbeatManager;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeBuilder;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.Effect;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.Group;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridge;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridges;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements bridgesFragment.OnFragmentInteractionListener, GroupsFragment.OnFragmentInteractionListener, LightsFragment.OnFragmentInteractionListener {

    // Load the HUE SDK
    static {
        System.loadLibrary("huesdk");
    }

    // Phillips Hue Connection Variables
    private Bridge bridge;
    private BridgeConnectionCallback callback;
    private BridgeStateUpdatedCallback updatedCallback;
    private HeartbeatManager heartbeatManager;
    private BridgeConnection connected;
    private String bridgeIP;
    private boolean disconnectWasNeeded = false;

    // Variables for app operation
    private boolean storageSet = false;
    private String deviceID;
    private String filePath;
    private Context context;
    private Toast message;
    private int toastLength = Toast.LENGTH_SHORT;
    private boolean connectedOnStart = false;

    // Variables to deal with fragments
    private FragmentManager manager;
    private FragmentTransaction transaction;
    private boolean groupFragAdded = false;
    private boolean lightFragAdded = false;
    private boolean bridgeFragAdded = false;
    private bridgesFragment bridgeFrag;
    private GroupsFragment groupFrag;
    private LightsFragment lightFrag;
    private CurrentFragment currentFragment;

    // Tablayout
    private TabLayout mainTabLayout;

    // Error Toast
    private Toast errorToast;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this.getApplicationContext();
        mainTabLayout = findViewById(R.id.mainTabLayout);


        // Get the device ID and also the storage filepath
        deviceID = Settings.Secure.getString(MainActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);
        filePath = getFilesDir().getAbsolutePath();

        // If the storage has already been set then don't set it again.
        if(!storageSet) {
            Persistence.setStorageLocation(filePath, deviceID);
            storageSet = true;
        }

        bridgeIP = getLastBridgeIp();
        if(bridgeIP == null) {
            // No bridge was found, bring bridge fragment up first
            connectedOnStart = false;
            if(message != null) {
                message.cancel();
            }
            message = Toast.makeText(context, "No previous bridge found, choose a bridge to connect to.", toastLength);
            message.show();

        }
        else {
            // A bridge was found, connect to it.
            connectedOnStart = true;
            if(message != null) {
                message.cancel();
            }
            showToast("Connecting to last used bridge...");
            connectToBridge(bridgeIP);
        }


        groupFrag = new GroupsFragment();
        bridgeFrag = new bridgesFragment();
        lightFrag = new LightsFragment();




        if(connectedOnStart) {
            // Bring up the lights fragment first
            groupFragAdded = true;
            currentFragment = CurrentFragment.Groups;
            manager = getSupportFragmentManager();
            transaction = manager.beginTransaction();
            transaction.add(R.id.fragmentContainer, groupFrag);
            transaction.commit();
        }
        else {
            // Bring up the bridge fragment first
            TabLayout.Tab tempTab = mainTabLayout.getTabAt(2);
            tempTab.select();
            bridgeFragAdded = true;
            currentFragment = CurrentFragment.Bridges;
            manager = getSupportFragmentManager();
            transaction = manager.beginTransaction();
            transaction.add(R.id.fragmentContainer, bridgeFrag);
            transaction.commit();
        }

        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        // Groups fragment should be shown
                        if(currentFragment != CurrentFragment.Groups) {
                            currentFragment = CurrentFragment.Groups;
                            manager = getSupportFragmentManager();
                            transaction = manager.beginTransaction();
                            transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                            transaction.replace(R.id.fragmentContainer, groupFrag);
                            transaction.addToBackStack(currentFragment.toString());
                            transaction.commit();
                        }
                        break;
                    case 1:
                        // Lights fragment should be shown
                        if(currentFragment != CurrentFragment.Lights) {
                            currentFragment = CurrentFragment.Lights;
                            manager = getSupportFragmentManager();
                            transaction = manager.beginTransaction();
                            transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                            transaction.replace(R.id.fragmentContainer, lightFrag);
                            transaction.addToBackStack(currentFragment.toString());
                            transaction.commit();
                        }
                        break;
                    case 2:
                        // Bridges fragment should be shown
                        if(currentFragment != CurrentFragment.Bridges) {
                            currentFragment = CurrentFragment.Bridges;
                            manager = getSupportFragmentManager();
                            transaction = manager.beginTransaction();
                            transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                            transaction.replace(R.id.fragmentContainer, bridgeFrag);
                            transaction.addToBackStack(currentFragment.toString());
                            transaction.commit();
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    // This will get the last bridge that was connected if it exist.
    private String getLastBridgeIp() {
        List<KnownBridge> bridges = KnownBridges.getAll();

        if(bridges.isEmpty()) {
            return null;
        }

        return Collections.max(bridges, new Comparator<KnownBridge>() {
            @Override
            public int compare(KnownBridge o1, KnownBridge o2) {
                return o1.getLastConnected().compareTo((o2.getLastConnected()));
            }
        }).getIpAddress();
    }

    // This will connect to the bridge given the ip address.
    private void connectToBridge(String ip) {
        disconnectFromBridge();
        bridgeIP = ip;
        bridge = new BridgeBuilder("Hue Controller - Android", deviceID)
                .setIpAddress(ip)
                .setConnectionType(BridgeConnectionType.LOCAL)
                .setBridgeConnectionCallback(bridgeConnectionCallback)
                .addBridgeStateUpdatedCallback(bridgeStateUpdatedCallback)
                .build();

        bridge.connect();
        disconnectWasNeeded = false;
    }

    // This method will be called by the bridgesFragment class when the user chooses a bridge to connect to
    public void connectToChosenBridge(String ip) {
        // Keep the callback from trying to re-connect
        disconnectWasNeeded = true;
        connectToBridge(ip);
    }

    // This function will disconnect from the current bridge
    private void disconnectFromBridge() {
        if (bridge != null) {
            bridge.disconnect();
            bridge = null;
        }
    }

    // Handles the connection events
    private BridgeConnectionCallback bridgeConnectionCallback = new BridgeConnectionCallback() {
        @Override
        public void onConnectionEvent(final BridgeConnection bridgeConnection, com.philips.lighting.hue.sdk.wrapper.connection.ConnectionEvent connectionEvent) {
            final com.philips.lighting.hue.sdk.wrapper.connection.ConnectionEvent event = connectionEvent;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (event) {
                        case CONNECTED:
                            break;
                        case LINK_BUTTON_NOT_PRESSED:
                            showToast("Press the link button on the top of the bridge to authenticate.");
                            break;
                        case COULD_NOT_CONNECT:
                            showToast("Couldn't connect to bridge.");
                            break;
                        case CONNECTION_LOST:
                            showToast("Connection to bridge lost, attempting to reconnect.");
                            break;
                        case CONNECTION_RESTORED:
                            showToast("Connection to bridge restored.");
                            break;
                        case DISCONNECTED:
                            showToast("Disconnected from bridge.");
                            if(!disconnectWasNeeded)
                                connectToBridge(bridgeIP);
                            break;
                    }
                }
            });


        }

        @Override
        public void onConnectionError(BridgeConnection bridgeConnection, List<HueError> list) {

        }
    };

    // Handles the state updates from the bridge
    private BridgeStateUpdatedCallback bridgeStateUpdatedCallback = new BridgeStateUpdatedCallback() {
        @Override
        public void onBridgeStateUpdated(final Bridge bridge, com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedEvent bridgeStateUpdatedEvent) {
            final com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedEvent event = bridgeStateUpdatedEvent;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (event) {
                        case INITIALIZED:
                            showToast("Bridge Connected!");
                            setConnected();
                            break;
                        case LIGHTS_AND_GROUPS:
                            //showToast("Light or Group updated!");
                            lightFrag.findLights();
                            break;
                        case FULL_CONFIG:
                            // Give the lightFragment the updated bridge.
                            lightFrag.findLights();
                            break;
                        default:
                            break;
                    }
                }
            });

        }
    };

    // This will be called when a toast needs to be displayed
    public void showToast(final String m) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(message != null) {
                    message.cancel();
                }
                message = Toast.makeText(context, m, toastLength);
                message.setGravity(Gravity.CENTER|Gravity.BOTTOM, 0, 150);
                message.show();
            }
        });

    }

    public Bridge getBridge() {
        return bridge;
    }

    public void setConnected() {
        connected = bridge.getBridgeConnection(BridgeConnectionType.LOCAL);
        startHeartbeat();
    }
    private void startHeartbeat() {
        heartbeatManager = connected.getHeartbeatManager();
        heartbeatManager.startHeartbeat(BridgeStateCacheType.LIGHTS_AND_GROUPS, 10000);
    }

    // Called when refreshing the lights and groups fragments
    public void HeartBeatPulse() {
        heartbeatManager.performOneHeartbeat(BridgeStateCacheType.LIGHTS_AND_GROUPS);
    }

    // Light Adapter will call this method which will then call method in LightsFragment
    public void updateLights(LightPoint light, boolean allLights) {
        lightFrag.updateLightState(light, allLights);
    }

    public void updateGroupLights(LightPoint light, boolean allLights) {
        groupFrag.updateLightState(light, allLights);
    }

    // Call the groupFrag to bring up the dialog for the lights in the group
    public void groupFragShowLights(Group group) {
        groupFrag.showGroupLightsDialog(group);
    }

}
