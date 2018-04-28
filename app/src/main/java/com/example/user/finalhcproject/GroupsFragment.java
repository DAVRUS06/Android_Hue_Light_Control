package com.example.user.finalhcproject;


import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeState;
import com.philips.lighting.hue.sdk.wrapper.domain.ClipAttribute;
import com.philips.lighting.hue.sdk.wrapper.domain.DomainType;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.Alert;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightConfiguration;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.Group;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.GroupType;
import com.philips.lighting.hue.sdk.wrapper.utilities.HueColor;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupsFragment extends Fragment {

    // Variables for the layout
    private FloatingActionButton refreshFAB;
    private FloatingActionButton newGroupFAB;
    private FloatingActionButton removeGroupFAB;
    private FloatingActionButton addLightToGroupFAB;
    private FloatingActionButton removeLightFromGroupFAB;
    private RecyclerView recyclerViewGroup;
    private RecyclerView.Adapter groupDiscoveryAdapter;
    private List<CheckBox> checkList;

    // Variable for contacting bridge
    private Bridge bridge;

    // Used for contacting main activity
    MainActivity act;

    // Variables for group operations
    List<Group> groupList;

    // Delay the first start
    boolean firstStart = true;

    // Used for the groupLight Dialog
    private RecyclerView recyclerViewGroupLights;
    private RecyclerView.Adapter groupLightDiscoveryAdapter;
    private List<String> lightIds;
    private List<LightPoint> groupLights;

    // Variables for the alert Dialog
    private EditText editTextLightName;
    private Button buttonIdentify;
    private SeekBar seekBarRed;
    private SeekBar seekBarGreen;
    private SeekBar seekBarBlue;
    private SeekBar seekBarSaturation;
    private SeekBar seekBarBrightness;
    private ToggleButton toggleButtonColorLoop;
    private ToggleButton toggleButtonLightOnOff;
    private ImageView imageViewCurrentColor;

    // Varibles for colors
    private int Red = 255;
    private int Green = 255;
    private int Blue = 255;

    private Group groupCache;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public GroupsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupsFragment newInstance(String param1, String param2) {
        GroupsFragment fragment = new GroupsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        // Get the views of the layout
        refreshFAB = view.findViewById(R.id.floatButtonGroupsRefresh);
        newGroupFAB = view.findViewById(R.id.floatButtonGroupsAdd);
        removeGroupFAB = view.findViewById(R.id.floatButtonGroupsRemove);
        recyclerViewGroup = view.findViewById(R.id.recyclerViewGroups);
        act = (MainActivity)getActivity();

        // Set up the group recycler view
        recyclerViewGroup.setHasFixedSize(true);
        recyclerViewGroup.setLayoutManager(new LinearLayoutManager(getActivity()));

        removeGroupFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveGroupDialog();
            }
        });

        refreshFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Refresh the group list
                refreshList();
            }
        });

        newGroupFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bring up the dialog to add a new group
                showAddGroupDialog();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(firstStart) {
            delayStart(1000);
            firstStart = false;
        }
        else
            findGroups();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void findGroups() {
        // Get most up to date copy of bridge
        updateGroupBridge();

        // Get all groups from the bridge.
        if(bridge != null) {
            BridgeState bridgeState = bridge.getBridgeState();
            groupList = bridgeState.getGroups();

            if(groupList.size() == 1) {
                // Its just the master group, don't display anything.
            }
            else {
                // Remove the master group from the list
                groupList.remove(0);
                groupDiscoveryAdapter = new GroupsAdapter(groupList, getActivity());
                recyclerViewGroup.setAdapter(groupDiscoveryAdapter);
            }
        }
    }

    public void updateGroupBridge() {
        if(act != null) {
            bridge = act.getBridge();
        }
    }

    // Used to make sure the list contents is as up to date as possible
    private void refreshList() {
        groupList.clear();
        groupDiscoveryAdapter.notifyDataSetChanged();
        act.HeartBeatPulse();
        updateGroupBridge();
        findGroups();
    }

    private void delayStart(int delaytime) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findGroups();
            }
        }, delaytime);
    }

    private void delayRefresh(int delaytime) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshList();
            }
        }, delaytime);
    }

    public void showGroupLightsDialog(final Group group) {
        groupCache = group;
        // Bring up and alert dialog using the light list xml
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.NewDialogTheme);

        View groupLightsView = LayoutInflater.from(getActivity()).inflate(R.layout.groups_lights_dialog, null);

        addLightToGroupFAB = groupLightsView.findViewById(R.id.floatingActionButtonAddLight);
        removeLightFromGroupFAB = groupLightsView.findViewById(R.id.floatingActionButtonRemoveLight);

        addLightToGroupFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddLightDialog(group);
            }
        });

        removeLightFromGroupFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveLightDialog(group);
            }
        });

        // Set up the group recycler view
        recyclerViewGroupLights = groupLightsView.findViewById(R.id.recyclerViewGroupsLights);
        recyclerViewGroupLights.setHasFixedSize(true);
        recyclerViewGroupLights.setLayoutManager(new LinearLayoutManager(getActivity()));

        getLightsByGroup();

        // Pass this list to the groupLightAdapter
        groupLightDiscoveryAdapter = new GroupLightAdapter(groupLights, getActivity());
        recyclerViewGroupLights.setAdapter(groupLightDiscoveryAdapter);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                refreshLightList();
            }
        });
        builder.setView(groupLightsView);
        AppCompatDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_custom);
        dialog.show();
    }


    private void showRemoveLightDialog(final Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.NewDialogTheme);

        View removeLightsView = LayoutInflater.from(getActivity()).inflate(R.layout.add_remove_light_group, null);
        ViewGroup container = removeLightsView.findViewById(R.id.LightCheckBoxContainer);


        checkList = new ArrayList<>();
        for(int i = 1; i < groupLights.size(); i++) {
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setText(groupLights.get(i).getName());
            checkBox.setTextSize(18.0f);
            checkBox.setPadding(0,0,0,20);
            checkBox.setGravity(Gravity.FILL);
            checkList.add(checkBox);
            container.addView(checkBox);
        }


        builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                for(int i = 0; i < checkList.size(); i++) {
                    if(checkList.get(i).isChecked()) {
                        // Remove the light from the group
                        group.removeLight(groupLights.get(i + 1));
                    }
                }
                bridge.updateResource(group);
                refreshLightList();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        builder.setMessage("Select to remove:");
        builder.setView(removeLightsView);
        AppCompatDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_custom);
        dialog.show();
    }


    private void showAddLightDialog(final Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.NewDialogTheme);

        View addLightsView = LayoutInflater.from(getActivity()).inflate(R.layout.add_remove_light_group, null);
        ViewGroup container = addLightsView.findViewById(R.id.LightCheckBoxContainer);

        // Get the lights from group zero, which is all lights
        Group groupZero = bridge.getBridgeState().getGroup("0");
        List<String> groupZeroIDList;
        groupZeroIDList = groupZero.getLightIds();
        List<LightPoint> groupZeroLights = new ArrayList<>();
        // Get the lights from the IDs that were given by the group
        for(int i = 0; i < groupZeroIDList.size(); i++) {
            LightPoint light = (LightPoint)bridge.getBridgeState().getDevice(DomainType.LIGHT_POINT, groupZeroIDList.get(i));
            groupZeroLights.add(light);
        }

        // Get the lights from the current group
        List<String> currentGroupIDList = group.getLightIds();
        List<LightPoint> currentGroupLightList = new ArrayList<>();
        // Get the lights from the IDs that were given by the group
        for(int i = 0; i < currentGroupIDList.size(); i++) {
            LightPoint light = (LightPoint)bridge.getBridgeState().getDevice(DomainType.LIGHT_POINT, currentGroupIDList.get(i));
            currentGroupLightList.add(light);
        }

        checkList = new ArrayList<>();
        // Set the checkboxes
        for(int i = 0; i < groupZeroLights.size(); i++) {
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setText(groupZeroLights.get(i).getName());
            checkBox.setTextSize(18.0f);
            checkBox.setPadding(0,0,0,20);
            checkBox.setGravity(Gravity.FILL);
            for(int j = 0; j < currentGroupLightList.size(); j++) {
                if(groupZeroLights.get(i).getIdentifier().equals(currentGroupLightList.get(j).getIdentifier())) {
                    checkBox.setChecked(true);
                }
            }
            checkList.add(checkBox);
            container.addView(checkBox);
        }

        final List<LightPoint> finalGroupZero = groupZeroLights;
        final List<LightPoint> finalCurrentGroup = currentGroupLightList;
        builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                for(int i = 0; i < checkList.size(); i++) {
                    if(checkList.get(i).isChecked()) {
                        // Add the light from the group
                        boolean isAlreadyInGroup = false;
                        for(int j = 0; j < finalCurrentGroup.size(); j++) {
                            if(finalGroupZero.get(i).getIdentifier().equals(finalCurrentGroup.get(j).getIdentifier())) {
                                isAlreadyInGroup = true;
                            }
                        }
                        if(!isAlreadyInGroup)
                            group.addLight(finalGroupZero.get(i));
                    }
                }
                bridge.updateResource(group);
                refreshLightList();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        builder.setMessage("Select to add:");
        builder.setView(addLightsView);
        AppCompatDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_custom);
        dialog.show();
    }

    private void showAddGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.NewDialogTheme);

        View addLightsView = LayoutInflater.from(getActivity()).inflate(R.layout.add_group_dialog, null);
        ViewGroup container = addLightsView.findViewById(R.id.addGroupLightCheckBoxContainer);

        final EditText editTextName = addLightsView.findViewById(R.id.editTextAddGroupName);


        // Get the lights from group zero, which is all lights
        Group groupZero = bridge.getBridgeState().getGroup("0");
        List<String> groupZeroIDList;
        groupZeroIDList = groupZero.getLightIds();
        final List<LightPoint> groupZeroLights = new ArrayList<>();
        // Get the lights from the IDs that were given by the group
        for(int i = 0; i < groupZeroIDList.size(); i++) {
            LightPoint light = (LightPoint)bridge.getBridgeState().getDevice(DomainType.LIGHT_POINT, groupZeroIDList.get(i));
            groupZeroLights.add(light);
        }

        checkList = new ArrayList<>();
        // Set the checkboxes
        for(int i = 0; i < groupZeroLights.size(); i++) {
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setText(groupZeroLights.get(i).getName());
            checkBox.setTextSize(18.0f);
            checkBox.setPadding(0,0,0,20);
            checkBox.setGravity(Gravity.FILL);
            checkList.add(checkBox);
            container.addView(checkBox);
        }

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Add the group
                Group newGroup = new Group();
                if(editTextName.getText().toString() == "" || editTextName.getText().toString().equals("")) {
                    newGroup.setName("New Group");
                }
                else {
                    newGroup.setName(editTextName.getText().toString());
                }

                for(int i = 0; i < groupZeroLights.size(); i++) {
                    if(checkList.get(i).isChecked()) {
                        newGroup.addLight(groupZeroLights.get(i));
                    }
                }

                bridge.createResource(newGroup);
                delayRefresh(500);
                //findGroups();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        builder.setMessage("Add a group:");
        builder.setView(addLightsView);
        AppCompatDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_custom);
        dialog.show();
    }

    private void showRemoveGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.NewDialogTheme);

        View addLightsView = LayoutInflater.from(getActivity()).inflate(R.layout.add_remove_light_group, null);
        ViewGroup container = addLightsView.findViewById(R.id.LightCheckBoxContainer);

        checkList = new ArrayList<>();
        // Set the checkboxes
        for(int i = 0; i < groupList.size(); i++) {
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setText(groupList.get(i).getName());
            checkBox.setTextSize(18.0f);
            checkBox.setPadding(0,0,0,20);
            checkBox.setGravity(Gravity.FILL);
            checkList.add(checkBox);
            container.addView(checkBox);
        }

        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Remove the group(s)
                for(int i = 0; i < groupList.size(); i++) {
                    if(checkList.get(i).isChecked()) {
                        bridge.deleteResource(groupList.get(i));
                    }
                }
                delayRefresh(500);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        builder.setMessage("Remove a group(s):");
        builder.setView(addLightsView);
        AppCompatDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_custom);
        dialog.show();
    }


    private void getLightsByGroup() {
        lightIds = groupCache.getLightIds();

        // Get the IDs of the lights in the group
        lightIds = groupCache.getLightIds();

        // Initialize groupLights list
        groupLights = new ArrayList<>();

        // Get the lights from the IDs that were griven by the group
        for(int i = 0; i < lightIds.size(); i++) {
            LightPoint light = (LightPoint)bridge.getBridgeState().getDevice(DomainType.LIGHT_POINT, lightIds.get(i));
            if(i == 0)
                groupLights.add(light);
            groupLights.add(light);
        }
        // Pass this list to the groupLightAdapter
        groupLightDiscoveryAdapter = new GroupLightAdapter(groupLights, getActivity());
        recyclerViewGroupLights.setAdapter(groupLightDiscoveryAdapter);
    }



    public void updateLightState(final LightPoint light, final boolean allLights) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.NewDialogTheme);

        View lightOptionView = LayoutInflater.from(getActivity()).inflate(R.layout.light_options_alert_dialog, null);

        editTextLightName = lightOptionView.findViewById(R.id.editTextLightOptionsName);
        buttonIdentify = lightOptionView.findViewById(R.id.buttonLightOptionsIdentify);
        seekBarRed = lightOptionView.findViewById(R.id.seekBarLightOptionsRed);
        seekBarGreen = lightOptionView.findViewById(R.id.seekBarLightOptionsGreen);
        seekBarBlue = lightOptionView.findViewById(R.id.seekBarLightOptionsBlue);
        seekBarSaturation = lightOptionView.findViewById(R.id.seekBarLightOptionsSaturation);
        seekBarBrightness = lightOptionView.findViewById(R.id.seekBarLightOptionsBrightness);
        toggleButtonColorLoop = lightOptionView.findViewById(R.id.toggleButtonLightOptionsColorLoop);
        toggleButtonLightOnOff = lightOptionView.findViewById(R.id.toggleButtonLightOptionsOnOff);
        imageViewCurrentColor = lightOptionView.findViewById(R.id.imageViewLightOptionsColor);

        // Start pulling the current information from the selected light.
        final LightState currentState = light.getLightState();
        final HueColor currentColor = currentState.getColor();
        HueColor.RGB currentRGB = currentColor.getRGB();
        HueColor.XY currentXY = currentColor.getXY();
        hueColorConversion converter = new hueColorConversion();
        currentRGB = converter.convertXYtoRGB(currentXY.x,currentXY.y, currentRGB, currentColor.getBrightness());
        Red = currentRGB.r;
        Green = currentRGB.g;
        Blue = currentRGB.b;
        imageViewCurrentColor.setBackgroundColor(Color.argb(255, currentRGB.r, currentRGB.g, currentRGB.b));

        // Get the name of the light
        editTextLightName.setText(light.getName());

        // Get the color information from the light
        seekBarRed.setProgress(currentRGB.r);
        seekBarGreen.setProgress(currentRGB.g);
        seekBarBlue.setProgress(currentRGB.b);
        seekBarSaturation.setProgress(currentState.getSaturation());
        seekBarBrightness.setProgress(currentState.getBrightness());

        // Setup the Seekbar listener
        seekBarRed.setOnSeekBarChangeListener(SeekListener);
        seekBarGreen.setOnSeekBarChangeListener(SeekListener);
        seekBarBlue.setOnSeekBarChangeListener(SeekListener);
        seekBarSaturation.setOnSeekBarChangeListener(SeekListener);
        seekBarBrightness.setOnSeekBarChangeListener(SeekListener);

        // Listener for the identify button
        buttonIdentify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LightState identifyState = new LightState();
                identifyState.setHue(currentState.getHue());
                identifyState.setBrightness(currentState.getBrightness());
                identifyState.setAlert(Alert.SELECT);
                light.updateState(identifyState);
            }
        });

        // Get the effect of the light
        com.philips.lighting.hue.sdk.wrapper.domain.clip.Effect lightEffect = currentState.getEffect();
        if(lightEffect.toString() == "NONE")
            toggleButtonColorLoop.setChecked(false);
        else
            toggleButtonColorLoop.setChecked(true);

        // Get the on/off state of the light
        toggleButtonLightOnOff.setChecked(currentState.isOn());
        toggleButtonLightOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LightState newState = new LightState();
                newState.setHue(currentState.getHue());
                newState.setBrightness(currentState.getBrightness());
                if(toggleButtonLightOnOff.isChecked())
                    newState.setOn(true);
                else
                    newState.setOn(false);
                if(allLights) {
                    /*for(int i = 1; i < lightList.size(); i++) {
                        LightState listLightState = lightList.get(i).getLightState();
                        LightState newListItemState = new LightState();
                        newListItemState.setBrightness(listLightState.getBrightness());
                        newListItemState.setHue(listLightState.getHue());
                        if(toggleButtonLightOnOff.isChecked())
                            newListItemState.setOn(true);
                        else
                            newListItemState.setOn(false);
                        lightList.get(i).updateState(newListItemState);
                    }*/

                    com.philips.lighting.hue.sdk.wrapper.domain.resource.Group group = bridge.getBridgeState().getGroup("0");
                    group.apply(newState);

                }
                else {
                    light.updateState(newState);
                }
            }
        });

        builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Push the changes to the light.
                LightConfiguration newUserconfig = light.getLightConfiguration();
                LightState newUserState = new LightState();
                HueColor newUserColor;

                if(allLights) {
                    // Change all lights

                    // Create a new hue color based on the RGB values
                    newUserColor = new HueColor(
                            new HueColor.RGB(Red, Green, Blue),
                            newUserconfig.getModelIdentifier(),
                            newUserconfig.getSwVersion()
                    );
                    newUserState.setXY(newUserColor.getXY().x, newUserColor.getXY().y);

                    // Get and set the brightness and saturation
                    newUserState.setBrightness(seekBarBrightness.getProgress());
                    newUserState.setSaturation(seekBarSaturation.getProgress());

                    // See if the user wants colorLoop activated
                    if (toggleButtonColorLoop.isChecked())
                        newUserState.setEffect(com.philips.lighting.hue.sdk.wrapper.domain.clip.Effect.COLORLOOP);
                    else
                        newUserState.setEffect(com.philips.lighting.hue.sdk.wrapper.domain.clip.Effect.NONE);

                    newUserState.setOn(toggleButtonLightOnOff.isChecked());

                    // Finally update the state and configuration to the light.
                    //for(int i = 0; i < lightList.size(); i++) {
                    //    lightList.get(i).updateState(newUserState);
                    //}

                    com.philips.lighting.hue.sdk.wrapper.domain.resource.Group group = bridge.getBridgeState().getGroup("0");
                    group.apply(newUserState);

                }
                else {
                    // Check the input on the name and see if it needs to be changed.
                    if (editTextLightName.getText() == null || editTextLightName.getText().toString() == "" || editTextLightName.getText().toString() == light.getName()) {
                        // User deleted name and didn't input another, don't take it as input
                    } else {
                        newUserconfig.setName(editTextLightName.getText().toString());
                        light.updateConfiguration(newUserconfig);
                    }

                    // Create a new hue color based on the RGB values
                    newUserColor = new HueColor(
                            new HueColor.RGB(Red, Green, Blue),
                            newUserconfig.getModelIdentifier(),
                            newUserconfig.getSwVersion()
                    );
                    newUserState.setXY(newUserColor.getXY().x, newUserColor.getXY().y);

                    // Get and set the brightness and saturation
                    newUserState.setBrightness(seekBarBrightness.getProgress());
                    newUserState.setSaturation(seekBarSaturation.getProgress());

                    // See if the user wants colorLoop activated
                    if (toggleButtonColorLoop.isChecked())
                        newUserState.setEffect(com.philips.lighting.hue.sdk.wrapper.domain.clip.Effect.COLORLOOP);
                    else
                        newUserState.setEffect(com.philips.lighting.hue.sdk.wrapper.domain.clip.Effect.NONE);

                    newUserState.setOn(toggleButtonLightOnOff.isChecked());

                    // Finally update the state and configuration to the light.
                    light.updateState(newUserState);
                }
                refreshLightList();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Just update the list, no other changes needed.
                refreshLightList();
            }
        });
        builder.setView(lightOptionView);
        AppCompatDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void refreshLightList() {
        groupLights.clear();
        groupLightDiscoveryAdapter.notifyDataSetChanged();
        act.HeartBeatPulse();
        updateGroupBridge();
        getLightsByGroup();
        groupLightDiscoveryAdapter.notifyDataSetChanged();
    }

    private final SeekBar.OnSeekBarChangeListener SeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            imageViewCurrentColor.setBackgroundColor(Color.argb(255, Red, Green, Blue));

            switch (seekBar.getId()) {
                case R.id.seekBarLightOptionsRed:
                    Red = progress;
                    act.showToast("Red: " + Integer.toString(progress));
                    imageViewCurrentColor.setBackgroundColor(Color.argb(255, Red, Green, Blue));
                    break;
                case R.id.seekBarLightOptionsGreen:
                    Green = progress;
                    act.showToast("Green: " + Integer.toString(progress));
                    imageViewCurrentColor.setBackgroundColor(Color.argb(255, Red, Green, Blue));
                    break;
                case R.id.seekBarLightOptionsBlue:
                    Blue = progress;
                    act.showToast("Blue: " + Integer.toString(progress));
                    imageViewCurrentColor.setBackgroundColor(Color.argb(255, Red, Green, Blue));
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}
