package vuki.com.chromecastapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import hr.cs.aviion.player.item.PlayerItem;
import hr.cs.aviion.player.item.PlayerItemType;
import vuki.com.chromecastapp.databinding.ActivityCastHelperBinding;
import vuki.com.chromecastapp.utils.Utils;

/**
 * Created by mvukosav on 3.8.2016..
 */
public class CastHelper extends AppCompatActivity {

    ActivityCastHelperBinding binding;
    CastSession session;
    CastContext castContext;
    PlaybackState playbackState;
    PlayerItem playerItem;
    MediaInfo mediaInfo;
    PlaybackLocation playbackLocation;
    SessionManagerListener<CastSession> sessionSessionManagerListener;

    /**
     * indicates whether we are doing a local or a remote playback
     */
    public enum PlaybackLocation {
        LOCAL,
        REMOTE
    }

    /**
     * List of various states that we can be in
     */
    public enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }

    @Override
    protected void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        binding = DataBindingUtil.setContentView( this, R.layout.activity_cast_helper );

        setupActionBar();
        setPlayItem();

        castContext = CastContext.getSharedInstance( this );
        castContext.registerLifecycleCallbacksBeforeIceCreamSandwich( this, savedInstanceState );
        session = castContext.getSessionManager().getCurrentCastSession();

        setPlaying();
        setupCastListener();
        binding.playCircle.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                playbackState = PlaybackState.PLAYING;
