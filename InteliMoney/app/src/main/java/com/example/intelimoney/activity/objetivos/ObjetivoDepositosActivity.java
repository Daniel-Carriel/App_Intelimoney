package com.example.intelimoney.activity.objetivos;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.intelimoney.R;
import com.example.intelimoney.adapter.ExtratoDepositoAdapter;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.ActivityObjetivoDepositosBinding;
import com.example.intelimoney.databinding.DialogDepositoObjetivoBinding;
import com.example.intelimoney.databinding.DialogExcluirDepositoBinding;
import com.example.intelimoney.databinding.DialogRecusarDepositoObjetivoBinding;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.helper.DateCustom;
import com.example.intelimoney.helper.RecyclerItemClickListener;
import com.example.intelimoney.model.ExtratoDeposito;
import com.example.intelimoney.model.Objetivo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjetivoDepositosActivity extends AppCompatActivity {

    private ActivityObjetivoDepositosBinding binding;
    private DialogDepositoObjetivoBinding dialogDepositoObjetivoBinding;
    private DialogExcluirDepositoBinding dialogExcluirDepositoBinding;
    private DialogRecusarDepositoObjetivoBinding dialogRecusarDepositoBinding;

    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private final FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private final List<ExtratoDeposito> listaExtratoDepositos = new ArrayList<>();

    private final NumberFormat nb = NumberFormat.getCurrencyInstance();

    private ExtratoDepositoAdapter extratoDepositoAdapter;
    private Objetivo objetivo;
    private String diaObjetivo, mesObjetivo, anoObjetivo;
    private String diaAtual, mesAtual, anoAtual;
    double depositoTotal, deposito, depositoAtualizado;
    private int porcentagemProgresso;
    private long meses;

    private AlertDialog dialog;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityObjetivoDepositosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Objetivo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        configDados();
        recuperarExtratoDepositos();
        separarDataAtual();
        configRvExtratoDepositos();
        eventoClickRvExtratoDeposito();
        calcularProgresso(this.objetivo);
        exibirTrofeu();

        binding.btnAdicionarDeposito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calcularProgresso(objetivo);
                if (porcentagemProgresso == 100){
                    dialogRecusarDeposito();
                }else{
                    dialogDeposito(objetivo);
                }
            }
        });

        binding.btnConcluirObjetivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Calculando  a diferença entre os valores

                double valorTotalObjetivo = objetivo.getValorTotal();
                double valorAtualDepositos = objetivo.getValorDeposito();
                double diferenca = valorTotalObjetivo - valorAtualDepositos;

                atualizarDepositoTotal(objetivo, valorTotalObjetivo);
                registrarDeposito(diferenca);

                calcularProgresso(objetivo);
                exibirTrofeu();

                trofeuObjetivoConcluido();

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private void configDados(){
        this.objetivo = (Objetivo) getIntent().getSerializableExtra("objetivo");

        recuperarObjetivo();

        exibirDadosObjetivo(this.objetivo);
    }

    private void recuperarObjetivo(){

        String id_usuaio = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference resumoRef = firebaseRef.child("objetivos")
                .child(id_usuaio)
                .child(this.objetivo.getId_objetivo());

        ValueEventListener valueEventListenerObjetivo = resumoRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    Objetivo objetivo = snapshot.getValue(Objetivo.class);

                    separarDataObjetivo(objetivo);
                    calcularMesesRestantes();
                    calcularProgresso(objetivo);
                    calcularSugestaoDepositoMensal(objetivo);

                    exibirDadosObjetivo(objetivo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void exibirDadosObjetivo(Objetivo objetivo) {
        binding.txtNomeDataObjetivo.setText(objetivo.getDescricao() + " em: " + objetivo.getData());
        binding.txtValorAtualObjetivo.setText(nb.format(objetivo.getValorDeposito()) + " de " + nb.format(objetivo.getValorTotal()));

        binding.progressObjetivo.setProgress(porcentagemProgresso);
        binding.txtPorcentProgresso.setText(porcentagemProgresso + "%");

        binding.txtValorAtualObjetivo.setText(nb.format(objetivo.getValorDeposito()) + " de " + nb.format(objetivo.getValorTotal()));
    }

    private void recuperarExtratoDepositos() {
        String id_usuaio = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference extratoRef = firebaseRef.child("extratoDepositosObjetivo")
                .child(id_usuaio)
                .child(this.objetivo.getId_objetivo());


        ValueEventListener valueEventListenerExtrato = extratoRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    listaExtratoDepositos.clear();

                    for (DataSnapshot ds: snapshot.getChildren()) {
                        ExtratoDeposito extratoDeposito = ds.getValue(ExtratoDeposito.class);
                        extratoDeposito.setId_deposito(ds.getKey());
                        listaExtratoDepositos.add(extratoDeposito);
                    }

                    Collections.reverse(listaExtratoDepositos);
                    extratoDepositoAdapter.notifyDataSetChanged();

                } else {
                    int teste = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void configRvExtratoDepositos() {

        // Configurar adapter
        extratoDepositoAdapter = new ExtratoDepositoAdapter(listaExtratoDepositos, this);

        // Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvExtratoDepositos.setLayoutManager(layoutManager);
        binding.rvExtratoDepositos.setHasFixedSize(true);
        binding.rvExtratoDepositos.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        binding.rvExtratoDepositos.setAdapter(extratoDepositoAdapter);
    }

    private void eventoClickRvExtratoDeposito(){
        binding.rvExtratoDepositos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this, binding.rvExtratoDepositos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Toast.makeText(ObjetivoDepositosActivity.this, "Para excluir, segure na linha do deposito escolhido.", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                dialogExclusao(position);
                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }));
    }

    private void dialogExclusao(int position){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        alertDialog.setCancelable(false);

        ExtratoDeposito extratoDeposito = listaExtratoDepositos.get(position);

        dialogExcluirDepositoBinding = DialogExcluirDepositoBinding
                .inflate(LayoutInflater.from(this));

        dialogExcluirDepositoBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogExcluirDepositoBinding.btnSalvar.setOnClickListener(v -> {
            excluirDeposito(extratoDeposito.getId_deposito(), this.objetivo.getId_objetivo());
            recuperarExtratoDepositos();
            configRvExtratoDepositos();

            atualizarDepositosAposExclusao(extratoDeposito);

            calcularProgresso(this.objetivo);
            exibirTrofeu();

            Toast.makeText(this, "Deposito excluído com sucesso!", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        alertDialog.setView(dialogExcluirDepositoBinding.getRoot());

        dialog = alertDialog.create();
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void dialogDeposito(Objetivo objetivo){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        alertDialog.setCancelable(false);

        dialogDepositoObjetivoBinding = DialogDepositoObjetivoBinding
                .inflate(LayoutInflater.from(this));

        dialogDepositoObjetivoBinding.btnCalcelar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogDepositoObjetivoBinding.btnOk.setOnClickListener(v -> {
            depositoTotal      = objetivo.getValorDeposito();
            deposito           = (double) dialogDepositoObjetivoBinding.editDepositoObjetivo.getRawValue() / 100;
            depositoAtualizado = depositoTotal + deposito;

            atualizarDepositoTotal(objetivo, depositoAtualizado);
            Toast.makeText(this, "Salvo!", Toast.LENGTH_SHORT).show();

            registrarDeposito(deposito);
            recuperarObjetivo();
            recuperarExtratoDepositos();

            calcularProgresso(objetivo);
            exibirTrofeu();
            trofeuObjetivoConcluido();

            dialog.dismiss();
        });

        alertDialog.setView(dialogDepositoObjetivoBinding.getRoot());

        dialog = alertDialog.create();
        dialog.show();
    }

    private void dialogRecusarDeposito(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        alertDialog.setCancelable(false);

        dialogRecusarDepositoBinding = DialogRecusarDepositoObjetivoBinding
                .inflate(LayoutInflater.from(this));

        dialogRecusarDepositoBinding.buttonOk.setOnClickListener(v -> {
            dialog.dismiss();
        });

        alertDialog.setView(dialogRecusarDepositoBinding.getRoot());

        dialog = alertDialog.create();
        dialog.show();
    }

    private void registrarDeposito(double valorDeposito){
        ExtratoDeposito extratoDeposito = new ExtratoDeposito();

        extratoDeposito.setData(DateCustom.dataAtual());
        extratoDeposito.setValorDeposito(valorDeposito);

        extratoDeposito.salvar(this.objetivo.getId_objetivo());
    }

    private void atualizarDepositoTotal(Objetivo objetivo, double depositoAtualizado) {

        String id_usuaio = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference objetivoRef = firebaseRef.child("objetivos")
                .child(id_usuaio)
                .child(objetivo.getId_objetivo());

        objetivoRef.child("valorDeposito").setValue(depositoAtualizado);

        this.objetivo.setValorDeposito(depositoAtualizado);

    }

    private void atualizarDepositosAposExclusao(ExtratoDeposito extratoDeposito) {
        double depositoTotal = this.objetivo.getValorDeposito();
        double valorParaExclusao = extratoDeposito.getValorDeposito();
        double depositoTotalAtualizado = depositoTotal - valorParaExclusao;

        atualizarDepositoTotal(this.objetivo, depositoTotalAtualizado);
    }

    private void excluirDeposito(String id_deposito, String id_objetivo) {

        String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference extratoRef = firebaseRef.child("extratoDepositosObjetivo")
                .child(idUsuario)
                .child(id_objetivo);

        extratoRef.child( id_deposito ).removeValue();
    }

    public void calcularProgresso(Objetivo objetivo){

        double valorDeposito = objetivo.getValorDeposito();
        double valorTotalObjetivo = objetivo.getValorTotal();

        porcentagemProgresso = (int) ((valorDeposito / valorTotalObjetivo) * 100);

    }

    private void exibirTrofeu(){
        if (porcentagemProgresso >= 100) {
            binding.trofeu1.setVisibility(View.VISIBLE);
            binding.trofeu2.setVisibility(View.VISIBLE);
        }else {
            binding.trofeu1.setVisibility(View.GONE);
            binding.trofeu2.setVisibility(View.GONE);
        }
    }

    private void separarDataAtual() {
        String data = DateCustom.dataAtual();
        String [] dataFormatada = data.split("/");

        diaAtual = dataFormatada[0];
        mesAtual = dataFormatada[1];
        anoAtual = dataFormatada[2];
    }

    private void separarDataObjetivo(Objetivo objetivo) {
        String data = objetivo.getData();
        String [] dataFormatada = data.split("/");

        diaObjetivo = dataFormatada[0];
        mesObjetivo = dataFormatada[1];
        anoObjetivo = dataFormatada[2];
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void calcularMesesRestantes() {
        int diaAtual = Integer.parseInt(this.diaAtual);
        int mesAtual = Integer.parseInt(this.mesAtual);
        int anoAtual = Integer.parseInt(this.anoAtual);

        int diaObjetivo = Integer.parseInt(this.diaObjetivo);
        int mesObjetivo = Integer.parseInt(this.mesObjetivo);
        int anoObjetivo = Integer.parseInt(this.anoObjetivo);


        LocalDate dataInicio = LocalDate.of(anoAtual, mesAtual, diaAtual);
        LocalDate dataFim = LocalDate.of(anoObjetivo, mesObjetivo, diaObjetivo);

        meses = ChronoUnit.MONTHS.between(dataInicio, dataFim);
    }

    @SuppressLint("SetTextI18n")
    private void calcularSugestaoDepositoMensal(Objetivo objetivo){
        NumberFormat nb = NumberFormat.getCurrencyInstance();

        double totalObjetivo    = objetivo.getValorTotal();
        double totalEconomizado = objetivo.getValorDeposito();

        double diferenca = (totalObjetivo - totalEconomizado);

        double sugestaoDeEconomia = (diferenca / meses);

        binding.txtPouparPorMes.setText(nb.format(sugestaoDeEconomia));
    }

    private void trofeuObjetivoConcluido() {
        if (porcentagemProgresso == 100){

            Intent intent = new Intent(ObjetivoDepositosActivity.this, ObjetivoConcluidoActivity.class);
            intent.putExtra("objetivo", objetivo);
            startActivity(intent);
        }
    }

}