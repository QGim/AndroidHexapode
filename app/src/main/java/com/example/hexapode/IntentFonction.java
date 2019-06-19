package com.example.hexapode;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class IntentFonction extends AppCompatActivity {

    private Button modelidar;
    private Button testPattes;
    private Button rotate;
    private Button stop;
    private Handler mHandler;
    public  BluetoothSocket mmSocket;

    public InputStream mmInStream;
    public OutputStream mmOutStream;
    private byte[] mmBuffer;

    private final static int MESSAGE_READ = 2;
    private final static int MESSAGE_WRITE = 4;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intent_fonction);

        modelidar = findViewById(R.id.ml);
        testPattes = findViewById(R.id.testpattes);
        rotate = findViewById(R.id.rotate);
        stop = findViewById(R.id.stop);



        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                /*if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1)
                        btstatus.setText("Connected to Device: " + (String) (msg.obj));
                    else
                        btstatus.setText("Connection Failed");
                }*/
            }
        };

        /**
         * appel les fonction d'envoi de caractere lors du click sur les bouton de communication
         */
        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                write('D');
                //write('D');
            }
        });

        testPattes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                write('A');
                //write('A');
            }
        });

        modelidar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                write('B');
                //write('B');
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                write('C');
               // write('C');
            }
        });



    }

    public void write(char bytes) {
        try {
            // mmOutStream.write(bytes);
            mmOutStream.write(bytes);
            Message writtenMsg = mHandler.obtainMessage(
                    MESSAGE_WRITE,-1,-1,mmBuffer);
            writtenMsg.sendToTarget();

        } catch (IOException e) {
        }
    }



    /**
     * Thread de connexion
     */
    private class ConnectedThread extends Thread {


        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        /**
         * fonction run permettant la reception des data
         */
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }



    }

}


