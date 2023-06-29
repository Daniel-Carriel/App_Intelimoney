package com.example.intelimoney.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.intelimoney.R;
import com.example.intelimoney.model.ExtratoDeposito;

import java.text.NumberFormat;
import java.util.List;

public class ExtratoDepositoAdapter extends RecyclerView.Adapter<ExtratoDepositoAdapter.ViewHolderExtratoDepositos> {

    private List<ExtratoDeposito> listaExtratoDepositos;
    private Context context;

    public ExtratoDepositoAdapter(List<ExtratoDeposito> listaExtratoDepositos, Context context){
        this.listaExtratoDepositos = listaExtratoDepositos;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderExtratoDepositos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listaDepositos = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lista_extrato_depositos, parent, false);

        return new ExtratoDepositoAdapter.ViewHolderExtratoDepositos(listaDepositos);
    }

    @Override
    public void onBindViewHolder(@NonNull ExtratoDepositoAdapter.ViewHolderExtratoDepositos holder, int position) {
        ExtratoDeposito extratoDeposito = this.listaExtratoDepositos.get(position);

        NumberFormat nb = NumberFormat.getCurrencyInstance();

        holder.txtDataDeposito.setText(extratoDeposito.getData());
        holder.txtValorDeposito.setText(nb.format(extratoDeposito.getValorDeposito()));


    }

    @Override
    public int getItemCount() {
        return this.listaExtratoDepositos.size();
    }

    public class ViewHolderExtratoDepositos extends RecyclerView.ViewHolder{

        public TextView txtDataDeposito, txtValorDeposito;

        public ViewHolderExtratoDepositos(@NonNull View itemView) {
            super(itemView);

            txtDataDeposito  = itemView.findViewById(R.id.txtDataDeposito);
            txtValorDeposito = itemView.findViewById(R.id.txtValorDeposito);
        }
    }
}
