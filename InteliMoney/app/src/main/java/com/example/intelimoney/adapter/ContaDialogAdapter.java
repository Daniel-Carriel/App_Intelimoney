package com.example.intelimoney.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.intelimoney.R;
import com.example.intelimoney.model.Categoria;
import com.example.intelimoney.model.Conta;

import java.util.List;

public class ContaDialogAdapter extends RecyclerView.Adapter<ContaDialogAdapter.MyViewHolder> {

    private final List<String> contaSelecionada;
    private final List<Conta> listaContas;
    private OnClick onClick;

    public ContaDialogAdapter(List<String> contaSelecionada, List<Conta> listaContas, OnClick onClick) {
        this.contaSelecionada = contaSelecionada;
        this.listaContas = listaContas;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conta_dialog, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Conta conta = listaContas.get(position);

        holder.txtConta.setText(conta.getNomeConta());

        if(contaSelecionada.contains(conta.getId_conta())){
            holder.checkBoxConta.setChecked(true);
        }

        holder.itemView.setOnClickListener(view -> {
            onClick.onClickListener(conta);

            holder.checkBoxConta.setChecked(!holder.checkBoxConta.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return listaContas.size();
    }

    public interface OnClick{

        void onClickListener(Conta conta);
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView txtConta;
        public CheckBox checkBoxConta;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtConta = itemView.findViewById(R.id.txt_conta);
            checkBoxConta = itemView.findViewById(R.id.checkBoxConta);
        }
    }
}
