package vuki.com.chromecastapp;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.SessionManagerListener;

import vuki.com.chromecastapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private CastSession session;
    private CastContext castContext;
    private CastStateListener castStateListener;
    private MenuItem mediaRouteMenuItem;
    private MenuItem queueMenuItem;
    private IntroductoryOverlay introductoryOverlay;
    private final SessionManagerListener<CastSession> sessionSessionManagerListener = new MySessionManagerListener();

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        binding = DataBindingUtil.setContentView( this, R.layout.activity_main );
        castContext = CastContext.getSharedInstance( this );
        castContext.registerLifecycleCallbacksBeforeIceCreamSandwich( this, savedInstanceState );
        castStateListener = new CastStateListener() {
            @Override
            public void onCastStateChanged( int i ) {
                if( i != CastState.NO_DEVICES_AVAILABLE ) {
                    showIntroductoryOverlay();
                }
            }
        };

        setupActionBar();
        setBtnClick();
    }

    private void setBtnClick() {
        binding.btnStart.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                Intent intent = new Intent( MainActivity.this, CastHelper.class );
                ActivityCompat.startActivity( MainActivity.this, intent, null );
            }
        } );
    }

    @Override
    protected void onResume() {
        super.onResume();
        castContext.addCastStateListener( castStateListener );
        castContext.getSessionManager()
                .addSessionManagerListener( sessionSessionManagerListener, CastSession.class );
        if( session != null ) {
            session = CastContext.getSharedInstance( this ).getSessionManager().getCurrentCastSession();
        }

        if( queueMenuItem != null ) {
            queueMenuItem.setVisible( ( session != null ) && ( session.isConnected() ) );
        }
    }

    @Override
    protected void onPause() {
        castContext.removeCastStateListener( castStateListener );
        castContext.getSessionManager().removeSessionManagerListener( sessionSessionManagerListener, CastSession.class );
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        super.onCreateOptionsMenu( menu );
        getMenuInflater().inflate( R.menu.main, menu );
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton( getApplicationContext(), menu, R.id.media_route_menu_item );
        queueMenuItem = menu.findItem( R.id.action_show_queue );
        showIntroductoryOverlay();
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
            intent = new Intent( MainActivity.this, CastPreference.class );
            startActivity( intent );
        } else if( item.getItemId() == R.id.action_show_queue ) {
            //  intent = new Intent( MainActivity.this, QueueListViewActivity.class );
            // startActivity( intent );
        }
        return true;
    }

    private void setupActionBar() {
        setSupportActionBar( binding.toolbar );
    }

    private void showIntroductoryOverlay() {
        if( introductoryOverlay != null ) {
            introductoryOverlay.remove();
        }
        if( ( mediaRouteMenuItem != null ) && mediaRouteMenuItem.isVisible() ) {
            new Handler().post( new Runnable() {
                @Override
                public void run() {
                    introductoryOverlay = new IntroductoryOverlay.Builder( MainActivity.this, mediaRouteMenuItem )
                            .setTitleText( "Introducing cast" )
                            .setOverlayColor( R.color.colorPrimary )
                            .setSingleTime()
                            .setOnOverlayDismissedListener( new IntroductoryOverlay.OnOverlayDismissedListener() {
                                @Override
                                public void onOverlayDismissed() {
                                    introductoryOverlay = null;
                                }
                            } )
                            .build();
                    introductoryOverlay.show();
                }
            } );
        }

    }

    private class MySessionManagerListener implements SessionManagerListener<CastSession> {
        @Override
        public void onSessionStarting( CastSession castSession ) {

        }

        @Override
        public void onSessionStarted( CastSession castSession, String s ) {
            session = castSession;
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionStartFailed( CastSession castSession, int i ) {

        }

        @Override
        public void onSessionEnding( CastSession castSession ) {

        }

        @Override
        public void onSessionEnded( CastSession castSession, int i ) {
            if( castSession == session ) {
                session = null;
            }
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionResuming( CastSession castSession, String s ) {

        }

        @Override
        public void onSessionResumed( CastSession castSession, boolean b ) {
            session = castSession;
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionResumeFailed( CastSession castSession, int i ) {

        }

        @Override
        public void onSessionSuspended( CastSession castSession, int i ) {

        }
    }

}
