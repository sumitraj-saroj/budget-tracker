# Keep kotlinx.serialization generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.budgettracker.app.**$$serializer { *; }
-keepclassmembers class com.budgettracker.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.budgettracker.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Google ID
-if class com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
-keep class com.google.android.libraries.identity.googleid.** { *; }
