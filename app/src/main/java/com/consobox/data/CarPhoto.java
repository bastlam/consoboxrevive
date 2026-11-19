package com.consobox.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import com.consobox.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Car pictures: stored in the app storage, shown cropped to a square with rounded corners. */
public final class CarPhoto {
    /** Size of the stored square picture, in pixels. */
    private static final int STORED_SIZE = 512;

    private CarPhoto() {
    }

    /**
     * Crops the picture to a centred square, scales it to {@link #STORED_SIZE} and saves it
     * as a JPEG in the app storage. Returns the value to store in voiture.photo (file:// URI).
     */
    public static String save(Context context, Bitmap source) throws IOException {
        Bitmap square = centerSquare(source, STORED_SIZE);
        File dir = new File(context.getFilesDir(), "photos");
        dir.mkdirs();
        File file = new File(dir, System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            square.compress(Bitmap.CompressFormat.JPEG, 90, out);
        }
        return Uri.fromFile(file).toString();
    }

    /**
     * Shows the car picture in the view, stretched over the whole square with rounded corners
     * (radius in dp). Falls back to the default car icon when there is no readable picture.
     * Returns true when a picture was shown.
     */
    public static boolean show(ImageView view, String photo, int radiusDp) {
        Bitmap bmp = load(view.getContext(), photo);
        if (bmp == null) {
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            view.setImageResource(R.drawable.avatar_voiture);
            return false;
        }
        Bitmap square = centerSquare(bmp, Math.min(Math.min(bmp.getWidth(), bmp.getHeight()), STORED_SIZE));
        RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(view.getResources(), square);
        // FIT_XY gives the drawable the bounds of the view: the radius is in screen pixels
        d.setCornerRadius(Utils.dipToPixel(radiusDp, view.getContext()));
        d.setAntiAlias(true);
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        view.setImageDrawable(d);
        return true;
    }

    /** Decodes a stored picture (file:// in the app storage, or content:// from older versions). */
    private static Bitmap load(Context context, String photo) {
        if (photo == null || photo.trim().isEmpty()) {
            return null;
        }
        try {
            Uri uri = Uri.parse(photo);
            if ("file".equals(uri.getScheme()) || uri.getScheme() == null) {
                String path = uri.getScheme() == null ? photo : uri.getPath();
                if (path == null || !new File(path).exists()) {
                    return null;
                }
                return decodeSampled(path, null, context);
            }
            return decodeSampled(null, uri, context);
        } catch (Exception | OutOfMemoryError e) {
            return null;
        }
    }

    private static Bitmap decodeSampled(String path, Uri uri, Context context) throws IOException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        decode(path, uri, context, o);
        if (o.outWidth <= 0 || o.outHeight <= 0) {
            return null;
        }
        int sample = 1;
        while (Math.min(o.outWidth, o.outHeight) / (sample * 2) >= STORED_SIZE) {
            sample *= 2;
        }
        o = new BitmapFactory.Options();
        o.inSampleSize = sample;
        return decode(path, uri, context, o);
    }

    private static Bitmap decode(String path, Uri uri, Context context, BitmapFactory.Options o) throws IOException {
        if (path != null) {
            return BitmapFactory.decodeFile(path, o);
        }
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            return in == null ? null : BitmapFactory.decodeStream(in, null, o);
        }
    }

    /** Centred square crop, scaled to size x size. */
    static Bitmap centerSquare(Bitmap src, int size) {
        int side = Math.min(src.getWidth(), src.getHeight());
        int x = (src.getWidth() - side) / 2;
        int y = (src.getHeight() - side) / 2;
        Bitmap cropped = Bitmap.createBitmap(src, x, y, side, side);
        if (side == size) {
            return cropped;
        }
        return Bitmap.createScaledBitmap(cropped, size, size, true);
    }
}
