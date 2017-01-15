package de.tobbexiv.pruefungsleistung.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import de.tobbexiv.pruefungsleistung.R;

public class DisplayLocationView extends LinearLayout {

    private ImageView mImage;
    private TextView mLocationDescription;
    private TextView mCellId;
    private TextView mGpsData;

    public DisplayLocationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_display_location, this, true);

        getElements();

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DisplayLocationView, 0, 0);

            String imageSrc = a.getString(R.styleable.DisplayLocationView_imageSrc);
            String locationDescription = a.getString(R.styleable.DisplayLocationView_locationDescription);
            String cellId = a.getString(R.styleable.DisplayLocationView_cellId);
            String gpsData = a.getString(R.styleable.DisplayLocationView_gpsData);

            a.recycle();

            update(imageSrc, locationDescription, cellId, gpsData);
        }
    }

    public DisplayLocationView(Context context, String imageSrc, String locationDescription, String cellId, String gpsData) {
        this(context);

        update(imageSrc, locationDescription, cellId, gpsData);
    }

    public DisplayLocationView(Context context) {
        this(context, null);
    }

    private void getElements() {
        mImage = (ImageView) getChildAt(0);
        LinearLayout l1 = (LinearLayout) getChildAt(1);
        mLocationDescription = (TextView) l1.getChildAt(0);
        LinearLayout l2 = (LinearLayout) l1.getChildAt(1);
        mCellId = (TextView) l2.getChildAt(0);
        mGpsData = (TextView) l2.getChildAt(1);
    }

    private void update(String imageSrc, String locationDescription, String cellId, String gpsData) {
        updateImageSrc(imageSrc);
        updateLocationDescription(locationDescription);
        updateCellId(cellId);
        updateGpsData(gpsData);
    }

    public void updateImageSrc(String imageSrc) {
        File imgFile = new File(imageSrc);

        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            mImage.setImageBitmap(myBitmap);
        }
    }

    public void updateLocationDescription(String locationDescription) {
        mLocationDescription.setText(locationDescription);
    }

    public void updateCellId(String cellId) {
        mCellId.setText(cellId);
    }

    public void updateGpsData(String gpsData) {
        mGpsData.setText(gpsData);
    }

}