package com.example.intelimoney.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.intelimoney.R;
import com.example.intelimoney.activity.autenticacao.LoginActivity;
import com.example.intelimoney.activity.MeuPerfilActivity;
import com.example.intelimoney.contas.MinhaContaListaActivity;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.FragmentHomeBinding;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.helper.DateCustom;
import com.example.intelimoney.model.Conta;
import com.example.intelimoney.model.Transacao;
import com.example.intelimoney.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private DrawerLayout drawerLayout;
    private ImageView menu;
    private LinearLayout perfil, conta, sair;
    private TextView nome, email;

    private final FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private DatabaseReference transacaoRef;
    private ValueEventListener valueEventListenerTransacoes;

    private final List<Conta> listaConta = new ArrayList<>();
    private final List<Transacao> listaTransacoes = new ArrayList<>();
    private String mesAnoSelecionado;

    private final NumberFormat nb = NumberFormat.getCurrencyInstance();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        drawerLayout = view.findViewById(R.id.drawerLayout);
        menu         = view.findViewById(R.id.menu_nav_drawer);
        perfil       = view.findViewById(R.id.perfil);
        conta        = view.findViewById(R.id.conta);
        sair         = view.findViewById(R.id.sair);

        nome  = view.findViewById(R.id.nav_nome_usuario);
        email = view.findViewById(R.id.nav_email_usuario);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recuperarUsuario();
        configClicks();
        //recuperarResumoMes();
        recuperarTransacoes();
        recuperaSaldoConta();
    }

    private void configClicks() {
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer(drawerLayout);
            }
        });

        perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectActivity(getActivity(), MeuPerfilActivity.class);
            }
        });

        conta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectActivity(getActivity(), MinhaContaListaActivity.class);
            }
        });

        sair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deslogarUsuario();
            }
        });
    }

    private void recuperarUsuario(){
        String id_usuaio = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(id_usuaio);

        ValueEventListener valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);

                nome.setText(usuario.getNome());
                email.setText(usuario.getEmail());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarTransacoes() {
        mesAnoSelecionado = DateCustom.mesAnoEscolhido(DateCustom.dataAtual());
        String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        transacaoRef = firebaseRef.child("transacoes")
                .child(idUsuario)
                .child(mesAnoSelecionado);

        valueEventListenerTransacoes = transacaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaTransacoes.clear();
                double totalDespesa = 0, totalReceita = 0;

                for (DataSnapshot ds: snapshot.getChildren()){

                    Transacao transacao = ds.getValue(Transacao.class);
                    transacao.setId_transacao(ds.getKey());
                    listaTransacoes.add(transacao);

                    if (transacao.getTipo().equals("r")) {
                        totalReceita += transacao.getValor();
                    }

                    if (transacao.getTipo().equals("d")) {
                        totalDespesa += transacao.getValor();
                    }
                }


                binding.txtReceitaTotal.setText(nb.format(totalReceita));
                binding.txtDespesaTotal.setText(nb.format(totalDespesa));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperaSaldoConta(){
        String id_usuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference contaRef = firebaseRef.child("conta").child(id_usuario);
        ValueEventListener valueEventListenerContas = contaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                double saldo = 0;

                if (snapshot.exists()){
                    listaConta.clear();

                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Conta conta = ds.getValue(Conta.class);
                        conta.setId_conta(ds.getKey());
                        listaConta.add(conta);
                    }

                    saldo = listaConta.get(0).getSaldo();
                    binding.txtSaldoEmConta.setText(nb.format(saldo));

                } else {
                    binding.txtSaldoEmConta.setText(nb.format(saldo));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deslogarUsuario() {
        // Deslogar o usuario
        auth.signOut();

        // Retornar para a loginActivity
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    private static void openDrawer(DrawerLayout drawerLayout){
        // Abre o Naviigation Drawer
        drawerLayout.openDrawer(GravityCompat.START);
    }

    private static void closeDrawer(DrawerLayout drawerLayout){

        // Ao clicar na tela, verifica se o Navigation Drawer está aberto, se estiver, ele é fechado
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private static void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity, secondActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        closeDrawer(drawerLayout);
    }


}