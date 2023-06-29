package com.example.intelimoney.activity.objetivos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.intelimoney.databinding.ActivityObjetivoBinding;

public class ObjetivoActivity extends AppCompatActivity {

    private ActivityObjetivoBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityObjetivoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Objetivos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.btnCriarObjetivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ObjetivoActivity.this, ObjetivoCriarActivity.class));
            }
        });
    }

}