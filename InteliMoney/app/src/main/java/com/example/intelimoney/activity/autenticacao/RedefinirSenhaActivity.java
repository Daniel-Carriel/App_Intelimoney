package com.example.intelimoney.activity.autenticacao;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.ActivityRedefinirSenhaBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class RedefinirSenhaActivity extends AppCompatActivity {

    private ActivityRedefinirSenhaBinding binding;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRedefinirSenhaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBar.setVisibility(View.GONE);

        getSupportActionBar().setTitle("Redefinir Senha");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.btnEnviarEmailRS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redefinirSenha();
            }
        });
    }

    private void redefinirSenha() {
        
        String email = binding.editEmailRedefinirSenha.getText().toString();

        if (!email.isEmpty()){

            auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
            auth.sendPasswordResetEmail( email ).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    alertEmailEnviado();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String erro = e.toString();

                    opcoesDeErro(erro);
                }
            });

        }else {
        binding.editEmailRedefinirSenha.setError("Digite o email para redefinição de senha");
        }
    }

    private void alertEmailEnviado(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Email enviado!");
        alertDialog.setMessage("Enviamos um link para o email cadastrado para redefinição de senha.");
        alertDialog.setCancelable(false);

        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                irParaLogin();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();

    }

    private void opcoesDeErro(String erro){
        if (erro.equals("com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The email address is badly formatted.")){

            Toast.makeText(getBaseContext(), "Email inválido", Toast.LENGTH_SHORT).show();

        }else if (erro.equals("com.google.firebase.auth.FirebaseAuthInvalidUserException: There is no user record corresponding to this identifier. The user may have been deleted.")){

            Toast.makeText(getBaseContext(), "Email não cadastrado.", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getBaseContext(), erro, Toast.LENGTH_SHORT).show();

        }

    }

    private void irParaLogin(){
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}










