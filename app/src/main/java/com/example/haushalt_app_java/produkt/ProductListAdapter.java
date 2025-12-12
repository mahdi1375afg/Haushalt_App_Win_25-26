package com.example.haushalt_app_java.produkt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
    }

    static class ProductListViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewProductName;
        private final TextView textViewProductCategory;
        private final TextView textViewProductUnit;
        private final TextView textViewProductQuantity;
        private final Button buttonEditQuantity;
        private final CheckBox checkBoxMoveToVorrat;

        public ProductListViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductCategory = itemView.findViewById(R.id.textViewProductCategory);
            textViewProductUnit = itemView.findViewById(R.id.textViewProductUnit);
            textViewProductQuantity = itemView.findViewById(R.id.textViewProductQuantity);
            buttonEditQuantity = itemView.findViewById(R.id.buttonEditQuantity);
            checkBoxMoveToVorrat = itemView.findViewById(R.id.checkBoxMoveToVorrat);
        }

        public void bind(final EinkaufslisteEintrag eintrag, final OnItemClickListener listener, boolean isShoppingList) {
            textViewProductName.setText(eintrag.getName());
            textViewProductCategory.setText(eintrag.getKategorie());
            textViewProductUnit.setText(eintrag.getEinheit());
            textViewProductQuantity.setText(String.valueOf(eintrag.getMenge()));

            buttonEditQuantity.setOnClickListener(v -> listener.onEditClick(eintrag));

            if (isShoppingList) {
                checkBoxMoveToVorrat.setVisibility(View.VISIBLE);
                checkBoxMoveToVorrat.setChecked(false); // Reset checkbox state
                checkBoxMoveToVorrat.setOnClickListener(v -> {
                    if (checkBoxMoveToVorrat.isChecked()) {
                        listener.onMoveToVorratClick(eintrag);
                    }
                });
            } else {
                checkBoxMoveToVorrat.setVisibility(View.GONE);
            }
        }
    }
}
