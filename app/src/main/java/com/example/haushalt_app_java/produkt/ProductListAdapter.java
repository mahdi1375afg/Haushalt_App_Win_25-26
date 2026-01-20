package com.example.haushalt_app_java.produkt;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.einkaufsliste.ListenEintrag;

import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductListViewHolder> {

    private List<ListenEintrag> productList;
    private final OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    private final boolean isShoppingList;
    private boolean isSelectionMode = false;
    private List<ListenEintrag> selectedItems = new java.util.ArrayList<>();

    public ProductListAdapter(List<ListenEintrag> productList, OnItemClickListener listener, boolean isShoppingList) {
        this.productList = productList;
        this.listener = listener;
        this.isShoppingList = isShoppingList;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setSelectionMode(boolean selectionMode) {
        isSelectionMode = selectionMode;
        notifyDataSetChanged();
    }

    public void setSelectedItems(List<ListenEintrag> selectedItems) {
        this.selectedItems = selectedItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_list, parent, false);
        return new ProductListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductListViewHolder holder, int position) {
        ListenEintrag eintrag = productList.get(position);
        holder.bind(eintrag, listener, isShoppingList, isSelectionMode, selectedItems.contains(eintrag));

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(eintrag);
                return true;
            }
            return false;
        });
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
        void onItemClick(ListenEintrag eintrag);
        void onEditClick(ListenEintrag eintrag);
        void onMoveToVorratClick(ListenEintrag eintrag);
        void onDeleteClick(ListenEintrag eintrag);
        void onIncreaseQuantityClick(ListenEintrag eintrag);
        void onDecreaseQuantityClick(ListenEintrag eintrag);
        void onBookmarkClick(ListenEintrag eintrag, ImageButton bookmarkButton);
        void onAddToShoppingListClick(ListenEintrag eintrag);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(ListenEintrag eintrag);
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
        private final CheckBox selectionCheckbox;
        private final androidx.constraintlayout.widget.ConstraintLayout productItemContainer;

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
            selectionCheckbox = itemView.findViewById(R.id.selection_checkbox);
            productItemContainer = itemView.findViewById(R.id.product_item_container);
        }

        public void bind(final ListenEintrag eintrag, final OnItemClickListener listener, boolean isShoppingList, boolean isSelectionMode, boolean isSelected) {
            textViewProductName.setText(eintrag.getName());
            textViewProductCategory.setText(eintrag.getKategorie());
            textViewProductUnit.setText(eintrag.getEinheit());
            textViewProductQuantity.setText(String.valueOf(eintrag.getMenge()));

            if (isSelectionMode) {
                buttonEdit.setVisibility(View.GONE);
                buttonDelete.setVisibility(View.GONE);
                buttonBookmark.setVisibility(View.GONE);
                buttonAddToShoppingList.setVisibility(View.GONE);
                buttonMoveToVorrat.setVisibility(View.GONE);
                selectionCheckbox.setVisibility(View.VISIBLE);
                selectionCheckbox.setChecked(isSelected);
                buttonDecreaseQuantity.setVisibility(View.GONE);
                buttonIncreaseQuantity.setVisibility(View.GONE);
            } else {
                buttonEdit.setVisibility(View.VISIBLE);
                buttonDelete.setVisibility(View.VISIBLE);
                buttonBookmark.setVisibility(View.VISIBLE);
                selectionCheckbox.setVisibility(View.GONE);
                buttonDecreaseQuantity.setVisibility(View.VISIBLE);
                buttonIncreaseQuantity.setVisibility(View.VISIBLE);

                if (isShoppingList) {
                    buttonMoveToVorrat.setVisibility(View.VISIBLE);
                    buttonAddToShoppingList.setVisibility(View.GONE);
                } else {
                    buttonMoveToVorrat.setVisibility(View.GONE);
                    buttonAddToShoppingList.setVisibility(View.VISIBLE);
                }
            }

            itemView.setOnClickListener(v -> listener.onItemClick(eintrag));
            selectionCheckbox.setOnClickListener(v -> listener.onItemClick(eintrag));
            buttonEdit.setOnClickListener(v -> listener.onEditClick(eintrag));
            buttonDelete.setOnClickListener(v -> listener.onDeleteClick(eintrag));
            buttonIncreaseQuantity.setOnClickListener(v -> listener.onIncreaseQuantityClick(eintrag));
            buttonDecreaseQuantity.setOnClickListener(v -> listener.onDecreaseQuantityClick(eintrag));
            buttonBookmark.setOnClickListener(v -> listener.onBookmarkClick(eintrag, buttonBookmark));
            buttonAddToShoppingList.setOnClickListener(v -> listener.onAddToShoppingListClick(eintrag));
            buttonMoveToVorrat.setOnClickListener(v -> listener.onMoveToVorratClick(eintrag));

            if (eintrag.isBookmarked()) {
                buttonBookmark.setImageResource(R.drawable.ic_bookmark_checked);
            } else {
                buttonBookmark.setImageResource(R.drawable.ic_bookmark_unchecked);
            }

            int color;
            if (isSelected) {
                color = Color.CYAN;
            } else {
                color = Color.BLACK; // Default
                if (eintrag.getMengeImVorrat() == 0) {
                    color = Color.RED;
                } else if (eintrag.getMengeImVorrat() > 0 && eintrag.getMengeImVorrat() <= eintrag.getMindestmenge()) {
                    color = Color.rgb(255, 165, 0); // Orange
                }
            }

            LayerDrawable background = (LayerDrawable) productItemContainer.getBackground();
            GradientDrawable border = (GradientDrawable) background.findDrawableByLayerId(R.id.border);
            border.setStroke(5, color);
        }
    }
}
