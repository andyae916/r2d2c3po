# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class com.google.vr.** { *; }
-keep class com.google.vr.sdk.base.** { *; }
-keep class com.google.vrtoolkit.cardboard.** { *; }
-keep class com.google.protobuf.nano.** { *; }
-keep class com.google.common.logging.nano.** { *; }
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry
