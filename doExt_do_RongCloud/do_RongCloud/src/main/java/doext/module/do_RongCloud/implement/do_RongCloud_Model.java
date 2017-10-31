package doext.module.do_RongCloud.implement;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Map;

import core.DoServiceContainer;
import core.helper.DoIOHelper;
import core.helper.DoJsonHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.module.do_RongCloud.R;
import doext.module.do_RongCloud.define.do_RongCloud_IMethod;
import doext.module.do_RongCloud.receiver.NotificationClickReceiver;
import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;
import io.rong.push.RongPushClient;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_RongCloud_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象；
 * 获取DoInvokeResult对象方式new DoInvokeResult(this.getUniqueKey());
 */
public class do_RongCloud_Model extends DoSingletonModule implements do_RongCloud_IMethod {

    private final static String TAG = "rongCloud";
    Context ctx;
    SharedPreferences.Editor editor;

    public do_RongCloud_Model() throws Exception {
        super();
        ctx = DoServiceContainer.getPageViewFactory().getAppContext();

        RongIM.setConnectionStatusListener(new MyConnectionStatusListener());
        editor = ctx.getSharedPreferences("do_RongCloud_Config", Context.MODE_PRIVATE).edit();
        RongIM.setOnReceiveMessageListener(new myReceiveMessageListener());
        RongIM.setLocationProvider(new RongIM.LocationProvider() {
            @Override
            public void onStartLocation(Context context, LocationCallback locationCallback) {

            }
        });

//        RongIM.setUserInfoProvider(new RongIM.UserInfoProvider() {
//            @Override
//            public UserInfo getUserInfo(String s) {
//                return  findUserById(userId);//根据 userId 去你的用户系统里查询对应的用户信息返回给融云 SDK。
//            }
//        });
    }

    class myConversationBehaviorListener implements RongIM.ConversationBehaviorListener {

        @Override
        public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
            try {
                fireUserPortraitClickEvent(conversationType.getName(), userInfo.getUserId(), userInfo.getName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
            return false;
        }

        @Override
        public boolean onMessageClick(Context context, View view, Message message) {
            return false;
        }

        @Override
        public boolean onMessageLinkClick(Context context, String s) {
            return false;
        }

        @Override
        public boolean onMessageLongClick(Context context, View view, Message message) {
            return false;
        }
    }

    String content = ""; //本地通知要显示的内容

    class myReceiveMessageListener implements RongIMClient.OnReceiveMessageListener {

