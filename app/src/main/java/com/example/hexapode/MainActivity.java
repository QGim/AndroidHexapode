/**
 * @version final
 * @auteur quentin GIMONNET
 */

package com.example.hexapode;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

/**
 * Class principal
 */
public class MainActivity extends AppCompatActivity {


    /**
     * déclaration de l'interface
     */
    private BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> ArrayAdapter;
    private Button active;
    private Button desactive;
    private Button exist;
    private Button recherche;
    private Button modelidar;
    private Button testPattes;
    private Button rotate;
    private Button stop;
    private Button deappair;

    private TextView btstatus;
    private ListView ListViewDevice;
    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null;


        private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
        private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
        private final static int CONNECTING_STATUS = 3;
        private final static int MESSAGE_WRITE = 4;


    public  BluetoothSocket mmSocket;
    public InputStream mmInStream;
    public OutputStream mmOutStream;
    private byte[] mmBuffer;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier


    /**
     * permet de seconnecter avec l'interface
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        active = findViewById(R.id.ON);
        desactive = findViewById(R.id.OFF);
        exist = findViewById(R.id.Exist);
        recherche = findViewById(R.id.Recherche);
        btstatus = findViewById(R.id.status);
        modelidar = findViewById(R.id.ml);
        testPattes = findViewById(R.id.testpattes);
        rotate = findViewById(R.id.rotate);
        stop = findViewById(R.id.stop);
        deappair = findViewById(R.id.deappairer);


        ListViewDevice = findViewById(R.id.listdevice);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        ListViewDevice.setAdapter(ArrayAdapter);
        ListViewDevice.setOnItemClickListener(DeviceClickListener);


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

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1)
                        btstatus.setText("Connected to Device: " + (String) (msg.obj));
                    else
                        btstatus.setText("Connection Failed");
                }
            }
        };


        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "vous n'avez pas de bluetooth", Toast.LENGTH_LONG).show();
            btstatus.setText("Status: Bluetooth not found");
        }else{

        }

        /**
         * fonction des differents boutons
         */
        active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOn(view);
            }
        });

        desactive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothOff(v);
            }
        });

        exist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listPairedDevices(v);
            }
        });

        recherche.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discover(v);
            }
        });

        /**
         * appel les fonction d'envoi de caractere lors du click sur les bouton de communication
         */
        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                write('D');
            }
        });

        testPattes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                write('A');
            }
        });

        modelidar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                write('B');
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                write('C');
            }
        });

        deappair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                write('B');
                modelidar.setVisibility(View.INVISIBLE);
                testPattes.setVisibility(View.INVISIBLE);
                rotate.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.INVISIBLE);
                btstatus.setText("Bluetooth enabled");
                cancel();

            }
        });


    }


    /**
     * fonction d'allumage du bluetooth
     * @param view
     */
    private void bluetoothOn(View view) {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            btstatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                btstatus.setText("Enabled");
            } else
                btstatus.setText("Disabled");
        }
    }


    /**
     * fonction d'extinction du bluetooth
     * @param view
     */
    private void bluetoothOff(View view) {
        bluetoothAdapter.disable(); // turn off
        btstatus.setText("Bluetooth disabled");
        ArrayAdapter.clear();
        Toast.makeText(getApplicationContext(), "Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }


    /**
     * fonction permettant de rechercher les appareil bluetooth
     * autour de soi et de les afficher dans la list
     * @param view
     */
    private void discover(View view) {
        // Check if the device is already discovering
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "Discovery stopped", Toast.LENGTH_SHORT).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {
                ArrayAdapter.clear(); // clear items
                bluetoothAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Brodcast recever permet de verifier si le bluetooth est connecter
     */
    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //ArrayAdapter.clear();
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                ArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                ArrayAdapter.notifyDataSetChanged();
                ListViewDevice.setAdapter(ArrayAdapter);
            }
        }
    };


    /**
     * vas afficher dans la list les appareil bluetooth deja associer mais non appairer
     * @param view
     */
    private void listPairedDevices(View view) {
        ArrayAdapter.clear();
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (bluetoothAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : pairedDevices)
                ArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            ListViewDevice.setAdapter(ArrayAdapter);

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();

    }

    /**
     * fonction de connexion lors du click
     */
    private AdapterView.OnItemClickListener DeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }
            btstatus.setText("Connecting...");
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0, info.length() - 17);


            new Thread() {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();

                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                    if (fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name).sendToTarget();
                    }
                }
            }.start();
            modelidar.setVisibility(View.VISIBLE);
            testPattes.setVisibility(View.VISIBLE);
            rotate.setVisibility(View.VISIBLE);
            stop.setVisibility(View.VISIBLE);
        }

    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);

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

    /**
     *fonction write permettant permettant d'envoyer des octets a l'appareil cible
     * @param bytes
     */
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
     * fonction cancel permet de desappairer l'apareil esclave.
     */
    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
        }
    }

}

