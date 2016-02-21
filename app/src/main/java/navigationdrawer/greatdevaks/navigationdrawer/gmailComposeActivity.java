package navigationdrawer.greatdevaks.navigationdrawer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by amaneureka on 20-02-2016.
 */
public class gmailComposeActivity extends AppCompatActivity {

    private static final String[] SCOPES = { GmailScopes.GMAIL_COMPOSE, GmailScopes.GMAIL_MODIFY };
    GoogleAccountCredential mCredential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmail_compose);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(getIntent().getStringExtra("gmail_account"));

        (findViewById(R.id.gmail_send)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String aTo = ((EditText)findViewById(R.id.gmail_to)).getText().toString().trim();
                String aSubject = ((EditText)findViewById(R.id.gmail_subject)).getText().toString().trim();
                String aBody = ((EditText)findViewById(R.id.gmail_body)).getText().toString().trim();

                //Gmail Credentials
                HttpTransport transport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                com.google.api.services.gmail.Gmail mService = new com.google.api.services.gmail.Gmail.Builder(
                        transport, jsonFactory, mCredential)
                        .setApplicationName("UniMessenger")
                        .build();
                Message aMessage  = new Message();
                aMessage.setRaw(Base64.encodeBase64String(aBody.getBytes()));
                try {
                    mService.users().messages().send("me", createMessageWithEmail(createEmail(aTo, "charanyogeshwar@gmail.com", aSubject, aBody)));
                }
                catch (Exception e)
                {
                    Log.e("[gmail]", "Unable to send mail");
                }

                Log.e("[gmail]", aMessage.getId());
            }
        });
    }
    public MimeMessage createEmail(String to, String from, String subject,
                                          String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        InternetAddress tAddress = new InternetAddress(to);
        InternetAddress fAddress = new InternetAddress(from);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }
    public Message createMessageWithEmail(MimeMessage email)
            throws MessagingException, IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        email.writeTo(bytes);
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}
