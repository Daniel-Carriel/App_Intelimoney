package com.example.intelimoney.model;

import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.helper.DateCustom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Categoria {

    private String id;
    private String nome;

    public Categoria() {
    }

    public void salvarCategoriaDespesa(){
        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String id_usuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());

        DatabaseReference reference = ConfiguracaoFirebase.getFirebaseDatabase();
        reference.child("categorias")
                .child(id_usuario)
                .child("despesa")
                .push()
                .setValue(this);
    }

    public void salvarCategoriaReceita(){
        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String id_usuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());

        DatabaseReference reference = ConfiguracaoFirebase.getFirebaseDatabase();
        reference.child("categorias")
                .child(id_usuario)
                .child("receita")
                .push()
                .setValue(this);
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

}
