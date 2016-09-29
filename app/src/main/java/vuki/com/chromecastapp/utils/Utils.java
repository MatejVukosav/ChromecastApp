package vuki.com.chromecastapp.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Point;
import android.net.Uri;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;

import hr.cs.aviion.player.item.PlayerItem;

/**
 * Created by mvukosav on 3.8.2016..
 */
public class Utils {

    private static final String TAG = "Utils";

    /**
     * Gets the version of app.
     */
    public static String getAppVersionName( Context context ) {
        String versionString = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo( context.getPackageName(),
                    0 /* basic info */ );
            versionString = info.versionName;
        } catch( Exception e ) {
            // do nothing
        }
        return versionString;
    }

    /**
     * Returns the screen/display size
     *
     */
    public static Point getDisplaySize( Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        return new Point(width, height);
    }

    public static MediaInfo buildMediaInfo(PlayerItem playerItem) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

       // movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, studio);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, playerItem.getTitle());
        movieMetadata.addImage(new WebImage(Uri.parse(playerItem.getPosterPortrait())));
        movieMetadata.addImage(new WebImage(Uri.parse(playerItem.getPosterLandscape())));

        return new MediaInfo.Builder(playerItem.getStreamPath())
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
               // .setContentType(mimeType)
                .setContentType( "video/mp4" )
                .setMetadata(movieMetadata)
               // .setMediaTracks(tracks)
               // .setStreamDuration(duration * 1000)
               // .setCustomData(jsonObj)
                .build();
    }

}
