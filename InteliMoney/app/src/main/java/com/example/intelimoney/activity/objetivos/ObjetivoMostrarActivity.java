package com.example.intelimoney.activity.objetivos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.intelimoney.adapter.ObjetivoAdapter;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.ActivityObjetivoMostrarBinding;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.model.Objetivo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjetivoMostrarActivity extends AppCompatActivity {

    private ActivityObjetivoMostrarBinding binding;
    private ObjetivoAdapter objetivoAdapter;

    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private final FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private final List<Objetivo> listaObjetivos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityObjetivoMostrarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Objetivos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        binding.txtNomeObjetivo.setText("");
        binding.txtValorEconomizado.setText("R$ 0,00");
        binding.txtValorTotal.setText("R$ 0,00");

        recuperarObjetivo();
        configRvObjetivos();


        binding.fabNovoObjetivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ObjetivoMostrarActivity.this, ObjetivoCriarActivity.class));
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void mostrarObjetivo(int position){
        Objetivo objetivo = listaObjetivos.get(position);

        NumberFormat nb = NumberFormat.getCurrencyInstance();

        if (objetivo.getAtividade().equals("ativo")){

            binding.txtNomeObjetivo.setText(objetivo.getNomeObjetivo());
            binding.txtValorEconomizado.setText(nb.format(objetivo.getValorDeposito()));
            binding.txtValorTotal.setText(nb.format(objetivo.getValorTotal()));

        }

        if (objetivo.getAtividade().equals("inativo")){

            binding.txtNomeObjetivo.setText("");
            binding.txtValorEconomizado.setText("R$ 0,00");
            binding.txtValorTotal.setText("R$ 0,00");

        }
    }

    @SuppressLint("SetTextI18n")
    private void ocultarObjetivo(int position){
        Objetivo objetivo = listaObjetivos.get(position);

        NumberFormat nb = NumberFormat.getCurrencyInstance();

        if (objetivo.getAtividade().equals("inativo")){

            binding.txtNomeObjetivo.setText("");
            binding.txtValorEconomizado.setText(nb.format("0"));
            binding.txtValorTotal.setText(nb.format("0"));

            Toast.makeText(this, "passei aqui tambem", Toast.LENGTH_SHORT).show();


        }
    }


    private void recuperarObjetivo(){

        String id_usuaio = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference resumoRef = firebaseRef.child("objetivos")
                .child(id_usuaio);

        ValueEventListener valueEventListenerObjetivo = resumoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    listaObjetivos.clear();

                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Objetivo objetivo = ds.getValue(Objetivo.class);
                        objetivo.setId_objetivo(ds.getKey());
                        listaObjetivos.add(objetivo);

                    }

                    for (int i = 0; i < listaObjetivos.size(); i++) {

                        String atividade = listaObjetivos.get(i).getAtividade();

                        if (atividade.equals("ativo")){
                            mostrarObjetivo(i);
                        }
                    }

                    Collections.reverse(listaObjetivos);

                    objetivoAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private void configRvObjetivos() {
        // Configurar adapter
        objetivoAdapter = new ObjetivoAdapter(listaObjetivos, this);

        // Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvObjetivos.setLayoutManager(layoutManager);
        binding.rvObjetivos.setHasFixedSize(true);
        binding.rvObjetivos.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        binding.rvObjetivos.setAdapter(objetivoAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarObjetivo();
        configRvObjetivos();
    }
}