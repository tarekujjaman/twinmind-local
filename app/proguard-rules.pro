# Default ProGuard rules for TwinMind Local
-keepattributes *Annotation*
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static <methods>;
}
