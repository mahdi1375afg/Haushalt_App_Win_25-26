package com.example.haushalt_app_java.vorrat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class VorratActivity extends AppCompatActivity implements ProductListAdapter.OnItemClickListener, ProductListAdapter.OnItemLongClickListener, VorratRepository.OnVorratDataChangedListener {

    private VorratRepository vorratRepository;
    private EinkaufslisteRepository einkaufslisteRepository;
    private ProductRepository productRepository;
    private ProductListAdapter vorratAdapter;
    private String currentHaushaltId;
    private Spinner spinnerKategorie;
    private ArrayList<ListenEintrag> alleEintraege = new ArrayList<>();
    private String selectedKategorie = "Alle";
    private androidx.appcompat.widget.SearchView searchView;
    private String searchQuery = "";
    private Spinner spinnerSort;
    private String selectedSort = "Alphabet";
    private boolean isSelectionMode = false;
    private List<ListenEintrag> selectedItems = new ArrayList<>();
    private LinearLayout selectionActionBar;
    private Button buttonCancel, buttonAdd, buttonDelete;
    private FloatingActionButton fabMenu;

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
        vorratAdapter.setOnItemLongClickListener(this);
        recyclerView.setAdapter(vorratAdapter);

        vorratRepository = new VorratRepository();
        einkaufslisteRepository = new EinkaufslisteRepository();


        currentHaushaltId = getIntent().getStringExtra("HAUSHALT_ID");

        if (currentHaushaltId != null && !currentHaushaltId.isEmpty()) {
            productRepository = new ProductRepository(currentHaushaltId);
            vorratRepository.getVorrat(currentHaushaltId, this);
        } else {
            Toast.makeText(this, "Haushalts-ID nicht gefunden.", Toast.LENGTH_LONG).show();
            finish();
        }

        setupKategorieSpinner();
        setupSearchView();
        setupSortSpinner();
        setupSelectionActionBar();
        setupFabMenu();

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

    private void setupFabMenu() {
        fabMenu = findViewById(R.id.pAddScreen);
        fabMenu.setOnClickListener(this::showPopupMenu);
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.vorrat_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_activate_selection_mode) {
                if (!isSelectionMode) {
                    toggleSelectionMode();
                }
                return true;
            } else if (itemId == R.id.action_fill_below_min) {
                fillItems(false);
                return true;
            } else if (itemId == R.id.action_fill_empty) {
                fillItems(true);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void fillItems(boolean emptyOnly) {
        selectedItems.clear();
        for (ListenEintrag eintrag : alleEintraege) {
            if (emptyOnly) {
                if (eintrag.getMengeImVorrat() == 0) {
                    selectedItems.add(eintrag);
                }
            } else {
                if (eintrag.getMengeImVorrat() <= eintrag.getMindestmenge()) {
                    selectedItems.add(eintrag);
                }
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Keine Produkte zum Auffüllen gefunden.", Toast.LENGTH_SHORT).show();
            return; // Nichts zu tun
        }

        showAddDialog(true);

        // Auswahlmodus nach der Aktion beenden
        if (isSelectionMode) {
            toggleSelectionMode();
        }
    }

    private void setupSortSpinner() {
        spinnerSort = findViewById(R.id.spinnerSort);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);
        spinnerSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
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
                searchQuery = newText.toLowerCase().trim();
                filterAndSortProdukte();
                return true;
            }
        });
    }


    private void setupKategorieSpinner() {
        spinnerKategorie = findViewById(R.id.spinnerKategorie);
        ProductRepository productRepository = new ProductRepository(currentHaushaltId);
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

    private void setupSelectionActionBar() {
        selectionActionBar = findViewById(R.id.selection_action_bar);
        buttonCancel = findViewById(R.id.button_cancel);
        buttonAdd = findViewById(R.id.button_add);
        buttonDelete = findViewById(R.id.button_delete);

        buttonCancel.setOnClickListener(v -> toggleSelectionMode());
        buttonAdd.setOnClickListener(v -> {
            showAddDialog(false);
        });
        buttonDelete.setOnClickListener(v -> {
            List<String> itemIds = new ArrayList<>();
            for (ListenEintrag eintrag : selectedItems) {
                itemIds.add(eintrag.getProduktId());
            }
            vorratRepository.removeVorratItems(currentHaushaltId, itemIds, new VorratRepository.OnVorratItemsRemovedListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(VorratActivity.this, "Ausgewählte Elemente gelöscht", Toast.LENGTH_SHORT).show();
                    toggleSelectionMode();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(VorratActivity.this, "Fehler beim Löschen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void toggleSelectionMode() {
        isSelectionMode = !isSelectionMode;
        vorratAdapter.setSelectionMode(isSelectionMode);
        selectionActionBar.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        findViewById(R.id.bottomNavigationView).setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
        if (!isSelectionMode) {
            selectedItems.clear();
        }
        vorratAdapter.notifyDataSetChanged();
    }

    private void toggleItemSelection(ListenEintrag eintrag) {
        if (selectedItems.contains(eintrag)) {
            selectedItems.remove(eintrag);
        } else {
            selectedItems.add(eintrag);
        }
        vorratAdapter.setSelectedItems(selectedItems);
    }

    private void filterAndSortProdukte() {
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

        vorratAdapter.setProductList(filteredList);
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
        int newQuantity = eintrag.getMenge() + eintrag.getSchrittweite();
        vorratRepository.updateMenge(currentHaushaltId, eintrag.getProduktId(), newQuantity);
    }

    @Override
    public void onDecreaseQuantityClick(ListenEintrag eintrag) {
        int newQuantity = eintrag.getMenge() - eintrag.getSchrittweite();
        if (newQuantity >= 0) {
            vorratRepository.updateMenge(currentHaushaltId, eintrag.getProduktId(), newQuantity);
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
                Toast.makeText(VorratActivity.this, "Fehler beim Aktualisieren des Lesezeichens", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAddToShoppingListClick(ListenEintrag eintrag) {
        showAddToShoppingListDialog(eintrag);
    }

    @Override
    public void onItemClick(ListenEintrag eintrag) {
        if (isSelectionMode) {
            toggleItemSelection(eintrag);
        }
    }

    @Override
    public void onItemLongClick(ListenEintrag eintrag) {
        if (!isSelectionMode) {
            toggleSelectionMode();
        }
        toggleItemSelection(eintrag);
    }

    private void showAddDialog(boolean fromMenu) {
        final CharSequence[] items = {"Alle auf Zielbestand auffüllen", "Einzeln Werte eintragen"};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, R.layout.dialog_list_item, items);

        new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                .setTitle("Mengen für gewählte Produkte bestimmen:")
                .setAdapter(adapter, (dialog, which) -> {
                    if (which == 0) { // Alle auf Zielbestand auffüllen
                        addSelectedItemsToShoppingList(true, fromMenu);
                    } else { // Einzeln Werte eintragen
                        addSelectedItemsToShoppingList(false, fromMenu);
                    }
                })
                .setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void addSelectedItemsToShoppingList(boolean fillToTarget, boolean fromMenu) {
        if (fillToTarget) {
            for (ListenEintrag eintrag : selectedItems) {
                einkaufslisteRepository.fillToTargetStock(currentHaushaltId, eintrag.getProduktId(), new EinkaufslisteRepository.OnShoppingListItemListener() {
                    @Override
                    public void onSuccess() {
                        // Optional: Show a toast or log success
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(VorratActivity.this, "Fehler beim Hinzufügen von " + eintrag.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            Toast.makeText(this, "Elemente wurden zur Einkaufsliste hinzugefügt.", Toast.LENGTH_SHORT).show();
            if (!fromMenu) {
                toggleSelectionMode();
            }
        } else {
            showIndividualQuantityListDialog(fromMenu);
        }
    }

    private void showIndividualQuantityListDialog(boolean fromMenu) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_individual_quantity_list, null);
        builder.setView(dialogView);
        builder.setTitle("Mengen für Einkaufsliste");

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewIndividualQuantity);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final IndividualQuantityAdapter adapter = new IndividualQuantityAdapter(new ArrayList<>(selectedItems));
        recyclerView.setAdapter(adapter);

        builder.setPositiveButton("Hinzufügen", (dialog, which) -> {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                IndividualQuantityAdapter.ViewHolder holder = (IndividualQuantityAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                if (holder != null) {
                    String quantityStr = holder.itemQuantity.getText().toString();
                    if (!quantityStr.isEmpty()) {
                        int quantity = Integer.parseInt(quantityStr);
                        if (quantity > 0) {
                            ListenEintrag item = adapter.getItem(i);
                            einkaufslisteRepository.setQuantityOnShoppingList(currentHaushaltId, item.getProduktId(), quantity, new EinkaufslisteRepository.OnShoppingListItemListener() {
                                @Override
                                public void onSuccess() {
                                    // Optional
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(VorratActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }
            Toast.makeText(this, "Elemente zur Einkaufsliste hinzugefügt.", Toast.LENGTH_SHORT).show();
            if (!fromMenu) {
                toggleSelectionMode();
            }
        });

        builder.setNegativeButton("Abbrechen", (dialog, which) -> {
            dialog.dismiss();
            if (!fromMenu) {
                toggleSelectionMode();
            }
        });

        builder.setCancelable(false);
        builder.show();
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
        filterAndSortProdukte();
    }

    @Override
    public void onError(DatabaseError error) {
        Toast.makeText(this, "Fehler beim Laden der Vorratliste: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
