package com.example.haushalt_app_java.haushaltTest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;

import com.example.haushalt_app_java.haushalt_activity.AddUserActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AddUserActivityTest {

    @Test
    public void addUser_success_finishesWithResultOk() {
        System.out.println("=== TEST START: addUser_success_finishesWithResultOk ===");

        // 1. Mocking Setup
        System.out.println("✓ Erstelle Firebase Mocks...");
        FirebaseDatabase db = mock(FirebaseDatabase.class);
        DatabaseReference root = mock(DatabaseReference.class);
        DatabaseReference hauserRef = mock(DatabaseReference.class);
        DatabaseReference hausRef = mock(DatabaseReference.class);
        DatabaseReference benutzerRef = mock(DatabaseReference.class);
        DatabaseReference mitgliederRef = mock(DatabaseReference.class);
        DatabaseReference userIdRef = mock(DatabaseReference.class);
        DatabaseReference queryRef = mock(DatabaseReference.class);

        when(db.getReference()).thenReturn(root);
        when(root.child("Hauser")).thenReturn(hauserRef);
        when(root.child("Benutzer")).thenReturn(benutzerRef);

        // 2. Mock Haushalt-Abfrage
        System.out.println("✓ Mock Haushalt-Abfrage...");
        doAnswer(invocation -> {
            System.out.println("  → Haushalt-Listener aufgerufen");
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot snapshot = mock(DataSnapshot.class);
            DataSnapshot hausChild = mock(DataSnapshot.class);
            DataSnapshot nameSnapshot = mock(DataSnapshot.class);

            when(snapshot.getChildren()).thenReturn(java.util.Collections.singletonList(hausChild));
            when(hausChild.getKey()).thenReturn("haus1");
            when(hausChild.child("name")).thenReturn(nameSnapshot);
            when(nameSnapshot.getValue(String.class)).thenReturn("TestHaus");

            System.out.println("  → Haushalt gefunden: haus1 (TestHaus)");
            listener.onDataChange(snapshot);
            return null;
        }).when(hauserRef).addListenerForSingleValueEvent(any());

        // 3. Mock User-Suche
        System.out.println("✓ Mock User-Suche...");
        when(benutzerRef.orderByChild("name")).thenReturn(queryRef);
        when(queryRef.equalTo("TestUser")).thenReturn(queryRef);

        doAnswer(invocation -> {
            System.out.println("  → User-Suche ausgeführt: TestUser");
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot snapshot = mock(DataSnapshot.class);
            DataSnapshot userChild = mock(DataSnapshot.class);

            when(snapshot.exists()).thenReturn(true);
            when(snapshot.getChildren()).thenReturn(java.util.Collections.singletonList(userChild));
            when(userChild.getKey()).thenReturn("user123");

            System.out.println("  → User gefunden: user123");
            listener.onDataChange(snapshot);
            return null;
        }).when(queryRef).addListenerForSingleValueEvent(any());

        // 4. Mock setValue
        System.out.println("✓ Mock setValue für Mitglieder...");
        when(hauserRef.child("haus1")).thenReturn(hausRef);
        when(hausRef.child("mitgliederIds")).thenReturn(mitgliederRef);
        when(mitgliederRef.child("user123")).thenReturn(userIdRef);
        when(userIdRef.setValue(true)).thenAnswer(invocation -> {
            System.out.println("  → setValue(true) aufgerufen für user123");
            return Tasks.forResult(null);
        });

        try (MockedStatic<FirebaseDatabase> mockedDb = mockStatic(FirebaseDatabase.class)) {
            mockedDb.when(() -> FirebaseDatabase.getInstance(anyString())).thenReturn(db);

            // 5. Activity starten
            System.out.println("✓ Starte AddUserActivity...");
            var controller = Robolectric.buildActivity(AddUserActivity.class).create().start().resume();
            AddUserActivity activity = controller.get();

            // 6. User-Eingabe simulieren
            System.out.println("✓ Simuliere Eingabe: 'TestUser'");
            EditText nameInput = activity.findViewById(com.example.haushalt_app_java.R.id.name_input);
            nameInput.setText("TestUser");

            // 7. Button-Click simulieren
            System.out.println("✓ Simuliere Button-Click...");
            Button addBtn = activity.findViewById(com.example.haushalt_app_java.R.id.add_user);
            addBtn.performClick();

            // 8. Warte auf asynchrone Tasks
            System.out.println("✓ Warte auf asynchrone Tasks...");
            shadowOf(Looper.getMainLooper()).idle();

            // 9. Verifikation
            System.out.println("✓ Verifiziere Ergebnisse:");
            verify(userIdRef).setValue(true);
            System.out.println("  → setValue(true) wurde aufgerufen ✓");

            assertTrue("Activity sollte beendet sein", activity.isFinishing());
            System.out.println("  → Activity ist beendet ✓");

            assertEquals("Result Code sollte RESULT_OK sein",
                AddUserActivity.RESULT_OK, shadowOf(activity).getResultCode());
            System.out.println("  → RESULT_OK gesetzt ✓");

            System.out.println("=== TEST ERFOLGREICH ===\n");
        }
    }
}