package com.example.haushalt_app_java.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.adapter.EinkaufslisteAdapter;
import com.example.haushalt_app_java.domain.EinkaufslisteEintrag;
import com.example.haushalt_app_java.domain.EinkaufslisteRepository;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class EinkaufslisteActivity extends AppCompatActivity implements EinkaufslisteAdapter.OnItemClickListener, EinkaufslisteRepository.OnEinkaufslisteDataChangedListener {

    private EinkaufslisteRepository einkaufslisteRepository;
    private EinkaufslisteAdapter einkaufslisteAdapter;
    private String currentHaushaltId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_einkaufsliste);

        RecyclerView recyclerView = findViewById(R.id.einkaufslisteRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        einkaufslisteAdapter = new EinkaufslisteAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(einkaufslisteAdapter);

        einkaufslisteRepository = new EinkaufslisteRepository();

        currentHaushaltId = getIntent().getStringExtra("HAUSHALT_ID");

        if (currentHaushaltId != null && !currentHaushaltId.isEmpty()) {
            einkaufslisteRepository.getEinkaufsliste(currentHaushaltId, this);
        } else {
            Toast.makeText(this, "Haushalts-ID nicht gefunden.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onEditClick(EinkaufslisteEintrag eintrag) {
        showEditQuantityDialog(eintrag);
    }

    private void showEditQuantityDialog(EinkaufslisteEintrag eintrag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_quantity, null);
        builder.setView(dialogView);

        final EditText editTextQuantity = dialogView.findViewById(R.id.editTextQuantity);
        editTextQuantity.setText(String.valueOf(eintrag.getMenge()));

        builder.setTitle("Menge anpassen für " + eintrag.getName());
        builder.setPositiveButton("Speichern", (dialog, which) -> {
            String newQuantityStr = editTextQuantity.getText().toString();
            if (!newQuantityStr.isEmpty()) {
                int newQuantity = Integer.parseInt(newQuantityStr);
                einkaufslisteRepository.updateMenge(currentHaushaltId, eintrag.getProduktId(), newQuantity);
            }
        });
        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onEinkaufslisteDataChanged(List<EinkaufslisteEintrag> einkaufsliste) {
        einkaufslisteAdapter.setEinkaufsliste(einkaufsliste);
    }

    @Override
    public void onError(DatabaseError error) {
        Toast.makeText(this, "Fehler beim Laden der Einkaufsliste: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
