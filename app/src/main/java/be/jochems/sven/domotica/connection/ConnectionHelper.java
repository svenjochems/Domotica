package be.jochems.sven.domotica.connection;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by sven on 2/10/16.
 */

public class ConnectionHelper {
    private Socket socket;
    private InetAddress address;
    private int port;

    public ConnectionHelper(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public boolean closeConnection() {
        try {
            if (socket != null && socket.isConnected())
                socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public byte[] execute(boolean keepConnection, byte[]... params) throws Exception {
        try {
            return new TcpConnection().execute(params).get();
        } catch (Exception e) {
            throw e;
        } finally {
            if (!keepConnection) closeConnection();
        }
    }


    private class TcpConnection extends AsyncTask<byte[], String, byte[]> {

        @Override
        protected byte[] doInBackground(byte[]... params) {

            try {
                if (socket == null) {
                    socket = new Socket(address, port);
                } else {
                    if (socket.isClosed()) {
                        socket = new Socket(address, port);
                    }
                }

                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);

                dos.write(params[0]);

                InputStream in = socket.getInputStream();
                DataInputStream dis = new DataInputStream(in);

                //dis.read();
                int maxLines = 26;
                byte[] data = new byte[maxLines * 16];

                byte[] end = new byte[16];
                Arrays.fill(end, (byte) 255);

                int count = 0;
                while (count < maxLines) {
                    byte[] temp = new byte[16];
                    dis.read(temp, 0, 16);

                    if (Arrays.equals(temp, end) && count >= 2) {
                        //Log.d("Break","Break");
                        break;
                    }

                    for (int j = 0; j < temp.length; j++) {
                        data[count * 16 + j] = temp[j];
                    }
                    count++;
                }

                byte[] result = Arrays.copyOfRange(data, 32, count * 16);

                return result;

            } catch (Exception e) {
                Log.e("ConnectionHelper", "Error executing action");
            }

            return null;
        }
    }
}
