package com.example.haushalt_app_java.vorrat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.haushalt_app_java.einkaufsliste.EinkaufslisteRepository;
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
    private EinkaufslisteRepository einkaufslisteRepository;
    private ProductListAdapter vorratAdapter;
    private String currentHaushaltId;

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

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Vorrat"); // Set the title on the Toolbar

        RecyclerView recyclerView = findViewById(R.id.einkaufslisteRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        vorratAdapter = new ProductListAdapter(new ArrayList<>(), this, false); // Pass false for isShoppingList
        recyclerView.setAdapter(vorratAdapter);

        vorratRepository = new VorratRepository();
        einkaufslisteRepository = new EinkaufslisteRepository();

        currentHaushaltId = getIntent().getStringExtra("HAUSHALT_ID");

        if (currentHaushaltId != null && !currentHaushaltId.isEmpty()) {
            vorratRepository.getVorrat(currentHaushaltId, this);
        } else {
            Toast.makeText(this, "Haushalts-ID nicht gefunden.", Toast.LENGTH_LONG).show();
            finish();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
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
        // This method is not intended to be used in VorratActivity
    }

    @Override
    public void onDeleteClick(EinkaufslisteEintrag eintrag) {
        vorratRepository.removeVorratItem(currentHaushaltId, eintrag.getProduktId(), new VorratRepository.OnVorratItemRemovedListener() {
            @Override
            public void onVorratDataChanged(List<EinkaufslisteEintrag> vorratliste) {

            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onIncreaseQuantityClick(EinkaufslisteEintrag eintrag) {
        int newQuantity = eintrag.getMenge() + 1;
        vorratRepository.updateMenge(currentHaushaltId, eintrag.getProduktId(), newQuantity);
    }

    @Override
    public void onDecreaseQuantityClick(EinkaufslisteEintrag eintrag) {
        int newQuantity = eintrag.getMenge() - 1;
        if (newQuantity >= 0) {
            vorratRepository.updateMenge(currentHaushaltId, eintrag.getProduktId(), newQuantity);
        }
    }

    @Override
    public void onBookmarkClick(EinkaufslisteEintrag eintrag, ImageButton bookmarkButton) {
        // Toggle the bookmarked state and update the icon
        boolean isBookmarked = !eintrag.isBookmarked();
        eintrag.setBookmarked(isBookmarked);
        vorratRepository.updateBookmarkedStatus(currentHaushaltId, eintrag.getProduktId(), isBookmarked);

        if (isBookmarked) {
            bookmarkButton.setImageResource(R.drawable.ic_bookmark_checked);
        } else {
            bookmarkButton.setImageResource(R.drawable.ic_bookmark_unchecked);
        }
    }

    @Override
    public void onAddToShoppingListClick(EinkaufslisteEintrag eintrag) {
        showAddToShoppingListDialog(eintrag);
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

    private void showAddToShoppingListDialog(EinkaufslisteEintrag eintrag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_quantity, null);
        builder.setView(dialogView);

        final EditText editTextQuantity = dialogView.findViewById(R.id.editTextQuantity);
        editTextQuantity.setText("1"); // Default to 1

        builder.setTitle("Menge für Einkaufsliste");
        builder.setPositiveButton("Hinzufügen", (dialog, which) -> {
            String newQuantityStr = editTextQuantity.getText().toString();
            if (!newQuantityStr.isEmpty()) {
                int newQuantity = Integer.parseInt(newQuantityStr);
                einkaufslisteRepository.addShoppingListItem(currentHaushaltId, eintrag.getProduktId(), newQuantity, new EinkaufslisteRepository.OnShoppingListItemAddedListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(VorratActivity.this, eintrag.getName() + " zur Einkaufsliste hinzugefügt", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(VorratActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
