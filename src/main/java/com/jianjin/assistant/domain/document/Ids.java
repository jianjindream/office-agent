package com.jianjin.assistant.domain.document;

import java.security.SecureRandom;

public final class Ids {

    private static final SecureRandom RNG = new SecureRandom();
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private Ids() {}

    public static String newId(String prefix) {
        byte[] b = new byte[8];
        RNG.nextBytes(b);
        char[] chars = new char[16];
        for (int i = 0; i < 8; i++) {
            chars[2 * i] = HEX[(b[i] >> 4) & 0xF];
            chars[2 * i + 1] = HEX[b[i] & 0xF];
        }
        return prefix + "_" + new String(chars);
    }
}
