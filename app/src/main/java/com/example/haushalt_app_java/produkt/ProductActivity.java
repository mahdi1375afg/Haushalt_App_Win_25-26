package com.example.haushalt_app_java.produkt;

import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputType;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.StartActivity;
import com.example.haushalt_app_java.einkaufsliste.EinkaufslisteRepository;
import com.example.haushalt_app_java.vorrat.VorratRepository;
import com.example.haushalt_app_java.einkaufsliste.EinkaufslisteActivity;
import com.example.haushalt_app_java.vorrat.VorratActivity;
import com.example.haushalt_app_java.domain.Kategorie;
import com.example.haushalt_app_java.haushalt.HaushaltActivity;
import com.example.haushalt_app_java.notification.DatabaseChangeService;
import com.example.haushalt_app_java.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProductActivity extends AppCompatActivity implements MainProductListAdapter.OnItemClickListener {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private static final int REQ_UPDATE = 1001;

    private Button logout;
    private String currentHausId;
    private FirebaseDatabase database;
    private FloatingActionButton pAddScreen;
    private RecyclerView productRecyclerView;
    private ArrayList<Produkt> productList = new ArrayList<>();
    private MainProductListAdapter productAdapter;
    private Spinner spinnerKategorie;
    private ArrayList<Produkt> alleProdukte = new ArrayList<>();
    private String selectedKategorie = "Alle";

    private ProductRepository productRepository;
    private EinkaufslisteRepository einkaufslisteRepository;
    private VorratRepository vorratRepository;
    private SearchView searchView;
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm.isIgnoringBatteryOptimizations(getPackageName())) {
            Intent serviceIntent = new Intent(this, DatabaseChangeService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        }

        database = FirebaseDatabase.getInstance(DB_URL);

        loadHausIdAndInitialize();
    }

    private void loadHausIdAndInitialize() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference benutzerRef = database.getReference()
                .child("Benutzer")
                .child(currentUserId)
                .child("hausId");

        benutzerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentHausId = snapshot.getValue(String.class);

                    if (currentHausId == null || currentHausId.isEmpty()) {
                        Toast.makeText(ProductActivity.this, "Bitte wähle einen Haushalt", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ProductActivity.this, HaushaltActivity.class));
                        finish();
                    }

                    productRepository = new ProductRepository(currentHausId);
                    einkaufslisteRepository = new EinkaufslisteRepository();
                    vorratRepository = new VorratRepository();
                    com.example.haushalt_app_java.utils.HausIdManager.getInstance().setHausId(currentHausId);

                    initializeUI();
                    loadProducts();
                } else {
                    Toast.makeText(ProductActivity.this, "Kein Haushalt zugewiesen", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ProductActivity.this, HaushaltActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Fehler beim Laden der hausId: " + error.getMessage());
                Toast.makeText(ProductActivity.this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeUI() {
        logout = findViewById(R.id.logout);
        pAddScreen = findViewById(R.id.pAddScreen);
        productRecyclerView = findViewById(R.id.productRecyclerView);
        spinnerKategorie = findViewById(R.id.spinnerKategorie);
        searchView = findViewById(R.id.searchView);
        setupKategorieSpinner();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText.toLowerCase();
                filterProdukte();
                return true;
            }
        });

        pAddScreen.setOnClickListener(v -> {
            Intent intent = new Intent(ProductActivity.this, AddProductActivity.class);
            intent.putExtra("haus_id", currentHausId);
            startActivity(intent);
        });

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(ProductActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProductActivity.this, StartActivity.class));
            finish();
        });

        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new MainProductListAdapter(productList, this);
        productRecyclerView.setAdapter(productAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_products) {
                return true;
            } else if (itemId == R.id.nav_household) {
                startActivity(new Intent(ProductActivity.this, HaushaltActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(ProductActivity.this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_einkaufslisten) {
                Intent intent = new Intent(ProductActivity.this, EinkaufslisteActivity.class);
                intent.putExtra("HAUSHALT_ID", currentHausId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_vorrat) {
                Intent intent = new Intent(ProductActivity.this, VorratActivity.class);
                intent.putExtra("HAUSHALT_ID", currentHausId);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void setupKategorieSpinner() {
        ArrayList<String> kategorien = new ArrayList<>();
        kategorien.add("Alle");
        kategorien.add(Kategorie.LEBENSMITTEL.getDisplayName());
        kategorien.add(Kategorie.GETRAENKE.getDisplayName());
        kategorien.add(Kategorie.HYGIENE.getDisplayName());
        kategorien.add(Kategorie.HAUSHALT.getDisplayName());
        kategorien.add(Kategorie.SONSTIGES.getDisplayName());

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

    private void loadProducts() {
        DatabaseReference produkteRef = database.getReference()
                .child("Haushalte")
                .child(currentHausId)
                .child("produkte");

        produkteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alleProdukte.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Produkt produkt = snap.getValue(Produkt.class);
                    if (produkt == null) continue;

                    produkt.setProdukt_id(snap.getKey());
                    produkt.setHaus_id(currentHausId);
                    alleProdukte.add(produkt);
                }

                filterProdukte();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Fehler beim Laden: " + error.getMessage());
            }
        });
    }

    private void filterProdukte() {
        productList.clear();

        for (Produkt produkt : alleProdukte) {
            String produktKategorie = produkt.getKategorie() != null ? produkt.getKategorie() : "";

            // Kategoriefilter
            boolean kategorieMatch = selectedKategorie.equals("Alle")
                    || produktKategorie.equals(selectedKategorie)
                    || produktKategorie.equals(getEnumNameForDisplayName(selectedKategorie));

            // Suchfilter
            boolean searchMatch = searchQuery.isEmpty()
                    || produkt.getName().toLowerCase().contains(searchQuery);

            if (kategorieMatch && searchMatch) {
                productList.add(produkt);
            }
        }

        productAdapter.notifyDataSetChanged();
    }

    private String getEnumNameForDisplayName(String displayName) {
        for (Kategorie k : Kategorie.values()) {
            if (k.getDisplayName().equals(displayName)) {
                return k.name();
            }
        }
        return displayName;
    }

    @Override
    public void onDeleteClick(Produkt produkt) {
        new AlertDialog.Builder(this,R.style.AlertDialogCustom)
                .setTitle("Produkt löschen")
                .setMessage("Sind Sie sicher, dass Sie dieses Produkt löschen möchten?\n\nMit löschen werden alle zugehörigen Einträge im Vorrat und der Einkaufsliste entfernt.")
                .setPositiveButton("Löschen", (dialog, which) -> {
                    productRepository.deleteProduct(produkt.getProdukt_id(), new ProductRepository.OnProductDeletedListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(ProductActivity.this, "Produkt gelöscht", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ProductActivity.this, "Fehler beim Löschen des Produkts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    @Override
    public void onEditClick(Produkt produkt) {
        Intent i = new Intent(ProductActivity.this, UpdateProductActivity.class);
        i.putExtra("produkt_id", produkt.getProdukt_id());
        i.putExtra("haus_id", produkt.getHaus_id());
        i.putExtra("name", produkt.getName());
        i.putExtra("einheit", produkt.getEinheit());
        i.putExtra("kategorie", produkt.getKategorie());
        i.putExtra("mindBestand", produkt.getMindBestand());
        i.putExtra("zielbestand", produkt.getZielbestand());
        startActivityForResult(i, REQ_UPDATE);
    }

    @Override
    public void onAddToCartClick(Produkt produkt) {
        showShoppingListOrStockDialog(produkt);
    }

    @Override
    public void onBookmarkClick(Produkt produkt, ImageButton bookmarkButton) {
        boolean isBookmarked = !produkt.isBookmarked();
        produkt.setBookmarked(isBookmarked);
        productRepository.updateBookmarkStatus(produkt.getProdukt_id(), isBookmarked);

        if (isBookmarked) {
            bookmarkButton.setImageResource(R.drawable.ic_bookmark_checked);
        } else {
            bookmarkButton.setImageResource(R.drawable.ic_bookmark_unchecked);
        }
    }

    private void showShoppingListOrStockDialog(Produkt produkt) {
        new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                .setTitle("Hinzufügen zu...")
                .setItems(new CharSequence[]{"Einkaufsliste", "Vorrat"}, (dialog, which) -> {
                    if (which == 0) { // Einkaufsliste
                        showQuantityDialogForShoppingList(produkt);
                    } else { // Vorrat
                        showQuantityDialogForStock(produkt);
                    }
                })
                .show();
    }

    private void showQuantityDialogForShoppingList(Produkt produkt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle("Menge eingeben");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextColor(getResources().getColor(android.R.color.white));
        input.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            int quantity = Integer.parseInt(input.getText().toString());
            einkaufslisteRepository.addShoppingListItem(currentHausId, produkt.getProdukt_id(), quantity, new EinkaufslisteRepository.OnShoppingListItemListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ProductActivity.this, produkt.getName() + " zur Einkaufsliste hinzugefügt", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ProductActivity.this, "Fehler beim Hinzufügen zur Einkaufsliste: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showQuantityDialogForStock(Produkt produkt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle("Menge eingeben");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextColor(getResources().getColor(android.R.color.white));
        input.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            int quantity = Integer.parseInt(input.getText().toString());
            vorratRepository.addVorratItem(currentHausId, produkt, quantity, new VorratRepository.OnVorratItemAddedListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ProductActivity.this, produkt.getName() + " zum Vorrat hinzugefügt", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ProductActivity.this, "Fehler beim Hinzufügen zum Vorrat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
