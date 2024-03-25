package com.github.alantr7.bukkitplugin.annotations.processor.reader;

public class ByteBufferReader {

    private byte[] buffer;

    private int position = 0;

    public ByteBufferReader(byte[] buffer) {
        this.buffer = buffer;
    }

    public byte[] readBytes(int length) {
        var bytes = new byte[length];
        System.arraycopy(buffer, position, bytes, 0, length);

        position += length;
        return bytes;
    }

    public int readU1() {
        return readBytes(1)[0];
    }

    public String readString() {
        var length = buffer[position++];
        var string = readBytes(length);

        return new String(string);
    }

    public static int toInt(byte[] bytes) {
        int result = 0;
        int index = 0;

        while (index < bytes.length) {
            result <<= 8;
            result = result | (bytes[index++] & 255);
        }

        return result;
    }

    public static int toInt(byte[] bytes, int offset, int length) {
        byte[] buffer = new byte[length];
        System.arraycopy(bytes, offset, buffer, 0, length);

        return toInt(buffer);
    }

    private void _writeBytes(byte... bytes) {
        int target = position + bytes.length;
        for (int i = 0; position < target;) {
            buffer[position++] = bytes[i++];
        }
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
