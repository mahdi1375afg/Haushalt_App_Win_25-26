package com.example.haushalt_app_java.product_activity;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.domain.EinkaufslisteEintrag;
import com.example.haushalt_app_java.domain.Produkt;
import com.example.haushalt_app_java.domain.ShoppingListRepository;

import java.util.ArrayList;

public class ProductAdapter extends ArrayAdapter<Produkt> {

    private String haushaltId;

    public ProductAdapter(@NonNull Context context, ArrayList<Produkt> produkte, String haushaltId) {
        super(context, 0, produkte);
        this.haushaltId = haushaltId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Produkt produkt = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_product, parent, false);
        }

        TextView productText = convertView.findViewById(R.id.product_text);
        Button addToShoppingListButton = convertView.findViewById(R.id.add_to_shopping_list_button);

        if (produkt != null) {
            String name = produkt.getName() != null ? produkt.getName() : "";
            String einheit = produkt.getEinheit() != null ? produkt.getEinheit() : "";
            String kategorie = produkt.getKategorie() != null ? produkt.getKategorie() : "";
            String txt = name + " - " + einheit + " - " + kategorie;
            productText.setText(txt);

            addToShoppingListButton.setOnClickListener(v -> {
                showShoppingListOrStockDialog(produkt);
            });
        }

        return convertView;
    }

    private void showShoppingListOrStockDialog(Produkt produkt) {
        new AlertDialog.Builder(getContext(), R.style.AlertDialogCustom) // Apply custom style here
            .setTitle("Hinzufügen zu...")
            .setItems(new CharSequence[]{"Einkaufsliste", "Lagerbestand"}, (dialog, which) -> {
                if (which == 0) { // Einkaufsliste
                    showQuantityDialog(produkt);
                } else { // Lagerbestand
                    // TODO: Implement logic to add to stock
                    Toast.makeText(getContext(), "Add to stock clicked for " + produkt.getName(), Toast.LENGTH_SHORT).show();
                }
            })
            .show();
    }

    private void showQuantityDialog(Produkt produkt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogCustom); // Apply custom style here as well
        builder.setTitle("Menge eingeben");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        // Set text color for input for consistency with dark background
        input.setTextColor(getContext().getResources().getColor(android.R.color.white));
        input.setHintTextColor(getContext().getResources().getColor(android.R.color.darker_gray));
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            int quantity = Integer.parseInt(input.getText().toString());
            EinkaufslisteEintrag item = new EinkaufslisteEintrag(produkt, quantity);
            ShoppingListRepository shoppingListRepository = new ShoppingListRepository(haushaltId);
            shoppingListRepository.addShoppingListItem(item, new ShoppingListRepository.OnShoppingListItemAddedListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), produkt.getName() + " zur Einkaufsliste hinzugefügt", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Fehler beim Hinzufügen zur Einkaufsliste", Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
