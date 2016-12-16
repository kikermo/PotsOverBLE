package org.kikermo.blesansamp.utils;

/**
 * Created by EnriqueR on 10/12/2016.
 */

public class Utils {
    public static boolean notNull(Object object) {
        return !isNull(object);
    }

    public static boolean isNull(Object object) {
        return (object == null);
    }

    public static byte percentageToByte(int percentage) {
        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;

        return (byte) ((percentage * 255) / 100);
    }

    public static int byteToPercentage(byte mByte) {

        return (((mByte & 0xFF) * 100) / 255);
    }
}
