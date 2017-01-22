package be.jochems.sven.domotica.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import be.jochems.sven.domotica.Domotica;
import be.jochems.sven.domotica.R;

public class AppLoadActivity extends AppCompatActivity implements OnTaskComplete{
    private Domotica application;

    // widgets;
    ProgressBar progress;
    TextView txtLoad;
    Button btnExit ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_load);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = (ProgressBar) findViewById(R.id.progLoad);
        txtLoad     = (TextView) findViewById(R.id.txtLoad);
        btnExit       = (Button) findViewById(R.id.btnExit);

        application = (Domotica)getApplicationContext();

        application.init(this);

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onTaskCompleted(Object success) {
        if ((boolean) success) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            txtLoad.setText(R.string.error_connection);
            btnExit.setVisibility(View.VISIBLE);
        }
    }
}
