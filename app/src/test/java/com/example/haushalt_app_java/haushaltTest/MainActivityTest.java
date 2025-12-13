package com.example.haushalt_app_java.haushaltTest;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import android.content.Intent;
import android.widget.ListView;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.produkt.Produkt;
import com.example.haushalt_app_java.produkt.ProductActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MainActivityTest {

    @Test
    public void onDataChange_populatesListViewCorrectly() {
        // --- Mocks Setup ---
        FirebaseDatabase dbMock = mock(FirebaseDatabase.class);
        DatabaseReference rootRefMock = mock(DatabaseReference.class);
        DatabaseReference hauserRefMock = mock(DatabaseReference.class);
        DatabaseReference specificHausRefMock = mock(DatabaseReference.class);
        DatabaseReference produkteRefMock = mock(DatabaseReference.class);

        DataSnapshot rootSnapshotMock = mock(DataSnapshot.class);
        DataSnapshot p1SnapshotMock = mock(DataSnapshot.class);
        DataSnapshot p2SnapshotMock = mock(DataSnapshot.class);

        Produkt p1 = new Produkt("p1", "test-haus-id", "Milch", 1, "Lebensmittel", 1, "Liter");
        Produkt p2 = new Produkt("p2", "test-haus-id", "Brot", 1, "Lebensmittel", 1, "Stk");

        // Mock the database path
        when(dbMock.getReference()).thenReturn(rootRefMock);
        when(rootRefMock.child("Hauser")).thenReturn(hauserRefMock);
        when(hauserRefMock.child("test-haus-id")).thenReturn(specificHausRefMock);
        when(specificHausRefMock.child("produkte")).thenReturn(produkteRefMock);

        // Mock the snapshot data
        when(p1SnapshotMock.getValue(Produkt.class)).thenReturn(p1);
        when(p1SnapshotMock.getKey()).thenReturn("p1");
        when(p2SnapshotMock.getValue(Produkt.class)).thenReturn(p2);
        when(p2SnapshotMock.getKey()).thenReturn("p2");
        when(rootSnapshotMock.getChildren()).thenReturn(Arrays.asList(p1SnapshotMock, p2SnapshotMock));

        // Mock Auth
        FirebaseAuth authMock = mock(FirebaseAuth.class);
        FirebaseUser userMock = mock(FirebaseUser.class);
        when(authMock.getCurrentUser()).thenReturn(userMock);
        when(userMock.getUid()).thenReturn("test-user-id");

        try (MockedStatic<FirebaseDatabase> mockedDb = mockStatic(FirebaseDatabase.class);
             MockedStatic<FirebaseAuth> mockedAuth = mockStatic(FirebaseAuth.class)) {

            mockedDb.when(() -> FirebaseDatabase.getInstance(anyString())).thenReturn(dbMock);
            mockedAuth.when(FirebaseAuth::getInstance).thenReturn(authMock);

            // --- Activity Setup ---
            Intent intent = new Intent();
            intent.putExtra("haus_id", "test-haus-id");

            var controller = Robolectric.buildActivity(ProductActivity.class, intent).create().start().resume();
            ProductActivity activity = controller.get();

            // --- Verification ---
            // Capture the listener passed to the database reference
            ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
            verify(produkteRefMock).addValueEventListener(listenerCaptor.capture());

            // Manually trigger onDataChange with our mock data
            listenerCaptor.getValue().onDataChange(rootSnapshotMock);

            // --- Assertions ---
            ListView listView = activity.findViewById(R.id.listViewp);
            assertNotNull(listView.getAdapter());
            assertEquals(2, listView.getAdapter().getCount()); // Should have two items

            // Check if the items are formatted correctly
            String item1 = (String) listView.getAdapter().getItem(0);
            String item2 = (String) listView.getAdapter().getItem(1);
            assertEquals("Milch - 1 Liter - Lebensmittel", item1);
            assertEquals("Brot - 1 Stk - Lebensmittel", item2);
        }
    }
}
