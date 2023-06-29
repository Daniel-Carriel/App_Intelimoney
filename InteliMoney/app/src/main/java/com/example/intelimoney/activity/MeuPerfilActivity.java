package com.example.intelimoney.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.intelimoney.R;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.ActivityMeuPerfilBinding;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class MeuPerfilActivity extends AppCompatActivity {

    private ActivityMeuPerfilBinding binding;

    private final FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

    private String nome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMeuPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Meu perfil");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.progressBar.setVisibility(View.GONE);

        recuperarUsuario();

        binding.btnSalvarAlteracao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nome = binding.nomeUsuario.getText().toString();
                if (nome.isEmpty()){
                    Toast.makeText(MeuPerfilActivity.this, "Nome não pode ser vazio.", Toast.LENGTH_SHORT).show();
                } else {
                    binding.progressBar.setVisibility(View.VISIBLE);

                    if ( atualizaDados() ){
                        ocultarTeclado();
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(MeuPerfilActivity.this, "Alteração salva com sucesso!", Toast.LENGTH_SHORT).show();
                    }else {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(MeuPerfilActivity.this, "Erro ao salvar as alterações.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private boolean atualizaDados() {

        try {

            String id_usuaio = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
            DatabaseReference resumoRef = firebaseRef.child("usuarios")
                    .child(id_usuaio);

            resumoRef.child("nome").setValue(nome);

            return true;
        }catch (Exception e){
            return false;
        }
    }

    private void recuperarUsuario(){
        String id_usuaio = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(id_usuaio);

        ValueEventListener valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);

                binding.nomeUsuario.setText(usuario.getNome());
                binding.emailUsuario.setText(usuario.getEmail());

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Oculta o teclado do dispositivo
    private void ocultarTeclado(){
        View view = this.getCurrentFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}