package be.jochems.sven.domotica.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

import be.jochems.sven.domotica.connection.Connection;
import be.jochems.sven.domotica.data.ActionIdentifier;

public class ActionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                }

            } else {
                System.out.println("No message");
            }
        } else {
            try {
                doDefaultAction();
            } catch (IllegalArgumentException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        finish();
    }

    private void doAction(NdefMessage message) {
        NdefRecord[] records = message.getRecords();
        NdefRecord record = records[0];
        ActionIdentifier action = SerializationUtils.deserialize(record.getPayload());
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
