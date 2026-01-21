package com.example.haushalt_app_java.produkt;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.domain.Einheit;
import com.example.haushalt_app_java.domain.Kategorie;
import com.example.haushalt_app_java.produkt.Produkt;
import com.example.haushalt_app_java.produkt.ProductRepository;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateProductActivity extends AppCompatActivity {

    private EditText productName, productMinStock, productTargetStock, productStepSize;
    private Spinner productCategorySpinner, productUnitSpinner;
    private Button btnUpdate, btnDelete, btnCancel;
    private TextView labelTargetStock, labelStepSize;

    private ProductRepository productRepository;
    private String produktId;
    private String hausId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

        produktId = getIntent().getStringExtra("produkt_id");
        hausId = getIntent().getStringExtra("haus_id");

        if (TextUtils.isEmpty(hausId) || TextUtils.isEmpty(produktId)) {
            Toast.makeText(this, "Haushalts- oder Produkt-ID fehlt", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        productRepository = new ProductRepository(hausId);

        productName = findViewById(R.id.product_name);
        productMinStock = findViewById(R.id.product_min_stock_input);
        productTargetStock = findViewById(R.id.product_target_stock_input);
        productStepSize = findViewById(R.id.product_step_size_input);
        productCategorySpinner = findViewById(R.id.product_category_spinner);
        productUnitSpinner = findViewById(R.id.product_unit_spinner);
        btnUpdate = findViewById(R.id.button_update);
        btnDelete = findViewById(R.id.button_delete);
        btnCancel = findViewById(R.id.button_cancel);
        labelTargetStock = findViewById(R.id.label_product_target_stock);
        labelStepSize = findViewById(R.id.label_product_step_size);

        // Populate Spinners and set initial values
        populateAndSetSpinners();

        // Pre-fill fields
        productName.setText(getIntent().getStringExtra("name"));
        productMinStock.setText(String.valueOf(getIntent().getIntExtra("mindBestand", 0)));
        productTargetStock.setText(String.valueOf(getIntent().getIntExtra("zielbestand", 0)));
        productStepSize.setText(String.valueOf(getIntent().getIntExtra("schrittweite", 1)));

        btnUpdate.setOnClickListener(v -> updateProduct());
        btnDelete.setOnClickListener(v -> deleteProduct());
        btnCancel.setOnClickListener(v -> finish());

        labelTargetStock.setOnClickListener(v -> showTooltip("Zielbestand", getString(R.string.tooltip_zielbestand)));
        labelStepSize.setOnClickListener(v -> showTooltip("Schrittweite", getString(R.string.tooltip_schrittweite)));
    }

    private void showTooltip(String fieldName, String text) {
        new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                .setTitle("Information: " + fieldName)
                .setMessage(text)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void populateAndSetSpinners() {
        // Category Spinner
        List<String> categories = new ArrayList<>();
        for (Kategorie kategorie : Kategorie.values()) {
            categories.add(kategorie.getDisplayName());
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, categories);
        categoryAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        productCategorySpinner.setAdapter(categoryAdapter);

        String currentCategory = getIntent().getStringExtra("kategorie");
        if (currentCategory != null) {
            int categoryPosition = categoryAdapter.getPosition(currentCategory);
            productCategorySpinner.setSelection(categoryPosition);
        }

        // Unit Spinner
        List<String> units = new ArrayList<>();
        for (Einheit einheit : Einheit.values()) {
            units.add(einheit.getDisplayName());
        }
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, units);
        unitAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        productUnitSpinner.setAdapter(unitAdapter);

        String currentUnit = getIntent().getStringExtra("einheit");
        if (currentUnit != null) {
            int unitPosition = unitAdapter.getPosition(currentUnit);
            productUnitSpinner.setSelection(unitPosition);
        }
    }

    private void updateProduct() {
        String name = productName.getText().toString().trim();
        String minStockStr = productMinStock.getText().toString().trim();
        String targetStockStr = productTargetStock.getText().toString().trim();
        String stepSizeStr = productStepSize.getText().toString().trim();
        String categoryDisplayName = productCategorySpinner.getSelectedItem().toString();
        String unitDisplayName = productUnitSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(minStockStr) || TextUtils.isEmpty(targetStockStr) || TextUtils.isEmpty(stepSizeStr)) {
            Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
            return;
        }

        int minStock = Integer.parseInt(minStockStr);
        int targetStock = Integer.parseInt(targetStockStr);
        int stepSize = Integer.parseInt(stepSizeStr);

        Produkt updatedProduct = new Produkt(hausId, name, categoryDisplayName, minStock, targetStock, unitDisplayName, stepSize);
        updatedProduct.setProdukt_id(produktId);

        productRepository.updateProduct(updatedProduct, new ProductRepository.OnProductUpdatedListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(UpdateProductActivity.this, "Produkt aktualisiert", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UpdateProductActivity.this, "Fehler beim Aktualisieren des Produkts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProduct() {
        productRepository.deleteProduct(produktId, new ProductRepository.OnProductDeletedListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(UpdateProductActivity.this, "Produkt gelöscht", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UpdateProductActivity.this, "Fehler beim Löschen des Produkts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
