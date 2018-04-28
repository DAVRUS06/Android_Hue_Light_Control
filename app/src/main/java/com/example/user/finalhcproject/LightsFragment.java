package com.example.user.finalhcproject;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.effect.Effect;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeResponseCallback;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeState;
import com.philips.lighting.hue.sdk.wrapper.domain.ClipAttribute;
import com.philips.lighting.hue.sdk.wrapper.domain.DomainType;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.Alert;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.ClipResponse;
import com.philips.lighting.hue.sdk.wrapper.domain.device.Device;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightConfiguration;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;
import com.philips.lighting.hue.sdk.wrapper.utilities.HueColor;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LightsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LightsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LightsFragment extends Fragment {

    // Variables for layout
    private RecyclerView recyclerViewLight;
    private RecyclerView.Adapter lightDiscoveryAdapter;
    private List<LightPoint> lightList;
    private String lightName;
    private FloatingActionButton refreshLightsFAB;


    // Variables for the alert Dialog
    private EditText editTextLightName;
    private Button buttonIdentify;
    private SeekBar seekBarRed;
    private SeekBar seekBarGreen;
    private SeekBar seekBarBlue;
    private SeekBar seekBarSaturation;
    private  SeekBar seekBarBrightness;
    private ToggleButton toggleButtonColorLoop;
    private ToggleButton toggleButtonLightOnOff;
    private ImageView imageViewCurrentColor;

    // Varibles for colors
    private int Red = 255;
    private int Green = 255;
    private int Blue = 255;

    // Variable for contacting bridge
    private Bridge bridge;

    // Used for contacting main activity
    MainActivity act;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public LightsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LightsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LightsFragment newInstance(String param1, String param2) {
        LightsFragment fragment = new LightsFragment();
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
        View view = inflater.inflate(R.layout.fragment_lights, container, false);

        refreshLightsFAB = view.findViewById(R.id.floatButtonLightsRefresh);
        recyclerViewLight = view.findViewById(R.id.recyclerViewLights);

        // Set up the light recycler view
        recyclerViewLight.setHasFixedSize(true);
        recyclerViewLight.setLayoutManager(new LinearLayoutManager(getActivity()));
        lightList = new ArrayList<>();
        act = (MainActivity)getActivity();

        refreshLightsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshList();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        findLights();
        act = (MainActivity)getActivity();

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

    public void findLights() {
        // Make sure you have a updatedCopy of the bridge
        updateBridgeCopy();

        // Get all lights in the system.
        if(bridge != null) {
            BridgeState bridgeState = bridge.getBridgeState();
            lightList = bridgeState.getLights();

            // Master light is a placeholder for the master control of all lights.
            LightPoint masterLight = lightList.get(0);
            lightList.add(0, masterLight);

            lightDiscoveryAdapter = new LightAdapter(lightList, getActivity());
            recyclerViewLight.setAdapter(lightDiscoveryAdapter);
        }

    }

    private void updateBridgeCopy() {
        if(act != null) {
            bridge = act.getBridge();
        }
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
                    for(int i = 1; i < lightList.size(); i++) {
                        LightState listLightState = lightList.get(i).getLightState();
                        LightState newListItemState = new LightState();
                        newListItemState.setBrightness(listLightState.getBrightness());
                        newListItemState.setHue(listLightState.getHue());
                        if(toggleButtonLightOnOff.isChecked())
                            newListItemState.setOn(true);
                        else
                            newListItemState.setOn(false);
                        lightList.get(i).updateState(newListItemState);
                    }

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
                refreshList();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Just update the list, no other changes needed.
                refreshList();
            }
        });
        builder.setView(lightOptionView);
        AppCompatDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_custom);
        dialog.show();
    }

    public void setBridge(Bridge bridge) {
        this.bridge = bridge;
    }

    // Used to make sure the list contents is as up to date as possible
    private void refreshList() {
        lightList.clear();
        lightDiscoveryAdapter.notifyDataSetChanged();
        act.HeartBeatPulse();
        updateBridgeCopy();
        findLights();
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
