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

import be.jochems.sven.domotica.R;
import be.jochems.sven.domotica.data.ActionIdentifier;
import be.jochems.sven.domotica.nfc.NfcException;
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
            Toast.makeText(this, getApplicationContext().getString(R.string.intent_nfc_invalidData), Toast.LENGTH_SHORT).show();
            finish();
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(NfcWriteActivity.this);
        mNfcPendingIntent = PendingIntent.getActivity(NfcWriteActivity.this, 0,
                new Intent(NfcWriteActivity.this, NfcWriteActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        dialog = new AlertDialog.Builder(NfcWriteActivity.this)
                .setTitle(getApplicationContext().getString(R.string.nfc_dialog_title))
                .setCancelable(true)
                .setMessage(getApplicationContext().getString(R.string.nfc_dialog_message))
                .setNegativeButton(getApplicationContext().getString(R.string.nfc_dialog_cancel), new DialogInterface.OnClickListener() {
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
        byte[] itemData = item.toString().getBytes();

        // Tag writing mode
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefRecord record = NdefRecord.createMime("action/domotica", itemData);
            NdefMessage message = new NdefMessage(new NdefRecord[] { record });

            try {
                NfcUtils.writeTag(message, detectedTag);
                String toastText = getApplicationContext().getString(R.string.nfc_write_success, item.getName());
                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_LONG).show();
            } catch (NfcException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(getApplicationContext()), Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }
}
