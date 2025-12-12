package com.example.haushalt_app_java.haushaltTest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.os.Looper;
import android.widget.Button;

import com.example.haushalt_app_java.haushalt.delete_mitglied_Activity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class DeleteMitgliedActivityTest {

    @Test
    public void deleteMitglied_success_finishesWithResultOk() {
        System.out.println("=== TEST START: deleteMitglied_success_finishesWithResultOk ===");

        // 1. Auth Mocks
        System.out.println("✓ Erstelle Auth Mocks...");
        FirebaseAuth auth = mock(FirebaseAuth.class);
        FirebaseUser currentUser = mock(FirebaseUser.class);
        when(auth.getCurrentUser()).thenReturn(currentUser);
        when(currentUser.getUid()).thenReturn("currentUserId");
        System.out.println("  → CurrentUser: currentUserId");

        // 2. DB Mocks
        System.out.println("✓ Erstelle Firebase Mocks...");
        FirebaseDatabase db = mock(FirebaseDatabase.class);
        DatabaseReference root = mock(DatabaseReference.class);
        DatabaseReference benutzerRef = mock(DatabaseReference.class);
        DatabaseReference queryRef = mock(DatabaseReference.class);
        DatabaseReference hauserRef = mock(DatabaseReference.class);
        DatabaseReference hausRef = mock(DatabaseReference.class);
        DatabaseReference mitgliederRef = mock(DatabaseReference.class);
        DatabaseReference userIdRef = mock(DatabaseReference.class);

        when(db.getReference()).thenReturn(root);
        when(root.child("Benutzer")).thenReturn(benutzerRef);
        when(root.child("Hauser")).thenReturn(hauserRef);

        // 3. Mock User-Suche
        System.out.println("✓ Mock User-Suche...");
        when(benutzerRef.orderByChild("name")).thenReturn(queryRef);
        when(queryRef.equalTo("TestMitglied")).thenReturn(queryRef);

        doAnswer(invocation -> {
            System.out.println("  → User-Suche ausgeführt: TestMitglied");
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot snapshot = mock(DataSnapshot.class);
            DataSnapshot userChild = mock(DataSnapshot.class);

            when(snapshot.getChildren()).thenReturn(java.util.Collections.singletonList(userChild));
            when(userChild.getKey()).thenReturn("targetUserId");

            System.out.println("  → Mitglied gefunden: targetUserId (nicht currentUser!)");
            listener.onDataChange(snapshot);
            return null;
        }).when(queryRef).addListenerForSingleValueEvent(any());

        // 4. Mock removeValue
        System.out.println("✓ Mock removeValue für Mitglied...");
        when(hauserRef.child("haus1")).thenReturn(hausRef);
        when(hausRef.child("mitgliederIds")).thenReturn(mitgliederRef);
        when(mitgliederRef.child("targetUserId")).thenReturn(userIdRef);
        when(userIdRef.removeValue()).thenAnswer(invocation -> {
            System.out.println("  → removeValue() aufgerufen für targetUserId");
            return Tasks.forResult(null);
        });

        try (MockedStatic<FirebaseAuth> mockedAuth = mockStatic(FirebaseAuth.class);
             MockedStatic<FirebaseDatabase> mockedDb = mockStatic(FirebaseDatabase.class)) {

            mockedAuth.when(FirebaseAuth::getInstance).thenReturn(auth);
            mockedDb.when(() -> FirebaseDatabase.getInstance(anyString())).thenReturn(db);

            // 5. Activity mit Intent starten
            System.out.println("✓ Starte delete_mitglied_Activity...");
            Intent intent = new Intent();
            intent.putExtra("mitgliedName", "TestMitglied");
            intent.putExtra("hausId", "haus1");

            var controller = Robolectric.buildActivity(delete_mitglied_Activity.class, intent)
                .create().start().resume();
            delete_mitglied_Activity activity = controller.get();

            // 6. Delete-Button klicken
            System.out.println("✓ Simuliere Delete-Button-Click...");
            Button deleteBtn = activity.findViewById(com.example.haushalt_app_java.R.id.delete_mitglied_button);
            deleteBtn.performClick();

            // 7. Warte auf asynchrone Tasks
            System.out.println("✓ Warte auf asynchrone Tasks...");
            shadowOf(Looper.getMainLooper()).idle();

            // 8. Verifikation
            System.out.println("✓ Verifiziere Ergebnisse:");
            verify(userIdRef).removeValue();
            System.out.println("  → removeValue() wurde aufgerufen ✓");

            assertTrue("Activity sollte beendet sein", activity.isFinishing());
            System.out.println("  → Activity ist beendet ✓");

            assertEquals("Result Code sollte RESULT_OK sein",
                delete_mitglied_Activity.RESULT_OK, shadowOf(activity).getResultCode());
            System.out.println("  → RESULT_OK gesetzt ✓");

            System.out.println("=== TEST ERFOLGREICH ===\n");
        }
    }

    @Test
    public void deleteMitglied_selbstloeschungVerhindert() {
        System.out.println("=== TEST START: deleteMitglied_selbstloeschungVerhindert ===");

        // 1. Auth Mocks (gleiche User-ID)
        System.out.println("✓ Erstelle Auth Mocks (Selbst-Löschung)...");
        FirebaseAuth auth = mock(FirebaseAuth.class);
        FirebaseUser currentUser = mock(FirebaseUser.class);
        when(auth.getCurrentUser()).thenReturn(currentUser);
        when(currentUser.getUid()).thenReturn("sameUserId");
        System.out.println("  → CurrentUser: sameUserId");

        // 2. DB Mocks
        System.out.println("✓ Erstelle Firebase Mocks...");
        FirebaseDatabase db = mock(FirebaseDatabase.class);
        DatabaseReference root = mock(DatabaseReference.class);
        DatabaseReference benutzerRef = mock(DatabaseReference.class);
        DatabaseReference queryRef = mock(DatabaseReference.class);

        when(db.getReference()).thenReturn(root);
        when(root.child("Benutzer")).thenReturn(benutzerRef);

        // 3. Mock User-Suche (gibt gleiche ID zurück)
        System.out.println("✓ Mock User-Suche...");
        when(benutzerRef.orderByChild("name")).thenReturn(queryRef);
        when(queryRef.equalTo("IchSelbst")).thenReturn(queryRef);

        doAnswer(invocation -> {
            System.out.println("  → User-Suche ausgeführt: IchSelbst");
            ValueEventListener listener = invocation.getArgument(0);
            DataSnapshot snapshot = mock(DataSnapshot.class);
            DataSnapshot userChild = mock(DataSnapshot.class);

            when(snapshot.getChildren()).thenReturn(java.util.Collections.singletonList(userChild));
            when(userChild.getKey()).thenReturn("sameUserId");

            System.out.println("  → Mitglied gefunden: sameUserId (= currentUser!)");
            listener.onDataChange(snapshot);
            return null;
        }).when(queryRef).addListenerForSingleValueEvent(any());

        try (MockedStatic<FirebaseAuth> mockedAuth = mockStatic(FirebaseAuth.class);
             MockedStatic<FirebaseDatabase> mockedDb = mockStatic(FirebaseDatabase.class)) {

            mockedAuth.when(FirebaseAuth::getInstance).thenReturn(auth);
            mockedDb.when(() -> FirebaseDatabase.getInstance(anyString())).thenReturn(db);

            // 4. Activity mit Intent starten
            System.out.println("✓ Starte delete_mitglied_Activity...");
            Intent intent = new Intent();
            intent.putExtra("mitgliedName", "IchSelbst");
            intent.putExtra("hausId", "haus1");

            var controller = Robolectric.buildActivity(delete_mitglied_Activity.class, intent)
                .create().start().resume();
            delete_mitglied_Activity activity = controller.get();

            // 5. Delete-Button klicken
            System.out.println("✓ Simuliere Delete-Button-Click...");
            Button deleteBtn = activity.findViewById(com.example.haushalt_app_java.R.id.delete_mitglied_button);
            deleteBtn.performClick();

            // 6. Warte auf asynchrone Tasks
            System.out.println("✓ Warte auf asynchrone Tasks...");
            shadowOf(Looper.getMainLooper()).idle();

            // 7. Verifikation: Activity sollte NICHT beendet sein
            System.out.println("✓ Verifiziere Selbst-Löschung verhindert:");
            assertFalse("Activity sollte NICHT beendet sein", activity.isFinishing());
            System.out.println("  → Activity läuft weiter ✓");
            System.out.println("  → Selbst-Löschung erfolgreich blockiert ✓");

            System.out.println("=== TEST ERFOLGREICH ===\n");
        }
    }
}