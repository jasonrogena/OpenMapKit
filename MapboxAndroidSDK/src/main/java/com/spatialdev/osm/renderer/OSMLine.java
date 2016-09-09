package com.spatialdev.osm.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.OSMColorConfig;
import com.spatialdev.osm.model.OSMWay;

/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */
public class OSMLine extends OSMPath {

    // OSM GREEN
    public static final int DEFAULT_A = 125;
    public static final int DEFAULT_R = 126;
    public static final int DEFAULT_G = 188;
    public static final int DEFAULT_B = 111;
    public static final float DEFAULT_WIDTH = 13.0f;
    
    // GOLD
    public static final int DEFAULT_SELECTED_A = 180;
    public static final int DEFAULT_SELECTED_R = 255;
    public static final int DEFAULT_SELECTED_G = 140;
    public static final int DEFAULT_SELECTED_B = 0;
    public static final float DEFAULT_SELECTED_WIDTH = 15.0f;

    // MAROON
    public static final int DEFAULT_EDITED_A = 100;
    public static final int DEFAULT_EDITED_R = 245;
    public static final int DEFAULT_EDITED_G = 17;
    public static final int DEFAULT_EDITED_B = 135;
    
    private float width;
    private int a;
    private int r;
    private int g;
    private int b;
    
    /**
     * This should only be constructed by
     * OSMPath.createOSMPath
     * * * *
     * @param w
     */
    protected OSMLine(OSMWay w, MapView mv) {
        super(w, mv);

        // color polygon according to if it has been edited before
        if (w.isModified()) {
            this.a = DEFAULT_EDITED_A;
            this.r = DEFAULT_EDITED_R;
            this.g = DEFAULT_EDITED_G;
            this.b = DEFAULT_EDITED_B;
        } else {
            this.a = DEFAULT_A;
            this.r = DEFAULT_R;
            this.g = DEFAULT_G;
            this.b = DEFAULT_B;
        }
        width = DEFAULT_WIDTH;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        setStrokeWidth(width);
    }

    @Override
    public void select() {
    }

    @Override
    public void deselect() {
    }

    /**
     * ^_^ !!! NICK'S DANGLING VERTEX LINE CLIPPER !!! ^_^
     * 
     * For lines, we only want to draw paths if they are inside of the viewport.
     * So, if the path is inside the viewport, we should either start the drawing
     * point (moveTo), or connect the drawing point (lineTo).
     * 
     * If it is outside of the viewport, we should do nothing but tell the canvas
     * that it should start a new line (moveTo) next time it reenters the viewport.
     * 
     * Unless the next vertex is in the viewport. Then we DO want to draw it.
     * 
     * If I had more time, I'd do the trig to get you that vertex right on the viewport's
     * edge.
     *  
     * * * * * * * * * * * * * * *
     * @param path
     * @param projectedPoint
     * @param screenPoint
     */
    @Override
    void clipOrDrawPath(Path path, double[] projectedPoint, double[] nextProjectedPoint, double[] screenPoint) {

        int projX = (int) projectedPoint[0];
        int projY = (int) projectedPoint[1];
        
        if (viewPortBounds.contains(projX, projY)) {
            if (pathLineToReady) {
                path.lineTo( (float) screenPoint[0], (float) screenPoint[1] );
            } else {
                path.moveTo( (float) screenPoint[0], (float) screenPoint[1] );
                pathLineToReady = true;
            }
        } else {
            
            // last vertex was in the viewport, we want to make this last one dangle
            if (pathLineToReady) {
                path.lineTo( (float) screenPoint[0], (float) screenPoint[1] );
                // If we're going back in, we want to move to the vertex...
                pathLineToReady = false;
                return;
            }
            
            /**
             * If the next vertex is in the viewport, we want this vertex to be drawn.
             * Think of this as allowing the drawing of 1 dangling vertex outside of
             * the viewport. I'd be fine with drawing everything, but if it's too far out,
             * the android canvas seg faults. (Wishing i could be closer to the metal...)
             */
            if (nextProjectedPoint != null) {
                int projXNext = (int) nextProjectedPoint[0];
                int projYNext = (int) nextProjectedPoint[1];
                if (viewPortBounds.contains(projXNext, projYNext)) {
                    path.moveTo( (float) screenPoint[0], (float) screenPoint[1] );
                    pathLineToReady = true;
                }
            }

            /**
             * The only other situation, the point is the last and the one before
             * it was also outside of the viewport, obviously we do nothing...* *
             */
        }
    }

    @Override
    public void draw(Canvas c) {
        //set the color
        OSMColorConfig.ARGB nonEnabled;
        OSMColorConfig.ARGB argb;
        if(osmElement.isSelected()) {
            setStrokeWidth(DEFAULT_SELECTED_WIDTH);
            nonEnabled = new OSMColorConfig.ARGB(DEFAULT_SELECTED_A, DEFAULT_SELECTED_R, DEFAULT_SELECTED_G, DEFAULT_SELECTED_B);
            argb = OSMColorConfig.getFocusInARGB(osmElement, nonEnabled);
        } else {
            setStrokeWidth(width);
            nonEnabled = new OSMColorConfig.ARGB(a, r, g, b);
            argb = OSMColorConfig.getFocusOutARGB(osmElement, nonEnabled);
        }
        paint.setARGB(argb.a, argb.r, argb.g, argb.b);
        super.draw(c);
    }
}
