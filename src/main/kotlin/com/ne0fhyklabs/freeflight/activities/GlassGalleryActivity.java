package main.kotlin.com.ne0fhyklabs.freeflight.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.google.glass.custom.widget.MessageDialog;
import com.google.glass.custom.widget.MessageDialog.Builder;
import com.ne0fhyklabs.freeflight.R;
import com.ne0fhyklabs.freeflight.tasks.GetMediaObjectsListTask;
import com.ne0fhyklabs.freeflight.tasks.GetMediaObjectsListTask.MediaFilter;
import com.ne0fhyklabs.freeflight.tasks.LoadMediaThumbTask;
import com.ne0fhyklabs.freeflight.utils.ARDroneMediaGallery;
import com.ne0fhyklabs.freeflight.utils.ShareUtils;
import com.ne0fhyklabs.freeflight.vo.MediaVO;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class GlassGalleryActivity extends FragmentActivity {
	
	private final static String TAG = "GlassGalleryActivity";
	private final static String SELECTED_ELEMENT = "SELECTED_ELEMENT";
	public final static String MEDIA_FILTER = "MEDIA_FILTER";
	
	private GetMediaObjectsListTask mInitMediaTask = null;
	private MediaFilter mMediaFilter = null; 
	private ARDroneMediaGallery mMediaGallery;
	private GlassGalleryAdapter mAdapter; 
	private CardScrollView mCardScroller;
	private TextView mNoMediaView;
	private boolean mDisableExitSound = false;
	
	
	private void setup() {
		//mMediaGallery
		mMediaGallery = new ARDroneMediaGallery(getApplicationContext());
		//mAdapter
		mAdapter = new GlassGalleryAdapter(new ArrayList<MediaVO>(), getApplicationContext());
		//mCardScroller
		mCardScroller = (CardScrollView) findViewById(R.id.glass_gallery);
		mCardScroller.setAdapter(mAdapter);
		mCardScroller.setHorizontalScrollBarEnabled(true);
		mCardScroller.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				openOptionsMenu();
			}
		});
		//mNoMediaView
		mNoMediaView = (TextView) findViewById(R.id.glass_gallery_no_media);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_glass_gallery);
		Intent intent = getIntent();
		int selectedItem = 0;
		if(savedInstanceState != null) {
			savedInstanceState.getInt(SELECTED_ELEMENT, 0);
		}
		else {
			intent.getIntExtra(SELECTED_ELEMENT, 0);
		}
		int mediaFilterType =intent.getIntExtra(MEDIA_FILTER, MediaFilter.IMAGES.ordinal()); 
        mMediaFilter = MediaFilter.values()[mediaFilterType];
        initMediaTask(mMediaFilter, selectedItem);
	}	
	
	@Override
	protected void onDestroy() {
        super.onDestroy();
        if (mInitMediaTask != null && mInitMediaTask.getStatus() == Status.RUNNING) {
            mInitMediaTask.cancel(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!mDisableExitSound) {
        	AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audio.playSoundEffect(Sounds.DISMISSED);
        }
        //Restore the exit sound if it's been disabled
        mDisableExitSound = false;
    }

    @Override 
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_glass_gallery, menu);
        if (mMediaFilter == MediaFilter.IMAGES) {
            //Hide the play menu button
        	MenuItem menuItem = menu.findItem(R.id.menu_video_play);
        	menuItem.setEnabled(false);
        	menuItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Get the currently viewed media item
        int selectedPosition = mCardScroller.getSelectedItemPosition();
        MediaVO selectedMedia = (MediaVO) mAdapter.getItem(selectedPosition);
        
        switch(item.getItemId()) {
	        case R.id.menu_video_play: 
	        	playVideo(selectedMedia);
	        	return true;
	        case R.id.menu_media_delete:
	        	confirmDeleteMedia(selectedMedia);
	        	return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
        }
    }
    
    private void playVideo(MediaVO video) {
        if (!video.isVideo()) {
            Log.e(TAG, "Error: trying to play image as video");
            return;
        }
        mDisableExitSound = true;
        startActivity(new Intent(this, getClass()).putExtra(GlassVideoPlayerActivity.EXTRA_VIDEO_URI, video.getUri().toString()));
    }

    private void shareMedia(MediaVO media) {
        if (media.isVideo()) {
            ShareUtils.shareVideo(this, media.getPath());
        } else {
            ShareUtils.sharePhoto(this, media.getPath());
        }
    }

	private void confirmDeleteMedia(final MediaVO media) {
		Builder builder = new Builder(this);
		builder.setTemporaryIcon(R.drawable.ic_delete_50);
		builder.setTemporaryMessage("Deleting");
		builder.setIcon(R.drawable.ic_done_50);
		builder.setMessage("Deleted");
		builder.setDismissable(true);
		builder.setAutoHide(true);
		builder.setListener(new MessageDialog.SimpleListener() {

			@Override
			public void onDone() {
				super.onDone();
				deleteMedia(media);
			}
			
		});
		builder.build().show();
    }

    private void deleteMedia(MediaVO media) {
    	
//        val mediaId = IntArray(1)
//        mediaId.set(0, media.getId())
//
//        if (media.isVideo()) {
//            mMediaGallery.deleteVideos(mediaId)
//        } else {
//            mMediaGallery.deleteImages(mediaId)
//        }
//
//        mAdapter.remove(media)
//        updateView(null)
    }
    
    private void initMediaTask(MediaFilter filter, final Integer selectedElem) {
        if (mInitMediaTask == null || mInitMediaTask.getStatus() != Status.RUNNING) {
        	mInitMediaTask = new GetMediaObjectsListTask(this, filter) {

				@Override
				protected void onPostExecute(List<MediaVO> result) {
					super.onPostExecute(result);
					onMediaScanCompleted(selectedElem, result);
				}
        		
        	};

            try {
                mInitMediaTask.execute().get();
            } catch(InterruptedException e) {
               	Log.e(TAG, e.getMessage(), e);
            } catch (ExecutionException e) {
               	Log.e(TAG, e.getMessage(), e);
			}
        }
    }

    private void onMediaScanCompleted(int selectedElem, List<MediaVO> result) {
        initView(selectedElem, result);
        mInitMediaTask = null;
    }

    private void initView(int selectedElem, List<MediaVO> result) {
        mAdapter.clear();
        mAdapter.addAll(result);
        updateView(selectedElem);
    }

    private void updateView(Integer selectedElem) {
        if (mAdapter.getCount() > 0) {
            mNoMediaView.setVisibility(View.GONE);
            if (selectedElem != null) {
                mCardScroller.setSelection(selectedElem);
            }

            if (!mCardScroller.isActivated()) {
                mCardScroller.activate();
            }
        } else {
            mNoMediaView.setVisibility(View.VISIBLE);

            mCardScroller.deactivate();
            mCardScroller.setVisibility(View.GONE);
        }
    }

    @Override 
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        CardScrollView cardsScroller = (CardScrollView) findViewById(R.id.glass_gallery);
        outState.putInt(SELECTED_ELEMENT, cardsScroller.getSelectedItemPosition());
    }

    private class GlassGalleryAdapter extends CardScrollAdapter {
    	
    	private ArrayList<MediaVO> mediaList;
    	private Context context;
		private LayoutInflater mInflater;

		public GlassGalleryAdapter(ArrayList<MediaVO> mediaList, Context context) {
			super();
			this.mediaList = mediaList;
			this.context = context;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public void clear() {
            mediaList.clear();
            notifyDataSetChanged();
        }

        public void addAll(List<MediaVO> items) {
            mediaList.addAll(items);
            notifyDataSetChanged();
        }

        public void remove(MediaVO media) {
            mediaList.remove(media);
            notifyDataSetChanged();
        }

		@Override
		public int getCount() {
			return mediaList.size();
		}

		@Override
		public Object getItem(int position) {
			return mediaList.get(position);
		}

		@Override
		public int getPosition(Object item) {
            if (!(item instanceof MediaVO)) {
                return -1;
            }

            MediaVO mediaVO = (MediaVO) item;
            for(int i = 0; i < getCount()-1; i++) {
            	if(mediaVO.equals(getItem(i))) {
            		return i;
            	}
            }
            return -1;
		}

		@Override
		public View getView(int position, View view, ViewGroup viewGroup) {
			MediaVO mediaDetail = (MediaVO) getItem(position);

            View mediaCard = mInflater.inflate(R.layout.glass_gallery_item, viewGroup, false);
            ImageView imageView = (ImageView) mediaCard.findViewById(R.id.image);
            View imageIndicatorView = mediaCard.findViewById(R.id.btn_play);

            if (!mediaDetail.isVideo()) {
                imageIndicatorView.setVisibility(View.INVISIBLE);
                imageView.setScaleType(ScaleType.CENTER_CROP);
            }

            LoadMediaThumbTask task = new LoadMediaThumbTask(mediaDetail, imageView);
            task.execute();
            return mediaCard;
		}
    	
    }
}
