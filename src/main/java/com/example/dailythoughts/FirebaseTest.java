package com.example.dailythoughts;

import com.google.firebase.database.FirebaseDatabase;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
@DependsOn("firebaseInitializer") // ensures FirebaseInitializer runs first
public class FirebaseTest {

    private FirebaseDatabase db;

    @PostConstruct
    public void init() {
        // Lazy-load FirebaseDatabase only after FirebaseInitializer runs
        db = FirebaseDatabase.getInstance();
        System.out.println("✅ FirebaseTest ready!");
    }

    public void testWrite() {
        db.getReference("test").setValueAsync("Hello from Railway!");
        System.out.println("✅ Wrote test value to Firebase");
    }
}
