package be.jochems.sven.domotica.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.jochems.sven.domotica.R;
import be.jochems.sven.domotica.data.Group;
import be.jochems.sven.domotica.data.Module;
import be.jochems.sven.domotica.data.Mood;
import be.jochems.sven.domotica.data.Output;

/**
 * Created by sven on 2/10/16.
 */

public class Importer {

    private final static int NUMBER_OF_MODULES = 2;

    private List<Group> groups;
    private List<Module> modules;

    private TcpConnection tcp;

    public Importer(Context c){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        String prefIp = prefs.getString(c.getString(R.string.pref_key_ip), "");
        String prefPort = prefs.getString(c.getString(R.string.pref_key_port),"");

        if (prefIp.equals("") || prefPort.equals("")){
            //TODO: prefs not loaded, start settingsactivity, but not from this class
        }

        tcp = new TcpConnection("192.168.0.177", 10001);
        groups = new ArrayList<>();
        modules = new ArrayList<>();
        createModules(NUMBER_OF_MODULES);
        createMoodGroup(c);
    }

    // Import all data in memory at once, avoid multiple tcp connections
    public boolean importData(){
        boolean gr = loadGroups();
        boolean ou = loadOutputs();
        boolean mo = loadMoods();
        boolean co = tcp.closeConnection();

        return gr && ou && mo && co;
    }

    public List<Group> getGroups(){
        return this.groups;
    }

    public List<Module> getModules(){
        return this.modules;
    }

    private boolean loadGroups() {
        byte[] sendData = new byte[]{(byte)175, (byte)16 , (byte)8 , (byte)2 , (byte)24 , (byte)0 , (byte)32 , (byte)0
                , (byte)32 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)175};
        byte[] receive;

        String[] groupArray;
        try {
            receive = tcp.execute(sendData).get();

            String groupsl = new String(receive);
            groupsl = groupsl.substring(0,groupsl.length() - 16);
            groupArray = new String[groupsl.length()/32];

            for(int i = 0; i < groupArray.length; i++){
                Group g = new Group(groupsl.substring(i*32, (i+1)*32).trim());
                g.setIndex(i);
                groups.add(g);
            }

            Log.d("Groups","Imported: " + Arrays.toString(groupArray));

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void createModules(int numberOfModules){
        for (int i = 1; i <= numberOfModules; i++) {
            modules.add(new Module((byte)i));
        }
    }

    private void createMoodGroup(Context c){
        Group mood = new Group(c.getString(R.string.lstMoods));
        mood.setIndex(999);
        groups.add(mood);
    }

    private Group getGroupWithIndex(int index){
        for (Group group : groups) {
            if (group.getIndex() == index) {
                return group;
            }
        }
        return null;
    }

    private boolean loadOutputs(){

        try {
            //af 10 08 01 01 00 20 0c 20 ff ff ff ff ff ff af
            for (Module m : modules){
                byte[] sendData = new byte[]{(byte)175, (byte)16 , (byte)8 , m.getAddress() , (byte)1 , (byte)0 , (byte)32 , (byte)12
                        , (byte)32 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)175};

                byte[] bytes = tcp.execute(sendData).get();

                int length = bytes.length / 32;
                for (int i = 0; i < length; i++){
                    byte[] temp = Arrays.copyOfRange(bytes,i*32,(i+1)*32);
                    byte icon = temp[temp.length-2];
                    byte index = temp[temp.length-1];
                    String name = new String(temp).substring(0,30).trim();

                    Group group = getGroupWithIndex(index);

                    Output o = new Output(group, (byte)i, m, name, icon);
                    group.addItem(o);
                    m.addOutput(o);
                }

                Thread.sleep(200);
            }

        } catch (Exception e){
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
            receive = tcp.execute(sendData).get();

            String moodsl = new String(receive);
            moodsl = moodsl.substring(0, moodsl.length() - 16);
            moodsArray = new String[moodsl.length() / 32];

            for (int i = 0; i < moodsArray.length; i++) {
                String name = moodsl.substring(i * 32, (i + 1) * 32).trim();
                Group moodGroup = getGroupWithIndex(999);
                Mood mood = new Mood(moodGroup, (byte)i, name);
                moodGroup.addItem(mood);
            }

            Log.d("Moods","Imported: " + Arrays.toString(moodsArray));

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean toggleOutput(Output output){
        byte module = output.getModule().getAddress();
        byte address = output.getAddress();

        //AF02FF'mod'0000080108FFFFFFFFFFFFAF'mod''addr'02FFFF64FFFF
        byte[] sendData = new byte[]{(byte)175, (byte)2, (byte)255, module, (byte)0, (byte)0, (byte)8, (byte)1,
                (byte)8, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)175,
                module, address, (byte)2, (byte)255, (byte)255, (byte)100, (byte)255, (byte)255};
        byte[] receive;

        try {
            receive = tcp.execute(sendData).get();
            Log.i("Toggle output", "Module " + module + ", address " + address);

        } catch (Exception e){
            e.printStackTrace();
            return false;
        } finally {
            tcp.closeConnection();
        }
        return true;
    }

    public boolean toggleMood(Mood mood){
        byte module = (byte)255;
        byte address = mood.getAddress();

        //AF02FF'mod'0000080108FFFFFFFFFFFFAF'mod''addr'02FFFF64FFFF
        byte[] sendData = new byte[]{(byte)175, (byte)2, (byte)255, module, (byte)0, (byte)0, (byte)8, (byte)1,
                (byte)8, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)175,
                (byte)83, address, (byte)2, (byte)255, (byte)255, (byte)100, (byte)255, (byte)255};
        byte[] receive;

        try {
            receive = tcp.execute(sendData).get();
            Log.i("Toggle mood", "Module " + module + ", address " + address);

        } catch (Exception e){
            e.printStackTrace();
            return false;
        } finally {
            tcp.closeConnection();
        }
        return true;
    }

    public void setStatus(){

        try {

            for (Module m : modules) {
                byte[] sendData = new byte[]{(byte)175, (byte)1, (byte)8, m.getAddress(), (byte)0, (byte)0, (byte)0, (byte)1,
                        (byte)0, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)175};

                //TODO: show spinner while waiting
                // Dobiss Ip module refuses to many connections in too little timeframe
                Thread.sleep(50);
                byte[] rec = tcp.execute(sendData).get();
                Log.i("Get status", "Module " + m.getAddress());

                // trim unused addresses
                int lastIndex = -1;
                for (int j = 0; j < rec.length; j++){
                    if (rec[j] == (byte)255 ){
                        lastIndex = j;
                        break;
                    }
                }
                byte[] bytes = Arrays.copyOf(rec, lastIndex);

                for (int i = 0; i < bytes.length; i++) {
                    Output o = m.getOutputWithAddress(i);
                    if (o == null) {

                    }
                    o.setStatus(bytes[i] == (byte)0 ? false : true);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            tcp.closeConnection();
        }
    }
}
