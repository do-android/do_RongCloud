package doext.module.do_RongCloud;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import core.helper.DoUIModuleHelper;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;

/**
 * Created by feng_ on 2017/3/20.
 */
public class ConversationListActivity extends FragmentActivity implements View.OnClickListener {
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("do_RongCloud_Config", MODE_PRIVATE);
        setContentView(R.layout.conversationlist);
        ConversationListFragment fragment = (ConversationListFragment) getSupportFragmentManager().findFragmentById(R.id.conversationlist);
        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                .appendPath("conversationlist")
                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")//设置群组会话聚合显示
                .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")//设置讨论组会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")//设置系统会话非聚合显示
                .build();
        fragment.setUri(uri);

        TextView tv = (TextView) findViewById(R.id.textView);
        Button btn_left = (Button) findViewById(R.id.btn_left);
        btn_left.setOnClickListener(this);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.conversationList_titleBar);
        init(tv, relativeLayout);
    }

    private void init(TextView tv, RelativeLayout relativeLayout) {
        String titleBarColor = sp.getString("titleBarColor", "");
        String titleColor = sp.getString("titleColor", "");
        if (titleBarColor.length() > 0 && titleBarColor != null) {
            relativeLayout.setBackgroundColor(DoUIModuleHelper.getColorFromString(titleBarColor, Color.BLUE));
        }
        if (titleColor.length() > 0 && titleColor != null) {
            tv.setTextColor(DoUIModuleHelper.getColorFromString(titleColor, Color.WHITE));
        }
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}