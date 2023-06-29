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
import com.example.intelimoney.model.Transacao;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class TransacaoAdapter extends RecyclerView.Adapter<TransacaoAdapter.ViewHolderTransacao> {

    private List<Transacao> listarTransacoes;
    private Context context;

    public TransacaoAdapter(List<Transacao> listarTransacoes, Context context){
        this.listarTransacoes = listarTransacoes;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderTransacao onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listaTransacoes = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adapter_transacoes, parent, false);

        return new ViewHolderTransacao(listaTransacoes);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolderTransacao holder, int position) {
        Transacao transacao = this.listarTransacoes.get(position);

        NumberFormat nb = NumberFormat.getCurrencyInstance();

        holder.txtNomeTransacao.setText(transacao.getDescricao());
        holder.txtValorTransacao.setText(nb.format(transacao.getValor()));
        holder.txtDataTransacao.setText(transacao.getData());
        holder.txtCategoriaTransacao.setText(transacao.getIdCategoria());

       if (transacao.getTipo().equals("d")) {
            holder.txtValorTransacao.setTextColor(context.getResources().getColor(R.color.despesa));
            holder.txtValorTransacao.setText(nb.format(transacao.getValor()));
        }
    }

    @Override
    public int getItemCount() {
        return this.listarTransacoes.size();
    }

    public class ViewHolderTransacao extends RecyclerView.ViewHolder{

        public TextView txtNomeTransacao, txtDataTransacao, txtValorTransacao, txtCategoriaTransacao;

        public ViewHolderTransacao(@NonNull View itemView) {
            super(itemView);

            txtNomeTransacao      = itemView.findViewById(R.id.txtNomeTransacao);
            txtValorTransacao     = itemView.findViewById(R.id.txtValorTransacao);
            txtDataTransacao      = itemView.findViewById(R.id.txtDataTransacao);
            txtCategoriaTransacao = itemView.findViewById(R.id.txtCategoriaTransacao);
        }
    }

}