        @Override
        public boolean onReceived(Message message, int i) {
            try {
                content = fireReceiveEvent(message);
                if (!isAppForegroundRunning(ctx, ctx.getPackageName())) {
                    sendLocalNotification(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    private void sendLocalNotification(Message message) throws JSONException {
        //1、获得通知管理器
        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        //创建Builder,设置属性
        Intent clickIntent = new Intent(ctx, NotificationClickReceiver.class); //点击通知之后要发送的广播
        String result = getNotificationMessage(message);
        clickIntent.putExtra("rong_notification", result);
        //clickIntent.setAction("doext.module.do_RongCloud.intent.MESSAGE_CLICKED");
        PendingIntent contentIntent = PendingIntent.getBroadcast(ctx, 1, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(ctx)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setContentTitle("message")
                .setContentText(content)
                .setSmallIcon(R.drawable.rc_cs_admin)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
        //获得Notification对象
        Notification notification = builder.getNotification();
        notification.defaults |= Notification.DEFAULT_SOUND;
        //显示通知
        manager.notify(1, notification);

    }

    private String getNotificationMessage(Message message) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONObject rcobject = new JSONObject();
        rcobject.put("cType", message.getConversationType().getName());
        rcobject.put("fId", message.getSenderUserId());
        rcobject.put("oName", message.getObjectName());
        rcobject.put("tId", message.getTargetId());
        jsonObject.put("aps", content);
        jsonObject.put("rc", rcobject);
        jsonObject.put("appData", message.getExtra());
        return jsonObject.toString();
    }

    // 判断应用是否在前台运行
    @SuppressWarnings("deprecation")
    private boolean isAppForegroundRunning(Context context, String appPackageName) {
        boolean isForeground = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (currentPackageName != null && currentPackageName.equals(appPackageName)) {// 程序运行在前台
            isForeground = true;
        }
        return isForeground;
    }

    private String fireReceiveEvent(Message message) throws JSONException {
        DoInvokeResult _invokeResult = new DoInvokeResult(this.getUniqueKey());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("conversationType", message.getConversationType().getName());
        jsonObject.put("messageId", message.getMessageId());
        jsonObject.put("fromUserId", message.getSenderUserId());
        jsonObject.put("sendTime", message.getSentTime());
        jsonObject.put("receiveTime", message.getReceivedTime());
        jsonObject.put("messageContent", getResult(message.getContent(), "messageContent"));
        jsonObject.put("messageType", getResult(message.getContent(), "messageType"));
        _invokeResult.setResultNode(jsonObject);
        this.getEventCenter().fireEvent("message", _invokeResult);
        return jsonObject.getString("messageContent");
    }

    private void fireUserPortraitClickEvent(String conversationType, String userId, String userName) throws JSONException {
        DoInvokeResult _invokeResult = new DoInvokeResult(this.getUniqueKey());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("conversationType", conversationType);
        jsonObject.put("userId", userId);
        jsonObject.put("userName", userName);
        _invokeResult.setResultNode(jsonObject);
        this.getEventCenter().fireEvent("userPortraitClick", _invokeResult);
    }


    private class MyConnectionStatusListener implements RongIMClient.ConnectionStatusListener {
        @Override
        public void onChanged(ConnectionStatus connectionStatus) {

            switch (connectionStatus) {

                case CONNECTED://连接成功。
                    Log.i("onChanged", "连接成功");
                    break;
                case DISCONNECTED://断开连接。
                    Log.i("onChanged", "断开连接");
                    break;
                case CONNECTING://连接中。
                    Log.i("onChanged", "连接中");
                    break;
                case NETWORK_UNAVAILABLE://网络不可用。
                    Log.i("onChanged", "网络不可用。");
                    break;
                case KICKED_OFFLINE_BY_OTHER_CLIENT://用户账户在其他设备登录，本机会被踢掉线
                    Log.i("onChanged", "用户账户在其他设备登录，本机会被踢掉线。");
                    break;
            }
        }
    }


    /**
     * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
     *
     * @_methodName 方法名称
     * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
     * @_scriptEngine 当前Page JS上下文环境对象
     * @_invokeResult 用于返回方法结果对象
     */
    @Override
    public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas,
                                    DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult)
            throws Exception {
        if ("setTitleBarColor".equals(_methodName)) {
            setTitleBarColor(_dictParas, _scriptEngine, _invokeResult);
            return true;
        } else if ("setTitleColor".equals(_methodName)) {
            setTitleColor(_dictParas, _scriptEngine, _invokeResult);
            return true;
        } else if ("logout".equals(_methodName)) {
            logout(_dictParas, _scriptEngine, _invokeResult);
            return true;
        } else if ("getLatestMessage".equals(_methodName)) {
            getLatestMessage(_dictParas, _scriptEngine, _invokeResult);
            return true;
        } else if ("cacheUserInfo".equals(_methodName)) {
            cacheUserInfo(_dictParas, _scriptEngine, _invokeResult);
            return true;
        } else if ("cacheGroupInfo".equals(_methodName)) {
            cacheGroupInfo(_dictParas, _scriptEngine, _invokeResult);
            return true;
        }
        return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
    }

    /**
     * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用，
     * 可以根据_methodName调用相应的接口实现方法；
     *
     * @_methodName 方法名称
     * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
     * @_scriptEngine 当前page JS上下文环境
     * @_callbackFuncName 回调函数名
     * #如何执行异步方法回调？可以通过如下方法：
     * _scriptEngine.callback(_callbackFuncName, _invokeResult);
     * 参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
     * 获取DoInvokeResult对象方式new DoInvokeResult(this.getUniqueKey());
     */
    @Override
    public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas,
                                     DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
        if ("login".equals(_methodName)) { // 登录设备
            this.login(_dictParas, _scriptEngine, _callbackFuncName);
            return true;
        }
        if ("openConversation".equals(_methodName)) { // 登录设备
            this.openConversation(_dictParas, _scriptEngine, _callbackFuncName);
            return true;
        }
        if ("openConversationList".equals(_methodName)) { // 登录设备
            this.openConversationList(_dictParas, _scriptEngine, _callbackFuncName);
            return true;
        }
        if ("setUserInfo".equals(_methodName)) { // 登录设备
            this.setUserInfo(_dictParas, _scriptEngine, _callbackFuncName);
            return true;
        }
        if ("openGroupConversation".equals(_methodName)) { // 登录设备
            this.openGroupConversation(_dictParas, _scriptEngine, _callbackFuncName);
            return true;
        }
        if ("sendTextMessage".equals(_methodName)) { // 登录设备
            this.sendTextMessage(_dictParas, _scriptEngine, _callbackFuncName);
            return true;
        }
        return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
    }


    @Override
    public void setTitleBarColor(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
        String _color = DoJsonHelper.getString(_dictParas, "color", "");
        editor.putString("titleBarColor", _color);
        editor.commit();
    }

    @Override
    public void setTitleColor(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
        String _color = DoJsonHelper.getString(_dictParas, "color", "");
        editor.putString("titleColor", _color);
        editor.commit();
    }

    /**
     * 用户登录；
     *
     * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
     * @_scriptEngine 当前Page JS上下文环境对象
     * @_callbackFuncName 回调函数名
     */
    @Override
    public void login(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws JSONException {
        String _token = DoJsonHelper.getString(_dictParas, "token", "");
        String _appKey = DoJsonHelper.getString(_dictParas, "appKey", "");
        JSONObject _extraData = DoJsonHelper.getJSONObject(_dictParas, "extraData");
        init(_appKey, _extraData);
        editor.putString("loginToken", _token);
        editor.putString("loginAppkey", _appKey);
        editor.commit();
        connect(_token, _scriptEngine, _callbackFuncName);
    }

    private void init(String appKey, JSONObject extraData) throws JSONException {

        if (ctx.getApplicationInfo().packageName.equals(getCurProcessName(ctx.getApplicationContext())) ||
                "io.rong.push".equals(getCurProcessName(ctx.getApplicationContext()))) {
            /**
             * IMKit SDK调用第一步 初始化
             */
            if (extraData != null) {
                //注册小米推送
                JSONObject xiaomi = DoJsonHelper.getJSONObject(extraData, "xiaomi");
                if (xiaomi != null) {
                    String _appId = DoJsonHelper.getString(xiaomi, "appId", "");
                    String _appKey = DoJsonHelper.getString(xiaomi, "appKey", "");
                    //RongPushClient.registerMiPush(ctx, "2882303761517569481", "5191756970481");
                    RongPushClient.registerMiPush(ctx, _appId, _appKey);
                }
            }
            RongIM.init(ctx, appKey);
            RongIM.setConversationBehaviorListener(new myConversationBehaviorListener());
        }
    }

    private void connect(String token, final DoIScriptEngine scriptEngine, final String callbackFuncName) {
        if (ctx.getApplicationInfo().packageName.equals(getCurProcessName(ctx.getApplicationContext()))) {
            RongIM.connect(token, new RongIMClient.ConnectCallback() {
                /**
                 * Token 错误。可以从下面两点检查 1.  Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
                 * 2.  token 对应的 appKey 和工程里设置的 appKey 是否一致
                 */
                String tokenIncorrect = "Token 错误,可以从下面两点检查 1.Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token. 2. token 对应的 appKey 和工程里设置的 appKey 是否一致";

                @Override
                public void onTokenIncorrect() {
                    callBack(tokenIncorrect, scriptEngine, callbackFuncName);
                }

                /**
                 * 连接融云成功
                 *
                 * @param userid 当前 token 对应的用户 id
                 */
                @Override
                public void onSuccess(String userid) {
                    callBack(userid, scriptEngine, callbackFuncName);
                }

                /**
                 * 连接融云失败
                 *
                 * @param errorCode 错误码，可到官网 查看错误码对应的注释
                 */
                @Override
                public void onError(final RongIMClient.ErrorCode errorCode) {
                    getMessage(errorCode);
                    callBack(error, scriptEngine, callbackFuncName);
                }

                String error = "";

                private void getMessage(RongIMClient.ErrorCode errorCode) {
                    switch (errorCode.toString()) {
                        case "RC_CONN_OVERFREQUENCY":
                            error = "连接已存在，请勿重复登录";
                            break;
                        case "RC_CONN_ID_REJECT":
                            error = "appKey错误";
                            break;
                        case "RC_CONN_NOT_AUTHRORIZED":
                            error = "appKey与token不匹配";
                            break;
                        case "RC_CONN_APP_BLOCKED_OR_DELETED:":
                            error = "appKey被封禁或已经删除";
                            break;
                        case "RC_CONN_USER_BLOCKED:":
                            error = "用户被封禁";
                            break;
                        case "RC_DISCONN_KICK:":
                            error = "当前用户在其他设备已经登录,此设备被踢下线";
                            break;
                        case "PARAMETER_ERROR:":
                            error = "内部错误，请联系deviceone融云组件开发人员";
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }

    private void callBack(String message, DoIScriptEngine scriptEngine, final String callbackFuncName) {
        DoInvokeResult _invokeResult = new DoInvokeResult(this.getUniqueKey());
        if (!TextUtils.isEmpty(message))
            _invokeResult.setResultText(message);
        scriptEngine.callback(callbackFuncName, _invokeResult);
    }

    /**
     * 打开单个会话；
     *
     * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
     * @_scriptEngine 当前Page JS上下文环境对象
     * @_callbackFuncName 回调函数名
     */
    @Override
    public void openConversation(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
        String _userId = DoJsonHelper.getString(_dictParas, "userId", "");
        String _title = DoJsonHelper.getString(_dictParas, "title", "");
        String _headPortrait = DoJsonHelper.getString(_dictParas, "headPortrait", "");
        Uri _uri = getUrl(_headPortrait, _scriptEngine);
        UserInfo userInfo = new UserInfo(_userId, _title, _uri);
        RongIM.getInstance().refreshUserInfoCache(userInfo);
        RongIM.getInstance().startPrivateChat(ctx, _userId, _title);
        callBack(null, _scriptEngine, _callbackFuncName);
    }

    private Uri getUrl(String _headPortrait, DoIScriptEngine _scriptEngine) throws Exception {
        if (null != DoIOHelper.getHttpUrlPath(_headPortrait)) {
            return Uri.parse(_headPortrait);
        } else {
            String path = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentApp(), _headPortrait);
            return Uri.fromFile(new File(path));
        }
    }

    @Override
    public void openConversationList(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
        Map<String, Boolean> supportedConversation = new ArrayMap<>();
        supportedConversation.put(Conversation.ConversationType.NONE.getName(), false);
        supportedConversation.put("flag", true);
        RongIM.getInstance().startConversationList(ctx, supportedConversation);
        callBack(null, _scriptEngine, _callbackFuncName);
    }

    /**
     * 设置用户信息；
     *
     * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
     * @_scriptEngine 当前Page JS上下文环境对象
     * @_callbackFuncName 回调函数名
     */
    @Override
    public void setUserInfo(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws JSONException {
        String _nickName = DoJsonHelper.getString(_dictParas, "nickName", "");
        String _headPortrait = DoJsonHelper.getString(_dictParas, "headPortrait", "");
        DoInvokeResult _invokeResult = new DoInvokeResult(this.getUniqueKey());
        try {
            Uri _uri = getUrl(_headPortrait, _scriptEngine);
            UserInfo userInfo = new UserInfo(RongIM.getInstance().getCurrentUserId(), _nickName, _uri);
            RongIM.getInstance().setCurrentUserInfo(userInfo);
            RongIM.getInstance().setMessageAttachedUserInfo(true);
            //RongIM.getInstance().refreshUserInfoCache(userInfo);
            _invokeResult.setResultBoolean(true);
            _scriptEngine.callback(_callbackFuncName, _invokeResult);
        } catch (Exception ex) {
            _invokeResult.setResultBoolean(false);
            _scriptEngine.callback(_callbackFuncName, _invokeResult);
            DoServiceContainer.getLogEngine().writeError("do_RongCloud setUserInfo", ex);
        }
    }

    @Override
    public void openGroupConversation(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
        String _groupId = DoJsonHelper.getString(_dictParas, "groupId", "");
        String _title = DoJsonHelper.getString(_dictParas, "title", "");
        RongIM.getInstance().startGroupChat(ctx, _groupId, _title);
        callBack(null, _scriptEngine, _callbackFuncName);
    }

    @Override
    public void logout(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
        RongIM.getInstance().logout();
    }

    @Override
    public void getLatestMessage(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
        List<Conversation> conversationList = RongIM.getInstance().getConversationList();
        JSONArray jsonArray = new JSONArray();
        if (conversationList != null) {
            for (Conversation item : conversationList) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", item.getTargetId());
                jsonObject.put("message", getResult(item.getLatestMessage(), "message"));
                jsonObject.put("receivedTime", item.getReceivedTime());
                jsonObject.put("sentTime", item.getSentTime());
                //判断最后一条消息是否是当前用户发出的 如果是 则返回isSend 不是的话 则视为接收消息 不返回isSend
                if (RongIM.getInstance().getCurrentUserId().equals(item.getSenderUserId())) {
                    if (item.getSentStatus().name().equalsIgnoreCase("SENT"))
                        jsonObject.put("isSend", true);
                    else
                        jsonObject.put("isSend", false);
                }
                jsonArray.put(jsonObject);
            }
        }
        _invokeResult.setResultArray(jsonArray);
    }

    @Override
    public void sendTextMessage(JSONObject _dictParas, final DoIScriptEngine _scriptEngine, final String _callbackFuncName) throws Exception {
        String _text = DoJsonHelper.getString(_dictParas, "text", "");
        String _targetId = DoJsonHelper.getString(_dictParas, "targetId", "");
        String _conversationType = DoJsonHelper.getString(_dictParas, "conversationType", "");
        String _pushContent = DoJsonHelper.getString(_dictParas, "pushContent", "");

        try {
            TextMessage myTextMessage = TextMessage.obtain(_text);
            Conversation.ConversationType conversationType;
            if (_conversationType == "group") {
                conversationType = Conversation.ConversationType.GROUP;
            } else {
                conversationType = Conversation.ConversationType.PRIVATE;
            }
            Message myMessage = Message.obtain(_targetId, conversationType, myTextMessage);
            RongIM.getInstance().sendMessage(myMessage, _pushContent.length() > 0 ? _pushContent : null, null, new IRongCallback.ISendMessageCallback() {
                @Override
                public void onAttached(Message message) {
                    //消息本地数据库存储成功的回调
                }

                @Override
                public void onSuccess(Message message) {
                    sendMessagecallBack(true, _scriptEngine, _callbackFuncName);
                }

                @Override
                public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                    sendMessagecallBack(false, _scriptEngine, _callbackFuncName);
                }
            });
        } catch (Exception ex) {
            sendMessagecallBack(false, _scriptEngine, _callbackFuncName);
        }
    }

    @Override
    public void cacheUserInfo(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
        String _userId = DoJsonHelper.getString(_dictParas, "userId", "");
        String _nickName = DoJsonHelper.getString(_dictParas, "nickName", "");
        String _headPortrait = DoJsonHelper.getString(_dictParas, "headPortrait", "");
        try {
            Uri _uri = getUrl(_headPortrait, _scriptEngine);
            UserInfo userInfo = new UserInfo(_userId, _nickName, _uri);
            RongIM.getInstance().refreshUserInfoCache(userInfo);
            _invokeResult.setResultBoolean(true);
        } catch (Exception ex) {
            _invokeResult.setResultBoolean(false);
            DoServiceContainer.getLogEngine().writeError("do_RongCloud cacheUserInfo", ex);
        }
    }

    @Override
    public void cacheGroupInfo(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
        String _groupId = DoJsonHelper.getString(_dictParas, "groupId", "");
        String _groupName = DoJsonHelper.getString(_dictParas, "groupName", "");
        String _headPortrait = DoJsonHelper.getString(_dictParas, "headPortrait", "");
        try {
            Uri _uri = getUrl(_headPortrait, _scriptEngine);
            Group group = new Group(_groupId, _groupName, _uri);
            RongIM.getInstance().refreshGroupInfoCache(group);
            _invokeResult.setResultBoolean(true);
        } catch (Exception ex) {
            _invokeResult.setResultBoolean(false);
            DoServiceContainer.getLogEngine().writeError("do_RongCloud cacheGroupInfo", ex);
        }
    }

    private void sendMessagecallBack(boolean result, DoIScriptEngine scriptEngine, final String callbackFuncName) {
        DoInvokeResult _invokeResult = new DoInvokeResult(this.getUniqueKey());
        _invokeResult.setResultBoolean(result);
        scriptEngine.callback(callbackFuncName, _invokeResult);
    }

    private String getResult(MessageContent messageContent, String result) throws JSONException {
        if (messageContent instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) messageContent;
            return result == "messageType" ? "text" : textMessage.getContent();
        } else if (messageContent instanceof LocationMessage) {
            return result == "messageType" ? "location" : "[位置]";
        } else if (messageContent instanceof ImageMessage) {
            return result == "messageType" ? "image" : "[图片]";
        } else if (messageContent instanceof FileMessage) {
            return result == "messageType" ? "file" : "[文件]";
        } else if (messageContent instanceof VoiceMessage) {
            return result == "messageType" ? "voice" : "[语音]";
        } else {
            return result == "messageType" ? "other" : "[其他]";
        }
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}