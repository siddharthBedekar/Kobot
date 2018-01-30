package com.example.sidnotebook.summary;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private WebView myWebView;
    private TextView story;
    private Context context;
    private String selectedText;
    Button btnsum;
    LinearLayout lin;
    String [] chapts;
    boolean loadingFinished=true;
    private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        chapts = new String[2];
        chapts[0]="Watson examines a mysterious cane left in the office by an unknown visitor, and Holmes sits with his back facing his friend. Watson offers up his theory as to the origin of the walking stick, declaring that the inscription, \"To James Mortimer, M.R.C.S., from his friends of the C.C.H.,\" suggests an elderly doctor who was awarded the object after years of faithful service. Holmes encourages Watson's speculation, and the doctor continues, saying that the well-worn stick implies a country practitioner who walks about quite a bit. In addition, the C.C.H., he suggests, is probably the mark of \"the something hunt,\" a local group to whom Mortimer provided some service. Holmes goes on to suggest that the man must possess a small spaniel, given the bite marks on the cane, and, he playfully announces, given the appearance of master and dog at their front door.";
        chapts[1]="At the time of the \"Great Revolution,\" Mortimer reads, Hugo Baskerville lorded over the Baskerville mansion in Devonshire. Enraged at finding that his captive escaped, Hugo made a deal with the devil and released his hounds in pursuit of the young girl. Hugo had just had his throat ripped out by \"a foul thing, a great, black beast.\" Ever since, Mortimer reports, the supernatural hound has haunted the family. The hound just recently killed Sir Charles Baskerville, the latest inhabitant of Baskerville Hall. Mortimer unfolds the Devon County Chronicle of May 14, reading about Sir Charles' philanthropy and the circumstances surrounding his death. The chronicle mentions the myth only to discount it, citing the testimony of Sir Charles' servants, Mr. Barrymore and Mrs. Barrymore, and that of Mortimer himself. Charles was found dead, the paper reports, at the site of his nightly walk down the so-called Yew Alley, which borders the haunted moorlands. Finally, Mortimer announces that the scene of the crime contained, in addition to Sir Charles' tiptoeing steps, \"the footprints of a gigantic hound.\"";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = findViewById(R.id.readAloudBtn);
        story = findViewById(R.id.storyBox);

        //Internet permission setup
        final Context context = getApplicationContext();
        int res = context.checkCallingOrSelfPermission("android.permission.INTERNET");

        if (res!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.INTERNET"},1);
        }

        res = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
        if (res!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},1);
        }

        res = context.checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE");
        if (res!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_EXTERNAL_STORAGE"},1);
        }

        //setup webView
        myWebView = findViewById(R.id.webView);

        myWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {

                DownloadManager.Request req= new DownloadManager.Request(Uri.parse(s));
                req.setMimeType(s3);
                String cookies = CookieManager.getInstance().getCookie(s);
                req.addRequestHeader("cookie",cookies);
                req.allowScanningByMediaScanner();
                req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"greetings.mp3");
                DownloadManager dm =(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(req);
                }
        });

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadingFinished=false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loadingFinished = true;
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        btnsum= (Button)findViewById(R.id.sbtn);
        lin=(LinearLayout)findViewById(R.id.chapters);
        btnsum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (lin.getVisibility() == LinearLayout.GONE) {
                    lin.setVisibility(LinearLayout.VISIBLE);
                } else {
                    lin.setVisibility(LinearLayout.GONE);
                    String total = "";
                    if(((CheckBox)findViewById(R.id.checkBox1)).isChecked()){
                        total+= chapts[0];
                    }
                    if(((CheckBox)findViewById(R.id.checkBox2)).isChecked()){
                        total+= chapts[1];
                    }

                    story.setText(total);
                }
            }
        });











        //setup storyBox for reading
        story = findViewById(R.id.storyBox);

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int selStart, selEnd;
                int min = 0;
                int max = story.getText().length();
                if (story.isFocused()) {
                    selStart = story.getSelectionStart();
                    selEnd = story.getSelectionEnd();
                    Log.e("hello",""+selStart+","+selEnd);
                    min = Math.max(0, Math.min(selStart, selEnd));
                    max = Math.max(0, Math.max(selStart, selEnd));
                    selectedText = story.getText().subSequence(min,max).toString();
                }

                String data = selectedText;
                //data = story.getText().toString();
                String[] a = data.split(" ");

                data = "";
                for(int i =0; i <a.length; i++){
                    data +=   a[i]+"%20" ;
                }

                myWebView.getSettings().setJavaScriptEnabled(true);
                myWebView.getSettings().setLoadsImagesAutomatically(true);

                //delay - allow server to process the text
                myWebView.loadUrl("http://192.168.43.136:5000/audgen/?text="+data);

                File file = new File("/storage/emulated/0/Download","greetings.mp3");
                if (file.exists()){
                    file.delete();
                }
                else{
                    ;
                }

                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                myWebView.loadUrl("http://192.168.43.136:5000/download");
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        File file = new File("/storage/emulated/0/Download","greetings.mp3");
                        MediaPlayer greet = MediaPlayer.create(MainActivity.this, Uri.parse("/storage/emulated/0/Download/greetings.mp3"));
                        if(file.exists()) {
                            greet.start();
                        }
                    }
                },2000);

            }
        });





    }





}
