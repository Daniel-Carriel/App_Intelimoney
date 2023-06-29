package com.example.intelimoney.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.intelimoney.R;
import com.example.intelimoney.activity.PrincipalActivity;
import com.example.intelimoney.activity.autenticacao.LoginActivity;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        iniciarAplicativo();
    }

    private void iniciarAplicativo() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // Verifica se o usuario está logado
                auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
                if( auth.getCurrentUser() != null ){

                    // Se estiver logado, irá direto para a PrincipalActivity
                    irParaPrincipal();

                } else {

                    // Se não estiver logado, irá para a LoginActivity
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 3500);
    }

    private void irParaPrincipal() {
        startActivity(new Intent(this, PrincipalActivity.class));
        finish();
    }

}