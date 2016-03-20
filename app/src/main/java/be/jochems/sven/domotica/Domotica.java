package be.jochems.sven.domotica;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by sven on 20/03/16.
 */
public class Domotica extends Application{
    // Data
    private static int  numberOfModules = 2;
    private String[]    groups;
    private String[][]  outputs;
    private String[]    moods;

    private int[][]     outputIndex;
    private int[][]     outputIcon;

    private Socket socket;
    private Context context;
    private SharedPreferences prefs;


    @Override
    public void onCreate() {
        context = getApplicationContext();
        prefs = context.getSharedPreferences("connection", MODE_PRIVATE);
        super.onCreate();
    }

    public String[] getGroups() {
        if (!isDataLoaded()) loadData();
        return groups;
    }

    public String[][] getOutputs() {
        if (!isDataLoaded()) loadData();
        return outputs;
    }

    public String[] getMoods() {
        if (!isDataLoaded()) loadData();
        return moods;
    }

    public int[][] getOutputIndex() {
        if (!isDataLoaded()) loadData();
        return outputIndex;
    }

    public int[][] getOutputIcon() {
        if (!isDataLoaded()) loadData();
        return outputIcon;
    }

    private boolean isDataLoaded(){
        if (    groups == null ||
                outputs == null ||
                moods == null ||
                outputIndex == null ||
                outputIcon == null)
            return false;
        return true;
    }

    public boolean importData(){
        boolean gr = loadGroups();
        boolean ou = loadOutputs(numberOfModules);
        boolean mo = loadMoods();
        boolean co = closeConnection();
        boolean sa = saveData();

        return gr && ou && mo && co && sa;
    }

    private boolean saveData(){
        boolean a = saveStringArray(groups, "groups");
        boolean b = save2dStringArray(outputs, "outputs");
        boolean c = saveStringArray(moods, "moods");
        boolean d = save2dIntArray(outputIndex, "index");
        boolean e = save2dIntArray(outputIcon,"icon");

        Log.d("Save", "Saving imported data");

        return a && b && c && d && e;
    }

    public boolean loadData(){

        if(     prefs.getString("groups",null) == null ||
                prefs.getString("outputs",null) == null ||
                prefs.getString("moods",null) == null ||
                prefs.getString("index",null) == null ||
                prefs.getString("icon",null) == null){

            Log.i("Load Data", "No data available, importing");
            return importData();
        } else{
            groups = loadStringArray("groups");
            outputs = load2dStringArray("outputs");
            moods = loadStringArray("moods");
            outputIndex = load2dIntArray("index");
            outputIcon = load2dIntArray("icon");
            Log.i("Load Data", "Data available, loading");
            return true;
        }
    }

    public boolean closeConnection(){
        try{
            if (socket.isConnected())
                socket.close();
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean toggleOutput(int module, int address){
        //AF02FF'mod'0000080108FFFFFFFFFFFFAF'mod''addr'02FFFF64FFFF
        byte[] sendData = new byte[]{(byte)175, (byte)2, (byte)255, (byte)module, (byte)0, (byte)0, (byte)8, (byte)1,
                (byte)8, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)175,
                (byte)module, (byte)address, (byte)2, (byte)255, (byte)255, (byte)100, (byte)255, (byte)255};
        byte[] receive;

        try {
            receive = new Network().execute(sendData).get();
            Log.i("Toggle output", "Module " + module + ", address " + address);

        } catch (Exception e){
            e.printStackTrace();
            return false;
        } finally {
            closeConnection();
        }
        return true;
    }

    public boolean toggleMood(int address){
        int module = (byte)255;

        //AF02FF'mod'0000080108FFFFFFFFFFFFAF'mod''addr'02FFFF64FFFF
        byte[] sendData = new byte[]{(byte)175, (byte)2, (byte)255, (byte)module, (byte)0, (byte)0, (byte)8, (byte)1,
                (byte)8, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)175,
                (byte)53, (byte)address, (byte)2, (byte)255, (byte)255, (byte)100, (byte)255, (byte)255};
        byte[] receive;

        try {
            receive = new Network().execute(sendData).get();
            Log.i("Toggle mood", "Module " + module + ", address " + address);

        } catch (Exception e){
            e.printStackTrace();
            return false;
        } finally {
            closeConnection();
        }
        return true;
    }

    private boolean loadGroups() {
        byte[] sendData = new byte[]{(byte)175, (byte)16 , (byte)8 , (byte)2 , (byte)24 , (byte)0 , (byte)32 , (byte)0
                , (byte)32 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)175};
        byte[] receive;
        String[] groupArray;

        try {
            receive = new Network().execute(sendData).get();

            String groupsl = new String(receive);
            groupsl = groupsl.substring(0,groupsl.length() - 16);
            groupArray = new String[groupsl.length()/32];

            for(int i = 0; i < groupArray.length; i++){
                groupArray[i] = groupsl.substring(i*32, (i+1)*32).trim();
            }

            Log.d("Groups","Imported: " + Arrays.toString(groupArray));

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        groups = groupArray;
        return true;
    }

