package com.example.intelimoney.activity.objetivos;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.intelimoney.databinding.ActivityObjetivoConcluidoBinding;
import com.example.intelimoney.model.Objetivo;

public class ObjetivoConcluidoActivity extends AppCompatActivity {

    private ActivityObjetivoConcluidoBinding binding;
    private Objetivo objetivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityObjetivoConcluidoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mostrarObjetivoConcluido();
        configDados();

    }

    private void mostrarObjetivoConcluido() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(ObjetivoConcluidoActivity.this, ObjetivoMostrarActivity.class));
                finish();
                return;
            }
        }, 3000);
    }

    @SuppressLint("SetTextI18n")
    private void configDados() {
        this.objetivo = (Objetivo) getIntent().getSerializableExtra("objetivo");

        binding.txtFalarObjetivo.setText("Você concluiu o objetivo:\n"+ objetivo.getNomeObjetivo());

    }
}