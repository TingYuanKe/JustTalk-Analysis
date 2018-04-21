package com.example.tingyuan.justtalkclient;

/**
 * Created by TingYuanKe and Bajo on 2018/03/10.
 */

import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by TingYuanKeklle on 2018/03/10.
 */
public class ClientSocket extends AsyncTask<String, Void, String> {
    private String serverIP;
    private int serverPort;
    private String clientName;

    private Socket socket;

    public static boolean connected = false;

    public ClientSocket(String server, int port, String name) {
        serverIP = server;
        serverPort = port;
        clientName = name;
    }
    public void sendAudioFile(final String wavFileDir,final String pcmFileDir,final int count) {

        if (connected) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //sending name
                        OutputStream s = socket.getOutputStream();
                        DataOutputStream os=new DataOutputStream(s);
                        os.writeUTF(clientName+String.valueOf(count));
                        os.flush();
                        //sending Wav File
                        File wavFile = new File(wavFileDir);
                        byte[] mybytearrayWav = new byte[(int) wavFile.length()];
                        FileInputStream fis = new FileInputStream(wavFile);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        bis.read(mybytearrayWav, 0, mybytearrayWav.length);
                        System.out.println("Sending WAV:" + clientName + "(" + mybytearrayWav.length + " bytes)");
                        os.write(mybytearrayWav, 0, mybytearrayWav.length);

                        os.flush();


                      /*  File pcmFile = new File(pcmFileDir);
                        byte[] mybytearrayPCM = new byte[(int) pcmFile.length()];
                        FileInputStream fis2 = new FileInputStream(pcmFile);
                        BufferedInputStream bis2 = new BufferedInputStream(fis2);
                        bis2.read(mybytearrayPCM, 0, mybytearrayPCM.length);
                        System.out.println("Sending PCM:" + clientName + "(" + mybytearrayPCM.length + " bytes)");
                        os.write(mybytearrayPCM, 0, mybytearrayPCM.length);
                        os.flush();
                        */
                        System.out.println("Done.");

                        os.close();
                        bis.close();
                        fis.close();
                        socket.close();

                        //build new connection 
                        socket = new Socket(serverIP, serverPort);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (connected) {
                        socket.close();
                        connected = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void checkSocketConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!connected) {
                        socket = new Socket(serverIP, serverPort);
                        connected = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            System.out.println("Attempting to connect to " + serverIP + ":" + serverPort);
            socket = new Socket(serverIP, serverPort);
            connected = true;

        } catch (UnknownHostException uhe) {
            System.out.println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("Unexpected IO exception: " + ioe.getMessage());
        } catch (Exception fe) {
            System.out.println("Unexpected fatal exception: " + fe);
        }
        // TODO Auto-generated method stub
        return null;
    }

    public Socket getSocket() {
        return this.socket;
    }
}