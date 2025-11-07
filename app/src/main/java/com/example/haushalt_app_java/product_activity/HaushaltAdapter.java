package com.example.haushalt_app_java.product_activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.haushalt_app_java.domain.Haushalt;
import java.util.List;

public class HaushaltAdapter extends RecyclerView.Adapter<HaushaltAdapter.ViewHolder> {

    private List<Haushalt> haushalte;
    private OnHaushaltClickListener listener;

    public interface OnHaushaltClickListener {
        void onHaushaltClick(Haushalt haushalt);
    }

    public HaushaltAdapter(List<Haushalt> haushalte, OnHaushaltClickListener listener) {
        this.haushalte = haushalte;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Haushalt haushalt = haushalte.get(position);
        holder.textView.setText(haushalt.getName());
        holder.itemView.setOnClickListener(v -> listener.onHaushaltClick(haushalt));
    }

    @Override
    public int getItemCount() {
        return haushalte.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}