package com.example.haushalt_app_java.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.domain.EinkaufslisteEintrag;

import java.util.List;

public class EinkaufslisteAdapter extends RecyclerView.Adapter<EinkaufslisteAdapter.EinkaufslisteViewHolder> {

    private List<EinkaufslisteEintrag> einkaufsliste;
    private final OnItemClickListener listener;

    public EinkaufslisteAdapter(List<EinkaufslisteEintrag> einkaufsliste, OnItemClickListener listener) {
        this.einkaufsliste = einkaufsliste;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EinkaufslisteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_einkaufsliste, parent, false);
        return new EinkaufslisteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EinkaufslisteViewHolder holder, int position) {
        EinkaufslisteEintrag eintrag = einkaufsliste.get(position);
        holder.bind(eintrag, listener);
    }

    @Override
    public int getItemCount() {
        return einkaufsliste.size();
    }

    public void setEinkaufsliste(List<EinkaufslisteEintrag> einkaufsliste) {
        this.einkaufsliste = einkaufsliste;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onEditClick(EinkaufslisteEintrag eintrag);
    }

    static class EinkaufslisteViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewProductName;
        private final TextView textViewProductCategory;
        private final TextView textViewProductUnit;
        private final TextView textViewProductQuantity;
        private final Button buttonEditQuantity;

        public EinkaufslisteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductCategory = itemView.findViewById(R.id.textViewProductCategory);
            textViewProductUnit = itemView.findViewById(R.id.textViewProductUnit);
            textViewProductQuantity = itemView.findViewById(R.id.textViewProductQuantity);
            buttonEditQuantity = itemView.findViewById(R.id.buttonEditQuantity);
        }

        public void bind(final EinkaufslisteEintrag eintrag, final OnItemClickListener listener) {
            textViewProductName.setText(eintrag.getName());
            textViewProductCategory.setText(eintrag.getKategorie());
            textViewProductUnit.setText(eintrag.getEinheit());
            textViewProductQuantity.setText(String.valueOf(eintrag.getMenge()));

            buttonEditQuantity.setOnClickListener(v -> listener.onEditClick(eintrag));
        }
    }
}
