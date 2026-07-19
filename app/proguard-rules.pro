# Moshi — keep generated JSON adapters and the classes they reflect over.
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class **JsonAdapter {
    <init>(...);
    <fields>;
}
-keep class com.maangatech.gojuagent.core.network.dto.** { *; }
-keep class com.maangatech.gojuagent.core.ussd.model.** { *; }

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# SQLCipher
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# Hilt / Dagger — generated factories rely on reflection-free codegen, but keep entry points safe.
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# AccessibilityService must keep its class name — declared by fully-qualified name in
# ussd_accessibility_service_config.xml / the manifest.
-keep class com.maangatech.gojuagent.core.ussd.accessibility.UssdAccessibilityService { *; }
