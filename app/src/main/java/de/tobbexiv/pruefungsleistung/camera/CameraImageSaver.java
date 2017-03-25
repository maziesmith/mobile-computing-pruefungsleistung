package de.tobbexiv.pruefungsleistung.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CameraImageSaver implements Runnable {
    private static final int MAX_IMAGE_SIZE = 128;

    private Image image;
    private File file;

    public CameraImageSaver(Image image, File file) {
        this.image = image;
        this.file = file;
    }

    @Override
    public void run() {
        storeOriginalFile();
        storeScaledFile();
    }

    private void storeOriginalFile() {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            image.close();
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void storeScaledFile() {
        Bitmap bitmap = getScaledBitmap();
        FileOutputStream output = null;

        try {
            output = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap getScaledBitmap() {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = determineScaleFactor(width, height);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        matrix.postRotate(determineRotation());

        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        bitmap.recycle();
        return scaledBitmap;
    }

    private float determineScaleFactor(int originalWidth, int originalHeight) {
        float factorWidth  = 1.0f * MAX_IMAGE_SIZE / originalWidth;
        float factorHeight = 1.0f * MAX_IMAGE_SIZE / originalHeight;

        return factorWidth > factorHeight ? factorHeight : factorWidth;
    }

    private int determineRotation() {
        try {
            ExifInterface exif = new ExifInterface(file.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
