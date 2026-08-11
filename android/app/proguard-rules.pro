# R8 / Play release. Do not keep the whole app package — that undoes minify.

-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# --- Kotlinx serialization (Ktor DTOs) ---
-keepattributes *Annotation*
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.mizbamd.zikra.data.remote.**$$serializer { *; }
-keepclassmembers class com.mizbamd.zikra.data.remote.** {
    *** Companion;
}
-keepclasseswithmembers class com.mizbamd.zikra.data.remote.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# --- Koin (constructor DSL + Android context) ---
-keep class org.koin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* <methods>;
}
-dontwarn org.koin.**

# --- Ktor / OkHttp ---
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- Compose (compiler + runtime already ship consumer rules; keep ViewModel factories) ---
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**
