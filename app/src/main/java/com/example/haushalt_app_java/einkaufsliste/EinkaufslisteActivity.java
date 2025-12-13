package com.example.haushalt_app_java.einkaufsliste;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.domain.Kategorie;
import com.example.haushalt_app_java.vorrat.VorratActivity;
import com.example.haushalt_app_java.produkt.ProductListAdapter;
import com.example.haushalt_app_java.produkt.Produkt;
import com.example.haushalt_app_java.vorrat.VorratRepository;
import com.example.haushalt_app_java.haushalt.HaushaltActivity;
import com.example.haushalt_app_java.produkt.ProductActivity;
import com.example.haushalt_app_java.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class EinkaufslisteActivity extends AppCompatActivity implements ProductListAdapter.OnItemClickListener, EinkaufslisteRepository.OnEinkaufslisteDataChangedListener {

    private EinkaufslisteRepository einkaufslisteRepository;
    private VorratRepository vorratRepository;
    private ProductListAdapter einkaufslisteAdapter;
    private String currentHaushaltId;
    private Spinner spinnerKategorie;
    private ArrayList<EinkaufslisteEintrag> alleEintraege = new ArrayList<>();
    private String selectedKategorie = "Alle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_produktliste);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView title = findViewById(R.id.textViewTitle);
        title.setText("Einkaufsliste");

        RecyclerView recyclerView = findViewById(R.id.einkaufslisteRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        einkaufslisteAdapter = new ProductListAdapter(new ArrayList<>(), this, true);
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

        setupKategorieSpinner();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
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

    private void setupKategorieSpinner() {
        spinnerKategorie = findViewById(R.id.spinnerKategorie);
        ArrayList<String> kategorien = new ArrayList<>();
        kategorien.add("Alle");
        for (Kategorie kategorie : Kategorie.values()) {
            kategorien.add(kategorie.getDisplayName());
        }

        ArrayAdapter<String> kategorieAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                kategorien
        );
        kategorieAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerKategorie.setAdapter(kategorieAdapter);

        spinnerKategorie.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedKategorie = kategorien.get(position);
                filterProdukte();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedKategorie = "Alle";
                filterProdukte();
            }
        });
    }

    private void filterProdukte() {
        ArrayList<EinkaufslisteEintrag> filteredList = new ArrayList<>();
        if (selectedKategorie.equals("Alle")) {
            filteredList.addAll(alleEintraege);
        } else {
            for (EinkaufslisteEintrag eintrag : alleEintraege) {
                if (eintrag.getKategorie().equals(selectedKategorie)) {
                    filteredList.add(eintrag);
                }
            }
        }
        einkaufslisteAdapter.setProductList(filteredList);
    }

    @Override
    public void onEditClick(EinkaufslisteEintrag eintrag) {
        showEditQuantityDialog(eintrag);
    }

    @Override
    public void onMoveToVorratClick(EinkaufslisteEintrag eintrag) {
        Produkt produktToAdd = new Produkt(
                currentHaushaltId,
                eintrag.getName(),
                eintrag.getKategorie(),
                0, 
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

    @Override
    public void onDeleteClick(EinkaufslisteEintrag eintrag) {
        einkaufslisteRepository.removeShoppingListItem(currentHaushaltId, eintrag.getProduktId(), new EinkaufslisteRepository.OnShoppingListItemRemovedListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(EinkaufslisteActivity.this, eintrag.getName() + " von der Einkaufsliste entfernt", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EinkaufslisteActivity.this, "Fehler beim Entfernen von " + eintrag.getName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onIncreaseQuantityClick(EinkaufslisteEintrag eintrag) {
        int newQuantity = eintrag.getMenge() + 1;
        einkaufslisteRepository.updateMenge(currentHaushaltId, eintrag.getProduktId(), newQuantity);
    }

    @Override
    public void onDecreaseQuantityClick(EinkaufslisteEintrag eintrag) {
        int newQuantity = eintrag.getMenge() - 1;
        if (newQuantity >= 0) {
            einkaufslisteRepository.updateMenge(currentHaushaltId, eintrag.getProduktId(), newQuantity);
        }
    }

    @Override
    public void onBookmarkClick(EinkaufslisteEintrag eintrag, ImageButton bookmarkButton) {
        boolean isBookmarked = !eintrag.isBookmarked();
        eintrag.setBookmarked(isBookmarked);
        einkaufslisteRepository.updateBookmarkedStatus(currentHaushaltId, eintrag.getProduktId(), isBookmarked);

        if (isBookmarked) {
            bookmarkButton.setImageResource(R.drawable.ic_bookmark_checked);
        } else {
            bookmarkButton.setImageResource(R.drawable.ic_bookmark_unchecked);
        }
    }

    @Override
    public void onAddToShoppingListClick(EinkaufslisteEintrag eintrag) {
        // This method is not intended to be used in EinkaufslisteActivity
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
        alleEintraege.clear();
        alleEintraege.addAll(einkaufsliste);
        filterProdukte();
    }

    @Override
    public void onError(DatabaseError error) {
        Toast.makeText(this, "Fehler beim Laden der Einkaufsliste: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
