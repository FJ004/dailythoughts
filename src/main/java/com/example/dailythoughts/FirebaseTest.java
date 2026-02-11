package com.example.dailythoughts;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class FirebaseTest {

    @PostConstruct
    public void testWrite() {
        DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference("test");

        ref.setValueAsync("Firebase connected successfully ❤️");
    }
}