    private boolean loadOutputs(int numberOfModules){

        outputs         = new String[numberOfModules][];
        outputIndex     = new int[numberOfModules][];
        outputIcon      = new int[numberOfModules][];

        byte[][] receive = new byte[numberOfModules][];

        try {
            //af 10 08 01 01 00 20 0c 20 ff ff ff ff ff ff af
            for (int i = 1; i <= numberOfModules; i++){
                byte[] sendData = new byte[]{(byte)175, (byte)16 , (byte)8 , (byte)i , (byte)1 , (byte)0 , (byte)32 , (byte)12
                        , (byte)32 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)175};

                receive[i-1] = new Network().execute(sendData).get();
                Thread.sleep(200);
            }

            for(int i = 0; i < numberOfModules; i++){
                int length = receive[i].length / 32;

                outputs[i]      = new String[length];
                outputIndex[i]  = new int[length];
                outputIcon[i]   = new int[length];

                for (int j = 0; j < length; j++){
                    byte[] temp = Arrays.copyOfRange(receive[i],j*32,(j+1)*32);
                    byte icon = temp[temp.length-2];
                    byte index = temp[temp.length-1];
                    String name = new String(temp).substring(0,30).trim();

                    outputs[i][j] = name;
                    outputIcon[i][j] = icon;
                    outputIndex[i][j] = index;

                }
                Log.d("Outputs","Imported: " + Arrays.toString(outputs[i]));
            }


        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean loadMoods() {
        byte[] sendData = new byte[]{(byte) 175, (byte) 16, (byte) 8, (byte) 2, (byte) 12, (byte) 0, (byte) 32, (byte) 0
                , (byte) 32, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 175};
        byte[] receive;
        String[] moodsArray = null;

        try {
            receive = new Network().execute(sendData).get();

            String moodsl = new String(receive);
            moodsl = moodsl.substring(0, moodsl.length() - 16);
            moodsArray = new String[moodsl.length() / 32];

            for (int i = 0; i < moodsArray.length; i++) {
                moodsArray[i] = moodsl.substring(i * 32, (i + 1) * 32).trim();
            }

            Log.d("Moods","Imported: " + Arrays.toString(moodsArray));

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        moods = moodsArray;
        return true;
    }

    private class Network extends AsyncTask<byte[], String, byte[]> {

        @Override
        protected byte[] doInBackground(byte[]... params) {

            try {
                //TODO: get ip and port from shared preferences
                InetAddress address = InetAddress.getByName("192.168.0.177");
                int port = 10001;

                if (socket == null) {
                    socket = new Socket(address, port);
                } else{
                    if (socket.isClosed()){
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
                byte[] data = new byte[maxLines*16];

                byte[] end = new byte[16];
                Arrays.fill(end, (byte) 255);

                int count = 0;
                while (count < maxLines){
                    byte[] temp = new byte[16];
                    dis.read(temp,0,16);

                    if(Arrays.equals(temp,end) && count >= 2){
                        //Log.d("Break","Break");
                        break;
                    }

                    for(int j = 0; j < temp.length; j++) {
                        data[count*16 + j] = temp[j];
                    }
                    count++;
                }

                //Log.d("data", Arrays.toString(data));

                byte[] result = Arrays.copyOfRange(data, 32, count * 16);
                //Log.d("Recieved Data", Arrays.toString(result));

                return result;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    public boolean saveStringArray(String[] array, String arrayName) {
        SharedPreferences.Editor editor = prefs.edit();
        try {
            JSONArray json = new JSONArray(array);
            editor.putString(arrayName, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return editor.commit();
    }

    public String[] loadStringArray(String arrayName) {
        String jsonString = prefs.getString(arrayName, null);
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            String[] array = new String[jsonArray.length()];
            for (int i = 0; i < array.length; i++)
                array[i] = jsonArray.getString(i);
            return array;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new String[]{};
    }

    public boolean save2dStringArray(String[][] array, String arrayName) {
        SharedPreferences.Editor editor = prefs.edit();
        try {
            JSONArray json = new JSONArray(array);
            editor.putString(arrayName, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return editor.commit();
    }

    public String[][] load2dStringArray(String arrayName) {
        String jsonString = prefs.getString(arrayName, null);
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            String[][] array = new String[jsonArray.length()][];
            for (int i = 0; i < array.length; i++){
                JSONArray sub = new JSONArray(jsonArray.getString(i));
                array[i] = new String[sub.length()];
                for (int j = 0; j < array[i].length; j++) {
                    array[i][j] = sub.getString(j);
                }
            }

            return array;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new String[][]{};
    }

    public boolean save2dIntArray(int[][] array, String arrayName) {
        SharedPreferences.Editor editor = prefs.edit();
        try {
            JSONArray json = new JSONArray(array);
            editor.putString(arrayName, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return editor.commit();
    }

    public int[][] load2dIntArray(String arrayName) {
        String jsonString = prefs.getString(arrayName, null);
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            int[][] array = new int[jsonArray.length()][];
            for (int i = 0; i < array.length; i++){
                JSONArray sub = new JSONArray(jsonArray.getString(i));
                array[i] = new int[sub.length()];
                for (int j = 0; j < array[i].length; j++) {
                    array[i][j] = Integer.parseInt(sub.getString(j));
                }
            }

            return array;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new int[][]{};
    }
}
