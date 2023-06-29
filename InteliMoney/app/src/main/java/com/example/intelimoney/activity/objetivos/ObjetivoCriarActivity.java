package com.example.intelimoney.activity.objetivos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.intelimoney.databinding.ActivityObjetivoCriarBinding;
import com.example.intelimoney.helper.DatePickerFragment;
import com.example.intelimoney.model.Objetivo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ObjetivoCriarActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private ActivityObjetivoCriarBinding binding;

    private Objetivo objetivo;
    private String data, descricao, nome;
    private double valorTotal, deposito;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityObjetivoCriarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configToolBar();
        //configDados();

        binding.txtData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valorTotal = (double) binding.editValorObjetivo.getRawValue();
                nome       = binding.editNomeObjetivo.getText().toString();
                descricao  = binding.editDescricaoObjetivo.getText().toString();

                if (valorTotal > 0 || !nome.isEmpty() || !descricao.isEmpty()){

                    ocultarTeclado();
                    DialogFragment datePicker = new DatePickerFragment();
                    datePicker.show(getSupportFragmentManager(), "date picker");

                } else {
                    DialogFragment datePicker = new DatePickerFragment();
                    datePicker.show(getSupportFragmentManager(), "date picker");
                }
            }
        });

        binding.fabAddObjetivio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validarCampos()) {
                    Objetivo objetivo = new Objetivo();

                    objetivo.setValorTotal(valorTotal / 100);
                    objetivo.setValorDeposito(deposito / 100);
                    objetivo.setNomeObjetivo(nome);
                    objetivo.setData(data);
                    objetivo.setDescricao(descricao);

                    objetivo.salvar();

                    Toast.makeText(ObjetivoCriarActivity.this, "salvo", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(ObjetivoCriarActivity.this, ObjetivoMostrarActivity.class));
                    finish();

                }
            }
        });
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

    private boolean validarCampos(){
        valorTotal = (double) binding.editValorObjetivo.getRawValue();
        deposito   = (double) binding.editValorDeposito.getRawValue();
        nome       = binding.editNomeObjetivo.getText().toString();
        data       = binding.txtData.getText().toString();
        descricao  = binding.editDescricaoObjetivo.getText().toString();

        if (valorTotal > 0){
            if (!nome.isEmpty()){
                if (!data.isEmpty()){
                    if (!descricao.isEmpty()){

                       return true;

                    } else {
                        Toast.makeText(this, "Descrição vazia.", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else {
                    Toast.makeText(this, "Data vazia.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                Toast.makeText(this, "Nome do objetivo vazio.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(this, "Valor do objetivo deve ser superior a zero.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void ocultarTeclado(){
        View view = this.getCurrentFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }


    private void configToolBar() {
        getSupportActionBar().setTitle("Novo Objetivo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
    }
}