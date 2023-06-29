package com.example.intelimoney.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.intelimoney.R;
import com.example.intelimoney.adapter.TransacaoAdapter;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.DialogExcluirTransacaoBinding;
import com.example.intelimoney.databinding.FragmentTransacaoBinding;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.model.Conta;
import com.example.intelimoney.model.Transacao;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class TransacaoFragment extends Fragment {

    private FragmentTransacaoBinding binding;
    private DialogExcluirTransacaoBinding dialogExcluirTransacaoBinding;
    private TextView tituloFragment;

    private final FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private DatabaseReference transacaoRef;
    private ValueEventListener valueEventListenerTransacoes;

    private final List<Transacao> listaTransacoes = new ArrayList<>();
    private TransacaoAdapter transacaoAdapter;
    private Transacao transacao;

    private String mesAnoSelecionado;

    private double despesaTotal, receitaTotal, saldoTotal;

    private final List<Conta> listaConta = new ArrayList<>();
    private final NumberFormat nb = NumberFormat.getCurrencyInstance();

    private AlertDialog dialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTransacaoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        tituloFragment = view.findViewById(R.id.tituloFragment);

        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configToolbar();
        configCalendarView();
        recuperarTransacoes();
        configRvTransacoes();
        swipe();
    }

    private void recuperarTransacoes() {
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


    private void configCalendarView() {
        CharSequence meses[]   = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

        binding.calendarView.setTitleMonths(meses);

        CalendarDay dataAtual = binding.calendarView.getCurrentDate();
        String mesSelecionado = String.format("%02d", (dataAtual.getMonth() + 1));
        mesAnoSelecionado = String.valueOf(mesSelecionado + "" +  dataAtual.getYear());

        binding.calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                String mesSelecionado = String.format("%02d", (date.getMonth() + 1));
                mesAnoSelecionado = String.valueOf(mesSelecionado + "" + date.getYear());

                // Recupera as movimentações do mês escolhido no calendario
                transacaoRef.removeEventListener( valueEventListenerTransacoes );
                recuperarTransacoes();
            }
        });
    }

    private void configRvTransacoes() {
        // Configurar adapter
        transacaoAdapter = new TransacaoAdapter(listaTransacoes, getContext());

        // Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.rvTransacoes.setLayoutManager(layoutManager);
        binding.rvTransacoes.setHasFixedSize(true);
        binding.rvTransacoes.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayout.VERTICAL));
        binding.rvTransacoes.setAdapter(transacaoAdapter);
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
                excluirTransacao( viewHolder);
            }
        };

        new ItemTouchHelper(itemTouch).attachToRecyclerView(binding.rvTransacoes);
    }

    private void excluirTransacao(RecyclerView.ViewHolder viewHolder ){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);

        alertDialog.setCancelable(false);

        dialogExcluirTransacaoBinding = DialogExcluirTransacaoBinding
                .inflate(LayoutInflater.from(getContext()));

        dialogExcluirTransacaoBinding.btnFechar.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Cancelado", Toast.LENGTH_SHORT).show();
            transacaoAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialogExcluirTransacaoBinding.btnSalvar.setOnClickListener(v -> {
            int position = viewHolder.getAdapterPosition(); // recupera a popsiçaõ do item arrastado
            transacao = listaTransacoes.get( position );  // passa os dadsos do item arrastado para a lista de movimentações
            
            String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
            DatabaseReference transacaoRef = firebaseRef.child("transacoes")
                    .child(idUsuario)
                    .child(mesAnoSelecionado);

            transacaoRef.child( transacao.getId_transacao() ).removeValue();
            transacaoAdapter.notifyItemRemoved( position );
            atualizarSaldo();  // Atualizará o saldo geral quando a transação for excluída
            atualizarResumoMes();  // Atualizará o resumo do mes a transação for excluída


            dialog.dismiss();
        });

        alertDialog.setView(dialogExcluirTransacaoBinding.getRoot());

        dialog = alertDialog.create();
        dialog.show();
    }

    private void atualizarResumoMes() {
        String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference resumoRef = firebaseRef.child("resumoMes")
                .child(idUsuario)
                .child(mesAnoSelecionado);

        if ( transacao.getTipo().equals("r") ) {

            receitaTotal -= transacao.getValor();
            resumoRef.child("receitaTotal").setValue(receitaTotal);

        }

        if ( transacao.getTipo().equals("d") ) {
            despesaTotal -= transacao.getValor();
            resumoRef.child("despesaTotal").setValue(despesaTotal);
        }
    }

    private void atualizarSaldo() {
        String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference contaRef = firebaseRef.child("conta")
                                                .child(idUsuario)
                                                .child(listaConta.get(0).getId_conta());

        if ( transacao.getTipo().equals("r") ) {

            saldoTotal -= transacao.getValor();
            contaRef.child("saldo").setValue(saldoTotal);

        }

        if ( transacao.getTipo().equals("d") ) {
            saldoTotal += transacao.getValor();
            contaRef.child("saldo").setValue(saldoTotal);
        }
    }

    private void recuperaContas(){
        String id_usuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference contaRef = firebaseRef.child("conta")
                .child(id_usuario);
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

                    saldoTotal = listaConta.get(0).getSaldo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void configToolbar() {
        tituloFragment.setText("Transações");
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarTransacoes();
        recuperaContas();
    }

    @Override
    public void onStop() {
        super.onStop();
        transacaoRef.removeEventListener(valueEventListenerTransacoes);
    }
}