//                if( playbackState == PlaybackState.IDLE ) {
//                    playbackState = PlaybackState.PLAYING;
//                } else {
//                    playbackState = PlaybackState.IDLE;
//                }
                togglePlayback();
            }
        } );
        updateMetadata( true );
    }

    @Override
    protected void onResume() {
        super.onResume();
        castContext.getSessionManager().addSessionManagerListener( sessionSessionManagerListener, CastSession.class );
        if( session != null ) {
            updatePlaybackLocation( PlaybackLocation.REMOTE );
        } else {
            updatePlaybackLocation( PlaybackLocation.LOCAL );
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        super.onCreateOptionsMenu( menu );
        getMenuInflater().inflate( R.menu.player, menu );
        CastButtonFactory.setUpMediaRouteButton( getApplicationContext(), menu,
                R.id.media_route_menu_item );
        // mQueueMenuItem = menu.findItem(R.id.action_show_queue);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu( Menu menu ) {
        menu.findItem( R.id.action_show_queue ).setVisible( ( session != null ) && session.isConnected() );
        return super.onPrepareOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        Intent intent;
        if( item.getItemId() == R.id.action_settings ) {
            intent = new Intent( CastHelper.this, CastPreference.class );
            startActivity( intent );
        } else if( item.getItemId() == R.id.action_show_queue ) {
            //    intent = new Intent(CastHelper.this, QueueListViewActivity.class);
            //   startActivity(intent);
            Toast.makeText( this, "Chill man", Toast.LENGTH_SHORT ).show();
        } else if( item.getItemId() == android.R.id.home ) {
            ActivityCompat.finishAfterTransition( this );
        }
        return true;
    }

    @Override
    public void onConfigurationChanged( Configuration newConfig ) {
        super.onConfigurationChanged( newConfig );
        getSupportActionBar().show();
        if( newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ) {
            getWindow().clearFlags( WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN );
            getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN );
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ) {
                getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LOW_PROFILE );
            }
            updateMetadata( false );
            //  binding.getRoot().setBackgroundColor(getResources().getColor(black));

        } else {
            getWindow().setFlags( WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN );
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN );
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ) {
                getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_VISIBLE );
            }
            updateMetadata( true );
            // mContainer.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    private static final int PRELOAD_TIME_S = 20;

    private void togglePlayback() {
        switch( playbackState ) {
            case PAUSED:

                break;
            case PLAYING:
                switch( playbackLocation ) {
                    case LOCAL:
                        binding.videoView.start();
                        playbackState = PlaybackState.PLAYING;
                        updatePlaybackLocation( PlaybackLocation.LOCAL );
                        break;
                    case REMOTE:
                        final RemoteMediaClient remoteMediaClient = session.getRemoteMediaClient();
                        if(remoteMediaClient==null){
                            Log.e("toggle playback","null");
                            return;
                        }
                        MediaQueueItem queueItem = new MediaQueueItem.Builder( mediaInfo )
                                .setAutoplay( true )
                                .setPreloadTime( PRELOAD_TIME_S )
                                .build();

                        remoteMediaClient.queueInsertAndPlayItem( queueItem, 0, null );

                        //  Intent intent=new Intent( this,ExpandedControlsActivity.class );
                        //  startActivity( intent );

                        loadRemoteMedia( 0, true );
                        //  finish();
                        break;
                    default:
                        break;
                }
                break;
            case BUFFERING:
                break;
            case IDLE:
                switch( playbackLocation ) {
                    case LOCAL:
                        binding.videoView.setVideoPath( playerItem.getStreamPath() );
                        binding.videoView.seekTo( 0 );
                        binding.videoView.start();
                        updatePlaybackLocation( PlaybackLocation.LOCAL );
                        break;
                    case REMOTE:
                        if( session != null && session.isConnected() ) {
                            //pokazi queue popup
                        }
                        break;
                }
                break;
        }
        updatePlayButton( playbackState );
    }

    private void setupCastListener() {
        sessionSessionManagerListener = new SessionManagerListener<CastSession>() {
            @Override
            public void onSessionStarting( CastSession castSession ) {

            }

            @Override
            public void onSessionStarted( CastSession castSession, String s ) {
                onApplicationConnected( castSession );
            }

            @Override
            public void onSessionStartFailed( CastSession castSession, int i ) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionEnding( CastSession castSession ) {

            }

            @Override
            public void onSessionEnded( CastSession castSession, int i ) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionResuming( CastSession castSession, String s ) {

            }

            @Override
            public void onSessionResumed( CastSession castSession, boolean b ) {
                onApplicationConnected( castSession );
            }

            @Override
            public void onSessionResumeFailed( CastSession castSession, int i ) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionSuspended( CastSession castSession, int i ) {

            }

            private void onApplicationConnected( CastSession castSession ) {
                session = castSession;
                if( null != playerItem ) {
                    if( playbackState == PlaybackState.PLAYING ) {
                        binding.videoView.pause();
                        loadRemoteMedia( 0, true );
                    } else {
                        playbackState = PlaybackState.IDLE;
                        updatePlaybackLocation( PlaybackLocation.REMOTE );
                    }
                }
                updatePlayButton( playbackState );
                invalidateOptionsMenu();
            }

            private void onApplicationDisconnected() {
                updatePlaybackLocation( PlaybackLocation.LOCAL );
                playbackState = PlaybackState.IDLE;
                updatePlayButton( playbackState );
                invalidateOptionsMenu();
            }

        };
    }

    private void updatePlayButton( PlaybackState state ) {
        boolean isConnected = ( session != null ) && ( session.isConnected() || session.isConnecting() );

        switch( state ) {
            case PLAYING:
                binding.playCircle.setVisibility( isConnected ? View.VISIBLE : View.GONE );
                binding.videoView.setVisibility( View.VISIBLE );
                break;
            case PAUSED:
                binding.playCircle.setVisibility( isConnected ? View.VISIBLE : View.GONE );
                break;
            case BUFFERING:
                break;
            case IDLE:
                binding.playCircle.setVisibility( View.VISIBLE );
                binding.videoView.setVisibility( View.INVISIBLE );
                break;
            default:
                binding.playCircle.setVisibility( isConnected ? View.GONE : View.VISIBLE );
                break;
        }
    }

    private void updatePlaybackLocation( PlaybackLocation location ) {
        playbackLocation = location;
        if( location == PlaybackLocation.LOCAL ) {
            if( playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.BUFFERING ) {

            }
        } else {

        }
    }

    private void setPlaying() {
        binding.videoView.setVideoPath( playerItem.getStreamPath() );
        binding.videoView.seekTo( 0 );
        binding.videoView.setVisibility( View.VISIBLE );
        updatePlaybackLocation( PlaybackLocation.LOCAL );
        playbackState = PlaybackState.IDLE;
        updatePlayButton( playbackState );

        binding.videoView.start();

    }

    private void setPlayItem() {
        playerItem = new PlayerItem();
        playerItem.setTitle( "Testiranje chromecasta" );
        playerItem.setBookmark( 0 );
        playerItem.setDescription( "Ovo je opis za testiranje" );
        playerItem.setPosterLandscape( "" );
        playerItem.setPosterPortrait( "" );
        playerItem.setType( PlayerItemType.LIVE );
        playerItem.setStreamPath( "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/ElephantsDream.m3u8" );

//        Bundle mediaInfoBundle = new Bundle();
//        mediaInfoBundle.putAll( Utils.mediaInfoToBundle( Utils.getMediaInfo( this, playerItem ) ) );
//        mediaInfo = Utils.bundleToMediaInfo( mediaInfoBundle );
        mediaInfo = Utils.buildMediaInfo( playerItem );
    }

    private void setupActionBar() {
        setSupportActionBar( binding.toolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
    }

    private final float mAspectRatio = 72f / 128;

    private void updateMetadata( boolean visible ) {
        Point displaySize;
        if( !visible ) {
            displaySize = Utils.getDisplaySize( this );
            RelativeLayout.LayoutParams lp = new
                    RelativeLayout.LayoutParams( displaySize.x,
                    displaySize.y + getSupportActionBar().getHeight() );
            lp.addRule( RelativeLayout.CENTER_IN_PARENT );
            binding.videoView.setLayoutParams( lp );
            binding.videoView.invalidate();
        } else {
            MediaMetadata mm = mediaInfo.getMetadata();
            displaySize = Utils.getDisplaySize( this );
            RelativeLayout.LayoutParams lp = new
                    RelativeLayout.LayoutParams( displaySize.x,
                    (int) ( displaySize.x * mAspectRatio ) );
            lp.addRule( RelativeLayout.BELOW, R.id.toolbar );
            binding.videoView.setLayoutParams( lp );
            binding.videoView.invalidate();
        }
    }

    private void loadRemoteMedia( int position, boolean autoPlay ) {
        if( session == null ) {
            return;
        }

        final RemoteMediaClient remoteMediaClient = session.getRemoteMediaClient();
        if( remoteMediaClient == null ) {
            return;
        }
        remoteMediaClient.addListener( new RemoteMediaClient.Listener() {
            @Override
            public void onStatusUpdated() {
                Intent intent = new Intent( CastHelper.this, ExpandedControlsActivity.class );
                startActivity( intent );
                remoteMediaClient.removeListener( this );
            }

            @Override
            public void onMetadataUpdated() {

            }

            @Override
            public void onQueueStatusUpdated() {

            }

            @Override
            public void onPreloadStatusUpdated() {

            }

            @Override
            public void onSendingRemoteMediaRequest() {

            }
        } );
        remoteMediaClient.load( mediaInfo, autoPlay, position );
    }

}
