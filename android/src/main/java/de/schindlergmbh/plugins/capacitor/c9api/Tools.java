package de.schindlergmbh.plugins.capacitor.c9api;

public class Tools {

    public Tools() {
    }

    public static String Bytes2HexString(byte[] b, int size) {
        String ret = "";

        for (int i = 0; i < size; ++i) {
            String hex = Integer.toHexString(b[i] & 255);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }

            ret = ret + hex.toUpperCase();
        }

        return ret;
    }

    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }));
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }));
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    public static byte[] HexString2Bytes(String src) {
        int len = src.length() / 2;
        byte[] ret = new byte[len];
        byte[] tmp = src.getBytes();

        for (int i = 0; i < len; ++i) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }

        return ret;
    }

    public static int bytesToInt(byte[] bytes) {
        int addr = bytes[0] & 255;
        addr |= bytes[1] << 8 & '\uff00';
        addr |= bytes[2] << 16 & 16711680;
        addr |= bytes[3] << 25 & -16777216;
        return addr;
    }

    public static byte[] intToByte(int i) {
        byte[] abyte0 = new byte[] { (byte) ((16711680 & i) >> 16), (byte) (('\uff00' & i) >> 8), (byte) (255 & i) };
        return abyte0;
    }
}
