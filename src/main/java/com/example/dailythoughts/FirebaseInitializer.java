package com.example.dailythoughts;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Component("firebaseInitializer")
public class FirebaseInitializer {

    @PostConstruct
    public void init() throws IOException {
        System.out.println("\n========== 🔥 FIREBASE INITIALIZATION DEBUG START ==========");

        // =============================================
        // STEP 1: Check ALL environment variables
        // =============================================
        System.out.println("\n📋 ALL ENVIRONMENT VARIABLES:");
        System.out.println("----------------------------------------");
        Map<String, String> env = System.getenv();
        boolean foundFirebaseVars = false;

        for (Map.Entry<String, String> entry : env.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Check for any Firebase related variables
            if (key.toUpperCase().contains("FIREBASE") ||
                    key.toUpperCase().contains("FIRE_BASE") ||
                    key.contains("DB_URL") ||
                    key.contains("DATABASE")) {

                foundFirebaseVars = true;
                // Mask sensitive values
                if (key.contains("CONFIG") || key.contains("PRIVATE") || key.contains("SECRET")) {
                    System.out.println("   ✅ " + key + " = [SET - value hidden for security]");
                    System.out.println("      Length: " + (value != null ? value.length() : 0) + " characters");
                    System.out.println("      Starts with: " + (value != null && value.length() > 20 ?
                            value.substring(0, Math.min(20, value.length())) + "..." : "null"));
                } else {
                    System.out.println("   ✅ " + key + " = " + value);
                }
            }
        }

        if (!foundFirebaseVars) {
            System.out.println("   ❌ NO FIREBASE ENVIRONMENT VARIABLES FOUND!");
            System.out.println("   Available variables (first 10):");
            env.entrySet().stream()
                    .limit(10)
                    .forEach(e -> System.out.println("      " + e.getKey() + " = " +
                            (e.getKey().contains("KEY") || e.getKey().contains("SECRET") ? "[MASKED]" : e.getValue())));
        }

        // =============================================
        // STEP 2: Check specific required variables
        // =============================================
        System.out.println("\n🔍 CHECKING REQUIRED VARIABLES:");
        System.out.println("----------------------------------------");

        String firebaseConfig = System.getenv("FIREBASE_CONFIG");
        String dbUrl = System.getenv("FIREBASE_DB_URL");

        // Check FIREBASE_CONFIG
        if (firebaseConfig == null) {
            System.out.println("   ❌ FIREBASE_CONFIG = NULL (not set)");

            // Try alternative common names
            System.out.println("\n   🔄 Checking alternative variable names:");
            String[] altConfigNames = {
                    "FIREBASE_CONFIG_JSON",
                    "FIREBASE_SERVICE_ACCOUNT",
                    "FIREBASE_CREDENTIALS",
                    "GOOGLE_APPLICATION_CREDENTIALS",
                    "FIREBASE_CONFIGURATION",
                    "FB_CONFIG"
            };

            for (String altName : altConfigNames) {
                String altValue = System.getenv(altName);
                if (altValue != null) {
                    System.out.println("      ✅ Found alternative: " + altName + " is SET!");
                    firebaseConfig = altValue; // Use this instead
                    System.out.println("      ℹ️ Using " + altName + " as FIREBASE_CONFIG");
                    break;
                } else {
                    System.out.println("      ❌ " + altName + " = not set");
                }
            }
        } else {
            System.out.println("   ✅ FIREBASE_CONFIG = SET");
            System.out.println("      Length: " + firebaseConfig.length() + " characters");
            System.out.println("      Preview: " + firebaseConfig.substring(0, Math.min(50, firebaseConfig.length())) + "...");

            // Check if it's valid JSON
            String trimmed = firebaseConfig.trim();
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                System.out.println("      ✅ Looks like valid JSON format");
            } else {
                System.out.println("      ⚠️  Warning: Doesn't look like JSON (should start with '{' and end with '}')");
                System.out.println("      First char: '" + trimmed.charAt(0) + "', Last char: '" +
                        trimmed.charAt(trimmed.length() - 1) + "'");
            }
        }

