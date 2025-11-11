package com.example.haushalt_app_java.haushaltTest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import android.widget.ListView;

import com.example.haushalt_app_java.haushalt_activity.HaushaltActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;
import java.util.Arrays;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class HaushaltActivityTest {

    @Test
    public void loadHaushaltDaten_success_displaysHaushalte() {
        System.out.println("=== TEST START: loadHaushaltDaten_success_displaysHaushalte ===");

        // 1. Auth Mocks
        System.out.println("✓ Erstelle Auth Mocks...");
        FirebaseAuth auth = mock(FirebaseAuth.class);
        FirebaseUser user = mock(FirebaseUser.class);
        when(auth.getCurrentUser()).thenReturn(user);
        when(user.getUid()).thenReturn("testUser123");
        System.out.println("  → CurrentUser: testUser123");

        // 2. DB Mocks
        System.out.println("✓ Erstelle Firebase Mocks...");
        FirebaseDatabase db = mock(FirebaseDatabase.class);
        DatabaseReference root = mock(DatabaseReference.class);
        DatabaseReference hauserRef = mock(DatabaseReference.class);

        when(db.getReference()).thenReturn(root);
        when(root.child("Hauser")).thenReturn(hauserRef);

        // 3. Mock Haushalts-Daten
        System.out.println("✓ Mock Haushalts-Daten...");
        DataSnapshot hauserSnapshot = mock(DataSnapshot.class);
        DataSnapshot haus1Snap = mock(DataSnapshot.class);
        DataSnapshot haus2Snap = mock(DataSnapshot.class);

        when(haus1Snap.getKey()).thenReturn("haus1");
        when(haus1Snap.child("name")).thenReturn(mock(DataSnapshot.class));
        when(haus1Snap.child("name").getValue(String.class)).thenReturn("Haus Alpha");
        DataSnapshot mitglieder1 = mock(DataSnapshot.class);
        when(haus1Snap.child("mitgliederIds")).thenReturn(mitglieder1);
        when(mitglieder1.hasChild("testUser123")).thenReturn(true);
        System.out.println("  → Haus1: 'Haus Alpha' (Mitglied: testUser123)");

        when(haus2Snap.getKey()).thenReturn("haus2");
        when(haus2Snap.child("name")).thenReturn(mock(DataSnapshot.class));
        when(haus2Snap.child("name").getValue(String.class)).thenReturn("Haus Beta");
        DataSnapshot mitglieder2 = mock(DataSnapshot.class);
        when(haus2Snap.child("mitgliederIds")).thenReturn(mitglieder2);
        when(mitglieder2.hasChild("testUser123")).thenReturn(false);
        System.out.println("  → Haus2: 'Haus Beta' (KEIN Mitglied)");

        when(hauserSnapshot.getChildren()).thenReturn(Arrays.asList(haus1Snap, haus2Snap));

        // 4. Mock Haushalt-Listener (wird 2x aufgerufen: onCreate + onResume)
        System.out.println("✓ Mock Haushalt-Listener...");
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            System.out.println("  → Listener aufgerufen für Haushalte");
            listener.onDataChange(hauserSnapshot);
            return null;
        }).when(hauserRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        try (MockedStatic<FirebaseAuth> mockedAuth = mockStatic(FirebaseAuth.class);
             MockedStatic<FirebaseDatabase> mockedDb = mockStatic(FirebaseDatabase.class)) {

            mockedAuth.when(FirebaseAuth::getInstance).thenReturn(auth);
            mockedDb.when(() -> FirebaseDatabase.getInstance(anyString())).thenReturn(db);

            // 5. Activity starten
            System.out.println("✓ Starte HaushaltActivity...");
            var controller = Robolectric.buildActivity(HaushaltActivity.class)
                .create().start().resume();
            HaushaltActivity activity = controller.get();

            // 6. Warte auf alle asynchronen Tasks
            System.out.println("✓ Warte auf asynchrone Tasks...");
            shadowOf(Looper.getMainLooper()).idle();

            // 7. Verifikation (2x wegen onCreate + onResume)
            System.out.println("✓ Verifiziere Ergebnisse:");
            verify(hauserRef, times(2)).addListenerForSingleValueEvent(any());
            System.out.println("  → Listener wurde 2x aufgerufen ✓");

            ListView haushaltListView = activity.findViewById(
                com.example.haushalt_app_java.R.id.haushalt_listview);
            int count = haushaltListView.getAdapter().getCount();

            assertEquals("1 Haushalt sollte angezeigt werden", 1, count);
            System.out.println("  → 1 Haushalt angezeigt ✓");

            String hausName = (String) haushaltListView.getAdapter().getItem(0);
            assertEquals("Haus Alpha", hausName);
            System.out.println("  → Name: 'Haus Alpha' ✓");

            System.out.println("=== TEST ERFOLGREICH ===\n");
        }
    }

    @Test
    public void ladeMitglieder_success_displaysMitglieder() {
        System.out.println("=== TEST START: ladeMitglieder_success_displaysMitglieder ===");

        // 1. Auth Mocks
        System.out.println("✓ Erstelle Auth Mocks...");
        FirebaseAuth auth = mock(FirebaseAuth.class);
        FirebaseUser user = mock(FirebaseUser.class);
        when(auth.getCurrentUser()).thenReturn(user);
        when(user.getUid()).thenReturn("creator");

        // 2. DB Mocks
        System.out.println("✓ Erstelle Firebase Mocks...");
        FirebaseDatabase db = mock(FirebaseDatabase.class);
        DatabaseReference root = mock(DatabaseReference.class);
        DatabaseReference hauserRef = mock(DatabaseReference.class);
        DatabaseReference hausRef = mock(DatabaseReference.class);
        DatabaseReference mitgliederIdsRef = mock(DatabaseReference.class);
        DatabaseReference benutzerRef = mock(DatabaseReference.class);
        DatabaseReference user1Ref = mock(DatabaseReference.class);
        DatabaseReference user2Ref = mock(DatabaseReference.class);

        when(db.getReference()).thenReturn(root);
        when(root.child("Hauser")).thenReturn(hauserRef);
        when(hauserRef.child("haus1")).thenReturn(hausRef);
        when(hausRef.child("mitgliederIds")).thenReturn(mitgliederIdsRef);
        when(root.child("Benutzer")).thenReturn(benutzerRef);
        when(benutzerRef.child("user1")).thenReturn(user1Ref);
        when(benutzerRef.child("user2")).thenReturn(user2Ref);

        // 3. Mock Haushalts-Abfrage (für onCreate/onResume)
        System.out.println("✓ Mock Haushalts-Abfrage...");
        DataSnapshot emptyHauserSnapshot = mock(DataSnapshot.class);
        when(emptyHauserSnapshot.getChildren()).thenReturn(Arrays.asList());
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(emptyHauserSnapshot);
            return null;
        }).when(hauserRef).addListenerForSingleValueEvent(any());

        // 4. Mock Mitglieder-Daten
        System.out.println("✓ Mock Mitglieder-Daten...");
        DataSnapshot mitgliederSnapshot = mock(DataSnapshot.class);
        DataSnapshot child1 = mock(DataSnapshot.class);
        DataSnapshot child2 = mock(DataSnapshot.class);

        when(child1.getKey()).thenReturn("user1");
        when(child1.getValue()).thenReturn(true);
        when(child2.getKey()).thenReturn("user2");
        when(child2.getValue()).thenReturn(true);

        when(mitgliederSnapshot.getChildren()).thenReturn(Arrays.asList(child1, child2));
        System.out.println("  → 2 Mitglieder: user1, user2");

        // 5. Mock User-Namen
        System.out.println("✓ Mock Benutzer-Namen...");
        DataSnapshot user1Snapshot = mock(DataSnapshot.class);
        DataSnapshot user1NameSnapshot = mock(DataSnapshot.class);
        when(user1Snapshot.child("name")).thenReturn(user1NameSnapshot);
        when(user1NameSnapshot.getValue(String.class)).thenReturn("Alice");
        System.out.println("  → user1 → 'Alice'");

        DataSnapshot user2Snapshot = mock(DataSnapshot.class);
        DataSnapshot user2NameSnapshot = mock(DataSnapshot.class);
        when(user2Snapshot.child("name")).thenReturn(user2NameSnapshot);
        when(user2NameSnapshot.getValue(String.class)).thenReturn("Bob");
        System.out.println("  → user2 → 'Bob'");

        // 6. Mock Listener für Mitglieder
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mitgliederSnapshot);
            return null;
        }).when(mitgliederIdsRef).addListenerForSingleValueEvent(any());

        // 7. Mock Listener für User-Namen
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(user1Snapshot);
            return null;
        }).when(user1Ref).addListenerForSingleValueEvent(any());

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(user2Snapshot);
            return null;
        }).when(user2Ref).addListenerForSingleValueEvent(any());

        try (MockedStatic<FirebaseAuth> mockedAuth = mockStatic(FirebaseAuth.class);
             MockedStatic<FirebaseDatabase> mockedDb = mockStatic(FirebaseDatabase.class)) {

            mockedAuth.when(FirebaseAuth::getInstance).thenReturn(auth);
            mockedDb.when(() -> FirebaseDatabase.getInstance(anyString())).thenReturn(db);

            // 8. Activity starten
            System.out.println("✓ Starte HaushaltActivity...");
            var controller = Robolectric.buildActivity(HaushaltActivity.class)
                .create().start().resume();
            HaushaltActivity activity = controller.get();

            // 9. Private Methode mit Reflection aufrufen
            System.out.println("✓ Rufe ladeMitglieder() mit Reflection auf...");
            try {
                Method method = HaushaltActivity.class.getDeclaredMethod("ladeMitglieder", String.class);
                method.setAccessible(true); // ✅ WICHTIG für private Methoden
                method.invoke(activity, "haus1");
            } catch (Exception e) {
                fail("Fehler beim Aufrufen von ladeMitglieder: " + e.getMessage());
            }

            // 10. Warte auf alle asynchronen Listener
            System.out.println("✓ Warte auf asynchrone Tasks...");
            shadowOf(Looper.getMainLooper()).idle();

            // 11. Verifikation
            System.out.println("✓ Verifiziere Ergebnisse:");
            verify(mitgliederIdsRef).addListenerForSingleValueEvent(any());
            verify(user1Ref).addListenerForSingleValueEvent(any());
            verify(user2Ref).addListenerForSingleValueEvent(any());
            System.out.println("  → Alle Listener wurden aufgerufen ✓");

            ListView mitgliederListView = activity.findViewById(
                com.example.haushalt_app_java.R.id.hList);
            int count = mitgliederListView.getAdapter().getCount();

            assertEquals("2 Mitglieder sollten angezeigt werden", 2, count);
            System.out.println("  → 2 Mitglieder angezeigt ✓");

            String member1 = (String) mitgliederListView.getAdapter().getItem(0);
            String member2 = (String) mitgliederListView.getAdapter().getItem(1);

            assertTrue("Alice sollte vorhanden sein",
                member1.equals("Alice") || member2.equals("Alice"));
            assertTrue("Bob sollte vorhanden sein",
                member1.equals("Bob") || member2.equals("Bob"));
            System.out.println("  → Namen: 'Alice', 'Bob' ✓");

            System.out.println("=== TEST ERFOLGREICH ===\n");
        }
    }
}