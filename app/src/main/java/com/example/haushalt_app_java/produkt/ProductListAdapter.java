package com.example.haushalt_app_java.produkt;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.einkaufsliste.ListenEintrag;

import java.util.ArrayList;
import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductListViewHolder> {

    private List<ListenEintrag> productList;
    private final OnItemClickListener listener;
    private final boolean isShoppingList;
    private boolean selectionMode = false;
    private ArrayList<String> selectedProductIds = new ArrayList<>();
    public ProductListAdapter(List<ListenEintrag> productList, OnItemClickListener listener, boolean isShoppingList) {
        this.productList = productList;
        this.listener = listener;
        this.isShoppingList = isShoppingList;
    }

    @NonNull
    @Override
    public ProductListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_list, parent, false);
        return new ProductListViewHolder(view);
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        if (!selectionMode) {
            selectedProductIds.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public ArrayList<String> getSelectedProductIds() {
        return selectedProductIds;
    }

    public int getSelectedCount() {
        return selectedProductIds.size();
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull ProductListViewHolder holder, int position) {
        ListenEintrag eintrag = productList.get(position);
        holder.bind(eintrag, listener, isShoppingList);

        // Button-Sichtbarkeit basierend auf dem Kontext
        if (isShoppingList) {
            // In Einkaufsliste: nur button_move_to_vorrat anzeigen
            holder.buttonMoveToVorrat.setVisibility(View.VISIBLE);
            holder.buttonAddToShoppingList.setVisibility(View.GONE);
        } else {
            // Im Vorrat: nur button_add_to_shopping_list anzeigen
            holder.buttonMoveToVorrat.setVisibility(View.GONE);
            holder.buttonAddToShoppingList.setVisibility(View.VISIBLE);
        }


        // Auswahlmodus-Anzeige
        if (selectionMode) {
            holder.itemView.setBackgroundColor(
                    selectedProductIds.contains(eintrag.getProduktId()) ?
                            Color.parseColor("#E3F2FD") : Color.WHITE
            );

            holder.itemView.setOnClickListener(v -> {
                String produktId = eintrag.getProduktId();
                if (selectedProductIds.contains(produktId)) {
                    selectedProductIds.remove(produktId);
                } else {
                    selectedProductIds.add(produktId);
                }
                notifyItemChanged(position);
            });

            // Alle Buttons im Auswahlmodus ausblenden
            holder.buttonDeleteItem.setVisibility(View.GONE);
            holder.buttonIncreaseQuantity.setVisibility(View.GONE);
            holder.buttonDecreaseQuantity.setVisibility(View.GONE);
            holder.buttonBookmark.setVisibility(View.GONE);
            holder.buttonEdit.setVisibility(View.GONE);
            holder.buttonMoveToVorrat.setVisibility(View.GONE);
            holder.buttonAddToShoppingList.setVisibility(View.GONE);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.itemView.setOnClickListener(null);

            // Andere Buttons im normalen Modus anzeigen
            holder.buttonDeleteItem.setVisibility(View.VISIBLE);
            holder.buttonIncreaseQuantity.setVisibility(View.VISIBLE);
            holder.buttonDecreaseQuantity.setVisibility(View.VISIBLE);
            holder.buttonBookmark.setVisibility(View.VISIBLE);
            holder.buttonEdit.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void setProductList(List<ListenEintrag> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onEditClick(ListenEintrag eintrag);
        void onMoveToVorratClick(ListenEintrag eintrag);
        void onDeleteClick(ListenEintrag eintrag);
        void onIncreaseQuantityClick(ListenEintrag eintrag);
        void onDecreaseQuantityClick(ListenEintrag eintrag);
        void onBookmarkClick(ListenEintrag eintrag, ImageButton bookmarkButton);
        void onAddToShoppingListClick(ListenEintrag eintrag);
    }

    static class ProductListViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewProductName;
        private final TextView textViewProductCategory;
        private final TextView textViewProductUnit;
        private final TextView textViewProductQuantity;
        private final ImageButton buttonEdit;
        private final ImageButton buttonDelete;
        private final ImageButton buttonIncreaseQuantity;
        private final ImageButton buttonDecreaseQuantity;
        private final ImageButton buttonMoveToVorrat;
        private final ImageButton buttonAddToShoppingList;
        private final ImageButton buttonBookmark;
        private final ImageButton buttonDeleteItem;  // Geändert von MediaRouteButton

        public ProductListViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductCategory = itemView.findViewById(R.id.textViewProductCategory);
            textViewProductUnit = itemView.findViewById(R.id.textViewProductUnit);
            textViewProductQuantity = itemView.findViewById(R.id.textViewProductQuantity);
            buttonEdit = itemView.findViewById(R.id.button_edit_product);
            buttonDelete = itemView.findViewById(R.id.button_delete_product);
            buttonDeleteItem = buttonDelete;  // buttonDeleteItem verweist auf buttonDelete
            buttonIncreaseQuantity = itemView.findViewById(R.id.button_increase_quantity);
            buttonDecreaseQuantity = itemView.findViewById(R.id.button_decrease_quantity);
            buttonMoveToVorrat = itemView.findViewById(R.id.button_move_to_vorrat);
            buttonAddToShoppingList = itemView.findViewById(R.id.button_add_to_shopping_list);
            buttonBookmark = itemView.findViewById(R.id.button_bookmark);
        }

        public void bind(final ListenEintrag eintrag, final OnItemClickListener listener, boolean isShoppingList) {
            textViewProductName.setText(eintrag.getName());
            textViewProductCategory.setText(eintrag.getKategorie());
            textViewProductUnit.setText(eintrag.getEinheit());
            textViewProductQuantity.setText(String.valueOf(eintrag.getMenge()));

            buttonEdit.setOnClickListener(v -> listener.onEditClick(eintrag));
            buttonDelete.setOnClickListener(v -> listener.onDeleteClick(eintrag));
            buttonIncreaseQuantity.setOnClickListener(v -> listener.onIncreaseQuantityClick(eintrag));
            buttonDecreaseQuantity.setOnClickListener(v -> listener.onDecreaseQuantityClick(eintrag));
            buttonBookmark.setOnClickListener(v -> listener.onBookmarkClick(eintrag, buttonBookmark));

            if (isShoppingList) {
                buttonMoveToVorrat.setVisibility(View.VISIBLE);
                buttonAddToShoppingList.setVisibility(View.GONE);
                buttonMoveToVorrat.setOnClickListener(v -> listener.onMoveToVorratClick(eintrag));
            } else {
                buttonMoveToVorrat.setVisibility(View.GONE);
                buttonAddToShoppingList.setVisibility(View.VISIBLE);
                buttonAddToShoppingList.setOnClickListener(v -> listener.onAddToShoppingListClick(eintrag));
            }
        }
    }
}
