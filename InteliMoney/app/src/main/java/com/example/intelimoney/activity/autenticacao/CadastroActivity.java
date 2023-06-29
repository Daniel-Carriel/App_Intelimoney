package com.example.intelimoney.activity.autenticacao;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.intelimoney.activity.PrincipalActivity;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.ActivityCadastroBinding;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;

    private FirebaseAuth autenticacao;

    private String nome;
    private String email;
    private String senha;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Cadastro");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cadastrar();
            }
        });
    }

    private void cadastrar() {

        if (validarCampos()){

            usuario = new Usuario();

            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setSenha(senha);


            autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
            autenticacao.createUserWithEmailAndPassword(
                    usuario.getEmail(), usuario.getSenha()
            ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String id_usuario = Base64Custom.codificarBase64(usuario.getEmail());
                        usuario.setId_usuario(id_usuario);
                        usuario.salvar();
                        irParaPrincipal();
                    } else {
                        String excecao = "";
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e){    // Excecão usada quando a senha não é forte o suficiente
                            excecao = "Digite uma senha de 8 dígitos.";
                        } catch (FirebaseAuthInvalidCredentialsException e){  // Exceção usada quando e email não é válido
                            excecao = "Digite um email válido.";
                        } catch (FirebaseAuthUserCollisionException e) {   // Exceção usada quando o usuario já possui um cadastro no Firebase
                            excecao = "Esta conta já está cadastrada.";
                        } catch (Exception e){
                            excecao = "Erro ao realizar o cadastro.";
                        }
                        Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private boolean validarCampos(){
        nome  = binding.editNomeCadastro.getText().toString();
        email = binding.editEmailCadastro.getText().toString();
        senha = binding.editSenhaCadastro.getText().toString();

        if( !nome.isEmpty()){
            if( !email.isEmpty()){
                if( !senha.isEmpty()){
                    return true;
                }else {
                    binding.editSenhaCadastro.setError("Digite a senha.");
                    return false;
                }
            }else {
                binding.editEmailCadastro.setError("Digite o email.");
                return false;
            }
        }else {
            binding.editNomeCadastro.setError("Digite o nome.");
            return false;
        }
    }

    private void irParaPrincipal() {
        startActivity(new Intent(CadastroActivity.this, PrincipalActivity.class));
        finish();
    }
}