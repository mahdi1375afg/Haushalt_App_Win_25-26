package com.example.haushalt_app_java;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class delete_haushalt_Activity extends AppCompatActivity {

    private Button nein_button;
    private Button ja_button;
    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase db;
    private String hausId;
    private DatabaseReference hausRef;
    private ImageView back_button;
    private EditText neu_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delete_haushalt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nein_button = findViewById(R.id.nein_button);
        ja_button = findViewById(R.id.ja_button);
        back_button = findViewById(R.id.back_haus);
        back_button.setOnClickListener(v -> finish());
        neu_name = findViewById(R.id.neu_name);

        db = FirebaseDatabase.getInstance(DB_URL);

        Intent intent = getIntent();
        if(intent == null || !intent.hasExtra("hausId")) {
            finish(); // Beende die Aktivität, wenn keine hausId übergeben wurde
            return;
        }

        hausId = intent.getStringExtra("hausId");
        hausRef=db.getReference().child("Hauser").child(hausId);

        ja_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hausRef.removeValue()
                        .addOnSuccessListener(aVoid->{
                            Toast.makeText(delete_haushalt_Activity.this,"Haushalt gelöscht",Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(delete_haushalt_Activity.this,"umm, Etwas schief gelaufen",Toast.LENGTH_SHORT).show();
                            finish();
                        });

            }

        });
        nein_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = neu_name.getText().toString().trim();
                hausRef.child("name").setValue(name)
                        .addOnSuccessListener(aVoid->{
                            Toast.makeText(delete_haushalt_Activity.this,"Haushalt erfolgrich aktualiesiert",Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(delete_haushalt_Activity.this,"umm, Etwas schief gelaufen",Toast.LENGTH_SHORT).show();
                            finish();
                        });
            }
        });
    }
}