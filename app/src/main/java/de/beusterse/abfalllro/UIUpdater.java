package de.beusterse.abfalllro;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Updates the UI with given data
 *
 * Created by Felix Beuster
 */
public class UIUpdater {

    private Activity activity;
    private SharedPreferences pref;

    private ImageView canImage;
    private TextView canText;
    private TextView prevCanBlack;
    private TextView prevCanBlue;
    private TextView prevCanGreen;
    private TextView prevCanYellow;

    private ArrayList<int[]> cCans;
    private int cError;
    private int[] cPreview;

    public UIUpdater(Activity activity, SharedPreferences pref) {
        this.activity = activity;
        this.pref = pref;
    }

    public void prepare(ArrayList<int[]> cans, int error, int[] preview) {
        cCans = cans;
        cError = error;
        cPreview = preview;

        canText = (TextView) activity.findViewById(R.id.checkInfoTextView);
        canImage = (ImageView) activity.findViewById(R.id.trashCanImageView);

        prevCanBlack = (TextView) activity.findViewById(R.id.prevCanBlackText);
        prevCanBlue = (TextView) activity.findViewById(R.id.prevCanBlueText);
        prevCanGreen = (TextView) activity.findViewById(R.id.prevCanGreenText);
        prevCanYellow = (TextView) activity.findViewById(R.id.prevCanYellowText);
    }

    public void update() {

        updateLocationInfo();
        updateTrashMainDisplay();
        updatePreviewCans();
    }

    private void updateLocationInfo() {
        TextView genInfo = (TextView) activity.findViewById(R.id.curLocationTextView);
        String location = pref.getString(SettingsActivity.KEY_PREF_LOCATION, "");

        if (location.equals(SettingsActivity.CITY_WITH_STREETS)) {
            location += ", " + pref.getString(SettingsActivity.KEY_PREF_LOCATION_STREET, "");
        }

        genInfo.setText(location);
    }

    private void updatePreviewCans() {
        prevCanBlack.setText(   cPreview[0] == -1 ? "" : "" + cPreview[0]);
        prevCanBlue.setText(    cPreview[1] == -1 ? "" : "" + cPreview[1]);
        prevCanGreen.setText(   cPreview[2] == -1 ? "" : "" + cPreview[2]);
        prevCanYellow.setText(cPreview[3] == -1 ? "" : "" + cPreview[3]);


    }

    private void updateTrashMainDisplay() {
        if (cError != -1) {
            updateCanImage(new int[]{R.drawable.can_none_scale});
            canText.setText(activity.getString(cError));

        } else {
            String rawSentence = activity.getResources().getQuantityString(R.plurals.check_can_sentence, cCans.size());

            if (cCans.size() == 1) {
                updateCanImage(new int[]{cCans.get(0)[1]});
                canText.setText( String.format(rawSentence, activity.getString(cCans.get(0)[0])) );

            } else {
                String sCans = "";
                int[] resourceIds = new int[cCans.size()];

                for (int i = 0; i < cCans.size(); i++) {
                    resourceIds[i] = cCans.get(i)[1];

                    sCans += activity.getString(cCans.get(i)[0]);

                    if (i == cCans.size() - 2) {
                        sCans += " und ";
                    } else if (i != cCans.size() - 1) {
                        sCans += ", ";
                    }
                }

                updateCanImage(resourceIds);
                canText.setText( String.format(rawSentence, sCans) );
            }
        }
    }

    private void updateCanImage(int[] resourceIds) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size      = new Point();
        display.getSize(size);

        int margin  = 50;
        int padding = (int) activity.getResources().getDimension(R.dimen.activity_horizontal_margin);
        int width   = size.x - (margin + padding) * 2;
        int height  = width;
        int section = width / resourceIds.length;

        Bitmap drawing  = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas   = new Canvas(drawing);

        for (int i = 0; i < resourceIds.length; i++) {
            int offset = i * section;
            Drawable can            = ContextCompat.getDrawable(activity, resourceIds[i]);
            Bitmap wholeBitmap      = drawableToBitmap(can, width, height);
            Bitmap sectionBitmap    = Bitmap.createBitmap(wholeBitmap, offset, 0, section, height);
            canvas.drawBitmap( sectionBitmap, offset, 0, null );
        }

        canImage.setImageBitmap( drawing );
    }

    private Bitmap drawableToBitmap(Drawable drawable, int w, int h) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
