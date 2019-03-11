package ca.mymacewan.memorygame;

import aurelienribon.tweenengine.TweenAccessor;

public class BoxAccessor implements TweenAccessor<Box> {

    public static final int SCALE_X = 1;

    @Override
    public int getValues(Box target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case SCALE_X:
                returnValues[0] = target.getScaleX();
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

            default:
                assert false;
        }
    }
}
