package com.example.haushalt_app_java.einkaufsliste;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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
import com.example.haushalt_app_java.produkt.ProductRepository;
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
import java.util.Collections;
import java.util.List;

public class EinkaufslisteActivity extends AppCompatActivity implements ProductListAdapter.OnItemClickListener, EinkaufslisteRepository.OnEinkaufslisteDataChangedListener {

    private EinkaufslisteRepository einkaufslisteRepository;
    private VorratRepository vorratRepository;
    private ProductRepository productRepository;
    private ProductListAdapter einkaufslisteAdapter;
    private String currentHaushaltId;
    private Spinner spinnerKategorie;
    private ArrayList<ListenEintrag> alleEintraege = new ArrayList<>();
    private String selectedKategorie = "Alle";
    private androidx.appcompat.widget.SearchView searchView;
    private String searchQuery = "";
    private Spinner spinnerSort;
    private String selectedSort = "Alphabet";

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
            productRepository = new ProductRepository(currentHaushaltId);
            einkaufslisteRepository.getEinkaufsliste(currentHaushaltId, this);
        } else {
            Toast.makeText(this, "Haushalts-ID nicht gefunden.", Toast.LENGTH_LONG).show();
            finish();
        }

        setupKategorieSpinner();
        setupSearchView();
        setupSortSpinner();

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
                filterAndSortProdukte();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedKategorie = "Alle";
                filterAndSortProdukte();
            }
        });
    }

    private void setupSortSpinner() {
        spinnerSort = findViewById(R.id.spinnerSort);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);
        spinnerSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSort = parent.getItemAtPosition(position).toString();
                filterAndSortProdukte();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupSearchView() {
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText.toLowerCase();
                filterAndSortProdukte();
                return true;
            }
        });
    }

    private void filterAndSortProdukte() {
        ArrayList<ListenEintrag> filteredList = new ArrayList<>();

        for (ListenEintrag eintrag : alleEintraege) {
            boolean kategorieMatch = selectedKategorie.equals("Alle")
                || eintrag.getKategorie().equals(selectedKategorie);

            boolean searchMatch = searchQuery.isEmpty()
                || eintrag.getName().toLowerCase().contains(searchQuery);

            if (kategorieMatch && searchMatch) {
                filteredList.add(eintrag);
            }
        }

        // Sorting logic
        if (selectedSort.equals("Alphabet")) {
            filteredList.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        } else if (selectedSort.equals("Lagerbestand")) {
            filteredList.sort((o1, o2) -> {
                int status1 = getStatus(o1);
                int status2 = getStatus(o2);
                if (status1 != status2) {
                    return Integer.compare(status1, status2);
                }
                return o1.getName().compareToIgnoreCase(o2.getName());
            });
        } else if (selectedSort.equals("Lesezeichen")) {
            filteredList.sort((o1, o2) -> {
                if (o1.isBookmarked() != o2.isBookmarked()) {
                    return o1.isBookmarked() ? -1 : 1;
                }
                int status1 = getStatus(o1);
                int status2 = getStatus(o2);
                if (status1 != status2) {
                    return Integer.compare(status1, status2);
                }
                return o1.getName().compareToIgnoreCase(o2.getName());
            });
        }

        einkaufslisteAdapter.setProductList(filteredList);
    }

    private int getStatus(ListenEintrag eintrag) {
        if (eintrag.getMengeImVorrat() == 0) {
            return 1; // Rot
        } else if (eintrag.getMengeImVorrat() <= eintrag.getMindestmenge()) {
            return 2; // Orange
        } else {
            return 3; // Schwarz
        }
    }

    @Override
    public void onEditClick(ListenEintrag eintrag) {
        showEditQuantityDialog(eintrag);
    }

    @Override
    public void onMoveToVorratClick(ListenEintrag eintrag) {
        Produkt produktToAdd = new Produkt(
                currentHaushaltId,
                eintrag.getName(),
                eintrag.getKategorie(),
                eintrag.getMindestmenge(),
                eintrag.getZielmenge(),
                eintrag.getEinheit(),
                eintrag.getSchrittweite()
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
    public void onDeleteClick(ListenEintrag eintrag) {
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
    public void onIncreaseQuantityClick(ListenEintrag eintrag) {
        int newQuantity = eintrag.getMenge() + eintrag.getSchrittweite();
        einkaufslisteRepository.updateMenge(currentHaushaltId, eintrag.getProduktId(), newQuantity);
    }

    @Override
    public void onDecreaseQuantityClick(ListenEintrag eintrag) {
        int newQuantity = eintrag.getMenge() - eintrag.getSchrittweite();
        if (newQuantity >= 0) {
            einkaufslisteRepository.updateMenge(currentHaushaltId, eintrag.getProduktId(), newQuantity);
        }
    }

    @Override
    public void onBookmarkClick(ListenEintrag eintrag, ImageButton bookmarkButton) {
        boolean isBookmarked = !eintrag.isBookmarked();
        productRepository.updateBookmarkStatus(eintrag.getProduktId(), isBookmarked, new ProductRepository.OnBookmarkUpdatedListener() {
            @Override
            public void onSuccess() {
                eintrag.setBookmarked(isBookmarked);
                if (isBookmarked) {
                    bookmarkButton.setImageResource(R.drawable.ic_bookmark_checked);
                } else {
                    bookmarkButton.setImageResource(R.drawable.ic_bookmark_unchecked);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EinkaufslisteActivity.this, "Fehler beim Aktualisieren des Lesezeichens", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAddToShoppingListClick(ListenEintrag eintrag) {
        // This method is not intended to be used in EinkaufslisteActivity
    }

    @Override
    public void onItemClick(ListenEintrag eintrag) {
        // Handle item click if needed, for now it can be empty.
    }

    private void showEditQuantityDialog(ListenEintrag eintrag) {
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
    public void onEinkaufslisteDataChanged(List<ListenEintrag> einkaufsliste) {
        alleEintraege.clear();
        alleEintraege.addAll(einkaufsliste);
        filterAndSortProdukte();
    }

    @Override
    public void onError(DatabaseError error) {
        Toast.makeText(this, "Fehler beim Laden der Einkaufsliste: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
