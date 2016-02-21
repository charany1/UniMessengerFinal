package navigationdrawer.greatdevaks.navigationdrawer;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.melnykov.fab.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import navigationdrawer.greatdevaks.navigationdrawer.adapter.gmailAdapter;
import navigationdrawer.greatdevaks.navigationdrawer.helper.MailItem;
import navigationdrawer.greatdevaks.navigationdrawer.helper.MailItem;

public class gmailActivity extends AppCompatActivity {

    SwipeMenuListView mListView;
    TextDrawable.IBuilder mBuilder;
    FloatingActionButton mFloatingActionButton;
    SwipeRefreshLayout mSwipeRefreshLayout;

    GoogleAccountCredential mCredential;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_MODIFY };

    ColorGenerator generator = ColorGenerator.MATERIAL;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    List<String> mMETADATA;
    ArrayList<MailItem> mMailItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmail);

        mListView = (SwipeMenuListView)findViewById(R.id.listView);
        mFloatingActionButton = (FloatingActionButton)findViewById(R.id.fab);
        SetupDesign();

        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
    }

    void SetupDesign()
    {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu)
            {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                deleteItem.setWidth(90);
                deleteItem.setIcon(R.drawable.ic_delete);
                menu.addMenuItem(deleteItem);
            }
        };
        mListView.setMenuCreator(creator);

        mBuilder = TextDrawable.builder()
                .beginConfig()
                    .width(64)  // width in px
                    .height(64) // height in px
                .endConfig()
                .round();


        mMETADATA = new ArrayList<String>();
        mMETADATA.add("To");
        mMETADATA.add("Subject");
        mMETADATA.add("From");

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshResults();
            }
        });

        mFloatingActionButton.show(true);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aCompose = new Intent(gmailActivity.this, gmailComposeActivity.class);
                SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                aCompose.putExtra("gmail_account", settings.getString(PREF_ACCOUNT_NAME, null));
                startActivity(aCompose);
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if(firstVisibleItem == 0)
                    mSwipeRefreshLayout.setEnabled(true);
                else
                    mSwipeRefreshLayout.setEnabled(false);
            }
        });
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Account Unspecified", Toast.LENGTH_LONG);
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public TextDrawable getFancyAvatar(String aKey, String aText)
    {
        return mBuilder.build(aText, generator.getColor(aKey));
    }

    private void chooseAccount() {
        startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                gmailActivity.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private void refreshResults() {

        if (isGooglePlayServicesAvailable()) {
            if (mCredential.getSelectedAccountName() == null) {
                chooseAccount();
            } else {
                if (isDeviceOnline() && mMETADATA != null) {
                    new MakeRequestTask(this, mCredential).execute();
                } else {
                    Toast.makeText(this, "No network connection", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Play Services not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void  reloadAdapter()
    {
        if (mMailItems != null)
        {
            final gmailAdapter xAdapter = new gmailAdapter(this, mMailItems);
            mListView.setAdapter(xAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a, View v, int position, long id)
                {
                    MailItem aItem = (MailItem) xAdapter.getItem(position);
                    Intent intent = new Intent(gmailActivity.this, gmailReadActivity.class);
                    intent.putExtra("gmail_sender_name", aItem.getSenderName());
                    intent.putExtra("gmail_sender_subject", aItem.getSubject());
                    intent.putExtra("gmail_sender_body", aItem.getBody());
                    intent.putExtra("gmail_avatar", aItem.getAvatarChar());
                    startActivity(intent);
                }
            });
        }
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, ArrayList<MailItem>> {
        private gmailActivity mContext;
        private com.google.api.services.gmail.Gmail mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(gmailActivity aContext, GoogleAccountCredential credential) {
            mContext = aContext;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("UniMessenger")
                    .build();
        }

        @Override
        protected ArrayList<MailItem> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private ArrayList<MailItem> getDataFromApi() throws IOException {
            String user = "me";
            ArrayList<MailItem> Result = new ArrayList<MailItem>();

            ListMessagesResponse listResponse = mService.users()
                    .messages()
                    .list(user)
                    .setMaxResults(10L)
                    .execute();

            String _to = null, _from = null, _subject = null, _body = null;
            for (Message _m : listResponse.getMessages())
            {
                _m = mService.users().messages().get(user, _m.getId())
                        .setFormat("full")
                        .setMetadataHeaders(mMETADATA)
                        .setFields("payload").execute();

                if (_m.getPayload().getBody() != null && _m.getPayload().getBody().decodeData() != null) {
                    _body = new String(_m.getPayload().getBody().decodeData());
                    Log.e("Body", "+1");
                }
                for (MessagePartHeader _header : _m.getPayload().getHeaders())
                {
                    Log.d(_header.getName(), _header.getValue());
                    if (_header.getName().equals("To"))
                        _to = _header.getValue();
                    else if (_header.getName().equals("From"))
                        _from = _header.getValue();
                    else if (_header.getName().equals("Subject"))
                        _subject = _header.getValue();
                }
                if (_to == null || _from == null || _subject == null)
                    continue;
                MailItem _item = new MailItem(mContext, _body, _subject, _to, _from, _m.getSnippet());
                Result.add(_item);
            }
            return Result;
        }

        @Override
        protected void onPostExecute(ArrayList<MailItem> output) {
            mSwipeRefreshLayout.setRefreshing(false);
            if (output == null || output.size() == 0) {
                Toast.makeText(mContext, "Empty Body Found!", Toast.LENGTH_LONG).show();
            } else {
                mMailItems = output;
                mContext.reloadAdapter();
            }
        }

        @Override
        protected void onCancelled() {
            mSwipeRefreshLayout.setRefreshing(false);
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            gmailActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(mContext, "Error1" + mLastError.getMessage().toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(mContext, "Error2" + mLastError.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}