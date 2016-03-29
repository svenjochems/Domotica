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
    private String[]    groups;
    private String[][]  outputs;
    private String[]    moods;

    private int[][]     outputIndex;
    private int[][]     outputIcon;

    private Context context;
    private SharedPreferences prefs;


    @Override
    public void onCreate() {
        context = getApplicationContext();
        prefs = context.getSharedPreferences("connection", MODE_PRIVATE);
        super.onCreate();
    }

    protected Domotica getInstance(){
        return this;
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

    public boolean loadData(){

        if(     prefs.getString("groups",null) == null ||
                prefs.getString("outputs",null) == null ||
                prefs.getString("moods",null) == null ||
                prefs.getString("index",null) == null ||
                prefs.getString("icon",null) == null){

            boolean im = importData();

            return im && saveData();

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

    public boolean importData(){
        Log.i("Load Data", "No data available, importing");
        Connection con = new Connection(getApplicationContext());
        con.importData();

        groups      = con.getGroups();
        outputs     = con.getOutputs();
        moods       = con.getMoods();
        outputIndex = con.getOutputIndex();
        outputIcon  = con.getOutputIcon();

        return true;
    }

    // Save loaded data to shared preferences
    private boolean saveData(){
        boolean a = saveStringArray(groups, "groups");
        boolean b = save2dStringArray(outputs, "outputs");
        boolean c = saveStringArray(moods, "moods");
        boolean d = save2dIntArray(outputIndex, "index");
        boolean e = save2dIntArray(outputIcon,"icon");

        Log.d("Save", "Saving imported data");

        return a && b && c && d && e;
    }

    private boolean saveStringArray(String[] array, String arrayName) {
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

    private String[] loadStringArray(String arrayName) {
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

    private boolean save2dStringArray(String[][] array, String arrayName) {
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

    private String[][] load2dStringArray(String arrayName) {
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

    private boolean save2dIntArray(int[][] array, String arrayName) {
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

    private int[][] load2dIntArray(String arrayName) {
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
