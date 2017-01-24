
package com.sussex.ase1.gpstry3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private Context context;
    private String postcode;
    private String typeFind;
    private String lat;
    private String lon;
    private String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        Intent i = getIntent();
        if(i.hasExtra("typeFind"))
        {
            typeFind = i.getStringExtra("typeFind");
        }
        if(typeFind.equals("P")) {
            if (i.hasExtra("postcode")) {
                postcode = i.getStringExtra("postcode");
            }
            url = "http://lowcost-env.kdumcfjv2e.us-west-2.elasticbeanstalk.com/TestMapServlet?maptype=%22heatmap%22&typeFind=P&postcode=" + postcode;
        }
        else
        {
            if (i.hasExtra("latitude")) {
                lat = i.getStringExtra("latitude");
            }
            if (i.hasExtra("longitude")) {
                lon = i.getStringExtra("longitude");
            }
            url = "http://lowcost-env.kdumcfjv2e.us-west-2.elasticbeanstalk.com/TestMapServlet?maptype=%22heatmap%22&typeFind=L&latitude="+lat+"&longitude="+lon;
        }

        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);

    }
}