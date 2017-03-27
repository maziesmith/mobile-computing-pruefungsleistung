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
    private TextView mCellIdLac;
    private TextView mGpsData;

    private String locationId;
    private String cellId;
    private String lac;
    private String imageSrc;

    public DisplayLocationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.HORIZONTAL);
        setPadding(5, 5, 5, 5);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_display_location, this, true);

        getElements();

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DisplayLocationView, 0, 0);

            String locationId = a.getString(R.styleable.DisplayLocationView_locationId);
            String imageSrc = a.getString(R.styleable.DisplayLocationView_imageSrc);
            String locationDescription = a.getString(R.styleable.DisplayLocationView_locationDescription);
            String cellId = a.getString(R.styleable.DisplayLocationView_cellId);
            String lac = a.getString(R.styleable.DisplayLocationView_lac);
            String gpsData = a.getString(R.styleable.DisplayLocationView_gpsData);

            a.recycle();

            update(locationId, cellId, lac, locationDescription, imageSrc, gpsData);
        }
    }

    public DisplayLocationView(Context context, String locationId, String cellId, String lac, String locationDescription, String imageSrc, String gpsData) {
        this(context);

        update(locationId, cellId, lac, locationDescription, imageSrc, gpsData);
    }

    public DisplayLocationView(Context context) {
        this(context, null);
    }

    private void getElements() {
        mImage = (ImageView) getChildAt(0);
        LinearLayout l1 = (LinearLayout) getChildAt(1);
        mLocationDescription = (TextView) l1.getChildAt(0);
        mCellIdLac = (TextView) l1.getChildAt(1);
        mGpsData = (TextView) l1.getChildAt(2);
    }

    private void update(String locationId, String cellId, String lac, String locationDescription, String imageSrc, String gpsData) {
        this.locationId = locationId;
        updateImageSrc(imageSrc);
        updateLocationDescription(locationDescription);
        updateCellId(cellId, lac);
        updateGpsData(gpsData);
    }

    public void updateImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;

        File imgFile = new File(imageSrc);

        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            mImage.setImageBitmap(myBitmap);
        }
    }

    public void updateLocationDescription(String locationDescription) {
        mLocationDescription.setText(locationDescription);
    }

    public void updateCellId(String cellId, String lac) {
        this.cellId = cellId;
        this.lac = lac;
        mCellIdLac.setText(getContext().getString(R.string.display_location_view_cell_id, cellId, lac));
    }

    public void updateGpsData(String gpsData) {
        mGpsData.setText(getContext().getString(R.string.display_location_view_gps, gpsData));
    }

    public void redrawImage() {
        updateImageSrc(imageSrc);
    }

    public String getLocationId() {
        return locationId;
    }

    public String getCellId() {
        return cellId;
    }

    public String getLac() {
        return lac;
    }

}