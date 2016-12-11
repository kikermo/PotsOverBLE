package org.kikermo.blepotcontroller.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by EnriqueR on 10/12/2016.
 */

public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {
    private int resource;

    public BluetoothDeviceAdapter(Context context, int resource) {
        super(context, resource);
        this.resource = resource;
    }


    @Override
    public void add(BluetoothDevice itemToAdd) {
        for (int i = 0; i < getCount(); i++)
            if (getItem(i).getAddress().equals(itemToAdd.getAddress()))
                return;

        super.add(itemToAdd);
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource,parent,false);
            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(textView);
        }
        TextView tv = (TextView) convertView.getTag();

        BluetoothDevice item = getItem(position);
        tv.setText(item.getName());


        return convertView;
    }
}
