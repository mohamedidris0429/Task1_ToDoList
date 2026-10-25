# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\...\build-tools\...\proguard-android.txt
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.json.* <fields>;
}
