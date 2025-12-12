package com.example.haushalt_app_java.einkaufsliste;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.vorrat.VorratActivity;
import com.example.haushalt_app_java.produkt.ProductListAdapter;
import com.example.haushalt_app_java.produkt.Produkt;
import com.example.haushalt_app_java.vorrat.VorratRepository;
import com.example.haushalt_app_java.haushalt.HaushaltActivity;
import com.example.haushalt_app_java.produkt.ProductActivity;
import com.example.haushalt_app_java.profile.ProfileActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class EinkaufslisteActivity extends AppCompatActivity implements ProductListAdapter.OnItemClickListener, EinkaufslisteRepository.OnEinkaufslisteDataChangedListener {

    private EinkaufslisteRepository einkaufslisteRepository;
    private VorratRepository vorratRepository;
    private ProductListAdapter einkaufslisteAdapter;
    private String currentHaushaltId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produktliste);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Einkaufsliste"); // Set the title on the Toolbar

        RecyclerView recyclerView = findViewById(R.id.einkaufslisteRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        einkaufslisteAdapter = new ProductListAdapter(new ArrayList<>(), this, true); // Pass true for isShoppingList
        recyclerView.setAdapter(einkaufslisteAdapter);

        einkaufslisteRepository = new EinkaufslisteRepository();
        vorratRepository = new VorratRepository();

        currentHaushaltId = getIntent().getStringExtra("HAUSHALT_ID");

        if (currentHaushaltId != null && !currentHaushaltId.isEmpty()) {
            einkaufslisteRepository.getEinkaufsliste(currentHaushaltId, this);
        } else {
            Toast.makeText(this, "Haushalts-ID nicht gefunden.", Toast.LENGTH_LONG).show();
            finish();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_einkaufsliste_vorrat);
        bottomNav.setSelectedItemId(R.id.nav_einkaufslisten);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_products) {
                Intent intent = new Intent(EinkaufslisteActivity.this, ProductActivity.class);
                intent.putExtra("HAUSHALT_ID", currentHaushaltId);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_household) {
                Intent intent = new Intent(EinkaufslisteActivity.this, HaushaltActivity.class);
                intent.putExtra("HAUSHALT_ID", currentHaushaltId);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_einkaufslisten) {
                return true; // Already on this page
            } else if (itemId == R.id.nav_vorrat) {
                Intent intent = new Intent(EinkaufslisteActivity.this, VorratActivity.class);
                intent.putExtra("HAUSHALT_ID", currentHaushaltId);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(EinkaufslisteActivity.this, ProfileActivity.class);
                intent.putExtra("HAUSHALT_ID", currentHaushaltId);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onEditClick(EinkaufslisteEintrag eintrag) {
        showEditQuantityDialog(eintrag);
    }

    @Override
    public void onMoveToVorratClick(EinkaufslisteEintrag eintrag) {
        // Convert EinkaufslisteEintrag to Produkt for VorratRepository
        Produkt produktToAdd = new Produkt(
                currentHaushaltId,
                eintrag.getName(),
                eintrag.getKategorie(),
                0, // Mindestbestand, Zielbestand are not directly available from EinkaufslisteEintrag, set to default or retrieve from somewhere else if needed
                0,
                eintrag.getEinheit()
        );
        produktToAdd.setProdukt_id(eintrag.getProduktId());

        vorratRepository.addVorratItem(currentHaushaltId, produktToAdd, eintrag.getMenge(), new VorratRepository.OnVorratItemAddedListener() {
            @Override
            public void onSuccess() {
                einkaufslisteRepository.removeShoppingListItem(currentHaushaltId, eintrag.getProduktId(), new EinkaufslisteRepository.OnShoppingListItemRemovedListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(EinkaufslisteActivity.this, produktToAdd.getName() + " zum Vorrat hinzugefügt und von der Einkaufsliste entfernt.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(EinkaufslisteActivity.this, "Fehler beim Entfernen von der Einkaufsliste: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EinkaufslisteActivity.this, "Fehler beim Hinzufügen zum Vorrat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        einkaufslisteAdapter.setProductList(einkaufsliste);
    }

    @Override
    public void onError(DatabaseError error) {
        Toast.makeText(this, "Fehler beim Laden der Einkaufsliste: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
