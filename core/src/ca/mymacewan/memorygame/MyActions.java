package ca.mymacewan.memorygame;

import com.badlogic.gdx.scenes.scene2d.Action;

public class MyActions {
    public static Action flipOut(final float duration) {
        return new Action() {
            float left = duration;

            @Override
            public boolean act(float delta) {
                left -= delta;
                if (left <= 0) {
                    actor.setScaleX(0);
                    return true;
                }
                actor.setScaleX(left / duration);
                return false;
            }
        };
    }

    public static Action flipIn(final float duration) {
        return new Action() {
            float done = 0;

            @Override
            public boolean act(float delta) {
                done += delta;
                if (done >= duration) {
                    actor.setScaleX(1);
                    return true;
                }
                actor.setScaleX(done / duration);
                return false;
            }
        };
    }
}
