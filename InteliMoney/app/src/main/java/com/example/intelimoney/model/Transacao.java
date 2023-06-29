package com.example.intelimoney.model;

import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.helper.DateCustom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Transacao {

    private String id_transacao;
    private String data;
    private String descricao;
    private String idCategoria;
    private String idConta;
    private String tipo;
    private double valor;
    private double despesaTotal = 0;
    private double receitaTotal = 0;

    public Transacao() {
    }

    public void salvarTransacao(String dataSelecionada){
        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String id_usuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        String mesAno = DateCustom.mesAnoEscolhido(dataSelecionada);

        DatabaseReference reference = ConfiguracaoFirebase.getFirebaseDatabase();
        reference.child("transacoes")
                .child(id_usuario)
                .child(mesAno)
                .push()
                .setValue(this);
    }


    @Exclude
    public String getId_transacao() {
        return id_transacao;
    }

    public void setId_transacao(String id_transacao) {
        this.id_transacao = id_transacao;
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

    public String getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getIdConta() {
        return idConta;
    }

    public void setIdConta(String idConta) {
        this.idConta = idConta;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

}
