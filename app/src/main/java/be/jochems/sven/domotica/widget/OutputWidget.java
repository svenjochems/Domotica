package be.jochems.sven.domotica.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import be.jochems.sven.domotica.connection.Connection;
import be.jochems.sven.domotica.R;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OutputWidgetConfigureActivity OutputWidgetConfigureActivity}
 */
public class OutputWidget extends AppWidgetProvider {
    private static final String OUTPUT_ACTION = "toggleOutput_";
    private static final String MOOD_ACTION = "toggleMood_";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        int module = OutputWidgetConfigureActivity.loadModulePref(context, appWidgetId);
        int address = OutputWidgetConfigureActivity.loadAddrPref(context, appWidgetId);
        String name = OutputWidgetConfigureActivity.loadNamePref(context, appWidgetId);

        Intent intent = new Intent(context, OutputWidget.class);

        String action = module != -1 ? OUTPUT_ACTION + module + "_" + address : MOOD_ACTION + address;

        intent.setAction(action);
        intent.putExtra("name", name);
        PendingIntent pendingIntent =  PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.output_widget);
        views.setTextViewText(R.id.appwidget_text, name);
        views.setOnClickPendingIntent(R.id.appwidget_button, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            OutputWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().startsWith(OUTPUT_ACTION)) {
            Connection con = new Connection(context);

            String[] actions = intent.getAction().split("_");
            int module = Integer.parseInt(actions[1]);
            int address = Integer.parseInt(actions[2]);

            Log.d("Toggle", "module:" + module + ", address:" + address);
            con.toggleOutput(module, address);

            byte[][] status = con.getStatus();
            byte toggleStatus = status[module - 1][address];

            String name = intent.getStringExtra("name");
            String on = context.getString(R.string.widget_state_on);
            String off = context.getString(R.string.widget_state_off);
            String toastText = context.getString(R.string.widget_toggle_toast, name, toggleStatus == 1 ? on : off);

            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().startsWith(MOOD_ACTION)){
            Connection con = new Connection(context);

            String[] actions = intent.getAction().split("_");
            int address = Integer.parseInt(actions[1]);

            Log.d("Toggle mood", "address:" + address);
            con.toggleMood(address);

            String name = intent.getStringExtra("name");
            String toastText = context.getString(R.string.widget_mood_toast, name);

            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
        }

        super.onReceive(context, intent);
    }
}

