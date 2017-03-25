package de.tobbexiv.pruefungsleistung.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import de.tobbexiv.pruefungsleistung.R;

public class CameraActivity extends AppCompatActivity {
    private TextureView preview;
    private Button captureImage;

    private CameraBackgroundThread backgroundThread = new CameraBackgroundThread();
    private Camera camera;

    private static final int REQUEST_APP_PERMISSIONS = 200;

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            camera.open();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera);

        camera = new Camera(this, getIntent().getStringExtra("FILE_NAME"));

        preview = (TextureView) findViewById(R.id.camera_preview);
        preview.setSurfaceTextureListener(surfaceTextureListener);

        captureImage = (Button) findViewById(R.id.camera_take_picture);
        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        backgroundThread.start();

        if(preview == null) {
            return;
        }

        if (preview.isAvailable()) {
            camera.open();
        } else {
            preview.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        backgroundThread.stop();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        backgroundThread.stop();

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_APP_PERMISSIONS) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(CameraActivity.this, "You need to grant all permissions!", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }

            camera.open();
        }
    }

    public CameraManager getCameraManager() {
        return (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    }

    public void requestAppPermissions() {
        ActivityCompat.requestPermissions(CameraActivity.this, new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_APP_PERMISSIONS);
    }

    public SurfaceTexture getPreviewSurfaceTexture() {
        return preview.getSurfaceTexture();
    }

    public int getOrientation() {
        switch(getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                return 90;
            case Surface.ROTATION_90:
                return 0;
            case Surface.ROTATION_180:
                return 270;
            case Surface.ROTATION_270:
                return 180;
            default:
                return 0;
        }
    }

    public Handler getBackgroundHandler() {
        return backgroundThread.getHandler();
    }
}
