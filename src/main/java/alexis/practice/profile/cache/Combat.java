package alexis.practice.profile.cache;

import alexis.practice.profile.Profile;
import lombok.Getter;

import javax.annotation.Nullable;

@Getter
public final class Combat {
    private long time = 0;
    private Profile target;

    @Nullable
    public Profile get() {
        if (time < System.currentTimeMillis()) clear();

        return target;
    }

    public void set(Profile target) {
        this.target = target;
        time = System.currentTimeMillis() + 15 * 1000L;
    }

    public void clear() {
        target = null;
        time = 0;
    }
}