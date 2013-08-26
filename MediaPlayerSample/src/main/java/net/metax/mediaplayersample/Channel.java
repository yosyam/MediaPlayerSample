package net.metax.mediaplayersample;

import java.net.URL;
import android.text.format.Time;

/**
 * Created by yoshi on 13/08/27.
 */
public class Channel {
    private String mWrapperType;
    private String mKind;
    private int mArtistId;
    private int mCollectionId;
    private int mTrackId;
    private String mArtistName;
    private String mCollectionName;
    private String mTrackName;
    private URL mArtistViewUrl;
    private URL mCollectionViewUrl;
    private URL mFeedUrl;
    private URL mTrackViewUrl;
    private URL mArtworkUrl60;
    private URL mArtworkUrl100;
    private Time mReleaseDate;
    private String mCollectionExplicitness;
    private String mTrackExplicitness;

    public String getWrapperType() {
        return mWrapperType;
    }

    public void setWrapperType(String mWrapperType) {
        this.mWrapperType = mWrapperType;
    }

    public String getKind() {
        return mKind;
    }

    public void setKind(String mKind) {
        this.mKind = mKind;
    }

    public int getArtistId() {
        return mArtistId;
    }

    public void setArtistId(int mArtistId) {
        this.mArtistId = mArtistId;
    }

    public int getCollectionId() {
        return mCollectionId;
    }

    public void setCollectionId(int mCollectionId) {
        this.mCollectionId = mCollectionId;
    }

    public int getTrackId() {
        return mTrackId;
    }

    public void setTrackId(int mTrackId) {
        this.mTrackId = mTrackId;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String mArtistName) {
        this.mArtistName = mArtistName;
    }

    public String getCollectionName() {
        return mCollectionName;
    }

    public void setCollectionName(String mCollectionName) {
        this.mCollectionName = mCollectionName;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public void setTrackName(String mTrackName) {
        this.mTrackName = mTrackName;
    }

    public URL getArtistViewUrl() {
        return mArtistViewUrl;
    }

    public void setArtistViewUrl(URL mArtistViewUrl) {
        this.mArtistViewUrl = mArtistViewUrl;
    }

    public URL getCollectionViewUrl() {
        return mCollectionViewUrl;
    }

    public void setCollectionViewUrl(URL mCollectionViewUrl) {
        this.mCollectionViewUrl = mCollectionViewUrl;
    }

    public URL getFeedUrl() {
        return mFeedUrl;
    }

    public void setFeedUrl(URL mFeedUrl) {
        this.mFeedUrl = mFeedUrl;
    }

    public URL getTrackViewUrl() {
        return mTrackViewUrl;
    }

    public void setTrackViewUrl(URL mTrackViewUrl) {
        this.mTrackViewUrl = mTrackViewUrl;
    }

    public URL getArtworkUrl60() {
        return mArtworkUrl60;
    }

    public void setArtworkUrl60(URL mArtworkUrl60) {
        this.mArtworkUrl60 = mArtworkUrl60;
    }

    public URL getArtworkUrl100() {
        return mArtworkUrl100;
    }

    public void setArtworkUrl100(URL mArtworkUrl100) {
        this.mArtworkUrl100 = mArtworkUrl100;
    }

    public Time getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(Time mReleaseDate) {
        this.mReleaseDate = mReleaseDate;
    }

    public String getCollectionExplicitness() {
        return mCollectionExplicitness;
    }

    public void setCollectionExplicitness(String mCollectionExplicitness) {
        this.mCollectionExplicitness = mCollectionExplicitness;
    }

    public String getTrackExplicitness() {
        return mTrackExplicitness;
    }

    public void setTrackExplicitness(String mTrackExplicitness) {
        this.mTrackExplicitness = mTrackExplicitness;
    }
}
