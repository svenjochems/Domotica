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

/**
 * Created by sven on 10/09/16.
 */

public class OutputListAdapter extends ArrayAdapter<OutputListItem> {
    private Context context;

    public OutputListAdapter(Context context, int resource, ArrayList<OutputListItem> data) {
        super(context, resource, data);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OutputListItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_outputs, parent, false);
        }

        TextView outputText = (TextView) convertView.findViewById(R.id.listOutputText);
        ImageView outputImage = (ImageView) convertView.findViewById(R.id.listOutputImage);

        outputText.setText(item.getText());
        outputImage.setImageResource(item.getImgResource());
        return convertView;

    }
}
