package com.example.intelimoney.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.intelimoney.R;
import com.example.intelimoney.activity.QuadroDividasActivity;
import com.example.intelimoney.activity.objetivos.ObjetivoMostrarActivity;
import com.example.intelimoney.activity.objetivos.ObjetivoActivity;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.FragmentMaisBinding;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.model.Objetivo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MaisFragment extends Fragment {

    private FragmentMaisBinding binding;

    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private final FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private final List<Objetivo> listaObjetivos = new ArrayList<>();

    private TextView tituloFragment;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMaisBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        tituloFragment = view.findViewById(R.id.tituloFragment);

        return view;

    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configToolbar();
        recuperarObjetivo();

        binding.btnObjetivos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listaObjetivos.size() > 0){
                    startActivity(new Intent(getActivity(), ObjetivoMostrarActivity.class));
                }else{
                    startActivity(new Intent(getActivity(), ObjetivoActivity.class));
                }
            }
        });

        binding.btnQuadroDividas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), QuadroDividasActivity.class));

            }
        });

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

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }


    private void configToolbar() {
        tituloFragment.setText("Mais Opções");
    }
}