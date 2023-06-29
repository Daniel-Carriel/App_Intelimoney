package com.example.intelimoney.model;

import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.helper.Base64Custom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Objetivo implements Serializable {

    private String id_objetivo;
    private double valorTotal;
    private double valorDeposito;
    private String nomeObjetivo;
    private String data;
    private String descricao;
    private String atividade = "inativo";

    public Objetivo() {
    }

    public void salvar(){
        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        firebaseRef.child("objetivos")
                .child(idUsuario)
                .push()
                .setValue(this);
    }

    @Exclude
    public String getId_objetivo() {
        return id_objetivo;
    }

    public void setId_objetivo(String id_objetivo) {
        this.id_objetivo = id_objetivo;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public double getValorDeposito() {
        return valorDeposito;
    }

    public void setValorDeposito(double valorDeposito) {
        this.valorDeposito = valorDeposito;
    }

    public String getNomeObjetivo() {
        return nomeObjetivo;
    }

    public void setNomeObjetivo(String nomeObjetivo) {
        this.nomeObjetivo = nomeObjetivo;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getAtividade() {
        return atividade;
    }

    public void setAtividade(String atividade) {
        this.atividade = atividade;
    }
}
