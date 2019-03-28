package ca.mymacewan.memorygame;

import aurelienribon.tweenengine.TweenAccessor;

public class BoxAccessor implements TweenAccessor<Box> {

    public static final int SCALE_X = 1;
    public static final int SCALE_Y = 2;
    public static final int ALPHA = 3;


    @Override
    public int getValues(Box target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case SCALE_X:
                returnValues[0] = target.getScaleX();
                return 1;
            case SCALE_Y:
                returnValues[0] = target.getScaleX();
                return 1;
            case ALPHA:
                returnValues[0] = target.getAlpha();
                return 1;
            default:
                assert false;
                return -1;
        }
    }

    @Override
    public void setValues(Box target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case SCALE_X:
                target.setScaleX(newValues[0]);
                break;
            case SCALE_Y:
                target.setScaleX(newValues[0]);
                break;
            case ALPHA:
                target.setAlpha(newValues[0]);
                break;
            default:
                assert false;
        }
    }
}
