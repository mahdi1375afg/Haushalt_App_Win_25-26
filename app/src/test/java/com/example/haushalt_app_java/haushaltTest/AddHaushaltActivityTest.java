package com.example.haushalt_app_java.haushaltTest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import com.example.haushalt_app_java.haushalt_activity.AddHaushaltActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AddHaushaltActivityTest {

    @Test
    public void createHaushalt_success_finishesWithResultOk() {
        // Auth Mocks
        FirebaseAuth auth = mock(FirebaseAuth.class);
        FirebaseUser user = mock(FirebaseUser.class);
        when(auth.getCurrentUser()).thenReturn(user);
        when(user.getUid()).thenReturn("creatorX");
        when(user.getDisplayName()).thenReturn(null);

        // DB + Reference Mocks
        FirebaseDatabase db = mock(FirebaseDatabase.class);
        DatabaseReference root = mock(DatabaseReference.class);
        DatabaseReference hauserRef = mock(DatabaseReference.class);
        DatabaseReference newHausRef = mock(DatabaseReference.class);
        DatabaseReference hausIdRef = mock(DatabaseReference.class);
        DatabaseReference nameRef = mock(DatabaseReference.class);
        DatabaseReference lowerRef = mock(DatabaseReference.class);
        DatabaseReference mitgliederRef = mock(DatabaseReference.class);
        DatabaseReference creatorEntryRef = mock(DatabaseReference.class);

        DatabaseReference benutzerRef = mock(DatabaseReference.class);
        DatabaseReference benutzerCreatorRef = mock(DatabaseReference.class);
        DatabaseReference haushaltIdRef = mock(DatabaseReference.class);
        DatabaseReference linkTargetRef = mock(DatabaseReference.class);

        when(db.getReference()).thenReturn(root);
        when(root.child("Hauser")).thenReturn(hauserRef);
        when(hauserRef.push()).thenReturn(newHausRef);
        when(newHausRef.getKey()).thenReturn("hausNeu");

        when(newHausRef.child("haus_id")).thenReturn(hausIdRef);
        when(newHausRef.child("name")).thenReturn(nameRef);
        when(newHausRef.child("lowercaseName")).thenReturn(lowerRef);
        when(newHausRef.child("mitgliederIds")).thenReturn(mitgliederRef);
        when(mitgliederRef.child("creatorX")).thenReturn(creatorEntryRef);

        // Wichtig: Alle setValue-Aufrufe müssen gemockt werden
        when(hausIdRef.setValue("hausNeu")).thenReturn(Tasks.forResult(null));
        when(nameRef.setValue("TestHaus")).thenReturn(Tasks.forResult(null));
        when(lowerRef.setValue("testhaus")).thenReturn(Tasks.forResult(null));
        when(creatorEntryRef.setValue(true)).thenReturn(Tasks.forResult(null));

        when(root.child("Benutzer")).thenReturn(benutzerRef);
        when(benutzerRef.child("creatorX")).thenReturn(benutzerCreatorRef);
        when(benutzerCreatorRef.child("haushaltId")).thenReturn(haushaltIdRef);
        when(haushaltIdRef.child("hausNeu")).thenReturn(linkTargetRef);
        when(linkTargetRef.setValue(true)).thenReturn(Tasks.forResult(null));

        try (MockedStatic<FirebaseAuth> mockedAuth = mockStatic(FirebaseAuth.class);
             MockedStatic<FirebaseDatabase> mockedDb = mockStatic(FirebaseDatabase.class)) {

            mockedAuth.when(FirebaseAuth::getInstance).thenReturn(auth);
            mockedDb.when(() -> FirebaseDatabase.getInstance(anyString())).thenReturn(db);

            var controller = Robolectric.buildActivity(AddHaushaltActivity.class).create().start().resume();
            AddHaushaltActivity activity = controller.get();

            TextView nameInput = activity.findViewById(com.example.haushalt_app_java.R.id.hName);
            nameInput.setText("TestHaus");

            Button createBtn = activity.findViewById(com.example.haushalt_app_java.R.id.hAddName);
            createBtn.performClick();

            // Warte auf alle asynchronen Tasks auf dem Main-Thread
            shadowOf(Looper.getMainLooper()).idle();

            verify(hausIdRef).setValue("hausNeu");
            verify(nameRef).setValue("TestHaus");
            verify(lowerRef).setValue("testhaus");
            verify(linkTargetRef).setValue(true);

            assertTrue(activity.isFinishing());
            assertEquals(AddHaushaltActivity.RESULT_OK, shadowOf(activity).getResultCode());
        }
    }
}