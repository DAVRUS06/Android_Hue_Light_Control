package com.example.user.finalhcproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class bridgesFragment extends Fragment {

    // Used for finding bridges on the network
    private static final String findBridgeURL = "https://www.meethue.com/api/nupnp";

    // Variables for layout
    private TextView textViewBridgeName;
    private RecyclerView recyclerViewBridge;
    private RecyclerView.Adapter bridgeDiscoveryAdapter;
    private List<ListItemBridge> bridgeList;
    private String bridgeName;
    private FloatingActionButton refreshFAB;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public bridgesFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static bridgesFragment newInstance(String param1, String param2) {
        bridgesFragment fragment = new bridgesFragment();
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
        View view = inflater.inflate(R.layout.fragment_bridges, container, false);

        // Get the views
        refreshFAB = view.findViewById(R.id.floatButtonBridgesRefresh);
        textViewBridgeName = view.findViewById(R.id.textViewBridgeName);
        recyclerViewBridge = view.findViewById(R.id.recyclerViewBridges);

        // Setup the bridge recycler view
        recyclerViewBridge.setHasFixedSize(true);
        recyclerViewBridge.setLayoutManager(new LinearLayoutManager(getActivity()));
        bridgeList = new ArrayList<>();

        refreshFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bridgeList.clear();
                bridgeDiscoveryAdapter.notifyDataSetChanged();
                findBridges();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        findBridges();

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

    // Method to find the bridges connected to the network
    private void findBridges() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Searching for bridges...");
        progressDialog.show();

        // Create new HTTP request
        StringRequest request = new StringRequest(Request.Method.GET,
                findBridgeURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            //JSONObject jsonResponse = new JSONObject(response);
                            //JSONArray jsonArray = jsonResponse.getJSONArray("");

                            JSONArray jsonArray = new JSONArray(response);

                            for(int i = 0; i < jsonArray.length(); i++)
                            {
                                JSONObject o = jsonArray.getJSONObject(i);

                                ListItemBridge newItem = new ListItemBridge(
                                        "Getting Name...",
                                        o.getString("id"),
                                        o.getString("internalipaddress"),
                                        "BSB002"
                                );
                                bridgeList.add(newItem);
                            }

                            for(int i = 0; i < jsonArray.length(); i++)
                            {
                                getBridgeName(i);
                            }

                            bridgeDiscoveryAdapter = new BridgeAdapter(bridgeList, getActivity());
                            recyclerViewBridge.setAdapter(bridgeDiscoveryAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(request);
    }

    private void getBridgeName(final int position) {

        ListItemBridge currentItem = bridgeList.get(position);
        final String ID = currentItem.getBridgeID();
        final String IP = currentItem.getBridgeIP();

        // This URL will return basic information about the bridge, mainly looking to grab the name.
        String bridgeURL = "http://" + IP + "/api/config";

        StringRequest request = new StringRequest(Request.Method.GET,
                bridgeURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            bridgeName = jsonResponse.getString("name");
                            String m = jsonResponse.getString("modelid");
                            ListItemBridge temp = new ListItemBridge(bridgeName, ID, IP, m);
                            bridgeList.set(position, temp);
                            bridgeDiscoveryAdapter = new BridgeAdapter(bridgeList, getActivity());
                            recyclerViewBridge.setAdapter(bridgeDiscoveryAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(request);
    }

}
