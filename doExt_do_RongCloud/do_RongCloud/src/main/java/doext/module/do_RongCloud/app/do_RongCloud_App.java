package doext.module.do_RongCloud.app;

import android.content.Context;

import core.interfaces.DoIAppDelegate;
import io.rong.imkit.RongIM;

/**
 * APP启动的时候会执行onCreate方法；
 */
public class do_RongCloud_App implements DoIAppDelegate {

    private static do_RongCloud_App instance;

    private do_RongCloud_App() {

    }

    public static do_RongCloud_App getInstance() {
        if (instance == null) {
            instance = new do_RongCloud_App();
        }
        return instance;
    }

    @Override
    public void onCreate(Context context) {

    }

    @Override
    public String getTypeID() {
        return "do_RongCloud";
    }
}
