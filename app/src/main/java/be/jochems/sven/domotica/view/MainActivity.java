package be.jochems.sven.domotica.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

import be.jochems.sven.domotica.connection.Connection;
import be.jochems.sven.domotica.Domotica;
import be.jochems.sven.domotica.R;

public class MainActivity extends AppCompatActivity {

    // Widgets
    private ListView    lstGroups;
    private ListView    lstOutputs;

    // Fields
    private ArrayAdapter<String> adpGroups;
    private OutputListAdapter adpOutputs;

    private Domotica application;
    private Connection con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lstGroups = (ListView) findViewById(R.id.lstGroups);
        lstOutputs = (ListView) findViewById(R.id.lstOutputs);

        application = (Domotica)getApplicationContext();
        con = new Connection(getApplicationContext());

        String[] groups = application.getGroups();
        String[] lstData = Arrays.copyOf(groups, groups.length + 1);
        lstData[lstData.length - 1] = getString(R.string.lstMoods);

        adpGroups = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lstData);
        lstGroups.setAdapter(adpGroups);

        // load list with moods or outputs
        lstGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lstGroups.setVisibility(View.GONE);

                ArrayList<OutputListItem> outputList;

                if ((parent.getItemAtPosition(position)).equals(getString(R.string.lstMoods)))
                    outputList = populateMoods();
                else outputList = populateOutputs(position);

                adpOutputs = new OutputListAdapter(MainActivity.this, R.layout.list_outputs, outputList);
                lstOutputs.setAdapter(adpOutputs);
                lstOutputs.setVisibility(View.VISIBLE);
            }
        });

        // perform action on list
        lstOutputs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OutputListItem data = (OutputListItem) parent.getItemAtPosition(position);
                String name = data.getText();
                boolean isMood = data.isMood();

                if (isMood)  toggleMood(name);
                else {
                    boolean test = toggleOutput(name);
                    if (test) {
                        ArrayList<OutputListItem> outputList = populateOutputs(data.getOutputIndex());
                        adpOutputs = new OutputListAdapter(MainActivity.this, R.layout.list_outputs, outputList);
                        lstOutputs.setAdapter(adpOutputs);
                    }
                }
            }
        });
    }

    private ArrayList<OutputListItem> populateMoods(){
        //TODO: get status from moods
        ArrayList<OutputListItem> outputList = new ArrayList<>();

        String[] moods = application.getMoods();
        for (int i = 0; i < moods.length; i++) {
            OutputListItem item = constructListItem(moods[i], 3, (byte)0, true);
            outputList.add(item);
        }
        return outputList;
    }

    private ArrayList<OutputListItem> populateOutputs(int position){
        ArrayList<OutputListItem> outputList = new ArrayList<>();

        // load outputstatus
        byte[][] status = con.getStatus();

        int[][] outputIndex = application.getOutputIndex();
        String[][] outputs = application.getOutputs();
        int[][] outputIcon = application.getOutputIcon();

        for (int i = 0; i < outputIndex.length; i++) {
            for (int j = 0; j < outputIndex[i].length; j++) {
                if (outputIndex[i][j] == position) {
                    OutputListItem outputListItem = constructListItem(outputs[i][j], outputIcon[i][j], status[i][j], false);
                    outputListItem.setOutputIndex(position);
                    outputList.add(outputListItem);
                }
            }
        }
        return outputList;
    }

    private OutputListItem constructListItem(String name, int type, byte status, boolean isMood){
        int img = getImageResource(type, status);
        OutputListItem item = new OutputListItem(img, name, isMood);
        return item;
    }

    private int getImageResource(int type, byte status){
        int[] outputImage = new int[]{
                R.drawable.light_out,
                R.drawable.light_on,
                R.drawable.plug_out,
                R.drawable.plug_on,
                R.drawable.fan_out,
                R.drawable.fan_on,
                R.drawable.mood_off,
                R.drawable.mood_on
        };
        return outputImage[type * 2 + status];
    }

    private boolean toggleMood(String name){
        int address = -1;
        String[] moods = application.getMoods();

        for (int i = 0; i < moods.length; i++){
            if (moods[i].equals(name)){
                address = i;
                break;
            }
        }
        return con.toggleMood(address);
    }

    private boolean toggleOutput(String name){
        int module = -1;
        int address = -1;
        String[][] outputs = application.getOutputs();

        for (int i = 0; i < outputs.length; i++) {
            for (int j = 0; j < outputs[i].length; j++) {
                if (outputs[i][j].equals(name)) {
                    module = i + 1;
                    address = j;
                    break;
                }
            }
        }
        return con.toggleOutput(module, address);
    }

    @Override
    public void onBackPressed() {
        int visibility = lstOutputs.getVisibility();
        if (visibility == View.VISIBLE){
            lstOutputs.setVisibility(View.GONE);
            lstGroups.setVisibility(View.VISIBLE);
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        } else if (id == R.id.action_exit){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}
