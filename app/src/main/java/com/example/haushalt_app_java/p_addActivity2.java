
package com.example.haushalt_app_java;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.content.DialogInterface;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View.OnApplyWindowInsetsListener;
import androidx.annotation.NonNull;
import android.view.WindowInsets;
import android.widget.Toast;

import com.example.haushalt_app_java.domain.kategorie;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.example.haushalt_app_java.domain.Produkt;
import com.google.firebase.database.FirebaseDatabase;

public class p_addActivity2 extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase db; // zentrale, regionsspezifische Instanz
    private EditText pEinheit;
    private EditText pName;
    private EditText pMenge;
    private EditText pKategorie;
    private EditText pMindBestand;

    private Button add;
    private Button Abbrechen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_p_add2);

        View mainView = findViewById(R.id.main);

        mainView.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                v.setPadding(
                        insets.getSystemWindowInsetLeft(),
                        insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(),
                        insets.getSystemWindowInsetBottom()
                );
                return insets;
            }
        });

        db = FirebaseDatabase.getInstance(DB_URL);

        pEinheit = findViewById(R.id.pEinheit);
        pName = findViewById(R.id.pName);
        pMenge = findViewById(R.id.pMenge);
        pKategorie = findViewById(R.id.pKategorie);
        pMindBestand = findViewById(R.id.pMindBestand);
        add = findViewById(R.id.add);
        Abbrechen = findViewById(R.id.Abbrechen);

        // Keine Tastatur öffnen, nur Klick akzeptieren
        pKategorie.setInputType(InputType.TYPE_NULL);
        pKategorie.setFocusable(false);
        pKategorie.setClickable(true);

        pKategorie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kategorie[] values = kategorie.values();
                String[] items = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    items[i] = values[i].getDisplayName();
                }

                final int[] selectedIndex = new int[]{-1};

                AlertDialog.Builder builder = new AlertDialog.Builder(p_addActivity2.this)
                        .setTitle("Kategorie wählen")
                        .setSingleChoiceItems(items, selectedIndex[0], new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedIndex[0] = which;
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (selectedIndex[0] >= 0) {
                                    pKategorie.setText(items[selectedIndex[0]]);
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builder.show();
            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String einheit = pEinheit.getText().toString().trim();
                String name = pName.getText().toString().trim();
                int menge = 0;
                int mindBestand = 0;
                try {
                    String mengeStr = pMenge.getText().toString().trim();
                    if (!mengeStr.isEmpty()) {
                        menge = Integer.parseInt(mengeStr);
                    }
                    String mindStr = pMindBestand.getText().toString().trim();
                    if (!mindStr.isEmpty()) {
                        mindBestand = Integer.parseInt(mindStr);
                    }
                } catch (NumberFormatException e) {
                    // einfache Fehlerbehandlung: setze 0 bei ungültiger Eingabe
                }
                String selected = pKategorie.getText().toString().trim();
                kategorie kategorieEnum = findKategorieByDisplayName(selected);

                Produkt produkt = new Produkt(
                        null,// produkt_id wird von Firebase generiert
                        name,
                        menge,
                        kategorieEnum, // falls Kategorie ein String ist
                        mindBestand,
                        einheit
                );

                db.getReference("Produkt").push().setValue(produkt)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(p_addActivity2.this, "Erfolgrich gespeichert", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(p_addActivity2.this,"umm schief gelaufen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

    }


    private kategorie findKategorieByDisplayName(String display) {
        if (display == null) return null;
        for (kategorie k : kategorie.values()) {
            if (display.equals(k.getDisplayName())) return k;
            if (display.equalsIgnoreCase(k.name())) return k;
        }
        return null;
    }
}