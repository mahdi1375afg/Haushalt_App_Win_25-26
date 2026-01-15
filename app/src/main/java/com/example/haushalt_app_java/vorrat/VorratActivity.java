package com.example.haushalt_app_java.vorrat;

import android.annotation.SuppressLint;
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
import com.example.haushalt_app_java.einkaufsliste.EinkaufslisteRepository;
import com.example.haushalt_app_java.produkt.ProductListAdapter;
import com.example.haushalt_app_java.einkaufsliste.EinkaufslisteActivity;
import com.example.haushalt_app_java.einkaufsliste.ListenEintrag;
import com.example.haushalt_app_java.haushalt.HaushaltActivity;
import com.example.haushalt_app_java.produkt.ProductActivity;
import com.example.haushalt_app_java.produkt.ProductRepository;
import com.example.haushalt_app_java.produkt.Produkt;
import com.example.haushalt_app_java.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class VorratActivity extends AppCompatActivity implements ProductListAdapter.OnItemClickListener, VorratRepository.OnVorratDataChangedListener {

    private VorratRepository vorratRepository;
    private EinkaufslisteRepository einkaufslisteRepository;
    private ProductListAdapter vorratAdapter;
    private String currentHaushaltId;
    private Spinner spinnerKategorie;
    private ArrayList<ListenEintrag> alleEintraege = new ArrayList<>();
    private String selectedKategorie = "Alle";
    private androidx.appcompat.widget.SearchView searchView;
    private String searchQuery = "";
    private View layoutSelectionActions;
    private boolean selectionModeActive = false;
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
        title.setText("Vorrat");

        RecyclerView recyclerView = findViewById(R.id.einkaufslisteRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        vorratAdapter = new ProductListAdapter(new ArrayList<>(), this, false);
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

        setupKategorieSpinner();
        setupSearchView();

        setupSelectionMode();

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

    private void setupSelectionMode() {
        layoutSelectionActions = findViewById(R.id.layoutSelectionActions);

        // Lang-Klick aktiviert Auswahlmodus
        RecyclerView recyclerView = findViewById(R.id.einkaufslisteRecyclerView);
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener());

        findViewById(R.id.btnCancelSelection).setOnClickListener(v -> {
            disableSelectionMode();
        });

        findViewById(R.id.btnAddSelectedToShoppingList).setOnClickListener(v -> {
            addSelectedToShoppingList();
        });

        findViewById(R.id.btnDeleteSelected).setOnClickListener(v -> {
            deleteSelectedProducts();
        });

        // Toolbar-Button für Auswahlmodus
        ImageButton btnSelectMode = findViewById(R.id.btnSelectMode);
        if (btnSelectMode != null) {
            btnSelectMode.setOnClickListener(v -> {
                toggleSelectionMode();
            });
        }
    }

    private void toggleSelectionMode() {
        if (selectionModeActive) {
            disableSelectionMode();
        } else {
            enableSelectionMode();
        }
    }

    private void enableSelectionMode() {
        selectionModeActive = true;
        vorratAdapter.setSelectionMode(true);
        layoutSelectionActions.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Auswahlmodus aktiviert", Toast.LENGTH_SHORT).show();
    }

    private void disableSelectionMode() {
        selectionModeActive = false;
        vorratAdapter.setSelectionMode(false);
        layoutSelectionActions.setVisibility(View.GONE);
    }

    private void addSelectedToShoppingList() {
        ArrayList<String> selectedIds = vorratAdapter.getSelectedProductIds();

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Keine Produkte ausgewählt", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle("Zur Einkaufsliste hinzufügen");
        builder.setMessage(selectedIds.size() + " Produkt(e) zur Einkaufsliste hinzufügen?");

        builder.setPositiveButton("Bis Zielbestand", (dialog, which) -> {
            addSelectedProductsToList(selectedIds, true);
        });


        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void addSelectedProductsToList(ArrayList<String> selectedIds, boolean useZielbestand) {
        int totalProducts = selectedIds.size();
        final int[] successCount = {0};
        final int[] errorCount = {0};

        ProductRepository productRepository = new ProductRepository(currentHaushaltId);

        for (String produktId : selectedIds) {
            // Finde den Eintrag in alleEintraege
            ListenEintrag eintrag = null;
            for (ListenEintrag e : alleEintraege) {
                if (e.getProduktId().equals(produktId)) {
                    eintrag = e;
                    break;
                }
            }

            if (eintrag == null) continue;

            final ListenEintrag finalEintrag = eintrag;

            productRepository.getProductById(produktId, new ProductRepository.OnProductLoadedListener() {
                @Override
                public void onSuccess(Produkt produkt) {
                    int quantity = useZielbestand ?
                            Math.max(0, produkt.getZielbestand() - finalEintrag.getMenge()) : 1;

                    if (quantity > 0) {
                        einkaufslisteRepository.setQuantityOnShoppingList(
                                currentHaushaltId, produktId, quantity,
                                new EinkaufslisteRepository.OnShoppingListItemListener() {
                                    @Override
                                    public void onSuccess() {
                                        successCount[0]++;
                                        checkCompletion();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        errorCount[0]++;
                                        checkCompletion();
                                    }

                                    private void checkCompletion() {
                                        if (successCount[0] + errorCount[0] == totalProducts) {
                                            String message = successCount[0] + " Produkt(e) hinzugefügt";
                                            if (errorCount[0] > 0) {
                                                message += " (" + errorCount[0] + " Fehler)";
                                            }
                                            Toast.makeText(VorratActivity.this, message, Toast.LENGTH_SHORT).show();
                                            disableSelectionMode();
                                        }
                                    }
                                });
                    } else {
                        successCount[0]++;
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    errorCount[0]++;
                }
            });
        }
    }

    private void deleteSelectedProducts() {
        ArrayList<String> selectedIds = vorratAdapter.getSelectedProductIds();

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Keine Produkte ausgewählt", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle("Produkte löschen");
        builder.setMessage("Wirklich " + selectedIds.size() + " Produkt(e) löschen?");

        builder.setPositiveButton("Löschen", (dialog, which) -> {
            int totalProducts = selectedIds.size();
            final int[] successCount = {0};
            final int[] errorCount = {0};

            for (String produktId : selectedIds) {
                vorratRepository.removeVorratItem(currentHaushaltId, produktId,
                        new VorratRepository.OnVorratItemRemovedListener() {
                            @Override
                            public void onVorratDataChanged(List<ListenEintrag> vorratliste) {
                                successCount[0]++;
                                checkCompletion();
                            }

                            @Override
                            public void onError(DatabaseError error) {
                                errorCount[0]++;
                                checkCompletion();
                            }

                            private void checkCompletion() {
                                if (successCount[0] + errorCount[0] == totalProducts) {
                                    String message = successCount[0] + " Produkt(e) gelöscht";
                                    if (errorCount[0] > 0) {
                                        message += " (" + errorCount[0] + " Fehler)";
                                    }
                                    Toast.makeText(VorratActivity.this, message, Toast.LENGTH_SHORT).show();
                                    disableSelectionMode();
                                }
                            }
                        });
            }
        });

        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());
        builder.show();
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
                searchQuery = newText.toLowerCase().trim();
                filterProdukte();
                return true;
            }
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
        ArrayList<ListenEintrag> filteredList = new ArrayList<>();

        for (ListenEintrag eintrag : alleEintraege) {
            // Kategorie-Filter
            boolean kategorieMatch = selectedKategorie.equals("Alle") ||
                                    eintrag.getKategorie().equals(selectedKategorie);

            // Such-Filter
            boolean searchMatch = searchQuery.isEmpty() ||
                                 eintrag.getName().toLowerCase().contains(searchQuery);

            // Nur hinzufügen, wenn beide Filter zutreffen
            if (kategorieMatch && searchMatch) {
                filteredList.add(eintrag);
            }
        }

        vorratAdapter.setProductList(filteredList);
    }

    @Override
    public void onEditClick(ListenEintrag eintrag) {
        showEditQuantityDialog(eintrag);
    }

    @Override
    public void onMoveToVorratClick(ListenEintrag eintrag) {
        // This method is not intended to be used in VorratActivity
    }

    @Override
    public void onDeleteClick(ListenEintrag eintrag) {
        vorratRepository.removeVorratItem(currentHaushaltId, eintrag.getProduktId(), new VorratRepository.OnVorratItemRemovedListener() {
            @Override
            public void onVorratDataChanged(List<ListenEintrag> vorratliste) {

            }

            @Override
            public void onError(DatabaseError error) {

            }

        });
    }

    @Override
    public void onIncreaseQuantityClick(ListenEintrag eintrag) {
        int newQuantity = eintrag.getMenge() + 1;
        vorratRepository.updateMenge(currentHaushaltId, eintrag.getProduktId(), newQuantity);
    }

    @Override
    public void onDecreaseQuantityClick(ListenEintrag eintrag) {
        int newQuantity = eintrag.getMenge() - 1;
        if (newQuantity >= 0) {
            vorratRepository.updateMenge(currentHaushaltId, eintrag.getProduktId(), newQuantity);
        }
    }

    @Override
    public void onBookmarkClick(ListenEintrag eintrag, ImageButton bookmarkButton) {
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
    public void onAddToShoppingListClick(ListenEintrag eintrag) {
        showAddToShoppingListDialog(eintrag);
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
                vorratRepository.updateMenge(currentHaushaltId, eintrag.getProduktId(), newQuantity);
            }
        });
        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAddToShoppingListDialog(ListenEintrag eintrag) {
        ProductRepository productRepository = new ProductRepository(currentHaushaltId);
        productRepository.getProductById(eintrag.getProduktId(), new ProductRepository.OnProductLoadedListener() {
            @Override
            public void onSuccess(Produkt produkt) {
                // All dialog logic is now safely inside the onSuccess callback
                AlertDialog.Builder builder = new AlertDialog.Builder(VorratActivity.this, R.style.AlertDialogCustom);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_edit_quantity, null);
                builder.setView(dialogView);
                builder.setTitle("Menge für Einkaufsliste");

                final EditText editTextQuantity = dialogView.findViewById(R.id.editTextQuantity);
                editTextQuantity.setText("1");

                EinkaufslisteRepository.OnShoppingListItemListener itemListener = new EinkaufslisteRepository.OnShoppingListItemListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(VorratActivity.this, produkt.getName() + " zur Einkaufsliste hinzugefügt", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(VorratActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                };

                builder.setPositiveButton("Hinzufügen", (dialog, which) -> {
                    String newQuantityStr = editTextQuantity.getText().toString();
                    if (!newQuantityStr.isEmpty()) {
                        int newQuantity = Integer.parseInt(newQuantityStr);
                        einkaufslisteRepository.setQuantityOnShoppingList(currentHaushaltId, produkt.getProdukt_id(), newQuantity, itemListener);
                    }
                });

                builder.setNeutralButton("Zielbestand auffüllen", (dialog, which) -> {
                    einkaufslisteRepository.setQuantityOnShoppingList(currentHaushaltId, produkt.getProdukt_id(), produkt.getZielbestand()-eintrag.getMenge(), itemListener);
                });

                builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = builder.create();
                dialog.show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(VorratActivity.this, "Error loading product details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onVorratDataChanged(List<ListenEintrag> vorratliste) {
        alleEintraege.clear();
        alleEintraege.addAll(vorratliste);
        filterProdukte();
    }

    @Override
    public void onError(DatabaseError error) {
        Toast.makeText(this, "Fehler beim Laden der Vorratliste: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
