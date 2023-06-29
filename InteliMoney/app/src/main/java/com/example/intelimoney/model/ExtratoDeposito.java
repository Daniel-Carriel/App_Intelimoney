package com.example.intelimoney.model;

import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.helper.Base64Custom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class ExtratoDeposito {

    private String id_deposito;
    private String data;
    private double ValorDeposito;

    public ExtratoDeposito() {
    }

    public void salvar(String id_objetivo){
        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        firebaseRef.child("extratoDepositosObjetivo")
                .child(idUsuario)
                .child(id_objetivo)
                .push()
                .setValue(this);
    }

    @Exclude
    public String getId_deposito() {
        return id_deposito;
    }

    public void setId_deposito(String id_deposito) {
        this.id_deposito = id_deposito;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public double getValorDeposito() {
        return ValorDeposito;
    }

    public void setValorDeposito(double valorDeposito) {
        ValorDeposito = valorDeposito;
    }
}
