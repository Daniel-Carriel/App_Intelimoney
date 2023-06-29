package com.example.intelimoney.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.intelimoney.R;
import com.example.intelimoney.activity.transacoes.DespesaActivity;
import com.example.intelimoney.activity.transacoes.ReceitaActivity;
import com.example.intelimoney.databinding.ActivityPrincipalBinding;

public class PrincipalActivity extends AppCompatActivity {

    private ActivityPrincipalBinding binding;

    private NavHostFragment navHostFragment;
    private NavController navController;

    private boolean isAllVisible;
    private boolean isOpen = false;
    private Animation fabOpen, fabClose, rotateForward, rotateBackward;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrincipalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        invisibleFAB();
        initNavigation();
        animationFAB();


        binding.fabAddTransacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickFAB();
            }
        });

        binding.fabReceita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaReceita();
            }
        });

        binding.fabDespesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaDespesa();
            }
        });
    }

    private void animationFAB() {
        // animação
        fabOpen  = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        rotateForward  = AnimationUtils.loadAnimation(this, R.anim.rotate_foward);
        rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backwawrd);
    }

    private void initNavigation(){
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);


        if (navHostFragment != null){

            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigationPrincipal, navController);
        }
    }

    private void invisibleFAB() {
        binding.fabDespesa.hide();
        binding.fabReceita.hide();
        binding.layoutFab.setVisibility(View.GONE);
        binding.txtDespesa.setVisibility(View.GONE);
        binding.txtReceita.setVisibility(View.GONE);

        isAllVisible = false;
    }

    private void clickFAB(){
        /*  Metodo criado para configurar o clique do Floating Action Button que mostrará os botões de navegação
            para a tela de cração das receitas e despesas
         */
        if (!isAllVisible){

            binding.fabDespesa.show();
            binding.fabReceita.show();
            binding.txtDespesa.setVisibility(View.VISIBLE);
            binding.txtReceita.setVisibility(View.VISIBLE);
            binding.layoutFab.setVisibility(View.VISIBLE);

            isAllVisible = true;

        } else {

            binding.fabDespesa.hide();
            binding.fabReceita.hide();
            binding.txtDespesa.setVisibility(View.GONE);
            binding.txtReceita.setVisibility(View.GONE);
            binding.layoutFab.setVisibility(View.GONE);

            isAllVisible = false;
        }

        if (isOpen){
            binding.fabAddTransacao.startAnimation(rotateBackward);
            isOpen = false;
        }else {
            binding.fabAddTransacao.startAnimation(rotateForward);
            isOpen = true;
        }

    }

    private void irParaDespesa() {
        startActivity(new Intent(this, DespesaActivity.class));
        //recreate();
    }

    private void irParaReceita() {
        startActivity(new Intent(this, ReceitaActivity.class));
        //recreate();
    }

}