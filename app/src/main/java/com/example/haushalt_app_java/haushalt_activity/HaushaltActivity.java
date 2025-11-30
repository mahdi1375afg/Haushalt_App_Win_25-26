package com.example.haushalt_app_java.haushalt_activity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.domain.EinkaufslistenActivity;
import com.example.haushalt_app_java.product_activity.MainActivity;
import com.example.haushalt_app_java.profile.profile_Activity;
import com.example.haushalt_app_java.utils.HausIdManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.content.Intent;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HaushaltActivity extends AppCompatActivity {

    private static final String DB_URL = "https://haushalt-app-68451-default-rtdb.europe-west1.firebasedatabase.app";
    private static final int REQUEST_ADD_HAUSHALT = 1;
    private static final int REQUEST_ADD_USER = 2;
    private static final int REQUEST_DELETE_USER = 3;
    private static final int REQUEST_DELETE_HAUSHALT = 4;

    private FirebaseDatabase db;
    private ListView hList;
    private FloatingActionButton hAdd;
    private TextView haushaltNameTextView;
    private ArrayAdapter<String> adapter;
    private List<String> mitgliederNamen;
    private Set<String> geladeneIds;
    private String currentHausId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_haushalt);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        hAdd = findViewById(R.id.hAdd);
        hList = findViewById(R.id.hList);
        haushaltNameTextView = findViewById(R.id.haushalt_name);
        db = FirebaseDatabase.getInstance(DB_URL);

        mitgliederNamen = new ArrayList<>();
        geladeneIds = new HashSet<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mitgliederNamen);
        hList.setAdapter(adapter);

        loadBenutzerHaushalt();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView2);
        bottomNav.setSelectedItemId(R.id.nav_household);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_household) {
                return true;
            }
            if (itemId == R.id.nav_products) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, profile_Activity.class));
                return true;
            }
            if (itemId == R.id.nav_einkaufslisten) {
                Intent intent = new Intent(this, EinkaufslistenActivity.class);
                intent.putExtra("hausId", currentHausId);
                startActivity(intent);
                return true;
            }
            return false;
        });

        haushaltNameTextView.setOnLongClickListener(v -> {
            if (currentHausId != null) {
                Intent intent = new Intent(this, delete_haushalt_Activity.class);
                intent.putExtra("hausId", currentHausId);
                startActivityForResult(intent, REQUEST_DELETE_HAUSHALT);
            }
            return true;
        });

        hList.setOnItemClickListener((parent, view, position, id) -> {
            if (currentHausId == null) return;
            String mitgliedName = mitgliederNamen.get(position);
            Intent intent = new Intent(this, delete_mitglied_Activity.class);
            intent.putExtra("mitgliedName", mitgliedName);
            intent.putExtra("hausId", currentHausId);
            startActivityForResult(intent, REQUEST_DELETE_USER);
        });

        hAdd.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.add_haushalt, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.add_user) {
                    if (currentHausId == null) {
                        Toast.makeText(this,
                                "Bitte erst Haushalt erstellen",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Intent intent = new Intent(this, AddUserActivity.class);
                    intent.putExtra("hausId", currentHausId);
                    startActivityForResult(intent, REQUEST_ADD_USER);
                    return true;
                }

                if (itemId == R.id.add_household) {
                    if (currentHausId != null) {
                        Toast.makeText(this,
                                "Sie haben bereits einen Haushalt",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    startActivityForResult(
                            new Intent(this, AddHaushaltActivity.class),
                            REQUEST_ADD_HAUSHALT);
                    return true;
                } else if (itemId == R.id.invite_user) {
                    String hausId = HausIdManager.getInstance().getHausId();
                    if (hausId != null && !hausId.isEmpty()) {
                        String deepLink = "haushaltapp://join?hausId=" + hausId;
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Trete meinem Haushalt bei: " + deepLink);
                        startActivity(Intent.createChooser(shareIntent, "Einladung senden via"));
                    } else {
                        Toast.makeText(this, "Keine Haus ID gefunden", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

                return false;
            });

            popup.show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            loadBenutzerHaushalt();
            Toast.makeText(this, "Daten aktualisiert", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Wichtig: Tab wieder markieren
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView2);
        bottomNav.setSelectedItemId(R.id.nav_household);

        loadBenutzerHaushalt();
    }

    private void loadBenutzerHaushalt() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.getReference("Benutzer")
                .child(userId)
                .child("hausId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            currentHausId = snapshot.getValue(String.class);

                            if (currentHausId != null) {
                                db.getReference("Hauser")
                                        .child(currentHausId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot hausSnapshot) {
                                                String hausName =
                                                        hausSnapshot.child("name")
                                                                .getValue(String.class);
                                                haushaltNameTextView.setText(
                                                        hausName != null ? hausName : "Haushalt"
                                                );

                                                com.example.haushalt_app_java.utils
                                                        .HausIdManager.getInstance()
                                                        .setHausId(currentHausId);

                                                ladeMitglieder(currentHausId);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
                                        });
                            }
                        } else {
                            currentHausId = null;
                            haushaltNameTextView.setText("Kein Haushalt");
                            mitgliederNamen.clear();
                            adapter.notifyDataSetChanged();
                            Toast.makeText(HaushaltActivity.this,
                                    "Bitte erstellen Sie einen Haushalt",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void ladeMitglieder(String haushaltId) {
        mitgliederNamen.clear();
        geladeneIds.clear();
        adapter.notifyDataSetChanged();

        db.getReference()
                .child("Hauser")
                .child(haushaltId)
                .child("mitgliederIds")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String mitgliedId = child.getKey();

                            if (mitgliedId == null || geladeneIds.contains(mitgliedId)) {
                                continue;
                            }

                            geladeneIds.add(mitgliedId);

                            db.getReference()
                                    .child("Benutzer")
                                    .child(mitgliedId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                            String name = userSnapshot.child("name")
                                                    .getValue(String.class);

                                            if (name != null && !mitgliederNamen.contains(name)) {
                                                mitgliederNamen.add(name);
                                                adapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
