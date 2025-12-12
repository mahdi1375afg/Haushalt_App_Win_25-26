package com.example.haushalt_app_java.produkt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haushalt_app_java.R;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Produkt> productList;
    private final OnItemClickListener listener;

    public ProductAdapter(List<Produkt> productList, OnItemClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_list, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Produkt produkt = productList.get(position);
        holder.bind(produkt, listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void setProductList(List<Produkt> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onEditClick(Produkt produkt);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewProductName;
        private final TextView textViewProductCategory;
        private final TextView textViewProductUnit;
        private final TextView textViewProductQuantity;
        private final Button buttonEditQuantity;
        private final CheckBox checkBoxMoveToVorrat;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductCategory = itemView.findViewById(R.id.textViewProductCategory);
            textViewProductUnit = itemView.findViewById(R.id.textViewProductUnit);
            textViewProductQuantity = itemView.findViewById(R.id.textViewProductQuantity);
            buttonEditQuantity = itemView.findViewById(R.id.buttonEditQuantity);
            checkBoxMoveToVorrat = itemView.findViewById(R.id.checkBoxMoveToVorrat);
        }

        public void bind(final Produkt produkt, final OnItemClickListener listener) {
            textViewProductName.setText(produkt.getName());
            textViewProductCategory.setText(produkt.getKategorie());
            textViewProductUnit.setText(produkt.getEinheit());
            textViewProductQuantity.setText(String.valueOf(produkt.getMenge()));

            buttonEditQuantity.setOnClickListener(v -> listener.onEditClick(produkt));
            checkBoxMoveToVorrat.setVisibility(View.GONE); // Hide checkbox in product list
        }
    }
}
