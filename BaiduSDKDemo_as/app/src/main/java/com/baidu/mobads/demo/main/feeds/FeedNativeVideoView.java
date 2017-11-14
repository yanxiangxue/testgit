
package com.baidu.mobads.demo.main.feeds;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.io.IOException;

public class FeedNativeVideoView extends RelativeLayout implements SurfaceHolder.Callback, OnPreparedListener,
        OnErrorListener, OnSeekCompleteListener, OnCompletionListener {

    private static final String TAG = "FSVideoView";

    protected Context context;
    protected Activity activity;

    protected MediaPlayer mediaPlayer;
    protected SurfaceHolder surfaceHolder;
    protected SurfaceView surfaceView;
    protected boolean videoIsReady;
    protected boolean surfaceIsReady;
    protected boolean detachedByFullscreen;
    protected State currentState;
    protected State lastState; // Tells onSeekCompletion what to do

    protected View loadingView;

    protected ViewGroup parentView;
    protected ViewGroup.LayoutParams currentLayoutParams;

    protected boolean isFullscreen;
    // buffer加载完之后是不是自动播放，一般这个都设置为true
    protected boolean shouldAutoplay;
    protected int initialConfigOrientation;
    protected int initialMovieWidth;
    protected int initialMovieHeight;

    protected OnErrorListener errorListener;
    protected OnPreparedListener preparedListener;
    protected OnSeekCompleteListener seekCompleteListener;
    protected OnCompletionListener completionListener;

    /**
     * States of MediaPlayer
     * http://developer.android.com/reference/android/media/MediaPlayer.html
     */
    public enum State {
        IDLE,
        INITIALIZED,
        PREPARED,
        PREPARING,
        STARTED,
        STOPPED,
        PAUSED,
        PLAYBACKCOMPLETED,
        ERROR,
        END
    }

    public FeedNativeVideoView(Context context) {
        super(context);
        this.context = context;

        init();
    }

    public FeedNativeVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        init();
    }

    public FeedNativeVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        init();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Log.d(TAG, "onSaveInstanceState");
        return super.onSaveInstanceState();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow - detachedByFullscreen: " + detachedByFullscreen);

        super.onDetachedFromWindow();

        if (!detachedByFullscreen) {
            if (mediaPlayer != null) {
                if (currentState != State.IDLE) {
                    mVideoPlayCallback.onCloseVideo(getCurrentPosition());
                }
                this.mediaPlayer.setOnPreparedListener(null);
                this.mediaPlayer.setOnErrorListener(null);
                this.mediaPlayer.setOnSeekCompleteListener(null);
                this.mediaPlayer.setOnCompletionListener(null);

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
            videoIsReady = false;
            surfaceIsReady = false;
            currentState = State.END;
        }

        detachedByFullscreen = false;
    }

    @Override
    public synchronized void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated called = " + currentState);

        mediaPlayer.setDisplay(surfaceHolder);

        // If is not prepared yet - tryToPrepare()
        if (!surfaceIsReady) {
            surfaceIsReady = true;
            if (currentState != State.PREPARED
                    && currentState != State.PAUSED
                    && currentState != State.STARTED
                    && currentState != State.PLAYBACKCOMPLETED) {
                tryToPrepare();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged called");
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                resize();
            }
        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed called");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            pause();
        }

        surfaceIsReady = false;
    }

    @Override
    public synchronized void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared called");
        videoIsReady = true;
        tryToPrepare();
    }

    /**
     * Restore the last State before seekTo()
     *
     * @param mp the MediaPlayer that issued the seek operation
     */
    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.d(TAG, "onSeekComplete");

        stopLoading();
        if (lastState != null) {
            switch (lastState) {
                case STARTED: {
                    start();
                    break;
                }
                case PAUSED: {
                    pause();
                    break;
                }
                case PLAYBACKCOMPLETED: {
                    currentState = State.PLAYBACKCOMPLETED;
                    break;
                }
                case PREPARED: {
                    currentState = State.PREPARED;
                    break;
                }
                default:
                    break;
            }
        }

        if (this.seekCompleteListener != null) {
            this.seekCompleteListener.onSeekComplete(mp);
        }
    }

    boolean isCompleted = false;

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion, this.mediaPlayer.isLooping() is " + this.mediaPlayer.isLooping());
        isCompleted = true;
        if (!this.mediaPlayer.isLooping()) {
            this.currentState = State.PLAYBACKCOMPLETED;
        } else {
            start();
        }
        
        mVideoPlayCallback.onCompletion(mp);
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "onError called - " + what + " - " + extra);

        stopLoading();
        this.currentState = State.ERROR;
        
        mVideoPlayCallback.onError(mp, what, extra);
        return false;
    }

    /**
     * Initializes the UI
     */
    protected void init() {
        if (isInEditMode()) {
            return;
        }

        this.shouldAutoplay = true;
        this.currentState = State.IDLE;
        this.isFullscreen = false;
        this.initialConfigOrientation = -1;
        this.setBackgroundColor(Color.BLACK);

        this.mediaPlayer = new MediaPlayer();

        this.surfaceView = new SurfaceView(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        layoutParams.addRule(CENTER_IN_PARENT);
        this.surfaceView.setLayoutParams(layoutParams);
        addView(this.surfaceView);

        this.surfaceHolder = this.surfaceView.getHolder();
        // noinspection deprecation
        this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.surfaceHolder.addCallback(this);

        this.loadingView = new ProgressBar(context);
        layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(CENTER_IN_PARENT);
        this.loadingView.setLayoutParams(layoutParams);
        addView(this.loadingView);

    }

    /**
     * Calls prepare() method of MediaPlayer
     */
    protected void prepare() throws IllegalStateException {
        if (mVideoPlayCallback != null) {
            mVideoPlayCallback.onClickAd(); 
        }
        startLoading();

        this.videoIsReady = false;
        this.initialMovieHeight = -1;
        this.initialMovieWidth = -1;

        this.mediaPlayer.setOnPreparedListener(this);
        this.mediaPlayer.setOnErrorListener(this);
        this.mediaPlayer.setOnSeekCompleteListener(this);
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        this.currentState = State.PREPARING;
        this.mediaPlayer.prepareAsync();
    }

    /**
     * Try to call state PREPARED
     * Only if SurfaceView is already created and MediaPlayer is prepared
     * Video is loaded and is ok to play.
     */
    protected void tryToPrepare() {
        if (this.surfaceIsReady && this.videoIsReady) {
            if (this.mediaPlayer != null) {
                this.initialMovieWidth = this.mediaPlayer.getVideoWidth();
                this.initialMovieHeight = this.mediaPlayer.getVideoHeight();
            }

            resize();
            stopLoading();
            currentState = State.PREPARED;

            if (shouldAutoplay) {
                start();
            }

            if (this.preparedListener != null) {
                this.preparedListener.onPrepared(mediaPlayer);
            }
        }
    }

    protected void startLoading() {
        this.loadingView.setVisibility(View.VISIBLE);
    }

    protected void stopLoading() {
        this.loadingView.setVisibility(View.GONE);
    }

    /**
     * Get the current {@link FeedNativeVideoView.State}.
     *
     * @return
     */
    public synchronized State getCurrentState() {
        return currentState;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        this.initialConfigOrientation = activity.getRequestedOrientation();
    }

    public void resize() {
        if (initialMovieHeight == -1 || initialMovieWidth == -1) {
            return;
        }

        View currentParent = (View) getParent();
        if (currentParent != null) {
            float videoProportion = (float) initialMovieWidth / (float) initialMovieHeight;

            int screenWidth = currentParent.getWidth();
            int screenHeight = currentParent.getHeight();
            float screenProportion = (float) screenWidth / (float) screenHeight;

            int newWidth;
            int newHeight;
            if (videoProportion > screenProportion) {
                newWidth = screenWidth;
                newHeight = (int) ((float) screenWidth / videoProportion);
            } else {
                newWidth = (int) (videoProportion * (float) screenHeight);
                newHeight = screenHeight;
            }
            Log.d(TAG, "Resizing: newWidth: " + newWidth + " - newHeight: " + newHeight);
            
            ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
            Log.d(TAG, "Resizing: lp.width: " + lp.width + " - lp.height: " + lp.height);
            if (lp.width != newWidth || lp.height != newHeight) {
                lp.width = newWidth;
                lp.height = newHeight;
                surfaceView.setLayoutParams(lp);
            }

        }
    }

    public boolean isShouldAutoplay() {
        return shouldAutoplay;
    }

    /**
     * Tells application that it should begin playing as soon as buffering
     * is ok
     *
     * @param shouldAutoplay If true, call start() method when getCurrentState() == PREPARED. Default is false.
     */
    public void setShouldAutoplay(boolean shouldAutoplay) {
        this.shouldAutoplay = shouldAutoplay;
    }

    /**
     * Toggles view to fullscreen mode
     * It saves currentState and calls pause() method.
     * When fullscreen is finished, it calls the saved currentState before pause()
     * In practice, it only affects STARTED state.
     * If currenteState was STARTED when fullscreen() is called, it calls start() method
     * after fullscreen() has ended.
     */
    public void fullscreen() throws IllegalStateException {
        if (mediaPlayer == null) {
            throw new RuntimeException("Media Player is not initialized");
        }

        boolean wasPlaying = mediaPlayer.isPlaying();
        if (wasPlaying) {
            pause();
        }

        if (!isFullscreen) {
            isFullscreen = true;

            View rootView = getRootView();
            View v = rootView.findViewById(android.R.id.content);
            ViewParent viewParent = getParent();
            if (viewParent instanceof ViewGroup) {
                if (parentView == null) {
                    parentView = (ViewGroup) viewParent;
                }

                // Prevents MediaPlayer to became invalidated and released
                detachedByFullscreen = true;

                // Saves the last state (LayoutParams) of view to restore after
                currentLayoutParams = this.getLayoutParams();

                parentView.removeView(this);
            } else {
                Log.e(TAG, "Parent View is not a ViewGroup");
            }

            if (v instanceof ViewGroup) {
                ((ViewGroup) v).addView(this);
            } else {
                Log.e(TAG, "RootView is not a ViewGroup");
            }
        } else {
            isFullscreen = false;

            ViewParent viewParent = getParent();
            if (viewParent instanceof ViewGroup) {
                // Check if parent view is still available
                boolean parentHasParent = false;
                if (parentView != null && parentView.getParent() != null) {
                    parentHasParent = true;
                    detachedByFullscreen = true;
                }

                ((ViewGroup) viewParent).removeView(this);
                if (parentHasParent) {
                    parentView.addView(this);
                    this.setLayoutParams(currentLayoutParams);
                }
            }
        }

        resize();

        if (wasPlaying && mediaPlayer != null) {
            start();
        }
    }

    /**
     * {@link MediaPlayer} method (getCurrentPosition)
     * http://developer.android.com/reference/android/media/MediaPlayer.html#getCurrentPosition%28%29
     */
    public int getCurrentPosition() {
        if (currentState != State.IDLE && mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        } else if (currentState == State.IDLE) {
            return 0;  
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * {@link MediaPlayer} method (getDuration)
     * http://developer.android.com/reference/android/media/MediaPlayer.html#getDuration%28%29
     */
    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * {@link MediaPlayer} method (getVideoHeight)
     * http://developer.android.com/reference/android/media/MediaPlayer.html#getVideoHeight%28%29
     */
    public int getVideoHeight() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoHeight();
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * {@link MediaPlayer} method (getVideoWidth)
     * http://developer.android.com/reference/android/media/MediaPlayer.html#getVideoWidth%28%29
     */
    public int getVideoWidth() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVideoWidth();
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * {@link MediaPlayer} method (isLooping)
     * http://developer.android.com/reference/android/media/MediaPlayer.html#isLooping%28%29
     */
    public boolean isLooping() {
        if (mediaPlayer != null) {
            return mediaPlayer.isLooping();
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * {@link MediaPlayer} method (isPlaying)
     * http://developer.android.com/reference/android/media/MediaPlayer.html#isLooping%28%29
     */
    public boolean isPlaying() throws IllegalStateException {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * {@link MediaPlayer} method (pause)
     * http://developer.android.com/reference/android/media/MediaPlayer.html#pause%28%29
     */
    public void pause() throws IllegalStateException {
        Log.d(TAG, "pause");
        if (mediaPlayer != null) {
            currentState = State.PAUSED;
            mediaPlayer.pause();
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * {@link MediaPlayer} method (reset)
     * http://developer.android.com/reference/android/media/MediaPlayer.html#reset%28%29
     */
    public void reset() {
        Log.d(TAG, "reset");

        if (mediaPlayer != null) {
            currentState = State.IDLE;
            mediaPlayer.reset();
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * {@link MediaPlayer} method (start)
     * http://developer.android.com/reference/android/media/MediaPlayer.html#start%28%29
     */
    public void start() throws IllegalStateException {
        Log.d(TAG, "start");

        if (mediaPlayer != null) {
            Log.d(TAG, "start, mediaPlayer.getCurrentPosition() is " + mediaPlayer.getCurrentPosition());
            currentState = State.STARTED;
            mediaPlayer.setOnCompletionListener(this);
            if (isCompleted == true) {
                isCompleted = false;
                mediaPlayer.seekTo(0);
            }
            mediaPlayer.start();
            mVideoPlayCallback.onStart();
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * {@link MediaPlayer} method (stop)
     * http://developer.android.com/reference/android/media/MediaPlayer.html#stop%28%29
     */
    public void stop() throws IllegalStateException {
        Log.d(TAG, "stop");

        if (mediaPlayer != null) {
            currentState = State.STOPPED;
            mediaPlayer.stop();
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * {@link MediaPlayer} method (seekTo)
     * http://developer.android.com/reference/android/media/MediaPlayer.html#stop%28%29
     * <p/>
     * It calls pause() method before calling MediaPlayer.seekTo()
     *
     * @param msec the offset in milliseconds from the start to seek to
     * @throws IllegalStateException if the internal player engine has not been initialized
     */
    public void seekTo(int msec) throws IllegalStateException {
        Log.d(TAG, "seekTo = " + msec);

        if (mediaPlayer != null) {
            // No live streaming
            if (mediaPlayer.getDuration() > -1 && msec <= mediaPlayer.getDuration()) {
                lastState = currentState;
                pause();
                mediaPlayer.seekTo(msec);

                startLoading();
            }
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    public void setOnCompletionListener(OnCompletionListener l) {
        if (mediaPlayer != null) {
            this.completionListener = l;
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    public void setOnErrorListener(OnErrorListener l) {
        if (mediaPlayer != null) {
            errorListener = l;
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
        if (mediaPlayer != null) {
            mediaPlayer.setOnBufferingUpdateListener(l);
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    public void setOnInfoListener(OnInfoListener l) {
        if (mediaPlayer != null) {
            mediaPlayer.setOnInfoListener(l);
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
        if (mediaPlayer != null) {
            this.seekCompleteListener = l;
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener l) {
        if (mediaPlayer != null) {
            mediaPlayer.setOnVideoSizeChangedListener(l);
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    public void setOnPreparedListener(OnPreparedListener l) {
        if (mediaPlayer != null) {
            this.preparedListener = l;
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    public void setLooping(boolean looping) {
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(looping);
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    public void setVolume(float leftVolume, float rightVolume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(leftVolume, rightVolume);
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * VideoView method (setVideoPath)
     */
    public void setVideoPath(String path) throws IOException, IllegalStateException,
        SecurityException, IllegalArgumentException, RuntimeException {
        if (mediaPlayer != null) {
            if (currentState != State.IDLE) {
                throw new IllegalStateException("FSVideoView Invalid State: " + currentState);
            }

            mediaPlayer.setDataSource(path);

            currentState = State.INITIALIZED;
            prepare();
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }

    /**
     * VideoView method (setVideoURI)
     */
    public void setVideoURI(Uri uri) throws IOException, IllegalStateException,
        SecurityException, IllegalArgumentException, RuntimeException {
        if (mediaPlayer != null) {
            if (currentState != State.IDLE) {
                throw new IllegalStateException("FSVideoView Invalid State: " + currentState);
            }

            mediaPlayer.setDataSource(context, uri);

            currentState = State.INITIALIZED;
            prepare();
        } else {
            throw new RuntimeException("Media Player is not initialized");
        }
    }
    
    protected VideoPlayCallbackImpl mVideoPlayCallback;

    public void setVideoPlayCallback(VideoPlayCallbackImpl videoPlayCallback) {
        mVideoPlayCallback = videoPlayCallback;
    }
    
    public interface VideoPlayCallbackImpl {
        void onCloseVideo(int progress);
        void onClickAd();
        void onStart();
        void onFullScreen(int progress);
        void onCompletion(MediaPlayer mp);
        void onError(MediaPlayer mp, int what, int extra);
        
    }
    
}
