package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private TextView inputTxt;
    private TextView outputTxt;
    private Button btnSend;
    private Button btnCalculate;

    //like a message queue. Post a msg to it and it will eventually process it by calling run() method
    //and passing the message to it
    private Handler msgHandler = new Handler();
    private PrimeThread primeThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //getting all items
        this.inputTxt = (EditText) findViewById(R.id.inputTxt);
        this.outputTxt = (TextView) findViewById(R.id.outputTxt);
        this.btnSend = (Button) findViewById(R.id.btnSend);
        this.btnCalculate = (Button) findViewById(R.id.btnCalculate);


    }

    // had to add <uses-permission android:name="android.permission.INTERNET" /> in AndroidManifest
    public void communicateWithServer(View view) {
        primeThread = new PrimeThread();
        primeThread.start();
        try {
            primeThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void calculate(View view) {
        int result = 0;
        if (inputTxt != null && inputTxt.toString() != "") {
            String inputString = inputTxt.getText().toString();
            for(int i = 0; i< inputTxt.length();i++){
                result+= Integer.parseInt(Character.toString(inputString.charAt(i)));
            }
        }else{
            Toast.makeText(MainActivity.this,"Ungültige Matrikelnummer", Toast.LENGTH_LONG);
        }
        outputTxt.setText(Integer.toBinaryString(result));
    }

    // done here, so that "this.inputTxt" can be used
    class PrimeThread extends Thread {
        private String inputMsg = inputTxt.getText().toString();
        private String outputMsg = "";

        @Override
        public void run() {
            BufferedReader bufferedReader;
            Socket socket;
            DataOutputStream dataOutputStream;

            try {
                //create client socket to connect to server
                socket = new Socket("se2-isys.aau.at", 53212);
                //create output stream attached to socket
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                //create user input stream
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //to avoid errors if-condition
                if (inputMsg != null && inputMsg != "") {
                    //send line to server
                    dataOutputStream.writeBytes(inputMsg + '\n');
                } else {
                    //like an alert in js but takes (context, String, displaylengthparam.)
                    Toast.makeText(MainActivity.this, "The input-text is null", Toast.LENGTH_LONG);
                }

                //read line from server
                outputMsg = bufferedReader.readLine();

                // Causes the Runnable to be added to the message queue. The runnable will be run on the thread to which this handler is attached
                msgHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        outputTxt.setText(outputMsg);
                    }
                });

                socket.close();

            } //recommended by tutorial to be added (in case server does not exist)
            catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.btnSend);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}