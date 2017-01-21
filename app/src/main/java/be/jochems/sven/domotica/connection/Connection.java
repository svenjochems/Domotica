package be.jochems.sven.domotica.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Arrays;

import be.jochems.sven.domotica.R;

/**
 * Created by sven on 29/03/16.
 *
 * Create connection with Dobiss domotics Lan interface
 */
public class Connection {

    private ConnectionHelper tcp;

    private final static int NUMBER_OF_MODULES = 2;

    private String[]    groups;
    private String[][]  outputs;
    private String[]    moods;

    private int[][]     outputIndex;
    private int[][]     outputIcon;


    private String ip;
    private int port;

    public Connection(Context c){

        SharedPreferences prefs = c.getSharedPreferences("connection", Context.MODE_PRIVATE);
        // TODO: network discovery
        String prefIp = prefs.getString(c.getString(R.string.pref_key_ip), "");
        String prefPort = prefs.getString(c.getString(R.string.pref_key_port),"");

        if (prefIp.equals("") || prefPort.equals("")){
            //TODO: prefs not loaded, start settingsactivity, but not from this class
        }
        ip = "192.168.0.177";
        port = 10001;

        tcp = new ConnectionHelper("192.168.0.177", 10001);
    }

    // Import all data in memory at once, avoid multiple tcp connections
    public boolean importData(){
        boolean gr = loadGroups(true);
        boolean ou = loadOutputs(NUMBER_OF_MODULES, true);
        boolean mo = loadMoods(true);
        boolean co = tcp.closeConnection();

        return gr && ou && mo && co;
    }

    public String[] getGroups() {
        if(groups == null) {
            loadGroups(false);
        }
        return groups;
    }

    public String[][] getOutputs() {
        if (outputs == null){
            loadOutputs(NUMBER_OF_MODULES, false);
        }
        return outputs;
    }

    public String[] getMoods() {
        if (moods == null){
            loadMoods(false);
        }
        return moods;
    }

    public int[][] getOutputIndex() {
        if (outputIndex == null){
            loadOutputs(NUMBER_OF_MODULES, false);
        }
        return outputIndex;
    }

    public int[][] getOutputIcon() {
        if (outputIcon == null){
            loadOutputs(NUMBER_OF_MODULES, false);
        }
        return outputIcon;
    }

    private boolean loadGroups(boolean keepConnection) {
        byte[] sendData = new byte[]{(byte)175, (byte)16 , (byte)8 , (byte)2 , (byte)24 , (byte)0 , (byte)32 , (byte)0
                , (byte)32 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)175};
        byte[] receive;
        String[] groupArray;

        try {
            receive = tcp.execute(keepConnection, sendData);

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

    private boolean loadOutputs(int numberOfModules, boolean keepConnection){

        outputs         = new String[numberOfModules][];
        outputIndex     = new int[numberOfModules][];
        outputIcon      = new int[numberOfModules][];

        byte[][] receive = new byte[numberOfModules][];

        try {
            //af 10 08 01 01 00 20 0c 20 ff ff ff ff ff ff af
            for (int i = 1; i <= numberOfModules; i++){
                byte[] sendData = new byte[]{(byte)175, (byte)16 , (byte)8 , (byte)i , (byte)1 , (byte)0 , (byte)32 , (byte)12
                        , (byte)32 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)175};

                receive[i-1] = tcp.execute(keepConnection, sendData);
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

    private boolean loadMoods(boolean keepConnection) {
        byte[] sendData = new byte[]{(byte) 175, (byte) 16, (byte) 8, (byte) 2, (byte) 12, (byte) 0, (byte) 32, (byte) 0
                , (byte) 32, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 175};
        byte[] receive;
        String[] moodsArray = null;

        try {
            receive = tcp.execute(keepConnection, sendData);

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



    public boolean toggleOutput(int module, int address){
        //AF02FF'mod'0000080108FFFFFFFFFFFFAF'mod''addr'02FFFF64FFFF
        byte[] sendData = new byte[]{(byte)175, (byte)2, (byte)255, (byte)module, (byte)0, (byte)0, (byte)8, (byte)1,
                (byte)8, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)175,
                (byte)module, (byte)address, (byte)2, (byte)255, (byte)255, (byte)100, (byte)255, (byte)255};
        byte[] receive;

        try {
            receive = tcp.execute(false, sendData);
            Log.i("Toggle output", "Module " + module + ", address " + address);

        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean toggleMood(int address){
        int module = (byte)255;

        //AF02FF'mod'0000080108FFFFFFFFFFFFAF'mod''addr'02FFFF64FFFF
        byte[] sendData = new byte[]{(byte)175, (byte)2, (byte)255, (byte)module, (byte)0, (byte)0, (byte)8, (byte)1,
                (byte)8, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)175,
                (byte)83, (byte)address, (byte)2, (byte)255, (byte)255, (byte)100, (byte)255, (byte)255};
        byte[] receive;

        try {
            receive = tcp.execute(false, sendData);
            Log.i("Toggle mood", "Module " + module + ", address " + address);

        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public byte[][] getStatus(){
        byte[][] receive = new byte[NUMBER_OF_MODULES][];

        try {

            for (int i = 0; i < NUMBER_OF_MODULES; i++) {
                int module = i+1;
                byte[] sendData = new byte[]{(byte)175, (byte)1, (byte)8, (byte)module, (byte)0, (byte)0, (byte)0, (byte)1,
                        (byte)0, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)175};

                //TODO: show spinner while waiting
                // Dobiss Ip module refuses to many connections in too little timeframe
                Thread.sleep(50);
                byte[] rec = tcp.execute(false, sendData);
                Log.i("Get status", "Module " + module);

                // trim unused addresses
                int lastIndex = -1;
                for (int j = 0; j < rec.length; j++){
                    if (rec[j] == (byte)255 ){
                        lastIndex = j;
                        break;
                    }
                }
                receive[i] = Arrays.copyOf(rec, lastIndex);
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return receive;
    }
}
