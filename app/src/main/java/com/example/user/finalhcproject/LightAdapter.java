package com.example.user.finalhcproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.wrapper.domain.device.DeviceConfiguration;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;
import com.philips.lighting.hue.sdk.wrapper.utilities.HueColor;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class LightAdapter extends RecyclerView.Adapter<LightAdapter.ViewHolder>{

    private List<LightPoint> lightItems;
    private Context context;
    private MainActivity act;

    public LightAdapter(List<LightPoint> lightList, Context context) {
        this.lightItems = lightList;
        this.context = context;
        this.act = (MainActivity)context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.light_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final LightPoint light = lightItems.get(position);

        if(position == 0)
            holder.textViewLightName.setText("Master Control - All Lights");
        else {
            hueColorConversion converter = new hueColorConversion();
            HueColor.RGB currentRGB = converter.getRGBFromLight(light);
            holder.imageViewLightModel.setColorFilter(Color.argb(255, currentRGB.r, currentRGB.g, currentRGB.b));
            holder.textViewLightName.setText(light.getName());
        }

        selectModelImage(light, holder);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.showToast("You clicked " + light.getName());
                if(position == 0)
                    act.updateLights(light, true);
                else
                    act.updateLights(light, false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lightItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView textViewLightName;
        public GridLayout gridLayout;
        public CardView card;
        public ImageView imageViewLightModel;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewLightName = itemView.findViewById(R.id.textViewLightName);
            gridLayout = itemView.findViewById(R.id.gridLayoutLightItem);
            card = itemView.findViewById(R.id.cardViewItemLight);
            imageViewLightModel = itemView.findViewById(R.id.imageViewProductTypeLights);
        }
    }


    // This chooses the correct image to be shown next to the lights name based on the model ID
    public void selectModelImage(LightPoint lightPoint, ViewHolder hold) {
        DeviceConfiguration tempConfig = lightPoint.getConfiguration();
        String model = tempConfig.getModelIdentifier();

        switch (model) {
            case "LCT001":
            case "LCT007":
            case "LCT010":
            case "LCT014":
            case "LCW010":
            case "LCW001":
            case "LCW00$":
            case "LCW015":
            case "LWB004":
            case "LWB006":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_e27_waca);
                break;
            case "LWB010":
            case "LWB014":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_e27_white);
                break;
            case "LCT012":
            case "LTW012":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_e14);
                break;
            case "LCT002":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_br30);
                break;
            case "LCT011":
            case "LTW011":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_br30_slim);
                break;
            case "LCT003":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_gu10);
                break;
            case "LTW013":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_gu10_perfectfit);
                break;
            case "LST001":
            case "LST002":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_lightstrip);
                break;
            case "LLC006":
            case "LLC010":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_iris);
                break;
            case "LLC005":
            case "LLC011":
            case "LLC012":
            case "LLC007":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_bloom);
                break;
            case "LLC014":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_aura);
                break;
            case "LLC013":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_storylight);
                break;
            case "LLC020":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_go);
                break;
            case "HBL001":
            case "HBL002":
            case "HBL003":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_beyond_ceiling_pendant_table);
                break;
            case "HIL001":
            case "HIL002":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_impulse);
                break;
            case "HEL001":
            case "HEL002":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_entity);
                break;
            case "HML001":
            case "HML002":
            case "HML003":
            case "HML004":
            case "HML005":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_phoenix_ceiling_pendant_table_wall);
                break;
            case "HML006":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_phoenix_down);
                break;
            case "LTP001":
            case "LTP002":
            case "LTP003":
            case "LTP004":
            case "LTP005":
            case "LTD003":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_pendant);
                break;
            case "LDF002":
            case "LTF001":
            case "LTF002":
            case "LTC001":
            case "LTC002":
            case "LTC003":
            case "LTC004":
            case "LTD001":
            case "LTD002":
            case "LDF001":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_ceiling);
                break;
            case "LDD002":
            case "LFF001":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_floor);
                break;
            case "LDD001":
            case "LTT001":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_table);
                break;
            case "LDT001":
            case "MWM001":
                hold.imageViewLightModel.setImageResource(R.drawable.ic_recessed);
                break;
            default:
                hold.imageViewLightModel.setImageResource(R.drawable.ic_e27_waca);
                break;
        }
    }
}