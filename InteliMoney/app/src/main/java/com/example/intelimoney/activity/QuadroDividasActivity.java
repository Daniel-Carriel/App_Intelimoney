package com.example.intelimoney.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.intelimoney.R;
import com.example.intelimoney.adapter.DividaAdapter;
import com.example.intelimoney.configDatabase.ConfiguracaoFirebase;
import com.example.intelimoney.databinding.ActivityQuadroDividasBinding;
import com.example.intelimoney.databinding.DialogAtualizarDividaBinding;
import com.example.intelimoney.databinding.DialogAtualizarDividaPendenteBinding;
import com.example.intelimoney.databinding.DialogExcluirDividaBinding;
import com.example.intelimoney.databinding.DialogSalvarDividaBinding;
import com.example.intelimoney.helper.Base64Custom;
import com.example.intelimoney.helper.DateCustom;
import com.example.intelimoney.helper.DatePickerFragment;
import com.example.intelimoney.helper.RecyclerItemClickListener;
import com.example.intelimoney.model.Divida;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QuadroDividasActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private ActivityQuadroDividasBinding binding;
    private DialogSalvarDividaBinding dialogSalvarDividaBinding;
    private DialogAtualizarDividaBinding dialogAtualizarDividaBinding;
    private DialogAtualizarDividaPendenteBinding dialogAtualizarDividaPendenteBinding;
    private DialogExcluirDividaBinding dialogExcluirDividaBinding;

    private DividaAdapter dividaAdapter;

    private final FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private DatabaseReference dividaRef;
    private ValueEventListener valueEventListenerDividas;

    private final NumberFormat nb = NumberFormat.getCurrencyInstance();
    private final List<Divida> listaDividas = new ArrayList<>();
    private Divida divida;

    private String mesAnoSelecionado;
    private String nomeDivida, vencimento;
    private double valor, valorTotal, totalDividasPagas, totalDividasPendentes;

    int i;


    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuadroDividasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configToolBar();
        configCalendarView();
        recuperarDividasPagas();
        recuperarDividasPendentes();
        configRvDividas();
        eventoClickRvDividas();
        swipe();


        binding.dividasPendentes.setBackgroundResource(R.color.buttonLightBlue);

        binding.dividasPendentes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                recuperarDividasPendentes();
                binding.txtValorTotalDividas.setText("Valor Total:");

                binding.dividasPendentes.setBackgroundResource(R.color.buttonLightBlue);
                binding.dividasPagas.setBackgroundResource(R.color.blueTheme);
            }
        });

        binding.dividasPagas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                recuperarDividasPagas();
                binding.txtValorTotalDividas.setText("Total Pago:");

                binding.dividasPagas.setBackgroundResource(R.color.buttonLightBlue);
                binding.dividasPendentes.setBackgroundResource(R.color.blueTheme);
            }
        });

        binding.fabNovaDivida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSalvarDivida();
            }
        });

    }

    private void dialogSalvarDivida() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        alertDialog.setCancelable(false);

        dialogSalvarDividaBinding = DialogSalvarDividaBinding
                .inflate(LayoutInflater.from(this));

        dialogSalvarDividaBinding.txtVencimento.setOnClickListener(v -> {
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.show(getSupportFragmentManager(), "date picker");
        });

        dialogSalvarDividaBinding.btnFechar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogSalvarDividaBinding.btnSalvar.setOnClickListener(v -> {

            if (validarCampos()){
                String mesAno = DateCustom.mesAnoEscolhido(vencimento);

                // Condição para testar se o mes de vencimento é igual ao mês atual para cadastrar a dívida
                if (mesAno.equals(mesAnoSelecionado)) {

                    salvarDivida();
                    recuperarDividasPendentes();
                    //recuperarDivitaTotal(mesAnoSelecionado);
                    dialog.dismiss();

                } else {

                    Toast.makeText(this, "Cadastre somente dividas referente ao mês vigente", Toast.LENGTH_SHORT).show();

                }
            }
        });

        alertDialog.setView(dialogSalvarDividaBinding.getRoot());

        dialog = alertDialog.create();
        dialog.show();
    }

    private void dialogAtualizarStatusPago(int position) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        alertDialog.setCancelable(false);


        dialogAtualizarDividaBinding = DialogAtualizarDividaBinding
                .inflate(LayoutInflater.from(this));

        dialogAtualizarDividaBinding.btnNao.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogAtualizarDividaBinding.btnSim.setOnClickListener(v -> {
            String id_divida = listaDividas.get(position).getId_divida();
            double valorDivida = listaDividas.get(position).getValor();
            boolean status;

            status = true;
            atualizarStatusDivida(id_divida, status);

            dialog.dismiss();

        });

        alertDialog.setView(dialogAtualizarDividaBinding.getRoot());

        dialog = alertDialog.create();
        dialog.show();
    }

    private void dialogAtualizarStatusPendente(int position) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        alertDialog.setCancelable(false);


        dialogAtualizarDividaPendenteBinding = DialogAtualizarDividaPendenteBinding
                .inflate(LayoutInflater.from(this));

        dialogAtualizarDividaPendenteBinding.btnNao.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogAtualizarDividaPendenteBinding.btnSim.setOnClickListener(v -> {
            String id_divida = listaDividas.get(position).getId_divida();
            boolean status;

            status = false;
            atualizarStatusDivida(id_divida, status);
            recuperarDividasPagas();

           /* recuperarDividasPendentes(mesAnoSelecionado);
            //recuperarDivitaTotal(mesAnoSelecionado);
            binding.txtDividaTotal.setText(nb.format(totalDividasPendentes));
            binding.txtValorTotalDividas.setText("Valor Total:");

            binding.dividasPendentes.setBackgroundResource(R.color.buttonLightBlue);
            binding.dividasPagas.setBackgroundResource(R.color.blueTheme);

            */

            dialog.dismiss();

        });

        alertDialog.setView(dialogAtualizarDividaPendenteBinding.getRoot());

        dialog = alertDialog.create();
        dialog.show();
    }

    private void dialogExcluirDivida( RecyclerView.ViewHolder viewHolder ) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        alertDialog.setCancelable(false);

        dialogExcluirDividaBinding = DialogExcluirDividaBinding
                .inflate(LayoutInflater.from(this));

        dialogExcluirDividaBinding.btnNao.setOnClickListener(v -> {
            dividaAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialogExcluirDividaBinding.btnSim.setOnClickListener(v -> {

            int position = viewHolder.getAdapterPosition(); // recupera a posiçaõ do item arrastado
            divida = listaDividas.get( position );  // passa os dados do item arrastado para a lista de dividas

            excluirDivida(divida.getId_divida());

            binding.txtDividaTotal.setText(nb.format(totalDividasPagas - divida.getValor()));

            dialog.dismiss();

        });

        alertDialog.setView(dialogExcluirDividaBinding.getRoot());

        dialog = alertDialog.create();
        dialog.show();
    }

    private void salvarDivida() {
        divida = new Divida();
        double valorDivida = valor / 100;

        divida.setNome(nomeDivida);
        divida.setValor(valorDivida);
        divida.setVencimento(vencimento);
        divida.setPago(false);

        divida.salvar(vencimento);

    }


    public void recuperarDividasPendentes() {
        String id_usuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        dividaRef = firebaseRef.child("dividas")
                .child(id_usuario)
                .child(mesAnoSelecionado);

       valueEventListenerDividas = dividaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaDividas.clear();

                double totalPendente = 0;

                for (DataSnapshot ds: snapshot.getChildren()){

                    Divida divida = ds.getValue(Divida.class);
                    divida.setId_divida(ds.getKey());

                    boolean status = divida.isPago();


                    if (!status) {
                        listaDividas.add(divida);

                        totalPendente += divida.getValor();
                    }
                    totalDividasPendentes = totalPendente;

                    binding.txtDividaTotal.setText(nb.format(totalDividasPendentes));
                }

                    dividaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void recuperarDividasPagas() {
        String id_usuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        dividaRef = firebaseRef.child("dividas")
                .child(id_usuario)
                .child(mesAnoSelecionado);

        valueEventListenerDividas = dividaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaDividas.clear();

                double totalPago = 0;

                for (DataSnapshot ds: snapshot.getChildren()){

                    Divida divida = ds.getValue(Divida.class);
                    divida.setId_divida(ds.getKey());

                    boolean status = divida.isPago();
                    if (status) {
                        listaDividas.add(divida);

                        totalPago += divida.getValor();
                    }
                    totalDividasPagas = totalPago;

                    binding.txtDividaTotal.setText(nb.format(totalDividasPagas));
                    }

                    dividaAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void atualizarStatusDivida(String id_divida, boolean status){
        String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference statusRef = firebaseRef.child("dividas")
                .child(idUsuario)
                .child(mesAnoSelecionado)
                .child(id_divida);

        statusRef.child("pago").setValue(status);
    }

    private void excluirDivida(String id_divida){
        String idUsuario = Base64Custom.codificarBase64(auth.getCurrentUser().getEmail());
        DatabaseReference dividaRef = firebaseRef.child("dividas")
                .child(idUsuario)
                .child(mesAnoSelecionado)
                .child(id_divida);

        dividaRef.removeValue();
    }

    private void configRvDividas() {
        
        // Configurar adapter
        dividaAdapter = new DividaAdapter(listaDividas, this);

        // Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvDividas.setLayoutManager(layoutManager);
        binding.rvDividas.setHasFixedSize(true);
        binding.rvDividas.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        binding.rvDividas.setAdapter(dividaAdapter);
    }

    private void eventoClickRvDividas(){
        binding.rvDividas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this, binding.rvDividas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                Divida divida = listaDividas.get(position);
                                if (divida.isPago()) {
                                    dialogAtualizarStatusPendente(position);
                                } else {
                                    dialogAtualizarStatusPago(position);
                                }
                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }));
    }

    private void swipe(){
        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags  = ItemTouchHelper.ACTION_STATE_IDLE;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags( dragFlags, swipeFlags );
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                dialogExcluirDivida( viewHolder );
            }
        };

        new ItemTouchHelper(itemTouch).attachToRecyclerView(binding.rvDividas);
    }

    public void configCalendarView() {
        CharSequence meses[]   = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

        binding.calendarView.setTitleMonths(meses);

        CalendarDay dataAtual = binding.calendarView.getCurrentDate();
        String mesSelecionado = String.format("%02d", (dataAtual.getMonth() + 1));
        mesAnoSelecionado = String.valueOf(mesSelecionado + "" +  dataAtual.getYear());

        binding.calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                String mesSelecionado = String.format("%02d", (date.getMonth() + 1));
                mesAnoSelecionado = String.valueOf(mesSelecionado + "" + date.getYear());

                dividaRef.removeEventListener( valueEventListenerDividas );

                recuperarDividasPagas();
                recuperarDividasPendentes();
                // Recupera as dividas do mês escolhido no calendario



            }
        });
    }

    public void onDateSet(DatePicker datePicker, int ano, int mes, int dia) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, ano);
        calendar.set(Calendar.MONTH, mes);
        calendar.set(Calendar.DAY_OF_MONTH, dia);

        String dataFormatada = simpleDateFormat.format(calendar.getTime());

        dialogSalvarDividaBinding.txtVencimento.setText(dataFormatada);

    }

    private boolean validarCampos(){
        nomeDivida = dialogSalvarDividaBinding.editNomeDivida.getText().toString();
        valor      = (double) dialogSalvarDividaBinding.editValorDivida.getRawValue();
        vencimento = dialogSalvarDividaBinding.txtVencimento.getText().toString();

        if (!nomeDivida.isEmpty()){
            if (valor > 0){
                if (!vencimento.isEmpty()){
                    return true;
                }else {
                    Toast.makeText(this, "Data de vencimento vazio.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else {
                Toast.makeText(this, "Valor deve ser maior que 0 (zero).", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else {
            Toast
                    .makeText(this, "Nome da divida vazio.", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    private void configToolBar() {
        getSupportActionBar().setTitle("Quadro de dívidas");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
    }
}