package com.example.user.finalhcproject;

// The group adapter is used to setup the group fragment. This binds the widgets in the layout and sets the values
//

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

    // Fill in the group names and show a toast message.
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

    // Return the amount of groups detected in the system
    @Override
    public int getItemCount() {
        return groupItems.size();
    }

    // This gets the layout elements from the view so they can be modified later.
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
