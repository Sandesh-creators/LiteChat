-keepattributes *Annotation*
-keep class com.litechat.app.data.db.** { *; }
-keep class com.litechat.app.data.db.entity.** { *; }
-keep class com.litechat.app.network.signaling.** { *; }
-keep class com.litechat.app.data.repository.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn javax.annotation.**
-dontwarn org.webrtc.**
-keep class org.webrtc.** { *; }
-keep class com.litechat.app.network.github.** { *; }
-keepattributes kotlinx.serialization.KSerializer
-keepclassmembers class com.litechat.app.network.github.** {
    *** Companion;
}
-keepclasseswithmembers class com.litechat.app.network.github.** {
    kotlinx.serialization.KSerializer serializer(...);
}
