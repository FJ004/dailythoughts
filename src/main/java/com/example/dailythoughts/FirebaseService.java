package com.example.dailythoughts;

import com.google.firebase.database.*;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseService {

    private final DatabaseReference dbRef;

    public FirebaseService() {
        this.dbRef = FirebaseDatabase.getInstance().getReference();
    }

    // Save a thought to Firebase
    public void saveThought(String date, String author, String thought) {
        try {
            Map<String, Object> thoughtData = new HashMap<>();
            thoughtData.put("author", author);
            thoughtData.put("thought", thought);
            thoughtData.put("timestamp", System.currentTimeMillis());

            // Use date as key (replace colons for Firebase compatibility)
            String safeDate = date.replace(":", "-");
            dbRef.child("thoughts").child(safeDate).setValueAsync(thoughtData);

            System.out.println("✅ Saved to Firebase: " + thought);
        } catch (Exception e) {
            System.err.println("❌ Error saving to Firebase: " + e.getMessage());
        }
    }

    // Load all thoughts from Firebase
    public Map<String, Map<String, String>> loadAllThoughts() {
        Map<String, Map<String, String>> allThoughts = new HashMap<>();

        try {
            CompletableFuture<DataSnapshot> future = new CompletableFuture<>();

            dbRef.child("thoughts").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    future.complete(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    future.completeExceptionally(error.toException());
                }
            });

            DataSnapshot snapshot = future.get(); // Wait for data

            for (DataSnapshot thoughtSnapshot : snapshot.getChildren()) {
                String date = thoughtSnapshot.getKey().replace("-", ":"); // Convert back
                Map<String, String> thoughtData = new HashMap<>();

                String author = thoughtSnapshot.child("author").getValue(String.class);
                String thought = thoughtSnapshot.child("thought").getValue(String.class);
                String timestamp = thoughtSnapshot.child("timestamp").getValue(String.class);

                thoughtData.put("author", author);
                thoughtData.put("thought", thought);
                if (timestamp != null) thoughtData.put("timestamp", timestamp);

                allThoughts.put(date, thoughtData);
            }

            System.out.println("✅ Loaded " + allThoughts.size() + " thoughts from Firebase");

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error loading from Firebase: " + e.getMessage());
        }

        return allThoughts;
    }

    // Delete a thought (optional)
    public void deleteThought(String date) {
        try {
            String safeDate = date.replace(":", "-");
            dbRef.child("thoughts").child(safeDate).removeValueAsync();
            System.out.println("🗑️ Deleted thought: " + date);
        } catch (Exception e) {
            System.err.println("❌ Error deleting from Firebase: " + e.getMessage());
        }
    }
}