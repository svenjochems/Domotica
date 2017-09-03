package be.jochems.sven.domotica.view;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import org.apache.commons.lang3.SerializationUtils;
import be.jochems.sven.domotica.data.ActionIdentifier;
import be.jochems.sven.domotica.nfc.NfcUtils;

public class NfcWriteActivity extends AppCompatActivity {

    boolean mWriteMode = false;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    private Dialog dialog;
    private ActionIdentifier item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            item = (ActionIdentifier) extras.get("item");
        } catch (Exception e) {
            Toast.makeText(this, "No or invalid data passed, returning", Toast.LENGTH_SHORT).show();
            finish();
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(NfcWriteActivity.this);
        mNfcPendingIntent = PendingIntent.getActivity(NfcWriteActivity.this, 0,
                new Intent(NfcWriteActivity.this, NfcWriteActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        dialog = new AlertDialog.Builder(NfcWriteActivity.this)
                .setTitle("Touch tag to write")
                .setCancelable(true)
                .setMessage("touch tag")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        disableTagWriteMode();
                        finish();
                    }
                })
                .create();

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableTagWriteMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableTagWriteMode();
        dialog.dismiss();
    }


    private void enableTagWriteMode() {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] mWriteTagFilters = new IntentFilter[] { tagDetected };
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
    }

    private void disableTagWriteMode() {
        mWriteMode = false;
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        byte[] itemData = SerializationUtils.serialize(item);

        // Tag writing mode
        if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefRecord record = NdefRecord.createMime("action/domotica", itemData);
            NdefMessage message = new NdefMessage(new NdefRecord[] { record });
            if (NfcUtils.writeTag(message, detectedTag)) {
                Toast.makeText(this, "Wrote to NFC tag: " + item.getName(), Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }
}
