package com.example.user.finalhcproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class BridgeAdapter extends RecyclerView.Adapter<BridgeAdapter.ViewHolder>{

    private List<ListItemBridge> bridgeItems;
    private Context context;
    private MainActivity act;

    public BridgeAdapter(List<ListItemBridge> bridgeItems, Context context) {
        this.bridgeItems = bridgeItems;
        this.context = context;
        this.act = (MainActivity)context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.bridge_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final ListItemBridge bridge = bridgeItems.get(position);
        holder.textViewBridgeName.setText(bridge.getBridgeName());

        switch (bridge.getBridgeModel()) {
            case "BSB001":
                holder.imageViewBridgemodel.setImageResource(R.drawable.ic_bridge_v1_filled);
                break;
            case "BSB002":
                holder.imageViewBridgemodel.setImageResource(R.drawable.ic_bridge_v2);
        }


        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast test = new Toast(context);
                if(test != null) {
                    test.cancel();
                }

                act.showToast("You clicked " + bridge.getBridgeName());
                //When a user clicks call the main activity to establish a connection.
                act.connectToChosenBridge(bridge.getBridgeIP());
            }
        });
    }

    @Override
    public int getItemCount() {
        return bridgeItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView textViewBridgeName;
        public GridLayout gridLayout;
        public CardView card;
        public ImageView imageViewBridgemodel;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewBridgeName = itemView.findViewById(R.id.textViewBridgeName);
            gridLayout = itemView.findViewById(R.id.gridLayoutBridgdItem);
            card = itemView.findViewById(R.id.cardViewItem);
            imageViewBridgemodel = itemView.findViewById(R.id.imageViewProductTypeBridges);
        }
    }

}