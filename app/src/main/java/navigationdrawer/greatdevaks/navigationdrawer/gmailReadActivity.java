package navigationdrawer.greatdevaks.navigationdrawer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

/**
 * Created by amaneureka on 20-02-2016.
 */
public class gmailReadActivity extends AppCompatActivity {

    ImageView mViewAvatar;
    TextView mViewSenderName;
    TextView mViewSubject;
    WebView mViewBody;
    TextDrawable.IBuilder mBuilder;

    String mSender, mSubject, mBody, mAvatar;
    ColorGenerator generator = ColorGenerator.MATERIAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmail_mail);

        mBuilder = TextDrawable.builder()
                .beginConfig()
                .width(64)  // width in px
                .height(64) // height in px
                .endConfig()
                .round();

        mViewAvatar = (ImageView)findViewById(R.id.mail_pic);
        mViewSenderName = (TextView)findViewById(R.id.mail_from);
        mViewSubject = (TextView)findViewById(R.id.mail_subject);
        mViewBody = (WebView)findViewById(R.id.mail_body);

        mSender = getIntent().getStringExtra("gmail_sender_name");
        mSubject = getIntent().getStringExtra("gmail_sender_subject");
        mBody = getIntent().getStringExtra("gmail_sender_body");
        mAvatar = getIntent().getStringExtra("gmail_avatar");

        mViewAvatar.setImageDrawable(mBuilder.build(mAvatar, generator.getColor(mSender)));
        mViewSubject.setText(mSubject);
        mViewSenderName.setText(mSender);

        if (mBody != null)
            mViewBody.loadData(mBody, "text/html", "utf-8");
    }
}
