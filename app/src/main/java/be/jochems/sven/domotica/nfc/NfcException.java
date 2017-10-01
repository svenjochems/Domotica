package be.jochems.sven.domotica.nfc;

import android.content.Context;

public class NfcException extends Exception {
    private int messageId;

    public NfcException(int id) {
        this.messageId = id;
    }

    public String getMessage(Context context) {
        return context.getString(messageId);
    }
}
