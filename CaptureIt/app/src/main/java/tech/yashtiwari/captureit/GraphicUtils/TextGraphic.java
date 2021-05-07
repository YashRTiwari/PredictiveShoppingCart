package tech.yashtiwari.captureit.GraphicUtils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.google.firebase.ml.vision.text.FirebaseVisionText;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class TextGraphic extends GraphicOverlay.Graphic {

    private static final String TAG = "TextGraphic";
    private static final int TEXT_COLOR_75 = Color.RED;
    private static final int TEXT_COLOR_85 = Color.BLUE;
    private static final int TEXT_COLOR_100 = Color.GREEN;
    private static final int TEXT_COLOR = Color.GRAY;

    private static final float TEXT_SIZE = 20.0f;
    private static final float STROKE_WIDTH = 1.0f;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final FirebaseVisionText.Element element;

    public TextGraphic(GraphicOverlay overlay, FirebaseVisionText.Element element) {
        super(overlay);

        this.element = element;

        rectPaint = new Paint();

        if (element.getConfidence() < 75)
            rectPaint.setColor(TEXT_COLOR_75);
        else if (element.getConfidence() > 75 && element.getConfidence() < 85)
            rectPaint.setColor(TEXT_COLOR_85);
        else rectPaint.setColor(TEXT_COLOR_100);


        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        if (element == null) {
            throw new IllegalStateException("Attempting to draw a null text.");
        }

        // Draws the bounding box around the TextBlock.
        RectF rect = new RectF(element.getBoundingBox());
        canvas.drawRect(rect, rectPaint);

        // Renders the text at the bottom of the box.
        canvas.drawText(element.getText(), rect.left, rect.bottom, textPaint);
    }
}
