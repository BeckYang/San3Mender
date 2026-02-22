package com.beck.san3.mender;

public class EditConfig {
    public static boolean roleEditable = true;
    
    public static int highByte(final byte data) {
        return (data & 0xf0) >> 4;
    }
    public static int lowByte(final byte data) {
        return data & 0x0f;
    }
    public static byte highAndLow(final int highByte, final int lowByte) {
        byte b = (byte) ((highByte << 4) | lowByte);
        return b;
    }
    
    public static int editByte(final int data, final int mask, final boolean flag) {
        final boolean cf = (data & mask) > 0;
        if (flag == cf) {
            return data;
        }
        int x = data;
        if (flag) {
            x = x | (0xff & mask);
        } else {
            x = x ^ mask;
        }
        return x;
    }
    
}
