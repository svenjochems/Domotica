package be.jochems.sven.domotica.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import be.jochems.sven.domotica.connection.Connection;

public class ActionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            doDefaultAction();
        } catch (IllegalArgumentException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void doDefaultAction() throws IllegalArgumentException {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("nfc", Context.MODE_PRIVATE);
        String aDefault = prefs.getString("default", "");
        if (aDefault.isEmpty()) {
            Log.d("nfc", "empty");
            throw new IllegalArgumentException("No NFC default set. Long press on output to set default");
        }

        String[] split = aDefault.split("_");
        if (split.length != 3) {
            Log.d("nfc", "invalid default string");
            throw new IllegalArgumentException("Illegal NFC default set. Long press on output to reset default");
        }

        String module = split[0];
        String address = split[1];
        String name = split[2];
        int m = Integer.parseInt(module);
        int a = Integer.parseInt(address);
        toggle(m, a);
        Log.d("nfc", aDefault + " toggled");
        Toast.makeText(getApplicationContext(), name + " toggled", Toast.LENGTH_SHORT).show();
    }

    private void toggle(int module, int address) {
        try {
            Connection c = new Connection();
            c.openConnection();
            c.toggleOutput(module, address);
        } catch (Exception e) {
            Log.d("error", e.getMessage());
        }
    }
}
