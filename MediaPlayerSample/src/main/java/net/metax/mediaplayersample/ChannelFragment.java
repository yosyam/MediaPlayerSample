package net.metax.mediaplayersample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by yoshi on 13/09/03.
 */
public class ChannelFragment extends Fragment {
    final static String ARG_TITLE = "title";
    String mTitle = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getString(ARG_TITLE);
        }

        return inflater.inflate(R.layout.channel_view, container, false);

    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();
        if (args != null) {
            // Set article based on argument passed in
            updateChannelView(args.getString(ARG_TITLE));
        } else if (mTitle != null) {
            // Set article based on saved instance state defined during onCreateView
            updateChannelView(mTitle);
        }
    }

    public void updateChannelView(String title) {
        TextView textTitle = (TextView) getActivity().findViewById(R.id.textTitle);
        textTitle.setText(title);
        mTitle = title;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putString(ARG_TITLE, mTitle);
    }
}
