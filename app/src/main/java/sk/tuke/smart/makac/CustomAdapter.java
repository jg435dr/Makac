package sk.tuke.smart.makac;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sk.tuke.smart.makac.helpers.MainHelper;

/**
 * Created by Jakub on 18.11.2017.
 *
 */

public class CustomAdapter extends ArrayAdapter<DataItem> {

    private Context context;
    private int layoutResourceId;
    private List<DataItem> data;
    private String something;

    public CustomAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<DataItem> objects) {
        super(context, resource, objects);

        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
    }

    private static class DataHolder{
        ImageView image;
        TextView duration;
        TextView distance;
        TextView avgPace;
        TextView endTime;
    }
    @NonNull
    @Override
    @SuppressLint("DefaultLocale")
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Log.i("CustomAdapter", "View getView");
        DataHolder holder;

        if(convertView == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, null);

            holder = new DataHolder();
            holder.image = (ImageView)convertView.findViewById(R.id.workouts_list_image_sport);
            holder.duration = (TextView)convertView.findViewById(R.id.workouts_list_duration_value);
            holder.distance = (TextView)convertView.findViewById(R.id.workouts_list_distance_value);
            holder.avgPace = (TextView)convertView.findViewById(R.id.workouts_list_avg_speed_value);
            holder.endTime = (TextView)convertView.findViewById(R.id.workouts_list_endTime) ;

            convertView.setTag(holder);
        } else {
            holder = (DataHolder)convertView.getTag();
        }

        DataItem dataItem = data.get(position);
        holder.duration.setText(MainHelper.formatDuration(dataItem.duration));
        switch(dataItem.sport){
            case MainHelper.CYCLING:
                holder.image.setImageResource(R.drawable.image_cycling);
                break;
            case MainHelper.WALKING:
                holder.image.setImageResource(R.drawable.image_walking);
                break;
            default:
                holder.image.setImageResource(R.drawable.image_running);
                break;
        }

        if(dataItem.distance < 1000) {
            holder.distance.setText(String.valueOf(String.format("%.2f m",dataItem.distance)));
        } else {
            holder.distance.setText(String.valueOf(String.format("%.2f km",dataItem.distance/1000D)));
        }

        holder.avgPace.setText(String.format("%.2f km/h", dataItem.avgPace));
        holder.endTime.setText(dataItem.date);
        return convertView;
    }
}
