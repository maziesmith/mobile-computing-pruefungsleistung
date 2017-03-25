package de.tobbexiv.pruefungsleistung.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Camera {
    private CameraActivity activity;

    private CameraDevice camera;
    private Size[] outputSizes;

    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;

    private ImageReader reader;
    private String fileName;
    private File file;

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            camera = cameraDevice;
            createPreview();
        }
        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            camera.close();
        }
        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            camera.close();
            camera = null;
        }
    };

    public Camera(CameraActivity activity, String fileName) {
        this.activity = activity;
        this.fileName = fileName;

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestAppPermissions();
        }
    }

    public void open() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestAppPermissions();
            return;
        }

        if (file == null) {
            File dir = new File(Environment.getExternalStorageDirectory() + "/CellIdPictures");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file = new File(dir, fileName + ".jpg");
        }

        CameraManager manager = activity.getCameraManager();

        try {
            String cameraId = manager.getCameraIdList()[0];
            outputSizes = manager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void takePicture() {
        if(camera == null) {
            return;
        }

        try {
            cameraCaptureSession.stopRepeating();

            int outputWidth  = 480;
            int outputHeight = 640;

            if (outputSizes != null && outputSizes.length > 0) {
                outputWidth = outputSizes[0].getWidth();
                outputHeight = outputSizes[0].getHeight();
            }

            reader = ImageReader.newInstance(outputWidth, outputHeight, ImageFormat.JPEG, 5);

            List<Surface> outputSurfaces = new ArrayList<>(1);
            outputSurfaces.add(reader.getSurface());

            final CaptureRequest.Builder captureBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, activity.getOrientation());

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    activity.getBackgroundHandler().post(new CameraImageSaver(reader.acquireLatestImage(), file));
                }

            };
            reader.setOnImageAvailableListener(readerListener, activity.getBackgroundHandler());

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    returnResult();
                }
            };

            camera.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, activity.getBackgroundHandler());
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, activity.getBackgroundHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createPreview() {
        if(camera == null) {
            return;
        }

        try {
            SurfaceTexture texture = activity.getPreviewSurfaceTexture();
            texture.setDefaultBufferSize(outputSizes[0].getWidth(), outputSizes[0].getHeight());

            Surface surface = new Surface(texture);

            captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            camera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession ccs) {
                    if (null == camera) {
                        return;
                    }
                    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    cameraCaptureSession = ccs;
                    try {
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, activity.getBackgroundHandler());
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {}
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (camera != null) {
            camera.close();
            camera = null;
        }
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }

    private void returnResult() {
        closeCamera();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("FILE_PATH", file.getPath());
        this.activity.setResult(Activity.RESULT_OK, resultIntent);
        this.activity.finish();
    }
}
