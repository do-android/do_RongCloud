package doext.module.do_RongCloud;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import core.helper.DoUIModuleHelper;

/**
 * Created by feng_ on 2017/3/30.
 */

public class ConversationActivity extends FragmentActivity implements View.OnClickListener {
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("do_RongCloud_Config", MODE_PRIVATE);
        setContentView(R.layout.conversation);
        TextView tv = (TextView) findViewById(R.id.title_tv);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.conversation_titleBar);
        Button btn_left = (Button) findViewById(R.id.btn_left);
        btn_left.setOnClickListener(this);

        Intent intent = getIntent();
        if (intent == null || intent.getData() == null)
            return;

        String title = intent.getData().getQueryParameter("title");
        tv.setText(title);
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
