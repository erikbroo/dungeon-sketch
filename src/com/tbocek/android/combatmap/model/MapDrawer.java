package com.tbocek.android.combatmap.model;

import android.graphics.Canvas;

import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;

public class MapDrawer {
    private boolean mAreTokensManipulable;

    private FogOfWarMode mBackgroundFogOfWar;
    private boolean mDrawAnnotations;
    private boolean mDrawGmNotes;
    private boolean mDrawGridLines;
    private boolean mDrawTokens;
    private FogOfWarMode mGmNoteFogOfWar;

    public MapDrawer areTokensManipulable(boolean val) {
        this.mAreTokensManipulable = val;
        return this;
    }

    public MapDrawer backgroundFogOfWar(FogOfWarMode val) {
        this.mBackgroundFogOfWar = val;
        return this;
    }

    public void draw(Canvas canvas, MapData m) {
        m.getGrid().drawBackground(canvas);

        canvas.save();
        m.getWorldSpaceTransformer().setMatrix(canvas);
        if (this.mBackgroundFogOfWar == FogOfWarMode.CLIP
                && !m.getBackgroundFogOfWar().isEmpty()) {
            m.getBackgroundFogOfWar().clipFogOfWar(canvas);
        }
        m.getBackgroundLines().drawAllLinesBelowGrid(canvas);
        m.getBackgroundImages().draw(canvas, m.getWorldSpaceTransformer());
        canvas.restore();

        if (this.mDrawGridLines) {
            m.getGrid().draw(canvas, m.getWorldSpaceTransformer());
        }

        canvas.save();
        m.getWorldSpaceTransformer().setMatrix(canvas);
        if (this.mBackgroundFogOfWar == FogOfWarMode.CLIP
                && !m.getBackgroundFogOfWar().isEmpty()) {
            m.getBackgroundFogOfWar().clipFogOfWar(canvas);
        }
        m.getBackgroundLines().drawAllLinesAboveGrid(canvas);
        if (this.mBackgroundFogOfWar == FogOfWarMode.DRAW) {
            m.getBackgroundFogOfWar().drawFogOfWar(canvas);
        }
        canvas.restore();

        canvas.save();
        m.getWorldSpaceTransformer().setMatrix(canvas);

        if (this.mDrawGmNotes) {
            canvas.save();
            if (this.mGmNoteFogOfWar == FogOfWarMode.CLIP) {
                m.getGmNotesFogOfWar().clipFogOfWar(canvas);
            }
            m.getGmNoteLines().drawAllLines(canvas);
            if (this.mGmNoteFogOfWar == FogOfWarMode.CLIP) {
                canvas.restore();
            } else if (this.mGmNoteFogOfWar == FogOfWarMode.DRAW) {
                m.getGmNotesFogOfWar().drawFogOfWar(canvas);
            }
            canvas.restore();
        }

        if (this.mDrawAnnotations) {

            m.getAnnotationLines().drawAllLines(canvas);
        }
        canvas.restore();

        canvas.save();
        if (this.mBackgroundFogOfWar == FogOfWarMode.CLIP
                && !m.getBackgroundFogOfWar().isEmpty()) {
            m.getWorldSpaceTransformer().setMatrix(canvas);
            m.getBackgroundFogOfWar().clipFogOfWar(canvas);
            m.getWorldSpaceTransformer().setInverseMatrix(canvas);
        }
        CoordinateTransformer gridSpace =
                m.getGrid().gridSpaceToScreenSpaceTransformer(
                        m.getWorldSpaceTransformer());
        if (this.mDrawTokens) {
            m.getTokens().drawAllTokens(canvas, gridSpace,
                    m.getGrid().isDark(), this.mAreTokensManipulable);
        }
        canvas.restore();
    }

    public MapDrawer drawAnnotations(boolean val) {
        this.mDrawAnnotations = val;
        return this;
    }

    public MapDrawer drawGmNotes(boolean val) {
        this.mDrawGmNotes = val;
        return this;
    }

    public MapDrawer drawGridLines(boolean val) {
        this.mDrawGridLines = val;
        return this;
    }

    public MapDrawer drawTokens(boolean val) {
        this.mDrawTokens = val;
        return this;
    }

    public MapDrawer gmNotesFogOfWar(FogOfWarMode val) {
        this.mGmNoteFogOfWar = val;
        return this;
    }

    /**
     * Options for what to do with the fog of war.
     * 
     * @author Tim
     * 
     */
    public enum FogOfWarMode {
        /**
         * Use the fog of war to clip the background.
         */
        CLIP,

        /**
         * Draw the fog of war as an overlay.
         */
        DRAW,

        /**
         * Ignore the fog of war.
         */
        NOTHING
    }

}
