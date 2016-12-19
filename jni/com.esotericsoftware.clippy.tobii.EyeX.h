
#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_esotericsoftware_clippy_tobii_EyeX__1connect (JNIEnv*, jobject, jstring);

JNIEXPORT jboolean JNICALL Java_com_esotericsoftware_clippy_tobii_EyeX__1disconnect (JNIEnv*, jobject, jlong);

#ifdef __cplusplus
}
#endif
