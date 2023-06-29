package com.example.intelimoney.contas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.intelimoney.R;
import com.example.intelimoney.adapter.ContaListaAdapter;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.ActivityMinhaContaListaBinding;
import com.example.intelimoney.databinding.DialogExcluirContaBinding;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.helper.DateCustom;
import com.example.intelimoney.model.Conta;
import com.example.intelimoney.model.Transacao;
import com.example.intelimoney.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MinhaContaListaActivity extends AppCompatActivity {

    private ActivityMinhaContaListaBinding binding;
    private DialogExcluirContaBinding dialogExcluirContaBinding;

    private final FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

    private ContaListaAdapter contaListaAdapter;
    private final List<Conta> listaConta = new ArrayList<>();
    private Conta conta;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinhaContaListaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configToolBar();
        recuperaConta();
        configRvListaConta();
        configClicks();
        swipe();

    }

    private void configClicks() {
        binding.fabCriarNovaConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listaConta.size() > 0){
                    Toast.makeText(MinhaContaListaActivity.this, "Impossível criar conta, pois você já tem uma conta criada.", Toast.LENGTH_SHORT).show();
                }else {
                    startActivity(new Intent(MinhaContaListaActivity.this, CriarContaActivity.class));
                }
            }
        });

    }

    private void configRvListaConta() {

        // CONFIGURAR ADAPTER
        contaListaAdapter = new ContaListaAdapter(listaConta, this);

        // CONFIGURAR O RECYCLERVIEW
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        binding.rvListaContas.setLayoutManager(layoutManager);
        binding.rvListaContas.setHasFixedSize(true);
        binding.rvListaContas.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        binding.rvListaContas.setAdapter(contaListaAdapter);
    }

    private void recuperaConta(){
        String id_usuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference contaRef = firebaseRef.child("conta").child(id_usuario);
        ValueEventListener valueEventListenerContas = contaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    listaConta.clear();

                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Conta conta = ds.getValue(Conta.class);
                        conta.setId_conta(ds.getKey());
                        listaConta.add(conta);
                    }

                    contaListaAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void excluirConta(RecyclerView.ViewHolder viewHolder ){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        alertDialog.setCancelable(false);

        dialogExcluirContaBinding = DialogExcluirContaBinding
                .inflate(LayoutInflater.from(this));

        dialogExcluirContaBinding.btnNao.setOnClickListener(v -> {
            Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show();
            contaListaAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialogExcluirContaBinding.btnSim.setOnClickListener(v -> {
            int position = viewHolder.getAdapterPosition(); // recupera a popsiçaõ do item arrastado
            conta = listaConta.get( position );  // passa os dados do item arrastado para a lista de contas

            String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
            DatabaseReference contaRef = firebaseRef.child("conta")
                    .child(idUsuario);

            contaRef.child( conta.getId_conta() ).removeValue();
            contaListaAdapter.notifyItemRemoved( position );

            excluirTransacoes();

            Toast.makeText(this, "Conta " + conta.getNomeConta() + " excluída.", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
            recreate();
        });

        alertDialog.setView(dialogExcluirContaBinding.getRoot());

        dialog = alertDialog.create();
        dialog.show();
    }

    private void swipe(){
        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags  = ItemTouchHelper.ACTION_STATE_IDLE;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags( dragFlags, swipeFlags );
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                excluirConta( viewHolder);
                recuperaConta();
            }
        };

        new ItemTouchHelper(itemTouch).attachToRecyclerView(binding.rvListaContas);
    }

    /*private void recuperarTransacoes() {
        String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        transacaoRef = firebaseRef.child("transacoes")
                .child(idUsuario)
                .child(mesAnoSelecionado);

        valueEventListenerTransacoes = transacaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaTransacoes.clear();
                double totalDespesa = 0, totalReceita = 0;

                for (DataSnapshot ds: snapshot.getChildren()){

                    Transacao transacao = ds.getValue(Transacao.class);
                    transacao.setId_transacao(ds.getKey());
                    listaTransacoes.add(transacao);

                    if (transacao.getTipo().equals("r")) {
                        totalReceita += transacao.getValor();
                    }

                    if (transacao.getTipo().equals("d")) {
                        totalDespesa += transacao.getValor();
                    }
                }


                binding.txtReceitaTotal.setText(nb.format(totalReceita));
                binding.txtDespesaTotal.setText(nb.format(totalDespesa));

                transacaoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

     */

    private void excluirTransacoes(){
        String mesAnoSelecionado = DateCustom.mesAnoEscolhido(DateCustom.dataAtual());
        String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference transacaoRef = firebaseRef.child("transacoes")
                .child(idUsuario)
                .child(mesAnoSelecionado);

        transacaoRef.removeValue();
    }



    private void configToolBar() {
        getSupportActionBar().setTitle("Minha Conta");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperaConta();
    }

}