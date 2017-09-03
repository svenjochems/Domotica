package be.jochems.sven.domotica.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

import java.util.ArrayList;
import java.util.List;

import be.jochems.sven.domotica.connection.ActionHelper;
import be.jochems.sven.domotica.Domotica;
import be.jochems.sven.domotica.R;
import be.jochems.sven.domotica.data.ActionInterface;
import be.jochems.sven.domotica.data.Group;
import be.jochems.sven.domotica.data.Mood;
import be.jochems.sven.domotica.data.Output;

public class MainActivity extends AppCompatActivity {

    // Layout
    private RelativeLayout layoutLoad;
    private RelativeLayout layoutMain;

    // Widgets
    private ListView    lstGroups;
    private ListView    lstOutputs;

    // Fields
    private ArrayAdapter<Group> adpGroups;
    private OutputListAdapter adpOutputs;

    // LoadScreen
    private ProgressBar progLoad;
    private TextView txtLoad;
    private Button btnLoadExit;

    private Domotica application;
    private List<Group> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        layoutLoad = (RelativeLayout) findViewById(R.id.content_app_load);
        layoutMain = (RelativeLayout) findViewById(R.id.content_main);

        lstGroups = (ListView) findViewById(R.id.lstGroups);
        lstOutputs = (ListView) findViewById(R.id.lstOutputs);

        progLoad = (ProgressBar) findViewById(R.id.progLoad);
        txtLoad = (TextView) findViewById(R.id.txtLoad);
        btnLoadExit = (Button) findViewById(R.id.btnExit);

        layoutLoad.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);

        application = (Domotica)getApplicationContext();

        txtLoad.setText(R.string.connection_load);
        btnLoadExit.setText(R.string.action_exit);


        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;
                if (what == 1) {
                    onLoadFinished();
                } else {
                    txtLoad.setText(R.string.error_connection);
                    btnLoadExit.setVisibility(View.VISIBLE);
                }
            }
        };


        final Thread loadThread = new Thread() {
            @Override
            public void run() {
                boolean loadSuccess = application.init();
                handler.sendEmptyMessage(loadSuccess?1:0);
            }
        };
        loadThread.start();


        // load list with moods or outputs
        lstGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lstGroups.setVisibility(View.GONE);
                ActionHelper.updateStatus(groups);
                ArrayList<ActionInterface> items = new ArrayList<>();
                items.addAll(groups.get(position).getItems());
                adpOutputs = new OutputListAdapter(MainActivity.this, R.layout.list_outputs, items);
                lstOutputs.setAdapter(adpOutputs);
                lstOutputs.setVisibility(View.VISIBLE);
            }
        });

        // perform action on list
        lstOutputs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ActionInterface item = (ActionInterface) parent.getItemAtPosition(position);

                boolean toggleTest = ActionHelper.toggleAction(item);
                boolean statusTest = ActionHelper.updateStatus(groups);

                // update list with new statusses
                if (toggleTest && statusTest) {
                    ArrayList<ActionInterface> items = new ArrayList<>();
                    items.addAll(item.getGroup().getItems());
                    adpOutputs.updateData(items);
                    adpOutputs.notifyDataSetChanged();
                }
            }
        });

        lstOutputs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ActionInterface item = (ActionInterface) parent.getItemAtPosition(position);
                showLongPressPopup(item);
                return true;
            }
        });

        btnLoadExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        checkUpdate();
    }

    private void checkUpdate() {
        new AppUpdater(this)
                .setUpdateFrom(UpdateFrom.GITHUB)
                .setGitHubUserAndRepo("svenjochems", "Domotica")
                .start();
    }

    private boolean showLongPressPopup(final ActionInterface item) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.popup_action);
        dialog.show();

        ListView actions = (ListView) dialog.findViewById(R.id.listPopup);
        actions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        setNfcDefault(item);
                        System.out.println("Set as default");
                        break;
                    case 1:
                        writeNfcTag(item);
                        System.out.println("Write tag");
                        break;
                }
                dialog.dismiss();
            }
        });

        return true;
    }

    private void setNfcDefault(ActionInterface item) {
        int module;
        if (item instanceof Output) {
            Output o = (Output) item;
            module = o.getModule().getAddress();

        } else if (item instanceof Mood) {
            //todo: mood object standard on module -1?
            module = -1;
        } else {
            return;
        }

        String pref = "" + module + "_" + item.getAddress() + "_" + item.getName();

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("nfc", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("default", pref);
        editor.apply();
        //todo: translation
        Toast.makeText(getApplicationContext(), "NFC default set: " + item.getName(), Toast.LENGTH_LONG).show();
    }

    private void writeNfcTag(ActionInterface item) {
        Intent nfcIntent = new Intent(MainActivity.this, NfcWriteActivity.class);
        nfcIntent.putExtra("item", item.getIdentifier());
        startActivity(nfcIntent);
    }


    private void onLoadFinished() {
        layoutLoad.setVisibility(View.GONE);
        layoutMain.setVisibility(View.VISIBLE);
        groups = application.getMgroups();
        adpGroups = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groups);
        lstGroups.setAdapter(adpGroups);
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
        } else if (id == R.id.action_exit) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
