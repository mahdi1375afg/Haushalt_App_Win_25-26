package com.example.haushalt_app_java.haushaltTest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;

import com.example.haushalt_app_java.haushalt.delete_haushalt_Activity;
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
public class DeleteHaushaltActivityTest {

    @Test
    public void deleteHaushalt_success_finishesWithResultOk() {
        System.out.println("=== TEST START: deleteHaushalt_success_finishesWithResultOk ===");

        // 1. Mocking Setup
        System.out.println("✓ Erstelle Firebase Mocks...");
        FirebaseDatabase db = mock(FirebaseDatabase.class);
        DatabaseReference root = mock(DatabaseReference.class);
        DatabaseReference hauserRef = mock(DatabaseReference.class);
        DatabaseReference hausRef = mock(DatabaseReference.class);
        DatabaseReference benutzerRef = mock(DatabaseReference.class);

        when(db.getReference()).thenReturn(root);
        when(root.child("Hauser")).thenReturn(hauserRef);
        when(root.child("Benutzer")).thenReturn(benutzerRef);
        when(hauserRef.child("haus1")).thenReturn(hausRef);

        // 2. Mock Benutzer-Abfrage (leer = keine User zu bereinigen)
        System.out.println("✓ Mock Benutzer-Abfrage...");
        doAnswer(invocation -> {
            System.out.println("  → Benutzer-Listener aufgerufen");
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot snapshot = mock(DataSnapshot.class);
            when(snapshot.getChildren()).thenReturn(java.util.Collections.emptyList());
            System.out.println("  → Keine Benutzer zu bereinigen");
            listener.onDataChange(snapshot);
            return null;
        }).when(benutzerRef).addListenerForSingleValueEvent(any());

        // 3. Mock removeValue
        System.out.println("✓ Mock removeValue für Haushalt...");
        when(hausRef.removeValue()).thenAnswer(invocation -> {
            System.out.println("  → removeValue() aufgerufen für haus1");
            return Tasks.forResult(null);
        });

        try (MockedStatic<FirebaseDatabase> mockedDb = mockStatic(FirebaseDatabase.class)) {
            mockedDb.when(() -> FirebaseDatabase.getInstance(anyString())).thenReturn(db);

            // 4. Activity mit Intent starten
            System.out.println("✓ Starte delete_haushalt_Activity...");
            Intent intent = new Intent();
            intent.putExtra("hausId", "haus1");

            var controller = Robolectric.buildActivity(delete_haushalt_Activity.class, intent)
                .create().start().resume();
            delete_haushalt_Activity activity = controller.get();

            // 5. JA-Button klicken (löschen)
            System.out.println("✓ Simuliere JA-Button-Click (Löschen)...");
            Button jaBtn = activity.findViewById(com.example.haushalt_app_java.R.id.ja_button);
            jaBtn.performClick();

            // 6. Warte auf asynchrone Tasks
            System.out.println("✓ Warte auf asynchrone Tasks...");
            shadowOf(Looper.getMainLooper()).idle();

            // 7. Verifikation
            System.out.println("✓ Verifiziere Ergebnisse:");
            verify(hausRef).removeValue();
            System.out.println("  → removeValue() wurde aufgerufen ✓");

            assertTrue("Activity sollte beendet sein", activity.isFinishing());
            System.out.println("  → Activity ist beendet ✓");

            assertEquals("Result Code sollte RESULT_OK sein",
                delete_haushalt_Activity.RESULT_OK, shadowOf(activity).getResultCode());
            System.out.println("  → RESULT_OK gesetzt ✓");

            System.out.println("=== TEST ERFOLGREICH ===\n");
        }
    }

    @Test
    public void renameHaushalt_success_finishesWithResultOk() {
        System.out.println("=== TEST START: renameHaushalt_success_finishesWithResultOk ===");

        // 1. Mocking Setup
        System.out.println("✓ Erstelle Firebase Mocks...");
        FirebaseDatabase db = mock(FirebaseDatabase.class);
        DatabaseReference root = mock(DatabaseReference.class);
        DatabaseReference hauserRef = mock(DatabaseReference.class);
        DatabaseReference hausRef = mock(DatabaseReference.class);
        DatabaseReference nameRef = mock(DatabaseReference.class);
        DatabaseReference lowerRef = mock(DatabaseReference.class);

        when(db.getReference()).thenReturn(root);
        when(root.child("Hauser")).thenReturn(hauserRef);
        when(hauserRef.child("haus1")).thenReturn(hausRef);
        when(hausRef.child("name")).thenReturn(nameRef);
        when(hausRef.child("lowercaseName")).thenReturn(lowerRef);

        // 2. Mock setValue
        System.out.println("✓ Mock setValue für Umbenennung...");
        when(nameRef.setValue("NeuerName")).thenAnswer(invocation -> {
            System.out.println("  → setValue('NeuerName') aufgerufen");
            return Tasks.forResult(null);
        });
        when(lowerRef.setValue("neuername")).thenAnswer(invocation -> {
            System.out.println("  → setValue('neuername') aufgerufen");
            return Tasks.forResult(null);
        });

        try (MockedStatic<FirebaseDatabase> mockedDb = mockStatic(FirebaseDatabase.class)) {
            mockedDb.when(() -> FirebaseDatabase.getInstance(anyString())).thenReturn(db);

            // 3. Activity mit Intent starten
            System.out.println("✓ Starte delete_haushalt_Activity...");
            Intent intent = new Intent();
            intent.putExtra("hausId", "haus1");

            var controller = Robolectric.buildActivity(delete_haushalt_Activity.class, intent)
                .create().start().resume();
            delete_haushalt_Activity activity = controller.get();

            // 4. Namen eingeben
            System.out.println("✓ Simuliere Eingabe: 'NeuerName'");
            EditText nameInput = activity.findViewById(com.example.haushalt_app_java.R.id.neu_name);
            nameInput.setText("NeuerName");

            // 5. NEIN-Button klicken (umbenennen)
            System.out.println("✓ Simuliere NEIN-Button-Click (Umbenennen)...");
            Button neinBtn = activity.findViewById(com.example.haushalt_app_java.R.id.nein_button);
            neinBtn.performClick();

            // 6. Warte auf asynchrone Tasks
            System.out.println("✓ Warte auf asynchrone Tasks...");
            shadowOf(Looper.getMainLooper()).idle();

            // 7. Verifikation
            System.out.println("✓ Verifiziere Ergebnisse:");
            verify(nameRef).setValue("NeuerName");
            verify(lowerRef).setValue("neuername");
            System.out.println("  → setValue() Aufrufe erfolgreich ✓");

            assertTrue("Activity sollte beendet sein", activity.isFinishing());
            System.out.println("  → Activity ist beendet ✓");

            assertEquals("Result Code sollte RESULT_OK sein",
                delete_haushalt_Activity.RESULT_OK, shadowOf(activity).getResultCode());
            System.out.println("  → RESULT_OK gesetzt ✓");

            System.out.println("=== TEST ERFOLGREICH ===\n");
        }
    }
}