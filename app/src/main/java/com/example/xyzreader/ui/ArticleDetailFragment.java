package com.example.xyzreader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import butterknife.ButterKnife;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_COLOR_ID = "color_id";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Cursor mCursor;
    private long mItemId;
    private String mItemTitle = "";
    private int mMutedColor = 0xFF333333;
    OnHeadlineSelectedListener mCallback;
    private View mRootView;
    NestedScrollView mNestedScrollView;
    ImageView imageView;
    CollapsingToolbarLayout collapsingToolbarLayout;
    Toolbar mToolbar;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    public ArticleDetailFragment() {
    }



    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnHeadlineSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, mRootView);

        mNestedScrollView = (NestedScrollView) mRootView.findViewById(R.id.view);

        imageView = (ImageView) mRootView.findViewById(R.id.imagebig);
        collapsingToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsing_toolbar);
        mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);

        ArticleDetailActivity activity = (ArticleDetailActivity) getActivity();
        activity.setSupportActionBar(mToolbar);

        mCallback.onArticleSelected(collapsingToolbarLayout);

        mNestedScrollView.setOnScrollChangeListener(
                new NestedScrollView.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(
                            NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                        if (scrollY > oldScrollY) {
                            mCallback.onNestedScrollChange(true);
                        }else if (scrollY < oldScrollY){
                            mCallback.onNestedScrollChange(false);
                        }
                    }
                });


        bindViews();
        return mRootView;
    }



    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser && isResumed()){
            collapsingToolbarLayout.setTitle(mItemTitle);
            mCallback.onArticleSelected(collapsingToolbarLayout);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }


    public void bindViews(){
        if (mRootView == null) {
            return;
        }
        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);

        if (mCursor != null) {
            mItemTitle = mCursor.getString(ArticleLoader.Query.TITLE);
            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            collapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));

            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#000000'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));

            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

            Glide.with(getActivity().getBaseContext())
                    .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                    .asBitmap()
                    .fitCenter()
                    .into(imageView);
        } else {
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }


}
