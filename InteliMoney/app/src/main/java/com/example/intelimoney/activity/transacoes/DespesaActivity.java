package com.example.intelimoney.activity.transacoes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.intelimoney.R;
import com.example.intelimoney.adapter.CategoriaDialogAdapter;
import com.example.intelimoney.adapter.ContaDialogAdapter;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.ActivityDespesaBinding;
import com.example.intelimoney.databinding.DialogCriarCategoriaDespesaBinding;
import com.example.intelimoney.databinding.DialogListaCategoriasDespesaBinding;
import com.example.intelimoney.databinding.DialogListaContasBinding;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.helper.DateCustom;
import com.example.intelimoney.helper.DatePickerFragment;
import com.example.intelimoney.model.Categoria;
import com.example.intelimoney.model.Conta;
import com.example.intelimoney.model.Transacao;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DespesaActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, CategoriaDialogAdapter.OnClick, ContaDialogAdapter.OnClick{

    private ActivityDespesaBinding binding;
    private DialogListaCategoriasDespesaBinding dialogCategoriaBinding;
    private DialogListaContasBinding dialogContaBinding;
    private DialogCriarCategoriaDespesaBinding dialogCriarCategoriaDespesaBinding;

    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private final FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference categoriasRef, contaRef;

    private ValueEventListener valueEventListenerCategorias;
    private final List<Categoria> listaCategoria = new ArrayList<>();
    private final List<String> listaCategoriaSelecionada = new ArrayList<>();
    private final List<String> idsCategoriaSelecionada = new ArrayList<>();

    private ValueEventListener valueEventListenerContas;
    private final List<Conta> listaConta = new ArrayList<>();
    private final List<String> listaContaSelecionada = new ArrayList<>();
    private final List<String> idContaSelecionada = new ArrayList<>();

    private Transacao transacao;
    private String data, descricao, categoria, conta;
    private double valor;

    private double saldoTotal, saldoAtualizado;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDespesaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configToolbar();
        configClicks();

        binding.txtData.setText(DateCustom.dataAtual());
    }

    private void configClicks() {
        /* Configura os cliques feitos na DespesaActivity */

        binding.txtData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        binding.txtCategoriaDespesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                descricao = binding.editDescricaoDespesa.getText().toString();

                if (descricao.isEmpty()){
                    recuperaCategorias();
                    showDialogCategorias();
                }else {
                    ocultarTeclado();
                    recuperaCategorias();
                    showDialogCategorias();
                }
            }
        });

        binding.txtContaDespesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperaContas();
                showDialogConta();
            }
        });


        binding.fabAdicionarDespesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validarCampos()) {

                    String mesAnoAtual     = DateCustom.mesAnoEscolhido(data);
                    String mesAnoEscolhido = DateCustom.mesAnoEscolhido(DateCustom.dataAtual());

                    if (mesAnoAtual.equals(mesAnoEscolhido)){

                        transacao = new Transacao();
                        double despesaGerada = valor / 100;


                        transacao.setValor(despesaGerada);
                        transacao.setData(data);
                        transacao.setDescricao(descricao);
                        transacao.setIdCategoria(listaCategoriaSelecionada.get(0));
                        transacao.setIdConta(idContaSelecionada.get(0));
                        transacao.setTipo("d");

                        saldoAtualizado = saldoTotal - despesaGerada;
                        atualizaSaldoConta(saldoAtualizado);

                        transacao.salvarTransacao(data);

                        finish();
                    } else {
                        Toast.makeText(DespesaActivity.this, "Você pode apenas registrar despesas para o mês vigente.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void configRvCategorias(){
        dialogCategoriaBinding.rvCategorias.setLayoutManager(new LinearLayoutManager(this));
        dialogCategoriaBinding.rvCategorias.setHasFixedSize(true);
        CategoriaDialogAdapter categoriaDialogAdapter = new CategoriaDialogAdapter(idsCategoriaSelecionada, listaCategoria, this);
        dialogCategoriaBinding.rvCategorias.setAdapter(categoriaDialogAdapter);
    }

    private void showDialogCategorias(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        alertDialog.setCancelable(false);

        dialogCategoriaBinding = DialogListaCategoriasDespesaBinding
                .inflate(LayoutInflater.from(this));

        dialogCategoriaBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogCategoriaBinding.btnSalvar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogCategoriaBinding.btnCriarCaretgoriaDespesa.setOnClickListener(view -> {
            dialog.dismiss();
            showDialogCariarCategorias();
        });

        if(listaCategoria.isEmpty()){
            dialogCategoriaBinding.txtInfo.setText("Nenhuma categoria cadastrada");
        } else {
            dialogCategoriaBinding.txtInfo.setText("");
        }
        dialogCategoriaBinding.progressBar2.setVisibility(View.GONE);

        configRvCategorias();

        alertDialog.setView(dialogCategoriaBinding.getRoot());

        dialog = alertDialog.create();
        dialog.show();
    }

    private void showDialogCariarCategorias(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        alertDialog.setCancelable(false);

        dialogCriarCategoriaDespesaBinding = DialogCriarCategoriaDespesaBinding
                .inflate(LayoutInflater.from(this));

        dialogCriarCategoriaDespesaBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogCriarCategoriaDespesaBinding.btnSalvar.setOnClickListener(v -> {
            Categoria categoria = new Categoria();
            categoria.setNome(dialogCriarCategoriaDespesaBinding.nomeCategoriaDespesa.getText().toString());
            categoria.salvarCategoriaDespesa();

            Toast.makeText(this, "Categoria salva com sucesso!", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
            recuperaCategorias();
            showDialogCategorias();
        });

        alertDialog.setView(dialogCriarCategoriaDespesaBinding.getRoot());

        dialog = alertDialog.create();
        dialog.show();
    }


    private void recuperaCategorias(){
        String id_usuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        categoriasRef = firebaseRef.child("categorias")
                                   .child(id_usuario)
                                   .child("despesa");
        valueEventListenerCategorias = categoriasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    listaCategoria.clear();

                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Categoria categorias = ds.getValue(Categoria.class);
                        categorias.setId(ds.getKey());
                        listaCategoria.add(categorias);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onClickListener(Categoria categorias) {
        /* Método usado para recuperar a categoria selecionada no AlertDialog de categorias */

        if (!idsCategoriaSelecionada.contains(categorias.getId())){ // Adiciona

            idsCategoriaSelecionada.add(categorias.getId());
            listaCategoriaSelecionada.add(categorias.getNome());

        } else{  //Remove
            idsCategoriaSelecionada.remove(categorias.getId());
            listaCategoriaSelecionada.remove(categorias.getNome());
        }

        if (!listaCategoriaSelecionada.isEmpty()) {
            binding.txtCategoriaDespesa.setText(listaCategoriaSelecionada.get(0));
        }else {
            binding.txtCategoriaDespesa.setText("");
        }
    }

    private void configRvContas(){
        dialogContaBinding.rvContas.setLayoutManager(new LinearLayoutManager(this));
        dialogContaBinding.rvContas.setHasFixedSize(true);
        ContaDialogAdapter contaDialogAdapter = new ContaDialogAdapter(idContaSelecionada, listaConta, this);
        dialogContaBinding.rvContas.setAdapter(contaDialogAdapter);
    }

    private void showDialogConta(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        alertDialog.setCancelable(false);

        dialogContaBinding = DialogListaContasBinding
                .inflate(LayoutInflater.from(this));

        dialogContaBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogContaBinding.btnSalvar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        if(listaConta.isEmpty()){
            dialogContaBinding.txtInfo.setText("Nenhuma Conta cadastrada");
        } else {
            dialogContaBinding.txtInfo.setText("");
        }
        dialogContaBinding.progressBar3.setVisibility(View.GONE);

        configRvContas();

        alertDialog.setView(dialogContaBinding.getRoot());

        dialog = alertDialog.create();
        dialog.show();
    }

    private void recuperaContas(){
        String id_usuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        contaRef = firebaseRef.child("conta")
                .child(id_usuario);
        valueEventListenerContas = contaRef.addValueEventListener(new ValueEventListener() {
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

    public void onClickListener(Conta conta) {
        // Método usado para recuperar a conta selecionada no AlertDialog de contas

        if (!idContaSelecionada.contains(conta.getId_conta())){ // Adiciona

            idContaSelecionada.add(conta.getId_conta());
            listaContaSelecionada.add(conta.getNomeConta());

        } else{  //Remove
            idContaSelecionada.remove(conta.getId_conta());
            listaContaSelecionada.remove(conta.getNomeConta());
        }

        if (!listaContaSelecionada.isEmpty()) {
            binding.txtContaDespesa.setText(listaContaSelecionada.get(0));
        }else {
            binding.txtContaDespesa.setText("");
        }
    }

    private boolean validarCampos(){
        valor     = (double) binding.editValorDespesa.getRawValue();
        data      = binding.txtData.getText().toString();
        descricao = binding.editDescricaoDespesa.getText().toString();
        categoria = binding.txtCategoriaDespesa.getText().toString();
        conta     = binding.txtContaDespesa.getText().toString();

        if (valor > 0){
            if (!data.isEmpty()){
                if (!descricao.isEmpty()){
                    if (!categoria.isEmpty()){
                        if (!conta.isEmpty()){

                            return true;

                        } else {
                            Toast.makeText(this, "Conta vazia.", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    } else {
                        Toast.makeText(this, "Categoria vazia.", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else {
                    Toast.makeText(this, "Descrição vazia.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                Toast.makeText(this, "Data vazia.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(this, "Valor da despesa deve ser superior a zero.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void onDateSet(DatePicker datePicker, int ano, int mes, int dia) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, ano);
        calendar.set(Calendar.MONTH, mes);
        calendar.set(Calendar.DAY_OF_MONTH, dia);

        String dataFormatada = simpleDateFormat.format(calendar.getTime());

        binding.txtData.setText(dataFormatada);

    }

    private void atualizaSaldoConta(double saldo) {
        String id_usuaio = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference resumoRef = firebaseRef.child("conta")
                .child(id_usuaio)
                .child(listaConta.get(0).getId_conta());

        resumoRef.child("saldo").setValue(saldo);
    }


    private void configToolbar() {
        getSupportActionBar().setTitle("Nova Despesa");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
    }

    private void ocultarTeclado(){
        View view = this.getCurrentFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperaCategorias();
        recuperaContas();
    }
}