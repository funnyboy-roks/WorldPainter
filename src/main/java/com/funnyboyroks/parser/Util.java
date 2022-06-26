package com.funnyboyroks.parser;

import java.util.Arrays;

public class Util {

    public static byte[] slice(byte[] input, int offset, int length) {

        byte[] out = new byte[length];
        System.arraycopy(input, offset, out, 0, length);
        return out;

    }

    public static long sliceBE(byte[] input, int offset, int length) {

        long out = 0;
        for (int i = 0; i < length; i++) {
            out |= (long) (input[offset + i] & 0xff) << (8 * (length - i - 1));
        }
        return out;

    }

    public static byte[] join(byte[]... arrs) {
        int size = Arrays.stream(arrs).mapToInt(a -> a.length).sum();
        byte[] out = new byte[size];
        int pointer = 0;
        for (byte[] arr : arrs) {
            System.arraycopy(arr, 0, out, pointer, arr.length);
            pointer += arr.length;
        }
        return out;
    }

    public static String padStart(String str, int length, String padding) {
        if (str.length() < length) {
            return padding.repeat(length - str.length()) + str;
        }
        return str.substring(0, length);
    }

    public static float map(float n, float start1, float stop1, float start2, float stop2) {
        return ((n - start1) / (stop1 - start1)) * (stop2 - start2) + start2;
    }

}
