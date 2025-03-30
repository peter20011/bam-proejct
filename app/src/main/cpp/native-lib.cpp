#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_projekt_NativeXor_encryptXor(
        JNIEnv* env,
        jobject,
        jbyteArray input,
        jbyte key) {

    jsize len = env->GetArrayLength(input);
    jbyte* buffer = env->GetByteArrayElements(input, nullptr);

    for (int i = 0; i < len; ++i) {
        buffer[i] = buffer[i] ^ key;
    }

    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, buffer);
    env->ReleaseByteArrayElements(input, buffer, 0);

    return result;
}
