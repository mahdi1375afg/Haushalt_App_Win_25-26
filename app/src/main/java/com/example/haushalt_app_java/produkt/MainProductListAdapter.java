package com.example.haushalt_app_java.produkt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haushalt_app_java.R;

import java.util.List;

public class MainProductListAdapter extends RecyclerView.Adapter<MainProductListAdapter.MainProductViewHolder> {

    private List<Produkt> productList;
    private final OnItemClickListener listener;

    public MainProductListAdapter(List<Produkt> productList, OnItemClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MainProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_product_list, parent, false);
        return new MainProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainProductViewHolder holder, int position) {
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
        void onDeleteClick(Produkt produkt);
        void onEditClick(Produkt produkt);
        void onAddToCartClick(Produkt produkt);
        void onBookmarkClick(Produkt produkt, ImageButton bookmarkButton);
    }

    static class MainProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewProductName;
        private final TextView textViewProductCategory;
        private final TextView textViewProductUnit;
        private final ImageButton buttonDelete, buttonEdit, buttonAddToCart, buttonBookmark;

        public MainProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductCategory = itemView.findViewById(R.id.textViewProductCategory);
            textViewProductUnit = itemView.findViewById(R.id.textViewProductUnit);
            buttonDelete = itemView.findViewById(R.id.button_delete_product);
            buttonEdit = itemView.findViewById(R.id.button_edit_product);
            buttonAddToCart = itemView.findViewById(R.id.button_add_to_cart);
            buttonBookmark = itemView.findViewById(R.id.button_bookmark);
        }

        public void bind(final Produkt produkt, final OnItemClickListener listener) {
            textViewProductName.setText(produkt.getName());
            textViewProductCategory.setText(produkt.getKategorie());
            textViewProductUnit.setText("[" + produkt.getEinheit() + "]");

            if (produkt.isBookmarked()) {
                buttonBookmark.setImageResource(R.drawable.ic_bookmark_checked);
            } else {
                buttonBookmark.setImageResource(R.drawable.ic_bookmark_unchecked);
            }

            buttonDelete.setOnClickListener(v -> listener.onDeleteClick(produkt));
            buttonEdit.setOnClickListener(v -> listener.onEditClick(produkt));
            buttonAddToCart.setOnClickListener(v -> listener.onAddToCartClick(produkt));
            buttonBookmark.setOnClickListener(v -> listener.onBookmarkClick(produkt, buttonBookmark));
        }
    }
}
