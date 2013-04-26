package ca.spencerelliott.miiverse;

import java.io.File;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class MiiverseActivity extends Activity {

	private WebView webView = null;
	private CookieSyncManager cManager = null;
	private DownloadManager dManager;
	private ca.spencerelliott.miiverse.download.DownloadManager bc_dManager;
	
	private SharedPreferences prefs;
	
	private final static int CONTEXT_IMAGE = 0;
	
	OnLongClickListener listener = new OnLongClickListener() {
		@SuppressLint("NewApi")
		@Override
		public boolean onLongClick(View arg0) {
			WebView.HitTestResult hitTestResult = webView.getHitTestResult();
			switch (hitTestResult.getType()) {
				case WebView.HitTestResult.IMAGE_TYPE:
					//Make sure the folder exists before using it
					File fileDir = new File(MiiverseActivity.this.getExternalFilesDir(null) + "/Miiverse/");
					if(!fileDir.exists()) fileDir.mkdirs();
					
					//Generate the filename to save as
					String filename = "file://" + MiiverseActivity.this.getExternalFilesDir(null) + "/Miiverse/" + System.currentTimeMillis() + ".jpg";
					
					//Notify the user the image is being downloaded
					Toast.makeText(MiiverseActivity.this, R.string.downloading_post, Toast.LENGTH_SHORT).show();
					
					if(!prefs.contains("notify_location")) {
						Toast.makeText(MiiverseActivity.this, R.string.images_available, Toast.LENGTH_LONG).show();
						
						//Save that we've notified the user about where the images are downloaded
						SharedPreferences.Editor editor = prefs.edit();
						editor.putBoolean("notify_location", true);
						editor.commit();
					}
					
					//Use the built in DownloadManager for Honeycomb or above
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						DownloadManager.Request request = new DownloadManager.Request(Uri.parse(hitTestResult.getExtra()));
						
						request.setDestinationUri(Uri.parse(filename));
						request.allowScanningByMediaScanner();
						request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
						dManager.enqueue(request);
					//Allows https links to be downloaded with custom DownloadManager
					} else {
						ca.spencerelliott.miiverse.download.DownloadManager.Request request = new ca.spencerelliott.miiverse.download.DownloadManager.Request(Uri.parse(hitTestResult.getExtra()));
						request.setDestinationUri(Uri.parse(filename));
						
						bc_dManager.enqueue(request);
					}
					break;
			}
			
			return true;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_miiverse);
		
		prefs = this.getPreferences(MODE_PRIVATE);
		
		cManager = CookieSyncManager.createInstance(this);
		
		dManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
		bc_dManager = new ca.spencerelliott.miiverse.download.DownloadManager(getContentResolver(), "ca.spencerelliott.miiverse");
		
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
	    
	    //Register for long clicks
	    webView.setOnLongClickListener(listener);
	    
	    //Register for context menus to be able to save images
	    //registerForContextMenu(webView);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		cManager.startSync();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		cManager.stopSync();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.miiverse, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_refresh:
				if(webView != null) {
					//Tell the web view to reload the page
					webView.post(new Runnable() {
						@Override
						public void run() {
							webView.reload();
						}
					});
				}
				break;
		}
		return true;
	}

}
