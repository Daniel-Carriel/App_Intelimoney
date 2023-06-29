package com.example.intelimoney.activity.autenticacao;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.intelimoney.activity.PrincipalActivity;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.ActivityLoginBinding;
import com.example.intelimoney.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private FirebaseAuth autenticacao;

    private Usuario usuario;
    private String email;
    private String senha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBar.setVisibility(View.GONE);

        binding.btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ocultarTeclado();
                logarUsuario();
            }
        });

        binding.btnEsqueceuSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaRedefinirSenha();
            }
        });

        binding.btnCadastreSe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaCadastro();
            }
        });
    }

    private void logarUsuario() {
        if ( validarCampos() ){

            usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setSenha(senha);

            autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
            autenticacao.signInWithEmailAndPassword(
                    usuario.getEmail(), usuario.getSenha()
            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        binding.progressBar.setVisibility(View.VISIBLE);
                        irParaPrincipal();
                    }else{
                        String excecao = "";
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) { // Exceção usada quando o email do usuario não existe ou foi desabilitado
                            excecao = "Usuário não está cadastrado";
                        } catch (FirebaseAuthInvalidCredentialsException e) {  // Exceção usada quando e email não é válido
                            excecao = "Email e senha não correspondem a um usuário cadastrado";}
                        catch (Exception e){
                            excecao = "Erro ao logar usuário.";
                        }
                        Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private boolean validarCampos(){
        email = binding.editEmail.getText().toString();
        senha = binding.editSenha.getText().toString();

        if( !email.isEmpty()){
            if( !senha.isEmpty()){
                return true;
            }else {
                binding.editSenha.setError("Digite o senha.");
                return false;
            }
        }else {
            binding.editEmail.setError("Digite o email.");
            return false;
        }
    }

    // Oculta o teclado do dispositivo
    private void ocultarTeclado(){
        View view = this.getCurrentFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private void irParaCadastro() {
        startActivity(new Intent(LoginActivity.this, CadastroActivity.class));
    }

    private void irParaRedefinirSenha() {
        startActivity(new Intent(this, RedefinirSenhaActivity.class));
    }

    private void irParaPrincipal() {
        startActivity(new Intent(this, PrincipalActivity.class));
        finish();
    }

}