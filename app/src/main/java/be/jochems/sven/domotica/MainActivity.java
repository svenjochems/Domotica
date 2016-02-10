package be.jochems.sven.domotica;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Button      btnLamp;
    private TextView    text;

    private static int  numberOfModules = 2;
    private String[]    groups;
    private String[][]  outputs;
    private String[]    moods;

    private int[][]     outputIndex;
    private int[][]     outputIcon;

    Intent settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        settings = new Intent(this,SettingsActivity.class);

        text = (TextView) findViewById(R.id.txtReceive);

        btnLamp = (Button) findViewById(R.id.btnLamp);
        btnLamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importData();
                Log.d("Done","Done");
            }
        });



       /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    private void importData(){
        setGroups();
        setOutputs(numberOfModules);
        setMoods();
    }

    private void setGroups() {
        byte[] sendData = new byte[]{(byte)175, (byte)16 , (byte)8 , (byte)2 , (byte)24 , (byte)0 , (byte)32 , (byte)0
                , (byte)32 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)255 , (byte)175};
        byte[] receive;
        String[] groupArray = null;

        try {
            receive = new Network().execute(sendData).get();
            Log.d("Groups",Arrays.toString(receive));

            String groupsl = new String(receive);
            groupsl = groupsl.substring(0,groupsl.length() - 16);
            groupArray = new String[groupsl.length()/32];

            for(int i = 0; i < groupArray.length; i++){
                groupArray[i] = groupsl.substring(i*32, (i+1)*32).trim();
            }

            Log.d("Groups",groupsl);

        }catch (Exception e){
            e.printStackTrace();
        }

        groups = groupArray;
    }

    private void setOutputs(int numberOfModules){

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
                Log.d("Outputs",Arrays.toString(receive[i-1]));
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
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setMoods() {
        byte[] sendData = new byte[]{(byte) 175, (byte) 16, (byte) 8, (byte) 2, (byte) 12, (byte) 0, (byte) 32, (byte) 0
                , (byte) 32, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 175};
        byte[] receive;
        String[] moodsArray = null;

        try {
            receive = new Network().execute(sendData).get();
            Log.d("Moods", Arrays.toString(receive));

            String moodsl = new String(receive);
            moodsl = moodsl.substring(0, moodsl.length() - 16);
            moodsArray = new String[moodsl.length() / 32];

            for (int i = 0; i < moodsArray.length; i++) {
                moodsArray[i] = moodsl.substring(i * 32, (i + 1) * 32).trim();
            }

            Log.d("Moods", moodsl);

        } catch (Exception e) {
            e.printStackTrace();
        }

        moods = moodsArray;
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
            startActivity(settings);
            return true;
        } else if (id == R.id.action_exit){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    private class Network extends AsyncTask<byte[], String, byte[]>{

        @Override
        protected byte[] doInBackground(byte[]... params) {

            try {
                InetAddress address = InetAddress.getByName("192.168.0.177");
                int port = 10001;

                Socket socket = new Socket(address, port);

                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);

                dos.write(params[0]);

                InputStream in = socket.getInputStream();
                DataInputStream dis = new DataInputStream(in);

                //dis.read();
                int maxLines = 26;
                byte[] data = new byte[maxLines*16];

                byte[] end = new byte[16];
                Arrays.fill(end,(byte)255);

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

                Log.d("data", Arrays.toString(data));
                socket.close();

                byte[] result = Arrays.copyOfRange(data, 32, count * 16);
                Log.d("Recieved Data", Arrays.toString(result));

                return result;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }
}
