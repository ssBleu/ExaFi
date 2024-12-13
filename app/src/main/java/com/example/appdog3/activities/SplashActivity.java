package com.example.appdog3.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdog3.MainActivity;
import com.example.appdog3.R;
import android.content.Intent;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Usamos Handler para hacer una pausa antes de ir a la siguiente actividad
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Iniciar la MainActivity después de un delay de 3 segundos
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();  // Finaliza el SplashActivity para que no pueda volver atrás
            }
        }, 3000);  // 3000 milisegundos = 3 segundos
    }
}
