package com.example.projekt

object NativeXor {
    init {
        System.loadLibrary("native-lib")
    }

    external fun encryptXor(input: ByteArray, key: Byte): ByteArray
}