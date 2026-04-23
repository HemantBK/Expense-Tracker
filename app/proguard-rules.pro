# SPDX-License-Identifier: GPL-3.0-or-later

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keepclasseswithmembernames class * { @dagger.* <methods>; }

# Keep Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *

# Keep SQLCipher
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# Keep TFLite
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**

# Keep Tesseract
-keep class com.googlecode.tesseract.android.** { *; }
-keep class cz.adaptech.tesseract4android.** { *; }

# Keep serialization generated code
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep,includedescriptorclasses class com.paisavault.**$$serializer { *; }
-keepclassmembers class com.paisavault.** {
    *** Companion;
}
-keepclasseswithmembers class com.paisavault.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep domain models (used across reflection-sensitive boundaries)
-keep class com.paisavault.core.domain.model.** { *; }
-keep class com.paisavault.core.database.entity.** { *; }

# Strip verbose logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Timber: do not strip warn/error
-keep class timber.log.Timber { *; }
