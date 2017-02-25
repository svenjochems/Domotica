package be.jochems.sven.domotica.connection;

import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by sven on 22/01/17.
 */

public class UdpDiscovery extends AsyncTask<Void, Void, InetAddress> {
    private final String TAG = "UdpDiscovery";
    private final int port = 30718;

    @Override
    protected InetAddress doInBackground(Void... params) {

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(5000);
            byte[] data = new byte[]{(byte)0, (byte)0, (byte)0, (byte)248};

            try {
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), port);
                socket.send(sendPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();

                    if (broadcast != null) {
                        DatagramPacket packet = new DatagramPacket(data, data.length, broadcast, port);
                        socket.send(packet);
                    }
                }
            }

            byte[] recvBuf = new byte[1];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(receivePacket);

            //We have a response
            Log.i(TAG, "Connection found on address " + receivePacket.getAddress());
            return receivePacket.getAddress();

        } catch (Exception e) {
            Log.i(TAG, "No connected installation found", e);
        } finally {
            if (socket != null && !socket.isClosed())
            socket.close();
        }
        return null;
    }

    @Override
    protected void onPostExecute(InetAddress address) {
        super.onPostExecute(address);
    }
}
