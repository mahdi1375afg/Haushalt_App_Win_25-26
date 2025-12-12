package com.example.haushalt_app_java.vorrat;

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
import com.example.haushalt_app_java.produkt.ProductListAdapter;
import com.example.haushalt_app_java.einkaufsliste.EinkaufslisteActivity;
import com.example.haushalt_app_java.einkaufsliste.EinkaufslisteEintrag;
import com.example.haushalt_app_java.haushalt.HaushaltActivity;
import com.example.haushalt_app_java.produkt.ProductActivity;
import com.example.haushalt_app_java.profile.ProfileActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class VorratActivity extends AppCompatActivity implements ProductListAdapter.OnItemClickListener, VorratRepository.OnVorratDataChangedListener {

    private VorratRepository vorratRepository;
    private ProductListAdapter vorratAdapter;
    private String currentHaushaltId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produktliste);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Vorrat"); // Set the title on the Toolbar

        RecyclerView recyclerView = findViewById(R.id.einkaufslisteRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        vorratAdapter = new ProductListAdapter(new ArrayList<>(), this, false); // Pass false for isShoppingList
        recyclerView.setAdapter(vorratAdapter);

        vorratRepository = new VorratRepository();

        currentHaushaltId = getIntent().getStringExtra("HAUSHALT_ID");

        if (currentHaushaltId != null && !currentHaushaltId.isEmpty()) {
            vorratRepository.getVorrat(currentHaushaltId, this);
        } else {
            Toast.makeText(this, "Haushalts-ID nicht gefunden.", Toast.LENGTH_LONG).show();
            finish();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_einkaufsliste_vorrat);
        bottomNav.setSelectedItemId(R.id.nav_vorrat);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_products) {
                Intent intent = new Intent(VorratActivity.this, ProductActivity.class);
                intent.putExtra("HAUSHALT_ID", currentHaushaltId);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_household) {
                Intent intent = new Intent(VorratActivity.this, HaushaltActivity.class);
                intent.putExtra("HAUSHALT_ID", currentHaushaltId);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_einkaufslisten) {
                Intent intent = new Intent(VorratActivity.this, EinkaufslisteActivity.class);
                intent.putExtra("HAUSHALT_ID", currentHaushaltId);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_vorrat) {
                return true; // Already on this page
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(VorratActivity.this, ProfileActivity.class);
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
        // This method is not intended to be used in VorratActivity, checkbox is hidden
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
                vorratRepository.updateMenge(currentHaushaltId, eintrag.getProduktId(), newQuantity);
            }
        });
        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onVorratDataChanged(List<EinkaufslisteEintrag> vorratliste) {
        vorratAdapter.setProductList(vorratliste);
    }

    @Override
    public void onError(DatabaseError error) {
        Toast.makeText(this, "Fehler beim Laden der Vorratliste: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
