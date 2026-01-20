package com.example.haushalt_app_java.vorrat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.einkaufsliste.ListenEintrag;

import java.util.List;
import java.util.Locale;

public class IndividualQuantityAdapter extends RecyclerView.Adapter<IndividualQuantityAdapter.ViewHolder> {

    private List<ListenEintrag> items;

    public IndividualQuantityAdapter(List<ListenEintrag> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_individual_quantity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListenEintrag item = items.get(position);
        holder.itemName.setText(item.getName());
        int quantityToAdd = Math.max(0, item.getZielmenge() - item.getMengeImVorrat());
        holder.itemQuantity.setText(String.format(Locale.getDefault(), "%d", quantityToAdd));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public ListenEintrag getItem(int position) {
        return items.get(position);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        EditText itemQuantity;

        ViewHolder(View view) {
            super(view);
            itemName = view.findViewById(R.id.textViewItemName);
            itemQuantity = view.findViewById(R.id.editTextItemQuantity);
        }
    }
}
