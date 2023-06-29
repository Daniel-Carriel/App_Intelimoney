package com.example.intelimoney.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.intelimoney.R;
import com.example.intelimoney.databinding.FragmentMaisBinding;
import com.example.intelimoney.databinding.FragmentPlanejamentoBinding;

public class PlanejamentoFragment extends Fragment {

    private FragmentPlanejamentoBinding binding;

    private TextView tituloFragment;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlanejamentoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        tituloFragment = view.findViewById(R.id.tituloFragment);

        return view;

    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configToolbar();

    }

    private void configToolbar() {
        tituloFragment.setText("Planejamento");
    }
}