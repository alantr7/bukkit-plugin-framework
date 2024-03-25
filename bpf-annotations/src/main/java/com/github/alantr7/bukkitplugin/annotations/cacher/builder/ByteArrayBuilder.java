package com.github.alantr7.bukkitplugin.annotations.cacher.builder;

import java.nio.charset.StandardCharsets;

public class ByteArrayBuilder {

    private byte[] buffer = new byte[0];

    private int position = 0;

    public void writeBytes(byte[] bytes) {
        ensureCapacity(buffer.length + bytes.length);
        _writeBytes(bytes);
    }

    public void writeString(String string) {
        ensureCapacity(buffer.length + string.length() + 1);
        buffer[position++] = (byte) (string.length() & 0xff);
        _writeBytes(string.getBytes(StandardCharsets.UTF_8));
    }

    public void writeU1(int num) {
        ensureCapacity(buffer.length + 1);
        _writeBytes((byte) (num & 0xff));
    }

    public void writeU2(int num) {
        ensureCapacity(buffer.length + 2);
        _writeBytes(toBytes(num, 2));
    }

    public static byte[] toBytes(long number) {
        int index = 0;
        int length = 0;
        long temp = number;

        while (temp > 0) {
            temp >>= 8;
            length++;
        }

        byte[] bytes = new byte[length];
        while (number > 0) {
            bytes[index++] = (byte) ((number & 255));
            number >>= 8;
        }

        byte[] reversed = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            reversed[bytes.length - 1 - i] = bytes[i];
        }

        return reversed;
    }

    public static byte[] toBytes(long number, int capacity) {
        var bytes = toBytes(number);
        if (bytes.length >= capacity)
            return bytes;

        int offset = capacity - bytes.length;
        var upgraded = new byte[capacity];
        System.arraycopy(bytes, 0, upgraded, offset, bytes.length);

        return upgraded;
    }

    private void _writeBytes(byte... bytes) {
        System.arraycopy(bytes, 0, buffer, position, bytes.length);
        position += bytes.length;
    }

    private void ensureCapacity(int capacity) {
        if (buffer.length >= capacity)
            return;

        var newBuffer = new byte[capacity];
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);

        buffer = newBuffer;
    }

    public byte[] getBuffer() {
        return buffer;
    }

}