        // Check FIREBASE_DB_URL
        if (dbUrl == null) {
            System.out.println("   ❌ FIREBASE_DB_URL = NULL (not set)");

            // Try alternative common names
            System.out.println("\n   🔄 Checking alternative database URL names:");
            String[] altDbNames = {
                    "FIREBASE_DATABASE_URL",
                    "FIREBASE_URL",
                    "DATABASE_URL",
                    "DB_URL",
                    "FIREBASE_REALTIME_DB",
                    "FB_DB_URL"
            };

            for (String altName : altDbNames) {
                String altValue = System.getenv(altName);
                if (altValue != null) {
                    System.out.println("      ✅ Found alternative: " + altName + " = " + altValue);
                    dbUrl = altValue; // Use this instead
                    System.out.println("      ℹ️ Using " + altName + " as FIREBASE_DB_URL");
                    break;
                } else {
                    System.out.println("      ❌ " + altName + " = not set");
                }
            }
        } else {
            System.out.println("   ✅ FIREBASE_DB_URL = " + dbUrl);

            // Validate URL format
            if (dbUrl.startsWith("https://") && dbUrl.contains("firebaseio.com")) {
                System.out.println("      ✅ Valid Firebase database URL format");
            } else {
                System.out.println("      ⚠️  Warning: URL format may be incorrect");
                System.out.println("      Expected: https://your-project.firebaseio.com");
            }
        }

        // =============================================
        // STEP 3: System Properties Check
        // =============================================
        System.out.println("\n💻 SYSTEM PROPERTIES:");
        System.out.println("----------------------------------------");
        System.out.println("   Java version: " + System.getProperty("java.version"));
        System.out.println("   OS: " + System.getProperty("os.name"));
        System.out.println("   Working dir: " + System.getProperty("user.dir"));

        // =============================================
        // STEP 4: Final validation and initialization
        // =============================================
        System.out.println("\n🎯 FINAL VALIDATION:");
        System.out.println("----------------------------------------");

        if (firebaseConfig == null) {
            String errorMsg = "❌ CRITICAL: FIREBASE_CONFIG environment variable is not set!\n" +
                    "   Please add it in Railway with the exact name 'FIREBASE_CONFIG'\n" +
                    "   Value should be your complete Firebase service account JSON";
            System.out.println(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        if (dbUrl == null) {
            String errorMsg = "❌ CRITICAL: FIREBASE_DB_URL environment variable is not set!\n" +
                    "   Please add it in Railway with the exact name 'FIREBASE_DB_URL'\n" +
                    "   Example: https://your-project-id-default-rtdb.firebaseio.com";
            System.out.println(errorMsg);
            throw new RuntimeException("Firebase env variables not set! Missing FIREBASE_DB_URL");
        }

        // =============================================
        // STEP 5: Attempt Firebase Initialization
        // =============================================
        System.out.println("\n🚀 ATTEMPTING FIREBASE INITIALIZATION:");
        System.out.println("----------------------------------------");

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                System.out.println("   📦 Creating FirebaseOptions...");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(
                                new ByteArrayInputStream(firebaseConfig.getBytes())
                        ))
                        .setDatabaseUrl(dbUrl)
                        .build();

                System.out.println("   ✅ FirebaseOptions created successfully");

                System.out.println("   🔥 Initializing FirebaseApp...");
                FirebaseApp.initializeApp(options);

                System.out.println("   ✅ FirebaseApp initialized successfully!");
                System.out.println("   📊 Active Firebase Apps: " + FirebaseApp.getApps().size());

            } else {
                System.out.println("   ℹ️ Firebase already initialized, skipping...");
            }

            System.out.println("\n✨ Firebase initialization completed successfully!");

        } catch (Exception e) {
            System.out.println("\n❌ Firebase initialization FAILED!");
            System.out.println("   Error type: " + e.getClass().getName());
            System.out.println("   Error message: " + e.getMessage());

            // Detailed error analysis
            if (e.getMessage() != null) {
                if (e.getMessage().contains("JSON")) {
                    System.out.println("\n🔧 TROUBLESHOOTING: JSON Format Error");
                    System.out.println("   Your FIREBASE_CONFIG doesn't contain valid JSON");
                    System.out.println("   Make sure it's a complete Firebase service account JSON");
                    System.out.println("   Check that all quotes are properly escaped");
                    System.out.println("   Try using a single-line JSON without line breaks");
                } else if (e.getMessage().contains("private key")) {
                    System.out.println("\n🔧 TROUBLESHOOTING: Private Key Format Error");
                    System.out.println("   The private key in your JSON needs proper formatting");
                    System.out.println("   Replace newlines with \\n in the private key");
                } else if (e.getMessage().contains("URL")) {
                    System.out.println("\n🔧 TROUBLESHOOTING: Database URL Error");
                    System.out.println("   Check that FIREBASE_DB_URL is correct");
                }
            }

            e.printStackTrace();
            throw new RuntimeException("Firebase initialization failed: " + e.getMessage(), e);
        }

        System.out.println("\n========== 🔥 FIREBASE INITIALIZATION DEBUG END ==========\n");
    }
}