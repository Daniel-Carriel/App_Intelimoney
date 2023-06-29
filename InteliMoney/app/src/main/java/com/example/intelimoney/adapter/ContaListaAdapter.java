package com.example.intelimoney.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.intelimoney.R;
import com.example.intelimoney.model.Conta;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class ContaListaAdapter extends RecyclerView.Adapter<ContaListaAdapter.ViewHolderConta>{

    private List<Conta> listaContas;
    private Context context;

    public ContaListaAdapter(List<Conta> listaContas, Context context){
        this.listaContas = listaContas;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolderConta onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View listaContas = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lista_conta, parent, false);

        return new ViewHolderConta(listaContas);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderConta holder, int position) {

        NumberFormat nb = NumberFormat.getCurrencyInstance();

        Conta conta = listaContas.get(position);

        holder.txtNomeConta.setText(conta.getNomeConta());
        holder.txtValorConta.setText(nb.format(conta.getSaldo()));
    }

    @Override
    public int getItemCount() {
        return this.listaContas.size();
    }

    public class ViewHolderConta extends RecyclerView.ViewHolder{

        public TextView txtNomeConta, txtValorConta;

        public ViewHolderConta(@NonNull View itemView) {
            super(itemView);

            txtNomeConta  = itemView.findViewById(R.id.txtNomeConta);
            txtValorConta = itemView.findViewById(R.id.txtSaldoConta);
        }
    }
}
