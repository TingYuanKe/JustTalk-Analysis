package com.example.tingyuan.justtalkclient;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private ClientSocket client;
    private final int PORT = 7777;
    private boolean connectedAvailable = false;
    private String defaultName = "";
    private String defaultIP = "";
    public String dir_path_arguements = "";
    public File dir_arguements;
    public File file_arguements;

    private DataOutputStream out_timestamp;
    private DataOutputStream out_arguments;
    private DataInputStream in_arguments;

    private ToggleButton btn_record;

    private int timeStamp;
    private SeekBar skb_timeStamp;
    private TextView txv_timeStampValue;
    private TextView text;
    private TextView txv_log;
    private Handler handler;
    private int count;

    private boolean isRecording=false;
    private AudioUtil mAudioUtil;
    private static final int BUFFER_SIZE = 1024 * 2;
    private byte[] mBuffer;
    private File mAudioFile;
    private ExecutorService mExecutorService;
    private static final String TAG = "MainActivity";

    private Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        InitialDefaultName();
        ConnectCheck();
        inputName();

        //get time
        SimpleTimeZone pdt = new SimpleTimeZone(8 * 60 * 60 * 1000, "Asia/Taipei");
        calendar = new GregorianCalendar(pdt);

        mBuffer = new byte[BUFFER_SIZE];
        mAudioFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/record/"+defaultName+".pcm");
        mExecutorService = Executors.newSingleThreadExecutor();
        mAudioUtil = AudioUtil.getInstance(defaultName);

        text = (TextView)findViewById(R.id.txv_log);
        txv_timeStampValue = (TextView)findViewById(R.id.txv_timestampValue);
        skb_timeStamp = (SeekBar)findViewById(R.id.skb_timestamp);
        skb_timeStamp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i<10){
                    timeStamp = 10;
                }
                else{
                    timeStamp = i;
                }
                txv_timeStampValue.setText(String.valueOf(timeStamp));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        handler = new Handler();
        btn_record = (ToggleButton)findViewById(R.id.btn_record);
        btn_record.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    count=0;
                    handler.post(RecordingThread);
                    skb_timeStamp.setClickable(false);
                    text.setText(" ");
                    Log.d("start","start to send");
                }
                else{
                    isRecording=false;
                    mAudioUtil.stopRecord();
                    mAudioUtil.convertWavFile();
                    skb_timeStamp.setClickable(true);
                    handler.removeCallbacksAndMessages(null);

                }
            }
        });
    }
    private Runnable RecordingThread = new Runnable() {
        @Override
        public void run() {
            if(!isRecording) {
                StartRecording();

            }
            else{
                EndRecording();
                StartRecording();

            }
            handler.postDelayed(this,timeStamp*1000);
        }
    };
    //start recording wav file
    private void StartRecording(){
        isRecording = true;
        //set start timestamp
        Date trialTime = new Date();
        calendar.setTime(trialTime);
        String startTime = calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                calendar.get(Calendar.MINUTE) + ":" +
                calendar.get(Calendar.SECOND);
        text.append("Start time :" +startTime);

        //create timestamp file
        try {
            String dir_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record";
            File dir = new File(dir_path);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file_acc = new File(dir, "raw_acc_buffer.txt");
            if (file_acc.exists()) {
                file_acc.delete();
            }
            out_timestamp = new DataOutputStream(new FileOutputStream(file_acc, true));

            //write start timestamp
            out_timestamp.write(startTime.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        count++;
        mAudioUtil.recordData();
        mAudioUtil.startRecord();
        Log.d("start","start to send again");


    }
    //end recording wav file and send server
    private void EndRecording(){
        isRecording=false;
        //set end timestamp
        Date trialTime = new Date();
        calendar.setTime(trialTime);
        String endTime = calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                calendar.get(Calendar.MINUTE) + ":" +
                calendar.get(Calendar.SECOND);
        text.append("End time :" +endTime);

        //write end timestamp
        try {
            out_timestamp.write(endTime.getBytes());

        } catch(Exception e){
            e.printStackTrace();
        }
        //copy buffer and send
        String dir_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record";
        try (InputStream in = new FileInputStream(dir_path + "/raw_acc_buffer.txt")) {
            try (OutputStream out = new FileOutputStream(dir_path + "/raw_acc.txt")) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mAudioUtil.stopRecord();
        mAudioUtil.convertWavFile();

        //send file to server and close file
        try {
            //send file via socket
            client.sendAudioFile(AudioUtil.getWavFileDir(),dir_path,count);
            Log.d("output","sending 20sec wav");

            out_timestamp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        isRecording=true;
        count++;
        mAudioUtil.recordData();
        mAudioUtil.startRecord();

        //build a new connection


    }

    //initial default name and IP
    public void InitialDefaultName() {
        dir_path_arguements = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record";
        dir_arguements = new File(dir_path_arguements);
        if (!dir_arguements.exists()) {
            dir_arguements.mkdir();
        }
        //initial default name and IP
        file_arguements = new File(dir_arguements, "raw_arguements.txt");
        try {

            in_arguments = new DataInputStream(new FileInputStream(file_arguements));
            defaultName = in_arguments.readLine();
            defaultIP = in_arguments.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void inputName(){
        //建立一個POP OUT視窗要求使用者輸入User name
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AppTheme);
        builder.setCancelable(false);
        builder.setTitle("Please enter your name:");

        // 設定開始畫面Input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(defaultName);
        // 建立開始畫面name Button
        this.runOnUiThread(new Runnable() {
            public void run() {
                builder.setView(input)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                defaultName = input.getText().toString();
                                try {
                                    out_arguments = new DataOutputStream(new FileOutputStream(file_arguements, false));
                                    String nameTemp = (defaultName + "\n");
                                    out_arguments.write(nameTemp.getBytes());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .create().setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                    }
                });
                builder.show().getWindow().setLayout(800, 600);
            }
        });
    }
    protected void ConnectCheck(){
        //建立一個POP OUT視窗要求使用者輸入IP Address
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AppTheme);
        builder.setCancelable(false);
        builder.setTitle("Please enter server IP address");

        // 設定開始畫面Input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setText(defaultIP);
        // 建立開始畫面Connect Button
        this.runOnUiThread(new Runnable() {
            public void run() {
                builder.setView(input)
                        .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                defaultIP = input.getText().toString();
                                try {
                                    out_arguments = new DataOutputStream(new FileOutputStream(file_arguements, true));
                                    String ipTemp = (defaultIP + "\n");
                                    out_arguments.write(ipTemp.getBytes());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                //建立與client端地連線
                                connectClient();
                                //check if connected
                                if (connectedAvailable == false) //returns true if internet available
                                {
                                    Toast.makeText(MainActivity.this, "唉呦  好像沒有連上喔", Toast.LENGTH_LONG).show();
                                    ConnectCheck();
                                } else {
                                    Toast.makeText(MainActivity.this, "Connected showed from MainActivity!!", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .create().setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                    }
                });
                builder.show().getWindow().setLayout(800, 600);
            }
        });
    }
    private void connectClient() {
        //新增一個ClientSocket為client
        setClient(new ClientSocket(defaultIP, PORT, defaultName));
        //將client連  線設定為背景執行
        getClient().execute();
        connectedAvailable =true;
    }
    public void setClient(ClientSocket client) {
        this.client = client;
    }

    public ClientSocket getClient() {
        return client;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.disconnect();

    }
}
