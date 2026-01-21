package com.example.haushalt_app_java.produkt;

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

import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    private EditText productName, productMinStock, productTargetStock, productStepSize;
    private Spinner productCategorySpinner, productUnitSpinner;
    private Button cancelButton, addButton;
    private TextView labelTargetStock, labelStepSize;

    private ProductRepository productRepository;
    private String haushaltId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        haushaltId = getIntent().getStringExtra("haus_id");
        Log.d("AddProductActivity", "Received haushaltId: " + haushaltId);


        if (haushaltId == null || haushaltId.isEmpty()) {
            Toast.makeText(this, "Haushalt ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        productRepository = new ProductRepository(haushaltId);

        productName = findViewById(R.id.product_name);
        productMinStock = findViewById(R.id.product_min_stock_input);
        productTargetStock = findViewById(R.id.product_target_stock_input);
        productStepSize = findViewById(R.id.product_step_size_input);
        productCategorySpinner = findViewById(R.id.product_category_spinner);
        productUnitSpinner = findViewById(R.id.product_unit_spinner);
        cancelButton = findViewById(R.id.button_cancel);
        addButton = findViewById(R.id.button_add);
        labelTargetStock = findViewById(R.id.label_product_target_stock);
        labelStepSize = findViewById(R.id.label_product_step_size);

        //Populate Spinners
        List<String> categories = new ArrayList<>();
        for (Kategorie kategorie : Kategorie.values()) {
            categories.add(kategorie.getDisplayName());
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        productCategorySpinner.setAdapter(categoryAdapter);

        List<String> units = new ArrayList<>();
        for (Einheit einheit : Einheit.values()) {
            units.add(einheit.getDisplayName());
        }
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        productUnitSpinner.setAdapter(unitAdapter);


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProduct();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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

    private void addProduct() {
        String name = productName.getText().toString().trim();
        String minStockStr = productMinStock.getText().toString().trim();
        String targetStockStr = productTargetStock.getText().toString().trim();
        String stepSizeStr = productStepSize.getText().toString().trim();
        String categoryDisplayName = productCategorySpinner.getSelectedItem().toString();
        String unitDisplayName = productUnitSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(minStockStr) || TextUtils.isEmpty(targetStockStr) || TextUtils.isEmpty(stepSizeStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int minStock = Integer.parseInt(minStockStr);
        int targetStock = Integer.parseInt(targetStockStr);
        int stepSize = Integer.parseInt(stepSizeStr);

        // Verwende direkt die DisplayNames anstatt Enum-Namen
        Produkt product = new Produkt(haushaltId, name, categoryDisplayName, minStock, targetStock, unitDisplayName, stepSize);
        Log.d("Product", "Adding product with values: name(String): " + name + ", category(String): " + categoryDisplayName + ", unit(String): " + unitDisplayName + ", minStock(int): " + minStock + ", targetStock(int): " + targetStock + ", stepSize(int): " + stepSize);

        productRepository.addProduct(product, new ProductRepository.OnProductAddedListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddProductActivity.this, "Product added", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AddProductActivity.this, "Failed to add product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
