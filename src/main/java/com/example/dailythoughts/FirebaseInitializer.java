package com.example.dailythoughts;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component("firebaseInitializer") // explicit name for @DependsOn
public class FirebaseInitializer {

    @PostConstruct
    public void init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {

            // Read Firebase config from environment variable
            String firebaseConfig = System.getenv("FIREBASE_CONFIG");
            String dbUrl = System.getenv("FIREBASE_DB_URL");

            if (firebaseConfig == null || dbUrl == null) {
                throw new RuntimeException("Firebase env variables not set!");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ByteArrayInputStream(firebaseConfig.getBytes())
                    ))
                    .setDatabaseUrl(dbUrl)
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("✅ Firebase initialized successfully!");
        }
    }
}
