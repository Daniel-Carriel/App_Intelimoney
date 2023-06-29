package com.example.intelimoney.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.intelimoney.R;
import com.example.intelimoney.activity.objetivos.ObjetivoDepositosActivity;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.DialogExcluirObjetivoBinding;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.helper.DateCustom;
import com.example.intelimoney.model.Objetivo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ObjetivoAdapter extends RecyclerView.Adapter<ObjetivoAdapter.ViewHolderObjetivo> {

    private DialogExcluirObjetivoBinding dialogExcluirObjetivoBinding;
    private AlertDialog dialog;

    private List<Objetivo> listaObjetivos;
    private Context context;

    private int porcentagemProgresso;
    private long diasRestantes;

    private String diaObjetivo, mesObjetivo, anoObjetivo;
    private String diaAtual, mesAtual, anoAtual;

    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private final FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private final NumberFormat nb = NumberFormat.getCurrencyInstance();

    public ObjetivoAdapter(List<Objetivo> listaObjetivos, Context context){
        this.listaObjetivos = listaObjetivos;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderObjetivo onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listaObjetivos = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adapter_objetivos, parent, false);

        return new ViewHolderObjetivo(listaObjetivos);
    }

    @SuppressLint({"RecyclerView", "SetTextI18n"})
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolderObjetivo holder, int position) {
        Objetivo objetivo = this.listaObjetivos.get(position);

        calcularProgresso(position);
        calcularDiasRestantes(position);
        configListenerSwitch(holder, position);

        exibirTrofeu(holder, porcentagemProgresso);

        holder.txtObjetivo.setText(objetivo.getNomeObjetivo());
        holder.txtDataObjetivo.setText(objetivo.getData());
        holder.txtValorFaltante.setText(nb.format(objetivo.getValorDeposito()) + " de " + nb.format(objetivo.getValorTotal()));
        holder.txtDiasRestantes.setText("Restam: "+ diasRestantes +" Dias");
        holder.porcentagemProgresso.setText(porcentagemProgresso + "%");
        holder.progressoObjetivo.setProgress(porcentagemProgresso);

        // Abre o menu de opções do objetivo
        holder.opcoes.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                int visible = holder.opecoesObjetivo.getVisibility();

                if (visible == 8) {
                    holder.opecoesObjetivo.setVisibility(View.VISIBLE);
                } else {
                    holder.opecoesObjetivo.setVisibility(View.GONE);

                }
            }
        });

        // Botão excluir do menu do objetivo
        holder.excluirObjetivo.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                holder.opecoesObjetivo.setVisibility(View.GONE);

                dialogExclusao(view, position);
            }
        });

        // Botão depositos do menu do objetivo
        holder.depositosObjetivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Objetivo objetivo = listaObjetivos.get(position);
                if (objetivo != null) {

                    Intent intent = new Intent(view.getContext(), ObjetivoDepositosActivity.class);
                    intent.putExtra("objetivo", objetivo);
                    context.startActivity(intent);
                }else {
                    Toast.makeText(context, "Estamos recuperando as informações", Toast.LENGTH_SHORT).show();
                }
            }
        });


        configSwitch(holder, objetivo);
    }

    @Override
    public int getItemCount() {
        return this.listaObjetivos.size();
    }

    public class ViewHolderObjetivo extends RecyclerView.ViewHolder{

        public TextView txtObjetivo, txtDiasRestantes, txtValorFaltante, porcentagemProgresso, txtDataObjetivo;
        public TextView excluirObjetivo, depositosObjetivo;
        public ProgressBar progressoObjetivo;
        public Switch switchAtividade;
        public View trofeu;
        public ImageView opcoes;
        public LinearLayout opecoesObjetivo;

        public TextView txtNomeObjetivo, txtValorEconomizado, txtValorTotal;


        public ViewHolderObjetivo(@NonNull View itemView) {
            super(itemView);

            // Adapter
            txtObjetivo          = itemView.findViewById(R.id.txtObjetivo);
            txtDiasRestantes     = itemView.findViewById(R.id.txtDiasRestantes);
            txtValorFaltante     = itemView.findViewById(R.id.txtValorFaltante);
            txtDataObjetivo      = itemView.findViewById(R.id.txtDataObjetivo);
            porcentagemProgresso = itemView.findViewById(R.id.porcentagemProgresso);
            progressoObjetivo    = itemView.findViewById(R.id.progressoObjetivo);
            switchAtividade      = itemView.findViewById(R.id.switchAtividade);
            trofeu               = itemView.findViewById(R.id.trofeu);
            opecoesObjetivo      = itemView.findViewById(R.id.menuObjetivo);
            opcoes               = itemView.findViewById(R.id.opcoesObjetivo);
            excluirObjetivo      = itemView.findViewById(R.id.excluirObjetivo);
            depositosObjetivo    = itemView.findViewById(R.id.depositosObjetivo);

            //Objetivo Mostrar
            txtNomeObjetivo     = itemView.findViewById(R.id.txtNomeObjetivo);
            txtValorEconomizado = itemView.findViewById(R.id.txtValorEconomizado);
            txtValorTotal       = itemView.findViewById(R.id.txtValorTotal);
        }
    }

    private void configSwitch(@NonNull ViewHolderObjetivo holder, Objetivo objetivo) {
        if (objetivo.getAtividade().equals("inativo")){
            holder.switchAtividade.setChecked(false);
        }else{
            holder.switchAtividade.setChecked(true);
        }
    }


    public void calcularProgresso(int position){
        Objetivo objetivo = this.listaObjetivos.get(position);

        double valorDeposito = objetivo.getValorDeposito();
        double valorTotalObjetivo = objetivo.getValorTotal();

        porcentagemProgresso = (int) ((valorDeposito / valorTotalObjetivo) * 100);

    }

    private void exibirTrofeu(@NonNull ViewHolderObjetivo holder, int porcentagemProgresso) {
        if (porcentagemProgresso == 100) {
            holder.trofeu.setVisibility(View.VISIBLE);
        }else {
            holder.trofeu.setVisibility(View.GONE);
        }

    }

    private void configListenerSwitch(@NonNull ViewHolderObjetivo holder, int position) {
        holder.switchAtividade.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                String atividade = "";
                if (isChecked){
                    atividade = "ativo";
                    atualizaAtividade(atividade, position);
                }else {
                    atividade = "inativo";
                    atualizaAtividade(atividade, position);
                    //context.recreate();
                }

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void calcularDiasRestantes(int position){

        separarDataObjetivo(position);
        separarDataAtual();

        // Calcula os dias restantes

        int dia = Integer.parseInt(diaAtual);
        int mes = Integer.parseInt(mesAtual);
        int ano = Integer.parseInt(anoAtual);

        int diaFinal = Integer.parseInt(diaObjetivo);
        int mesFinal = Integer.parseInt(mesObjetivo);
        int anoFinal = Integer.parseInt(anoObjetivo);


        LocalDate dataInicio = LocalDate.of(ano, mes, dia);
        LocalDate dataFim = LocalDate.of(anoFinal, mesFinal, diaFinal);

        diasRestantes = ChronoUnit.DAYS.between(dataInicio, dataFim);

    }

    private void separarDataAtual() {
        // Separa a data atual

        String data = DateCustom.dataAtual();
        String [] dataFormatada = data.split("/");

        diaAtual = dataFormatada[0];
        mesAtual = dataFormatada[1];
        anoAtual = dataFormatada[2];
    }

    private void separarDataObjetivo(int position) {
        Objetivo objetivo = this.listaObjetivos.get(position);

        // Separa a data do objetivo

        String dataObjetivo = objetivo.getData();
        String [] dataObjetivoFormatada = dataObjetivo.split("/");

        diaObjetivo = dataObjetivoFormatada[0];
        mesObjetivo = dataObjetivoFormatada[1];
        anoObjetivo = dataObjetivoFormatada[2];
    }

    public void atualizaAtividade( String atividade, int position){
        Objetivo objetivo = this.listaObjetivos.get(position);

        String id_usuaio = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference resumoRef = firebaseRef.child("objetivos")
                .child(id_usuaio)
                .child(objetivo.getId_objetivo());

        resumoRef.child("atividade").setValue(atividade);
    }

    private void excluirObjetivo(int position){
        Objetivo objetivo = listaObjetivos.get(position);

        String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference objetivoRef = firebaseRef.child("objetivos")
                .child(idUsuario);

        objetivoRef.child( objetivo.getId_objetivo() ).removeValue();
    }

    private void excluirDepositos(int position){
        Objetivo objetivo = listaObjetivos.get(position);

        String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference extratoRef = firebaseRef.child("extratoDepositosObjetivo")
                .child(idUsuario);

        extratoRef.child( objetivo.getId_objetivo() ).removeValue();
    }


    private void dialogExclusao(View view, int position){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext(), R.style.CustomAlertDialog);
        alertDialog.setCancelable(false);

        Objetivo objetivo = listaObjetivos.get(position);

        dialogExcluirObjetivoBinding = DialogExcluirObjetivoBinding
                .inflate(LayoutInflater.from(view.getContext()));

        dialogExcluirObjetivoBinding.btnFechar.setOnClickListener(v -> {
            Toast.makeText(context, "Cancelado", Toast.LENGTH_SHORT).show();
            this.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialogExcluirObjetivoBinding.btnSalvar.setOnClickListener(v -> {
            excluirObjetivo(position);
            excluirDepositos(position);
            Toast.makeText(context, "Objetivo excluído: " + objetivo.getNomeObjetivo() + ".", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        alertDialog.setView(dialogExcluirObjetivoBinding.getRoot());

        dialog = alertDialog.create();
        dialog.show();
    }


}
