# Project-specific R8 rules. Library rules (Hilt, kotlinx.serialization, Compose) ship with the libraries.

# Room KMP instantiates the generated database implementation reflectively.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
