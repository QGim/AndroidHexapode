package com.example.hexapode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class IntentFonction extends AppCompatActivity {

    private Button modelidar;
    private Button testPattes;
    private Button rotate;
    private Button stop;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intent_fonction);

        modelidar = findViewById(R.id.ml);
        testPattes = findViewById(R.id.testpattes);
        rotate = findViewById(R.id.rotate);
        stop = findViewById(R.id.stop);
         final Intent i2 = new Intent();

        /**
         * appel les fonction d'envoi de caractere lors du click sur les bouton de communication
         */
        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentFonction.this.setResult('D', i2);
                //write('D');
            }
        });

        testPattes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentFonction.this.setResult('A', i2);
                //write('A');
            }
        });

        modelidar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentFonction.this.setResult('B', i2);
                //write('B');
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentFonction.this.setResult('C', i2);
               // write('C');
            }
        });



    }


}
