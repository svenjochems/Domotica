package be.jochems.sven.domotica.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import be.jochems.sven.domotica.R;
import be.jochems.sven.domotica.data.ActionInterface;

/**
 * Created by sven on 10/09/16.
 */

public class OutputListAdapter extends ArrayAdapter<ActionInterface> {
    private Context context;

    public OutputListAdapter(Context context, int resource, ArrayList<ActionInterface> data) {
        super(context, resource, data);
        this.context = context;
    }

    public void updateData(ArrayList<ActionInterface> data) {
        // if count of items is not changed: only update status
        // else reload all items

        if (data.size() == super.getCount()) {
            for (int i = 0; i < data.size(); i++) {
                super.getItem(i).setStatus(data.get(i).getStatus());
            }
        } else {
            super.clear();
            super.addAll(data);
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ActionInterface item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_outputs, parent, false);
        }

        TextView outputText = (TextView) convertView.findViewById(R.id.listOutputText);
        ImageView outputImage = (ImageView) convertView.findViewById(R.id.listOutputImage);

        outputText.setText(item.getName());
        outputImage.setImageResource(getImageResource(item.getIcon(), item.getStatus()));
        return convertView;
    }



    private int getImageResource(int type, boolean status){
        int[] outputImage = new int[]{
                R.drawable.light_out,
                R.drawable.light_on,
                R.drawable.plug_out,
                R.drawable.plug_on,
                R.drawable.fan_out,
                R.drawable.fan_on,
                R.drawable.mood_off,
                R.drawable.mood_on
        };
        int index = type * 2;
        if (status) index++;
        return outputImage[index];
    }
}
