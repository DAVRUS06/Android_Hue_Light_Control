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
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.wrapper.domain.resource.Group;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder>{

    private List<Group> groupItems;
    private Context context;
    private MainActivity act;

    public GroupsAdapter(List<Group> groupItems, Context context) {
        this.groupItems = groupItems;
        this.context = context;
        this.act = (MainActivity)context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.group_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Group group = groupItems.get(position);
        holder.textViewGroupName.setText(group.getName());

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.showToast("You clicked " + group.getName());
                act.groupFragShowLights(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView textViewGroupName;
        public GridLayout gridLayout;
        public CardView card;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewGroupName = itemView.findViewById(R.id.textViewGroupName);
            gridLayout = itemView.findViewById(R.id.gridLayoutGroupItem);
            card = itemView.findViewById(R.id.cardViewItemGroup);
        }
    }

}
