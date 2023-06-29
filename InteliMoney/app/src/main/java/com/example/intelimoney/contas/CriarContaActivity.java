package com.example.intelimoney.contas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.intelimoney.R;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.ActivityCriarContaBinding;
import com.example.intelimoney.model.Conta;
import com.google.firebase.database.DatabaseReference;

public class CriarContaActivity extends AppCompatActivity {

    private ActivityCriarContaBinding binding;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private Conta conta;
    private String nomeConta;
    private double saldo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCriarContaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Nova Conta");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);

        binding.fabCriarConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                criarConta();
            }
        });

    }

    private void criarConta() {
        if (validarCampos()) {
            conta = new Conta();

            conta.setNomeConta(nomeConta);
            conta.setSaldo(saldo / 100);

            conta.salvar();
            finish();
        }
    }

    private boolean validarCampos(){
        saldo     = (double) binding.editTextSaldoConta.getRawValue();
        nomeConta = binding.editTextNomeConta.getText().toString();

        if(saldo > 0){
            if(!nomeConta.isEmpty()){

                return true;

            } else {
                Toast.makeText(this, "Nome da conta vazio.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(this, "Saldo da conta deve ser superior a zero.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}