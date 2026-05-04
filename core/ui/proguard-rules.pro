# =============================================================================
# core:ui module ProGuard rules
# =============================================================================

# Jetpack Compose - keep stable/immutable annotations for runtime stability inference
-keep @androidx.compose.runtime.Stable class * { *; }
-keep @androidx.compose.runtime.Immutable class * { *; }
-keep @androidx.compose.ui.tooling.preview.Preview class * { *; }

# Material3 Adaptive
-keep class androidx.compose.material3.adaptive.** { *; }
-dontwarn androidx.compose.material3.adaptive.**

# Coil image loading
-keep class coil3.** { *; }
-dontwarn coil3.**

# OkHttp (used by coil-network-okhttp)
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlin metadata and coroutines
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Preserve source file and line number info for crash reports
-keepattributes SourceFile,LineNumberTable
