package com.example.bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StatusActivity2 extends AppCompatActivity {
    TextView mTvBluetoothStatus;
    TextView mTvReceiveData;
    TextView mTvSendData;
    TextView startFloor;
    TextView upFloor;
    TextView dwFloor;
    Button mBtnBluetoothOn;
    Button mBtnBluetoothOff;
    Button mBtnConnect;
    Button mBtnSendData;
    InputStream mInputStream;
    OutputStream mOutputStream;
    Thread mWorkerThread;
    int readBufferPositon;      //버퍼 내 수신 문자 저장 위치
    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;
    byte mDelimiter = 10;
    //private static final int REQUEST_ENABLE_BT = 3;
    //Set<BluetoothDevice> mDevices;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;
    private Thread workerThread = null; // 문자열 수신에 사용되는 쓰레드
    private int readBufferPosition; // 버퍼 내 문자 저장 위치
    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼
    private TextView textViewReceive; // 수신 된 데이터를 표시하기 위한 텍스트 뷰
    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림
    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        getSupportActionBar().hide();

        mTvBluetoothStatus = (TextView) findViewById(R.id.tvBluetoothStatus);
        mTvReceiveData = (TextView) findViewById(R.id.tvReceiveData);
        startFloor = (TextView) findViewById(R.id.start_floor);
        upFloor = (TextView) findViewById(R.id.upfloor);
        dwFloor  = (TextView) findViewById(R.id.dwfloor);
        //mTvSendData = (EditText) findViewById(R.id.tvSendData);
        mBtnBluetoothOn = (Button) findViewById(R.id.btnBluetoothOn);
        mBtnBluetoothOff = (Button) findViewById(R.id.btnBluetoothOff);
        mBtnConnect = (Button) findViewById(R.id.btnConnect);
        //mBtnSendData = (Button) findViewById(R.id.btnSendData);
        textViewReceive = (TextView)findViewById(R.id.tvReceiveData);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        mBtnBluetoothOn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOn();
            }
        });
        mBtnBluetoothOff.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOff();
            }
        });
        mBtnConnect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                listPairedDevices();
            }
        });
      /*  mBtnSendData.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mThreadConnectedBluetooth != null) {
                    mThreadConnectedBluetooth.write(mTvSendData.getText().toString());
                    //mTvSendData.setText("");
                }
            }
        });*/


        mBluetoothHandler = new Handler() {

            public void handleMessage(android.os.Message msg) {

                if (msg.what == BT_MESSAGE_READ) {
                    String readMessage = null;

                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    //int intVal= Integer.parseInt(readMessage);
                    mTvReceiveData.setText(readMessage);

                    int ardu_val = Integer.parseInt(readMessage);

                    if(ardu_val==1){
                        startFloor.setText("1 F");
                        upFloor.setText("2 F");
                        dwFloor.setText("- F");
                    }

                    else if(ardu_val==2){
                        startFloor.setText("2 F");
                        upFloor.setText("3 F");
                        dwFloor.setText("2 F");
                    }

                    else if(ardu_val==3){
                        startFloor.setText("3 F");
                        upFloor.setText("4 F");
                        dwFloor.setText("3 F");
                    }
                    else if (ardu_val==4){
                        startFloor.setText("2 F");
                        upFloor.setText("3 F");
                        dwFloor.setText("2 F");
                    }

                  else {
                        startFloor.setText("5 F");
                        upFloor.setText("5 F");
                        dwFloor.setText("5 F");
                    }
                    }
                }
            };


    }

    /*
    public void receiveData() {

        final Handler handler = new Handler();

        // 데이터를 수신하기 위한 버퍼를 생성

        readBufferPosition = 0;

        readBuffer = new byte[1024];



        // 데이터를 수신하기 위한 쓰레드 생성

        workerThread = new Thread(new Runnable() {

            @Override

            public void run() {

                while(Thread.currentThread().isInterrupted()) {

                    try {

                        // 데이터를 수신했는지 확인합니다.

                        int byteAvailable = inputStream.available();

                        // 데이터가 수신 된 경우

                        if(byteAvailable > 0) {

                            // 입력 스트림에서 바이트 단위로 읽어 옵니다.

                            byte[] bytes = new byte[byteAvailable];

                            inputStream.read(bytes);

                            // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.

                            for(int i = 0; i < byteAvailable; i++) {

                                byte tempByte = bytes[i];

                                // 개행문자를 기준으로 받음(한줄)

                                if(tempByte == '\n') {

                                    // readBuffer 배열을 encodedBytes로 복사

                                    byte[] encodedBytes = new byte[readBufferPosition];

                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                                    // 인코딩 된 바이트 배열을 문자열로 변환

                                    final String text = new String(encodedBytes, "US-ASCII");

                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {

                                        @Override

                                        public void run() {

                                            // 텍스트 뷰에 출력

                                            textViewReceive.append(text + "\n");
                                            if(textViewReceive.equals("a")){
                                                startFloor.setText("1 F");
                                                upFloor.setText("2 F");
                                                dwFloor.setText("- F");
                                            }

                                            else if(textViewReceive.equals("b")){
                                                startFloor.setText("2 F");
                                                upFloor.setText("3 F");
                                                dwFloor.setText("2 F");
                                            }
                                            else if(textViewReceive.equals("c")){
                                                startFloor.setText("3 F");
                                                upFloor.setText("4 F");
                                                dwFloor.setText("3 F");
                                            }
                                            else if (textViewReceive.equals("d")){
                                                startFloor.setText("2 F");
                                                upFloor.setText("3 F");
                                                dwFloor.setText("2 F");
                                            }

                                        }

                                    });

                                } // 개행 문자가 아닐 경우

                                else {

                                    readBuffer[readBufferPosition++] = tempByte;

                                }

                            }

                        }

                    } catch (IOException e) {

                        e.printStackTrace();

                    }

                    try {

                        // 1초마다 받아옴

                        Thread.sleep(1000);

                    } catch (InterruptedException e) {

                        e.printStackTrace();

                    }

                }

            }

        });

        workerThread.start();

    }
*/


    void bluetoothOn() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
                mTvBluetoothStatus.setText("활성화");
            } else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }

    void bluetoothOff() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show();
            mTvBluetoothStatus.setText("비활성화");
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_LONG).show();
                    mTvBluetoothStatus.setText("활성화");
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
                    Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();
                    mTvBluetoothStatus.setText("비활성화");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("장치 선택");

                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());


                        //TODO: 페어링 완료 되었을 시 status로 옮겨감
                        //startActivity(new Intent(StatusActivity.this, StatusActivity.class));
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    void connectSelectedDevice(String selectedDeviceName) {
        for (BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }


    }
}