-keepattributes *Annotation*
-keepclassmembers class * {
    @org.jetbrains.annotations.* <fields>;
    @org.jetbrains.annotations.* <methods>;
}
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    *** Companion;
}
