package be.jochems.sven.domotica.nfc;


import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;

import java.io.IOException;

import be.jochems.sven.domotica.R;

public class NfcUtils {

    public static boolean writeTag(NdefMessage message, Tag tag) throws NfcException {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable())
                    throw new NfcException(R.string.nfc_write_notWritable);
                if (ndef.getMaxSize() < size)
                    throw new NfcException(R.string.nfc_write_toSmall);
                ndef.writeNdefMessage(message);
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return true;
                    } catch (IOException e) {
                        throw new NfcException(R.string.nfc_write_toSmall);
                    }
                } else {
                    throw new NfcException(R.string.nfc_write_toSmall);
                }
            }
        } catch (Exception e) {
            throw new NfcException(R.string.nfc_write_toSmall);
        }
    }
}
