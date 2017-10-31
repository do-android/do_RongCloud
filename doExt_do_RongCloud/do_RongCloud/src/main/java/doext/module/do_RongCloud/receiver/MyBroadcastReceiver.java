package doext.module.do_RongCloud.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import core.helper.DoJsonHelper;
import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;


/**
 * Created by feng_ on 2017/4/20.
 */

public class MyBroadcastReceiver extends PushMessageReceiver {

    @Override
    public boolean onNotificationMessageArrived(Context context, PushNotificationMessage pushNotificationMessage) {
        return false;
    }

    @Override
    public boolean onNotificationMessageClicked(Context context, PushNotificationMessage pushNotificationMessage) {
        Intent _intent = new Intent();
        _intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _intent.putExtra("__TYPE", "notification");
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject rcobject = new JSONObject();
            rcobject.put("cType", pushNotificationMessage.getConversationType().getName());
            rcobject.put("fId", pushNotificationMessage.getSenderId());
            rcobject.put("oName", pushNotificationMessage.getObjectName());
            rcobject.put("tId", pushNotificationMessage.getTargetId());
            jsonObject.put("aps", pushNotificationMessage.getPushContent());
            jsonObject.put("rc", rcobject);
            jsonObject.put("appData", pushNotificationMessage.getPushData());
            if (jsonObject != null) {
                Iterator<String> _keys = jsonObject.keys();
                while (_keys.hasNext()) {
                    String _key = _keys.next();
                    String _value = jsonObject.getString(_key);
                    _intent.putExtra(_key, _value);
                }
            }

            // 通过包名启动其他应用
            PackageInfo _pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Intent _resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            _resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            _resolveIntent.setPackage(_pi.packageName);
            List<ResolveInfo> _apps = context.getPackageManager().queryIntentActivities(_resolveIntent, 0);
            ResolveInfo _ri = _apps.iterator().next();
            if (_ri != null) {
                ComponentName _cn = new ComponentName(_ri.activityInfo.packageName, _ri.activityInfo.name);
                _intent.setAction(Intent.ACTION_MAIN);
                _intent.addCategory(Intent.CATEGORY_LAUNCHER);
                _intent.setComponent(_cn);
                context.startActivity(_intent);
            }
        } catch (Exception _ex) {
            _ex.printStackTrace();
        }
        return true;
    }
}
