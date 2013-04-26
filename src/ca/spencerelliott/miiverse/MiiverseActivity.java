package ca.spencerelliott.miiverse;

import android.os.Bundle;
import android.app.Activity;
import android.content.res.Configuration;
import android.view.Menu;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MiiverseActivity extends Activity {

	private WebView webView = null;
	private CookieSyncManager manager = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_miiverse);
		
		manager = CookieSyncManager.createInstance(this);
		
		webView = (WebView)findViewById(R.id.webview);
		
		//Disable cache for now
		webView.getSettings().setJavaScriptEnabled(true);
	    webView.getSettings().setBuiltInZoomControls(false);
	    webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
	    
	    //Capture all URL loading in the app
	    webView.setWebViewClient(new WebViewClient() {
	    	@Override
	    	public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    		view.loadUrl(url);
	    		return true;
	    	}
	    	
	    	@Override
	    	public void onPageFinished(WebView view, String url) {
	    		CookieSyncManager.getInstance().sync();
	    	}
	    });
	    
	    //Load Miiverse
	    webView.loadUrl("https://miiverse.nintendo.net");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		manager.startSync();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		manager.stopSync();
	}
	
	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
	}
	
	@Override
	public void onBackPressed() {
		if(webView != null) {
			//Don't allow users to go back
			if(webView.getUrl().startsWith("https://id.nintendo.net/oauth/logout?")) {
				return;
			}
			
			//Kill the app if the user is on the activity feed
			if(webView.getUrl().equals("https://miiverse.nintendo.net") || webView.getUrl().equals("https://miiverse.nintendo.net/")) {
				finish();
				return;
			//Go back otherwise
			} else if(webView.canGoBack()) {
				webView.goBack();
				return;
			}
		}
		
		//Finish if for some reason we get here
		finish();
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.miiverse, menu);
		return true;
	}*/

}
