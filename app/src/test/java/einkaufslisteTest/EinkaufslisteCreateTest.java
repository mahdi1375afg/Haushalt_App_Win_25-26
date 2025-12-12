package einkaufslisteTest;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.haushalt_app_java.einkaufsliste.Einkaufsliste;
import com.example.haushalt_app_java.einkaufsliste.EinkaufslisteService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class EinkaufslisteCreateTest {

    @Test
    public void testAddEinkaufsliste() {

        FirebaseDatabase mockDb = mock(FirebaseDatabase.class);
        DatabaseReference mockRoot = mock(DatabaseReference.class);
        DatabaseReference mockHauser = mock(DatabaseReference.class);
        DatabaseReference mockHaus = mock(DatabaseReference.class);
        DatabaseReference mockListen = mock(DatabaseReference.class);
        DatabaseReference mockListeId = mock(DatabaseReference.class);

        when(mockDb.getReference()).thenReturn(mockRoot);
        when(mockRoot.child("Hauser")).thenReturn(mockHauser);
        when(mockHauser.child("haus123")).thenReturn(mockHaus);
        when(mockHaus.child("einkaufslisten")).thenReturn(mockListen);
        when(mockListen.child("listeABC")).thenReturn(mockListeId);


        when(mockListeId.setValue(any())).thenAnswer(invocation -> {

            Task<Void> task = mock(Task.class);

            doAnswer(successInv -> {
                com.google.android.gms.tasks.OnSuccessListener<Void> listener =
                        successInv.getArgument(0);
                listener.onSuccess(null);
                return task;
            }).when(task).addOnSuccessListener(any());

            when(task.addOnFailureListener(any())).thenReturn(task);

            return task;
        });

        try (MockedStatic<FirebaseDatabase> mockedStatic = mockStatic(FirebaseDatabase.class)) {
            mockedStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                    .thenReturn(mockDb);

            EinkaufslisteService service = new EinkaufslisteService();

            Einkaufsliste liste = new Einkaufsliste(
                    "listeABC",
                    "haus123",
                    "Testliste"
            );

            final boolean[] successCalled = { false };

            service.addEinkaufsliste(
                    liste,
                    () -> successCalled[0] = true,
                    () -> fail("onError darf nicht aufgerufen werden")
            );

            assertTrue("onSuccess wurde NICHT aufgerufen!", successCalled[0]);
        }
    }
}
