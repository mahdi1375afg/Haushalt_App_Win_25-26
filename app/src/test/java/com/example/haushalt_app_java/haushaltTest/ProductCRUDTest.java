package com.example.haushalt_app_java.haushaltTest;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;

import com.example.haushalt_app_java.R;
import com.example.haushalt_app_java.domain.Produkt;
import com.example.haushalt_app_java.product_activity.p_addActivity2;
import com.example.haushalt_app_java.product_activity.pUpdateActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ProductCRUDTest {

    @Test
    public void addProduct_success_savesToDatabaseAndFinishes() {
        // --- Mocks Setup ---
        FirebaseDatabase dbMock = mock(FirebaseDatabase.class);
        DatabaseReference rootRefMock = mock(DatabaseReference.class);
        DatabaseReference hauserRefMock = mock(DatabaseReference.class);
        DatabaseReference specificHausRefMock = mock(DatabaseReference.class);
        DatabaseReference produkteRefMock = mock(DatabaseReference.class);
        DatabaseReference newProduktRefMock = mock(DatabaseReference.class);
        DatabaseReference finalProduktRefMock = mock(DatabaseReference.class);

        // Chain the mock calls to simulate the database path
        when(dbMock.getReference()).thenReturn(rootRefMock);
        when(rootRefMock.child("Hauser")).thenReturn(hauserRefMock);
        when(hauserRefMock.child("test-haus-id")).thenReturn(specificHausRefMock);
        when(specificHausRefMock.child("produkte")).thenReturn(produkteRefMock);
        when(produkteRefMock.push()).thenReturn(newProduktRefMock);
        when(newProduktRefMock.getKey()).thenReturn("new-product-id");
        when(produkteRefMock.child("new-product-id")).thenReturn(finalProduktRefMock);

        // Mock the setValue task to be successful
        Task<Void> successTask = Tasks.forResult(null);
        when(finalProduktRefMock.setValue(any(Produkt.class))).thenReturn(successTask);

        try (MockedStatic<FirebaseDatabase> mockedDb = mockStatic(FirebaseDatabase.class)) {
            mockedDb.when(() -> FirebaseDatabase.getInstance(anyString())).thenReturn(dbMock);

            // --- Activity Setup ---
            Intent intent = new Intent();
            intent.putExtra("haus_id", "test-haus-id");

            var controller = Robolectric.buildActivity(p_addActivity2.class, intent).create().start().resume();
            p_addActivity2 activity = controller.get();

            // --- UI Interaction ---
            EditText nameInput = activity.findViewById(R.id.pName);
            EditText mengeInput = activity.findViewById(R.id.pMenge);
            EditText einheitInput = activity.findViewById(R.id.pEinheit);
            EditText mindestInput = activity.findViewById(R.id.pMindBestand);
            EditText kategorieInput = activity.findViewById(R.id.pKategorie);

            nameInput.setText("Test Tomaten");
            mengeInput.setText("500");
            einheitInput.setText("g");
            mindestInput.setText("100");
            kategorieInput.setText("Lebensmittel");

            Button addButton = activity.findViewById(R.id.add);
            addButton.performClick();

            ShadowLooper.idleMainLooper();

            // --- Verification ---
            ArgumentCaptor<Produkt> produktCaptor = ArgumentCaptor.forClass(Produkt.class);
            verify(finalProduktRefMock).setValue(produktCaptor.capture());

            Produkt savedProdukt = produktCaptor.getValue();
            assertEquals("new-product-id", savedProdukt.getProdukt_id());
            assertEquals("test-haus-id", savedProdukt.getHaus_id());
            assertEquals("Test Tomaten", savedProdukt.getName());
            assertEquals(500, savedProdukt.getMenge());
            assertEquals("g", savedProdukt.getEinheit());
            assertEquals(100, savedProdukt.getMindBestand());
            assertEquals("Lebensmittel", savedProdukt.getKategorie());

            assertTrue(activity.isFinishing());
        }
    }

    @Test
    public void updateProduct_success_updatesDatabaseAndFinishes() {
        // --- Mocks Setup ---
        FirebaseDatabase dbMock = mock(FirebaseDatabase.class);
        DatabaseReference rootRefMock = mock(DatabaseReference.class);
        DatabaseReference hauserRefMock = mock(DatabaseReference.class);
        DatabaseReference specificHausRefMock = mock(DatabaseReference.class);
        DatabaseReference produkteRefMock = mock(DatabaseReference.class);
        DatabaseReference specificProduktRefMock = mock(DatabaseReference.class);

        when(dbMock.getReference()).thenReturn(rootRefMock);
        when(rootRefMock.child("Hauser")).thenReturn(hauserRefMock);
        when(hauserRefMock.child("test-haus-id")).thenReturn(specificHausRefMock);
        when(specificHausRefMock.child("produkte")).thenReturn(produkteRefMock);
        when(produkteRefMock.child("test-produkt-id")).thenReturn(specificProduktRefMock);

        Task<Void> successTask = Tasks.forResult(null);
        when(specificProduktRefMock.updateChildren(any(Map.class))).thenReturn(successTask);

        try (MockedStatic<FirebaseDatabase> mockedDb = mockStatic(FirebaseDatabase.class)) {
            mockedDb.when(() -> FirebaseDatabase.getInstance(anyString())).thenReturn(dbMock);

            // --- Activity Setup ---
            Intent intent = new Intent();
            intent.putExtra("haus_id", "test-haus-id");
            intent.putExtra("produkt_id", "test-produkt-id");
            intent.putExtra("name", "Old Name");
            intent.putExtra("menge", 10);
            intent.putExtra("einheit", "Stk");
            intent.putExtra("kategorie", "Obst");
            intent.putExtra("mindBestand", 5);

            var controller = Robolectric.buildActivity(pUpdateActivity.class, intent).create().start().resume();
            pUpdateActivity activity = controller.get();

            // --- UI Interaction ---
            EditText nameInput = activity.findViewById(R.id.pName);
            EditText mengeInput = activity.findViewById(R.id.pMenge);

            nameInput.setText("New Updated Name");
            mengeInput.setText("25");

            Button updateButton = activity.findViewById(R.id.add);
            updateButton.performClick();

            ShadowLooper.idleMainLooper();

            // --- Verification ---
            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(specificProduktRefMock).updateChildren(mapCaptor.capture());

            Map<String, Object> capturedMap = mapCaptor.getValue();
            assertEquals("New Updated Name", capturedMap.get("name"));
            assertEquals("new updated name", capturedMap.get("name_lower"));
            assertEquals(25, capturedMap.get("menge"));

            assertTrue(activity.isFinishing());
            assertEquals(pUpdateActivity.RESULT_OK, shadowOf(activity).getResultCode());
        }
    }
}
