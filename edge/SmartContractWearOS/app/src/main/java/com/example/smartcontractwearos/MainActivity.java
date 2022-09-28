package com.example.smartcontractwearos;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.smartcontractwearos.databinding.ActivityMainBinding;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private TextView mTextView;
    private ActivityMainBinding binding;

    private static int TCP_SERVER_PORT = -1;
    private static String TCP_SERVER_HOST = null;

    private double AVERAGE_TIME = 0.0;
    private int COUNTER = 0;
    private int TIME = 30000;
    private boolean CONNECTED = false;
    private int CONFIGURATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTextView = binding.titleText;

        if (Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        binding.blockchainConf.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == true) {
                    binding.averageValue.setText("Blockchain");
                    CONFIGURATION = 0;
                }
            }
        });

        binding.listConf.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == true) {
                    binding.averageValue.setText("List");
                    CONFIGURATION = 1;
                }
            }
        });

        binding.connectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (CONNECTED == false) {
                    COUNTER = 0;
                    AVERAGE_TIME = 0.0;
                    long start = SystemClock.elapsedRealtime();

                    try {
                        TCP_SERVER_HOST = "192.168.2.35";
                        TCP_SERVER_PORT = 5000;

                        if (TCP_SERVER_HOST != null && TCP_SERVER_PORT != -1) {
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        long current = SystemClock.elapsedRealtime();
                                        while (current - start < TIME) {
                                            current = SystemClock.elapsedRealtime();
                                            if (CONFIGURATION == 0) {
                                                runTCPClient();
                                            } else if (CONFIGURATION == 1) {
                                                runAPIClient();
                                            }
                                        }

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                String tempString = "Average Time: " + AVERAGE_TIME / COUNTER;
                                                binding.averageValue.setText(tempString);
                                                CONNECTED = false;
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            thread.start();
                        } else {
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    binding.connectButton.setText("Connect");
                    CONNECTED = false;
                    finish();
                }
            }
        });

    }

    private void runTCPClient(){
        try {
            Socket s = new Socket(TCP_SERVER_HOST, TCP_SERVER_PORT);
            DataInputStream dataIn = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));

            long start = SystemClock.elapsedRealtime();
            String outMsg = "galaxy_cloud";
            dataOut.writeInt(outMsg.length());
            dataOut.writeBytes(outMsg);
            dataOut.flush();

            int length = dataIn.readInt();
            byte[] message = new byte[length];
            double server_time = 0.0;
            if(length > 0){
                dataIn.readFully(message, 0, message.length);
                server_time = Integer.parseInt(new String(message, StandardCharsets.UTF_8));
                Log.i("TCPClient", "length: " + (message.length - 1) + " value: " + server_time + " counter: " + COUNTER);
            }
            long end = SystemClock.elapsedRealtime();
            s.close();
            AVERAGE_TIME = AVERAGE_TIME + (end - start);
            COUNTER++;
        } catch (Exception e) {
            String tempString = "Error: " + e.toString();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.averageValue.setText(tempString);
                }
            });
            e.printStackTrace();
        }
    }

    public void runAPIClient(){
            try {
                long start = SystemClock.elapsedRealtime();
                URL requestURL = new URL("http://192.168.2.25:3000/list/request");
                HttpURLConnection conn1 = (HttpURLConnection) requestURL.openConnection();
                conn1.setRequestMethod("POST");
                conn1.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn1.setRequestProperty("Accept","application/json;charset=UTF-8");
                conn1.setRequestProperty("X-Api-Key", "");
                conn1.setDoOutput(true);
                conn1.setDoInput(true);

                JSONObject jsonRequestParam = new JSONObject();
                jsonRequestParam.put("source", "galaxy");
                jsonRequestParam.put("destination", "cloud");
                //Log.i("JSON", jsonParam.toString());
                DataOutputStream os1 = new DataOutputStream(conn1.getOutputStream());
                os1.writeBytes(jsonRequestParam.toString());

                os1.flush();
                os1.close();

                Log.i("REQUEST_STATUS", String.valueOf(conn1.getResponseCode()));
                //Log.i("MSG" , conn1.getResponseMessage());

                conn1.disconnect();

                URL sendURL = new URL("http://192.168.2.25:3000/list/send");
                HttpURLConnection conn2 = (HttpURLConnection) sendURL.openConnection();
                conn2.setRequestMethod("POST");
                conn2.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn2.setRequestProperty("Accept","application/json;charset=UTF-8");
                conn2.setRequestProperty("X-Api-Key", "");
                conn2.setDoOutput(true);
                conn2.setDoInput(true);

                JSONObject jsonSendParam = new JSONObject();
                jsonSendParam.put("source", "galaxy");
                jsonSendParam.put("destination", "cloud");
                jsonSendParam.put("password", "temp");
                //Log.i("JSON", jsonParam.toString());
                DataOutputStream os2 = new DataOutputStream(conn2.getOutputStream());
                os2.writeBytes(jsonSendParam.toString());

                os2.flush();
                os2.close();

                Log.i("SEND_STATUS", String.valueOf(conn2.getResponseCode()));
                //Log.i("MSG" , conn2.getResponseMessage());

                conn2.disconnect();
                long end = SystemClock.elapsedRealtime();
                AVERAGE_TIME = AVERAGE_TIME + (end - start);
                COUNTER++;
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    String tempString = "Error: " + e.toString();
                    @Override
                    public void run() {
                        binding.averageValue.setText(tempString);
                    }
                });
            }
    }


}