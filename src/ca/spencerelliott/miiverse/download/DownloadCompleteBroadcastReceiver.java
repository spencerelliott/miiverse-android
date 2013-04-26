package ca.spencerelliott.miiverse.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

public class DownloadCompleteBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) return;
		
		//Let the OS know there's new media available
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
	}

}
