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
import android.widget.Toast;

import be.jochems.sven.domotica.R;
import be.jochems.sven.domotica.connection.Connection;
import be.jochems.sven.domotica.data.ActionIdentifier;

public class ActionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        // triggered from nfc tag
        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                for (int i = 0; i < rawMessages.length; i++) {
                    NdefMessage message = (NdefMessage) rawMessages[i];
                    try {
                        doAction(message);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), R.string.action_nfc_error, Toast.LENGTH_LONG);
                    }
                }

            } else {
                Toast.makeText(getApplicationContext(), R.string.action_nfc_error, Toast.LENGTH_LONG);
            }

        // triggerd from elsewhere: do default
        } else {
            try {
                doDefaultAction();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.action_default_unexpected, Toast.LENGTH_LONG).show();
            }
        }

        finish();
    }

    private void doAction(NdefMessage message) throws Exception {
        NdefRecord[] records = message.getRecords();
        NdefRecord record = records[0];
        String data = new String(record.getPayload());
        ActionIdentifier identifier = ActionIdentifier.fromString(data);
        toggle(identifier);
    }

    private void doDefaultAction() throws Exception {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("nfc", Context.MODE_PRIVATE);
        String aDefault = prefs.getString("default", "");
        if (aDefault.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.action_default_empty, Toast.LENGTH_LONG).show();
            return;
        }

        ActionIdentifier identifier = ActionIdentifier.fromString(aDefault);
        toggle(identifier);
    }

    private void toggle(ActionIdentifier identifier) {
        try {
            Connection c = new Connection();
            c.openConnection();
            if (identifier.getModule() != -1) {
                c.toggleOutput(identifier.getModule(), identifier.getAddress());
                byte[][] status = c.getStatus();
                byte toggleStatus = status[identifier.getModule() - 1][identifier.getAddress()];

                String on = getApplicationContext().getString(R.string.widget_state_on);
                String off = getApplicationContext().getString(R.string.widget_state_off);
                String toastText = getApplicationContext().getString(R.string.widget_toggle_toast, identifier.getName(), toggleStatus == 1 ? on : off);
                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
            } else {
                c.toggleMood(identifier.getAddress());
                String toastText = getApplicationContext().getString(R.string.widget_mood_toast, identifier.getName());
                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.action_default_unexpected, Toast.LENGTH_LONG);
        }
    }
}
