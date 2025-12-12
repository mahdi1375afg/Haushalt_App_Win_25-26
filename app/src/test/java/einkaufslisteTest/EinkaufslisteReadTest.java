package einkaufslisteTest;

import static org.mockito.Mockito.*;

import com.example.haushalt_app_java.einkaufsliste.EinkaufslisteService;
import com.google.firebase.database.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class EinkaufslisteReadTest {

    @Test
    public void testGetEinkaufslisten_callsListener() {

        FirebaseDatabase mockDb = mock(FirebaseDatabase.class);
        DatabaseReference mockRoot = mock(DatabaseReference.class);
        DatabaseReference mockHauser = mock(DatabaseReference.class);
        DatabaseReference mockHaus = mock(DatabaseReference.class);
        DatabaseReference mockListen = mock(DatabaseReference.class);

        when(mockDb.getReference()).thenReturn(mockRoot);
        when(mockRoot.child("Hauser")).thenReturn(mockHauser);
        when(mockHauser.child("haus123")).thenReturn(mockHaus);
        when(mockHaus.child("einkaufslisten")).thenReturn(mockListen);

        ValueEventListener mockListener = mock(ValueEventListener.class);

        doAnswer(inv -> {
            ValueEventListener passedListener = inv.getArgument(0);
            passedListener.onDataChange(mock(DataSnapshot.class));
            return null;
        }).when(mockListen).addListenerForSingleValueEvent(any());

        try (MockedStatic<FirebaseDatabase> mockedStatic = mockStatic(FirebaseDatabase.class)) {

            mockedStatic.when(() -> FirebaseDatabase.getInstance(anyString()))
                    .thenReturn(mockDb);

            EinkaufslisteService service = new EinkaufslisteService();

            service.getEinkaufslisten("haus123", mockListener);

            verify(mockListen, times(1)).addListenerForSingleValueEvent(any());
            verify(mockListener, times(1)).onDataChange(any());
        }
    }
}
