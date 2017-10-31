package dotest.module.do_RongCloud.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import core.DoServiceContainer;
import doext.module.do_RongCloud.implement.do_RongCloud_Model;
import dotest.module.do_RongCloud.debug.DoService;


/**
 * webview组件测试样例
 */
public class WebViewSampleTestActivty extends DoTestActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initModuleModel() throws Exception {
        this.model = new do_RongCloud_Model();
    }

    @Override
    protected void initUIView() throws Exception {
//		Do_WebView_View view = new Do_WebView_View(this);
//		((DoUIModule) this.model).setCurrentUIModuleView(view);
//		((DoUIModule) this.model).setCurrentPage(currentPage);
//		view.loadView((DoUIModule) this.model);
//		LinearLayout uiview = (LinearLayout) findViewById(R.id.uiview);
//		uiview.addView(view);
    }

    @Override
    public void doTestProperties(View view) {
        Map<String, Object> _paras_loadString = new HashMap<String, Object>();
        _paras_loadString.put("appKey", "bmdehs6pbtpgs");
        _paras_loadString.put("token", "OnMpCpIP7asJybGqwkpD1xCKnBomlked33AJVDeM51ssxNvmX4Q2yBCCu3mUa2DSPdCCpQmsx7otq9BIkKe4Zw==");
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObject1 = new JSONObject();
        try {
            jsonObject1.put("appId", "2882303761517569481");
            jsonObject1.put("appKey", "5191756970481");
            jsonObject.put("xiaomi", jsonObject1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //_paras_loadString.put("extraData", jsonObject);
        DoService.asyncMethod(this.model, "login", _paras_loadString, new DoService.EventCallBack() {
            @Override
            public void eventCallBack(String _data) {// 回调函数
                DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
            }
        });
    }

    @Override
    protected void doTestSyncMethod() {

    }

    @Override
    protected void doTestAsyncMethod() {
//        setTitleBar();
        open();
//        Map<String, String> _paras_loadString = new HashMap<String, String>();
//        _paras_loadString.put("nickName", "www");
//        _paras_loadString.put("headPortrait", "http://oj8so80jf.bkt.clouddn.com/ee.jpg");
//        DoService.asyncMethod(this.model, "setUserInfo", _paras_loadString, new DoService.EventCallBack() {
//            @Override
//            public void eventCallBack(String _data) {// 回调函数
//                DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
//            }
//        });
    }

    private void setTitleBar() {
        Map<String, String> _paras_loadString = new HashMap<String, String>();
        _paras_loadString.put("color", "ff0000");
        DoService.syncMethod(this.model, "setTitleBarColor", _paras_loadString);
    }

    private void open() {
//        Map<String, Object> _paras_loadString = new HashMap<String, Object>();
//        _paras_loadString.put("userId", "qwert");
//        _paras_loadString.put("title", "Liuxin");
//        _paras_loadString.put("headPortrait", "https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=98039108,958778624&fm=173&s=0A4161840E5215D60382478A0300709C&w=300&h=200&img.JPEG");
//
//        DoService.asyncMethod(this.model, "openConversation", _paras_loadString, new DoService.EventCallBack() {
//            @Override
//            public void eventCallBack(String _data) {// 回调函数
//                DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
//            }
//        });
//        Map<String, String> _paras_loadString = new HashMap<String, String>();
//        _paras_loadString.put("groupId", "1122");
//        _paras_loadString.put("title", "eeeeeeeeeee");
//        DoService.asyncMethod(this.model, "openGroupConversation", _paras_loadString, new DoService.EventCallBack() {
//            @Override
//            public void eventCallBack(String _data) {// 回调函数
//                DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
//            }
//        });


        Map<String, String> _paras1 = new HashMap<String, String>();
        _paras1.put("userId", "xiaoming");
        _paras1.put("nickName", "小明");
        _paras1.put("headPortrait", "http://pic8.nipic.com/20100623/5208937_134307859911_2.jpg\n");
        DoService.syncMethod(this.model, "cacheUserInfo", _paras1);

        Map<String, String> _paras2 = new HashMap<String, String>();
        _paras2.put("userId", "xiaozhang");
        _paras2.put("nickName", "小张");
        _paras2.put("headPortrait", "http://pic1.cxtuku.com/00/06/78/b9903ad9ea2b.jpg");
        DoService.syncMethod(this.model, "cacheUserInfo", _paras2);

        Map<String, String> _paras3 = new HashMap<String, String>();
        _paras3.put("userId", "xiaochen");
        _paras3.put("nickName", "小陈");
        _paras3.put("headPortrait", "http://pic43.nipic.com/20140709/19187786_093847310751_2.jpg");
        DoService.syncMethod(this.model, "cacheUserInfo", _paras3);
        //-----------------------

        Map<String, Object> _paras_loadString = new HashMap<String, Object>();
        _paras_loadString.put("text", "QWERTY");
        _paras_loadString.put("targetId", "xiaoming");
        _paras_loadString.put("conversationType", "private");
        _paras_loadString.put("pushContent", "测试测试");
        DoService.asyncMethod(this.model, "sendTextMessage", _paras_loadString, new DoService.EventCallBack() {
            @Override
            public void eventCallBack(String _data) {// 回调函数
                DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
            }
        });

        Map<String, Object> _paras_loadString1 = new HashMap<String, Object>();
        _paras_loadString1.put("text", "wwwwwwwwwwwwwwwwww");
        _paras_loadString1.put("targetId", "xiaozhang");
        _paras_loadString1.put("conversationType", "private");
        DoService.asyncMethod(this.model, "sendTextMessage", _paras_loadString1, new DoService.EventCallBack() {
            @Override
            public void eventCallBack(String _data) {// 回调函数
                DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
            }
        });

        Map<String, Object> _paras_loadString2 = new HashMap<String, Object>();
        _paras_loadString2.put("text", "eeeeeeeeeeeeeeeeeee");
        _paras_loadString2.put("targetId", "xiaochen");
        _paras_loadString2.put("conversationType", "private");
        DoService.asyncMethod(this.model, "sendTextMessage", _paras_loadString2, new DoService.EventCallBack() {
            @Override
            public void eventCallBack(String _data) {// 回调函数
                DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
            }
        });
    }

    @Override
    protected void onEvent() {
        // 系统事件订阅
        DoService.subscribeEvent(this.model, "loaded", new DoService.EventCallBack() {
            @Override
            public void eventCallBack(String _data) {
                DoServiceContainer.getLogEngine().writeDebug("系统事件回调：name = loaded, data = " + _data);
                Toast.makeText(WebViewSampleTestActivty.this, "系统事件回调：loaded", Toast.LENGTH_LONG).show();
            }
        });
        // 自定义事件订阅
        DoService.subscribeEvent(this.model, "_messageName", new DoService.EventCallBack() {
            @Override
            public void eventCallBack(String _data) {
                DoServiceContainer.getLogEngine().writeDebug("自定义事件回调：name = _messageName, data = " + _data);
                Toast.makeText(WebViewSampleTestActivty.this, "自定义事件回调：_messageName", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setTitle() {
        Map<String, String> _paras_loadString = new HashMap<String, String>();
        _paras_loadString.put("color", "00ff00");
        DoService.syncMethod(this.model, "setTitleColor", _paras_loadString);
    }

    @Override
    public void doTestFireEvent(View view) {
        setTitle();
        openList();
    }

    private void openList() {
        Map<String, Object> _paras_loadString = new HashMap<String, Object>();
        DoService.asyncMethod(this.model, "openConversationList", _paras_loadString, new DoService.EventCallBack() {
            @Override
            public void eventCallBack(String _data) {// 回调函数
                DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
            }
        });
//            Map<String, String> _paras_loadString = new HashMap<String, String>();
//        _paras_loadString.put("nickName","www");
//        _paras_loadString.put("headPortrait","http://oj8so80jf.bkt.clouddn.com/ee.jpg");
//            DoService.asyncMethod(this.model, "setUserInfo", _paras_loadString, new DoService.EventCallBack() {
//            @Override
//            public void eventCallBack(String _data) {// 回调函数
//                DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
//            }
//        });

        Map<String, String> _paras3 = new HashMap<String, String>();
        DoService.syncMethod(this.model, "getLatestMessage", _paras3);
    }
}
