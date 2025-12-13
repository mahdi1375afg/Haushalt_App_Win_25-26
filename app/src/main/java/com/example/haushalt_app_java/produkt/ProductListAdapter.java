package com.example.haushalt_app_java.produkt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.einkaufsliste.EinkaufslisteEintrag;

import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductListViewHolder> {

    private List<EinkaufslisteEintrag> productList;
    private final OnItemClickListener listener;
    private final boolean isShoppingList;

    public ProductListAdapter(List<EinkaufslisteEintrag> productList, OnItemClickListener listener, boolean isShoppingList) {
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

    @Override
    public void onBindViewHolder(@NonNull ProductListViewHolder holder, int position) {
        EinkaufslisteEintrag eintrag = productList.get(position);
        holder.bind(eintrag, listener, isShoppingList);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void setProductList(List<EinkaufslisteEintrag> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onEditClick(EinkaufslisteEintrag eintrag);
        void onMoveToVorratClick(EinkaufslisteEintrag eintrag);
        void onDeleteClick(EinkaufslisteEintrag eintrag);
        void onIncreaseQuantityClick(EinkaufslisteEintrag eintrag);
        void onDecreaseQuantityClick(EinkaufslisteEintrag eintrag);
        void onBookmarkClick(EinkaufslisteEintrag eintrag, ImageButton bookmarkButton);
        void onAddToShoppingListClick(EinkaufslisteEintrag eintrag);
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

        public ProductListViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductCategory = itemView.findViewById(R.id.textViewProductCategory);
            textViewProductUnit = itemView.findViewById(R.id.textViewProductUnit);
            textViewProductQuantity = itemView.findViewById(R.id.textViewProductQuantity);
            buttonEdit = itemView.findViewById(R.id.button_edit_product);
            buttonDelete = itemView.findViewById(R.id.button_delete_product);
            buttonIncreaseQuantity = itemView.findViewById(R.id.button_increase_quantity);
            buttonDecreaseQuantity = itemView.findViewById(R.id.button_decrease_quantity);
            buttonMoveToVorrat = itemView.findViewById(R.id.button_move_to_vorrat);
            buttonAddToShoppingList = itemView.findViewById(R.id.button_add_to_shopping_list);
            buttonBookmark = itemView.findViewById(R.id.button_bookmark);
        }

        public void bind(final EinkaufslisteEintrag eintrag, final OnItemClickListener listener, boolean isShoppingList) {
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
