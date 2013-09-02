package net.metax.mediaplayersample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yoshi on 13/09/03.
 */
public class ChannelListFragment extends ListFragment {
    private static final String TAG = "ChannelsFlagment";

    OnChannelSelectedListener mCallback;

    private RequestQueue mQueue;
    private ImageLoader mImageLoader;

    List<Channel> channelList = new ArrayList<Channel>();

    public interface OnChannelSelectedListener {
        public void onChannelSelected(Channel channel);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        mImageLoader = new ImageLoader(mQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);

            @Override
            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                mCache.get(url);
            }
        });
        refreshData();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mCallback.onChannelSelected(channelList.get(position));

        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnChannelSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnChannelSelectedListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getFragmentManager().findFragmentById(R.id.channels_fragment) != null) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }

    private void refreshData() {
        String url = "https://itunes.apple.com/search?entity=podcast&country=jp&term=news&%e3%83%90%e3%82%a4%e3%83%aa%e3%83%b3%e3%82%ac%e3%83%ab%e3%83%8b%e3%83%a5%e3%83%bc%e3%82%b9";
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject result) {
                        try {
                            int count = parseJson(result);
                            if (count == -1) {
                                toast(R.string.server_error);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            toast(R.string.json_error);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                toast(R.string.connection_error);
            }
        }
        );
        jsonRequest.setTag(TAG);
        mQueue.add(jsonRequest);


    }

    /**
     * 得られたJSONデータを解析する
     *
     */
    private int parseJson(JSONObject root) throws JSONException {
        int returnValue;

        returnValue = root.getInt("resultCount");
        JSONArray arr = root.getJSONArray("results");
        channelList.clear();
        for (int i = 0; i < returnValue; i++) {
            Channel channel = new Channel();
            JSONObject json = arr.getJSONObject(i);
            channel.setArtistName(json.getString("artistName"));
            channel.setCollectionName(json.getString("collectionName"));
            channel.setCollectionName(json.getString("collectionName"));
            if (json.getString("artworkUrl100") != null) {
                try {
                    channel.setArtworkUrl100(new URL(json.getString("artworkUrl100")));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            channelList.add(channel);
        }
        ChannelAdapter channelAdapter = new ChannelAdapter(this.getActivity(), R.layout.row_channel, channelList);
        setListAdapter(channelAdapter);

        return returnValue;
    }

    private void toast(int id) {
        String text = getResources().getString(id);
        Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    private class ChannelAdapter extends ArrayAdapter<Channel> {
        private List<Channel> mItems;
        private LayoutInflater mInflater;

        public ChannelAdapter(Context context, int resourceId, List<Channel> items) {
            super(context, resourceId, items);
            this.mItems = items;
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // return super.getView(position, convertView, parent);
            View v = convertView;
            if (v == null) {
                v = mInflater.inflate(R.layout.row_channel, null);
            }
            Channel channel = mItems.get(position);
            TextView text1 = (TextView)v.findViewById(R.id.text1);
            text1.setText(channel.getCollectionName());
            TextView text2 = (TextView)v.findViewById(R.id.text2);
            text2.setText(channel.getArtistName());
            NetworkImageView channelImage = (NetworkImageView) v.findViewById(R.id.channelImage);
            if (channel.getArtworkUrl100() != null) {
                channelImage.setImageUrl(channel.getArtworkUrl100().toString(), mImageLoader);
            }
            return v;
        }
    }

}
