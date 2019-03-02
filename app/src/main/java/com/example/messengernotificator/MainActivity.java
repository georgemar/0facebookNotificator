package com.example.messengernotificator;

import android.support.v7.app.*;
import android.support.v4.app.*;
import android.os.Bundle;
import android.view.View;
import android.app.*;
import android.webkit.*;
import android.webkit.JavascriptInterface;
import android.widget.TextView;
import android.widget.*;
import android.os.*;
import java.lang.*;


public class MainActivity extends AppCompatActivity {
    static Boolean notifications_state = false;
    static int counter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WebView webview = findViewById(R.id.webview);

        //creates the notification channel (lol)
        createNotificationChannel();


        setWebViewProperties();
        //getting the first html code to analyze if logged in or not
        webview.loadUrl("https://0.facebook.com");

        //setting up the switch listener to toggle notifications
        final Switch notifications = findViewById(R.id.not_switch);
        notifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                notifications_state = isChecked;
            }
        });

        //every 10 seconds the page will reload if the user wants notifications to detect new messages
        final Handler handler = new Handler();
        final int delay = 10000; //milliseconds (1 sec = 1000 ms)
        handler.postDelayed(new Runnable(){
            public void run(){
                if (notifications_state) {
                    webview.loadUrl("https://0.facebook.com");
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "notif";
            String description = "notif2";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("mesnot", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "mesnot")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("0.facebook")
                .setContentText("You have a new 0.facebook message")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(6476, builder.build());
    }

    public void setWebViewProperties(){
        final WebView webview = findViewById(R.id.webview);
        Handler mHandler = new Handler();

        //enables js so the program can extract the html using js
        webview.getSettings().setJavaScriptEnabled(true);

        //initialize js interface to call java methods through js
        webview.addJavascriptInterface(new MyJavaScriptInterface(mHandler), "HtmlViewer");

        //after loading the page it extracts the html
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                webview.loadUrl("javascript:window.HtmlViewer.showHTML" +
                        "('&lt;html&gt;'+document.getElementsByTagName('html')[0].innerHTML+'&lt;/html&gt;');");
            }
        });
    }

    public void afterPageFinish(String HtmlText) {
        Switch notifications = findViewById(R.id.not_switch);
        TextView nottext = findViewById(R.id.not_txt);
        TextView logtext = findViewById(R.id.loggin_txt);
        final WebView webview = findViewById(R.id.webview);

        if (HtmlText.contains("δεν είναι διαθέσιμο")){
            logtext.setText("Το 0.facebook δεν είναι διαθέσιμο στο δίκτυο σας");
            logtext.setVisibility(View.VISIBLE);
        } else if (HtmlText.contains("Μηνύματα")){
            webview.setVisibility(View.INVISIBLE);
            if (counter < 2 && notifications_state)
                counter++;
            logtext.setText("You are logged in");
            logtext.setVisibility(View.VISIBLE);
            notifications.setVisibility(View.VISIBLE);
            if (HtmlText.contains("Μηνύματα<span")) {
                if (counter == 1) {
                    nottext.setVisibility(View.VISIBLE);
                    sendNotification();
                }
            } else {
                counter = 0;
                nottext.setVisibility(View.INVISIBLE);
            }
        } else {
            logtext.setText("You have to login");
            logtext.setVisibility(View.VISIBLE);
            webview.setVisibility(View.VISIBLE);
        }
    }

    class MyJavaScriptInterface {
        private Handler mHandler;
        MyJavaScriptInterface(Handler mHandler) {
            this.mHandler = mHandler;
        }

        @JavascriptInterface
        public void showHTML(String html)
        {
            final String html_ = html;
            mHandler.post(new Runnable() {
               @Override
               public void run()
               {
                   afterPageFinish(html_);
               }
           });
        }
    }
}
