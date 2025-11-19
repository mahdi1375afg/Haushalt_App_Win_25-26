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
public class EinkaufslisteUpdateTest {

    @Test
    public void testUpdateEinkaufslisteName() {

        FirebaseDatabase mockDb = mock(FirebaseDatabase.class);
        DatabaseReference mockRoot = mock(DatabaseReference.class);
        DatabaseReference mockHauser = mock(DatabaseReference.class);
        DatabaseReference mockHaus = mock(DatabaseReference.class);
        DatabaseReference mockListen = mock(DatabaseReference.class);
        DatabaseReference mockListeId = mock(DatabaseReference.class);
        DatabaseReference mockNameRef = mock(DatabaseReference.class);

        when(mockDb.getReference()).thenReturn(mockRoot);
        when(mockRoot.child("Hauser")).thenReturn(mockHauser);
        when(mockHauser.child("haus123")).thenReturn(mockHaus);
        when(mockHaus.child("einkaufslisten")).thenReturn(mockListen);
        when(mockListen.child("listeABC")).thenReturn(mockListeId);
        when(mockListeId.child("name")).thenReturn(mockNameRef);

        when(mockNameRef.setValue(anyString())).thenAnswer(inv -> {

            Task<Void> task = mock(Task.class);

            doAnswer(l -> {
                com.google.android.gms.tasks.OnSuccessListener<Void> s = l.getArgument(0);
                s.onSuccess(null);
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

            service.updateEinkaufslisteName(
                    "haus123", "listeABC", "NeuName",
                    () -> ok[0] = true,
                    () -> fail("Fehlerlistener darf nicht gerufen werden")
            );

            assertTrue("onSuccess wurde nicht ausgelöst!", ok[0]);
        }
    }
}
