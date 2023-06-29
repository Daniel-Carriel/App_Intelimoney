package com.example.intelimoney.model;

import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.helper.DateCustom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class Divida {

    private String id_divida;
    private String nome;
    private String vencimento;
    private boolean pago;
    private double valor;

    public Divida() {
    }

    public void salvar(String dataSelecionada){
        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String id_usuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        String mesAno = DateCustom.mesAnoEscolhido(dataSelecionada);

        DatabaseReference reference = ConfiguracaoFirebase.getFirebaseDatabase();
        reference.child("dividas")
                .child(id_usuario)
                .child(mesAno)
                .push()
                .setValue(this);

    }

    public String getId_divida() {
        return id_divida;
    }

    public void setId_divida(String id_divida) {
        this.id_divida = id_divida;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getVencimento() {
        return vencimento;
    }

    public void setVencimento(String vencimento) {
        this.vencimento = vencimento;
    }

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
}
