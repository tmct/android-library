package com.tealium.example.view;

import java.io.File;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.VideoView;

import com.tealium.example.R;

public class VideoLayout extends RelativeLayout {

	{
		// Attaching OnGlobalLayoutListener to ensure this is ran after adding
		// all of the children. (This method is available GINGERBREAD+)
		this.getViewTreeObserver().addOnGlobalLayoutListener(createOnGlobalLayoutListener());
	}

	public VideoLayout(Context context) {
		super(context);
	}

	public VideoLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public VideoLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public VideoLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	private void setup() {
		final VideoView videoView = (VideoView) this.findViewById(R.id.videolayout_videoview);
		final ImageButton imageButton = (ImageButton) this.findViewById(R.id.videolayout_toggle);
		final SeekBar seekBar = (SeekBar) this.findViewById(R.id.videolayout_seekbar);

		this.setupVideoView(videoView, seekBar, imageButton);

		imageButton.setOnClickListener(createToggleButtonClickListener(videoView));

		seekBar.setOnSeekBarChangeListener(this.createOnSeekBarChangeListener());
	}

	private void setupVideoView(VideoView videoView, SeekBar seekBar, ImageButton imageButton) {

		MediaController mediaController = new MediaController(this.getContext());
		mediaController.setVisibility(View.GONE);

		String path = "android.resource://" + getContext().getPackageName() + File.separator + R.raw.tmu;

		videoView.setOnErrorListener(this.createErrorListener());
		videoView.setOnPreparedListener(this.createOnPreparedListener(videoView, seekBar, imageButton));
		videoView.setMediaController(mediaController);
		videoView.setVideoURI(Uri.parse(path));
	}

	private OnSeekBarChangeListener createOnSeekBarChangeListener() {

		final VideoView videoView = (VideoView) this.findViewById(R.id.videolayout_videoview);

		return new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					videoView.seekTo(progress);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		};
	}

	private OnPreparedListener createOnPreparedListener(
		final VideoView videoView,
		final SeekBar seekBar,
		final ImageButton imageButton) {

		return new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				seekBar.setMax(videoView.getDuration());
				postDelayed(createControlUpdater(videoView, seekBar, imageButton), 1000);
			}
		};
	}

	private Runnable createControlUpdater(final VideoView videoView, final SeekBar seekBar, final ImageButton imageButton) {

		return new Runnable() {
			@Override
			public void run() {

				seekBar.setProgress(videoView.getCurrentPosition());

				if (videoView.isPlaying()) {
					imageButton.setImageResource(android.R.drawable.ic_media_pause);
				} else {
					imageButton.setImageResource(android.R.drawable.ic_media_play);
				}

				postDelayed(this, 1000);
			}
		};
	}

	private OnErrorListener createErrorListener() {
		return new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {

				View child;

				for (int i = 0; i < getChildCount(); i++) {
					if ((child = getChildAt(i)).getId() == R.id.videolayout_errorlabel) {
						child.setVisibility(View.VISIBLE);
					} else {
						child.setVisibility(View.GONE);
					}
				}

				return true;
			}
		};
	}

	private OnClickListener createToggleButtonClickListener(final VideoView videoView) {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (videoView.isPlaying()) {
					videoView.pause();
				} else {
					videoView.start();
				}
			}
		};
	}

	private OnGlobalLayoutListener createOnGlobalLayoutListener() {

		return new OnGlobalLayoutListener() {

			// Versioning code addresses the concerns suppressed by these
			// annotations.
			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {

				setup();

				// Clean up.
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		};
	}
}
