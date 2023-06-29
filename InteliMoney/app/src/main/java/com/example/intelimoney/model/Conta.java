package com.example.intelimoney.model;

import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.helper.Base64Custom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Conta {

    private String id_conta;
    private String nomeConta;
    private double saldo;

    public Conta() {
    }

    public void salvar(){
        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String id_usuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());

        DatabaseReference reference = ConfiguracaoFirebase.getFirebaseDatabase();
        reference.child("conta")
                .child(id_usuario)
                .push()
                .setValue(this);
    }

    @Exclude
    public String getId_conta() {
        return id_conta;
    }

    public void setId_conta(String id_conta) {
        this.id_conta = id_conta;
    }

    public String getNomeConta() {
        return nomeConta;
    }

    public void setNomeConta(String nomeConta) {
        this.nomeConta = nomeConta;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
}
