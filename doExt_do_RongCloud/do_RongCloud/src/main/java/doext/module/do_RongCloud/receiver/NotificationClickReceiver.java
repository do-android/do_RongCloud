package doext.module.do_RongCloud.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import org.json.JSONObject;

import java.util.List;

import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.interfaces.DoISingletonModuleFactory;
import core.object.DoInvokeResult;
import core.object.DoModule;
import doext.module.do_RongCloud.app.do_RongCloud_App;

/**
 * Created by feng_ on 2017/4/25.
 */
public class NotificationClickReceiver extends BroadcastReceiver {
    String TAG = "NotificationClickReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String result = intent.getStringExtra("rong_notification");
            JSONObject jsonObject = DoJsonHelper.loadDataFromText(result);
            wakeUpApp(context, jsonObject);
            fireMessageClick(jsonObject);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fireMessageClick(JSONObject json) throws Exception {
        String typeId = do_RongCloud_App.getInstance().getTypeID();
        DoISingletonModuleFactory smFactory = DoServiceContainer.getSingletonModuleFactory();
        if (null != smFactory) {
            DoModule module = DoServiceContainer.getSingletonModuleFactory().getSingletonModuleByID(null, typeId);
            DoInvokeResult jsonResult = new DoInvokeResult(module.getUniqueKey());
            jsonResult.setResultNode(json);
            module.getEventCenter().fireEvent("messageClicked", jsonResult);
        } else {
            Log.d(TAG, "-------------------------app is kissed！");
        }
    }

    private void wakeUpApp(Context context, JSONObject json) throws PackageManager.NameNotFoundException {
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(context.getPackageName());
        List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(resolveIntent, 0);
        String pushContent = DoJsonHelper.getText(json, "");
        if (apps.size() != 0) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ResolveInfo ri = apps.iterator().next();
            String packageName = ri.activityInfo.packageName;
            String className = ri.activityInfo.name;
            ComponentName cn = new ComponentName(packageName, className);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.putExtra("pushData", pushContent);
            context.startActivity(intent);
        }
    }
}
