package einkaufslisteTest;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.haushalt_app_java.domain.EinkaufslisteService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class EinkaufslisteDeleteTest {

    @Test
    public void testDeleteEinkaufsliste() {

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

        when(mockListeId.removeValue()).thenAnswer(inv -> {

            Task<Void> task = mock(Task.class);

            doAnswer(invSuccess -> {
                com.google.android.gms.tasks.OnSuccessListener<Void> listener =
                        invSuccess.getArgument(0);
                listener.onSuccess(null); // sofort Trigger
                return task;
            }).when(task).addOnSuccessListener(any());

            when(task.addOnFailureListener(any())).thenReturn(task);

            return task;
        });

        try (MockedStatic<FirebaseDatabase> mockedStatic = mockStatic(FirebaseDatabase.class)) {

            mockedStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                    .thenReturn(mockDb);

            EinkaufslisteService service = new EinkaufslisteService();

            final boolean[] ok = { false };

            service.deleteEinkaufsliste("haus123", "listeABC",
                    () -> ok[0] = true,
                    () -> fail("onError darf nicht gerufen werden"));

            assertTrue("onSuccess wurde NICHT aufgerufen!", ok[0]);
        }
    }
}
