package com.example.intelimoney.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.intelimoney.R;
import com.example.intelimoney.model.Divida;
import com.example.intelimoney.model.ExtratoDeposito;

import java.text.NumberFormat;
import java.util.List;

public class DividaAdapter extends RecyclerView.Adapter<DividaAdapter.ViewHolderDividas> {

    private List<Divida> listaDividas;
    private Context context;

    public DividaAdapter(List<Divida> listaDividas, Context context){
        this.listaDividas = listaDividas;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderDividas onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listaDividas = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adapter_dividas, parent, false);

        return new ViewHolderDividas(listaDividas);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolderDividas holder, int position) {
        Divida divida = this.listaDividas.get(position);

        NumberFormat nb = NumberFormat.getCurrencyInstance();

        holder.txtNomeDivida.setText(divida.getNome());
        holder.txtValorDivida.setText(nb.format(divida.getValor()));
        holder.txtVencimento.setText("Vencimento: " + divida.getVencimento());

    }

    @Override
    public int getItemCount() {
        return this.listaDividas.size();
    }

    public class ViewHolderDividas extends RecyclerView.ViewHolder{

        public TextView txtNomeDivida, txtValorDivida, txtVencimento;

        public ViewHolderDividas(@NonNull View itemView) {
            super(itemView);

            txtNomeDivida  = itemView.findViewById(R.id.txtNomeDivida);
            txtValorDivida = itemView.findViewById(R.id.txtValorDivida);
            txtVencimento  = itemView.findViewById(R.id.txtVencimento);
        }
    }
}
