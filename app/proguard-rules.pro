# R8 optimization rules for AutoMatrix

# Strip verbose logging from release builds
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# Room database implementations are resolved reflectively at runtime
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { <fields>; }
-keep @androidx.room.Dao interface * { *; }

# Keep names for coroutine debugging and exception handlers
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
