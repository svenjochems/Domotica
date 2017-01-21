package be.jochems.sven.domotica.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.jochems.sven.domotica.Domotica;
import be.jochems.sven.domotica.R;
import be.jochems.sven.domotica.data.ActionInterface;
import be.jochems.sven.domotica.data.Group;
import be.jochems.sven.domotica.data.Output;

/**
 * The configuration screen for the {@link OutputWidget OutputWidget} AppWidget.
 */
public class OutputWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "be.jochems.sven.domotica.widget.OutputWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    private final byte BACKADDRESS = (byte)255;

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private ListView lstWidgetGroups;
    private ListView lstWidgetOutputs;
    private ArrayAdapter<Group> adpGroups;
    private ArrayAdapter<ActionInterface> adpOutputs;
    private Domotica application;

    public OutputWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveSettingsPref(Context context, int appWidgetId, int module, int address, String name) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + "MOD_" + appWidgetId, module);
        prefs.putInt(PREF_PREFIX_KEY + "ADD_" + appWidgetId, address);
        prefs.putString(PREF_PREFIX_KEY + "NAME_" + appWidgetId, name);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static int loadModulePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int module = prefs.getInt(PREF_PREFIX_KEY + "MOD_" + appWidgetId, Integer.MIN_VALUE);
        if (module != Integer.MIN_VALUE) {
            return module;
        } else {
            return 1;
        }
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static int loadAddrPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int address = prefs.getInt(PREF_PREFIX_KEY + "ADD_" + appWidgetId, Integer.MIN_VALUE);
        if (address != Integer.MIN_VALUE) {
            return address;
        } else {
            return 1;
        }
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadNamePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String name = prefs.getString(PREF_PREFIX_KEY + "NAME_" + appWidgetId, null);
        if (name != null) {
            return name;
        } else {
            return "error";
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.output_widget_configure);

        lstWidgetGroups = (ListView) findViewById(R.id.lstWidgetGroups);
        lstWidgetOutputs = (ListView) findViewById(R.id.lstWidgetOutputs);

        application = (Domotica)getApplicationContext();

        final List<Group> mgroups = application.getMgroups();

        adpGroups = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mgroups);
        lstWidgetGroups.setAdapter(adpGroups);

        lstWidgetGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lstWidgetGroups.setVisibility(View.GONE);

                ArrayList<ActionInterface> items = new ArrayList<>();
                items.addAll(mgroups.get(position).getItems());
                items.add(new Output(null, BACKADDRESS, null, getString(R.string.lstBack), 0));

                adpOutputs = new ArrayAdapter<>(OutputWidgetConfigureActivity.this, android.R.layout.simple_list_item_1, items);
                lstWidgetOutputs.setAdapter(adpOutputs);
                lstWidgetOutputs.setVisibility(View.VISIBLE);
            }
        });

        lstWidgetOutputs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ActionInterface item = (ActionInterface) parent.getItemAtPosition(position);
                if (item.getAddress() == BACKADDRESS) {
                    lstWidgetOutputs.setVisibility(View.GONE);
                    lstWidgetGroups.setVisibility(View.VISIBLE);
                } else {
                    int module = item instanceof Output ? ((Output) item).getModule().getAddress() : -1;
                    int address = item.getAddress();

                    final Context context = OutputWidgetConfigureActivity.this;

                    saveSettingsPref(context,mAppWidgetId,module,address, item.getName());

                    // It is the responsibility of the configuration activity to update the app widget
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    OutputWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

                    // Make sure we pass back the original appWidgetId
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    setResult(RESULT_OK, resultValue);
                    finish();
                }
            }
        });


        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
    }
}

