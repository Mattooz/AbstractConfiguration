package it.niccolomattei.configuration.impl.handlers;

import it.niccolomattei.configuration.api.ResourceHandler;

import java.io.*;

public class FileResourceHandler implements ResourceHandler {

    private final File file;

    public FileResourceHandler(File file) {
        this.file = file;
    }

    public FileResourceHandler(String filePath) {
        this.file = new File(filePath);
    }

    @Override
    public byte[] toByteArray() throws IOException {
        if (!file.exists()) throw new IOException();

        FileInputStream fileInputStream = new FileInputStream(file);
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        byte[] temp = new byte[1024];
        int n;

        while ((n = fileInputStream.read(temp, 0, temp.length)) != -1) {
            byteOutputStream.write(temp, 0, n);
        }

        return byteOutputStream.toByteArray();
    }

    @Override
    public void toOriginalFormat(byte[] byteArray) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);

        outputStream.write(byteArray);
        outputStream.flush();
        outputStream.close();
    }
}
