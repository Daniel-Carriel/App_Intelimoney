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

import java.util.List;

public class CategoriaDialogAdapter extends RecyclerView.Adapter<CategoriaDialogAdapter.MyViewHolder> {

    private final List<String> categoriaSelecionada;
    private final List<Categoria> listaCategorias;
    private OnClick onClick;

    public CategoriaDialogAdapter(List<String> categoriaSelecionada, List<Categoria> listaCategorias, OnClick onClick) {
        this.categoriaSelecionada = categoriaSelecionada;
        this.listaCategorias = listaCategorias;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_categoria_dialog, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Categoria categoria = listaCategorias.get(position);

        holder.txtCategoria.setText(categoria.getNome());

        if(categoriaSelecionada.contains(categoria.getId())){
            holder.checkBoxCategorias.setChecked(true);
        }

        holder.itemView.setOnClickListener(view -> {
            onClick.onClickListener(categoria);

            holder.checkBoxCategorias.setChecked(!holder.checkBoxCategorias.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return listaCategorias.size();
    }

    public interface OnClick{

        void onClickListener(Categoria categorias);
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView txtCategoria;
        public CheckBox checkBoxCategorias;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoria = itemView.findViewById(R.id.txtCategoria);
            checkBoxCategorias = itemView.findViewById(R.id.checkBoxCategoria);
        }
    }
}
