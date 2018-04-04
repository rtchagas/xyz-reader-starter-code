package com.example.xyzreader.ui;


import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.util.PageSplitter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * An activity representing a single Article detail screen, letting you swipe between pages
 * in the same article.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String STATE_ITEM_ID = "state_item_id";

    private long mSelectedItemId;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    private SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.US);

    // Use default locale format
    private DateFormat outputFormat = DateFormat.getDateInstance();

    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        // Initializes the page adapter.
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

        // Configure the pager
        mPager = findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        if (savedInstanceState == null) {
            if ((getIntent() != null) && (getIntent().getData() != null)) {
                mSelectedItemId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        } else {
            mSelectedItemId = savedInstanceState.getLong(STATE_ITEM_ID);
        }

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_ITEM_ID, mSelectedItemId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(this, mSelectedItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        updateArticleInfo(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mPagerAdapter.notifyDataSetChanged();
    }

    private void updateArticleInfo(Cursor cursor) {

        final ImageView ivBackdrop = findViewById(R.id.image_backdrop);
        final View vScrim = findViewById(R.id.scrim);
        TextView titleView = findViewById(R.id.article_title);
        TextView bylineView = findViewById(R.id.article_byline);
        NestedScrollView scrollView = findViewById(R.id.scrollView);
        bylineView.setMovementMethod(new LinkMovementMethod());

        if ((cursor != null) && cursor.moveToFirst()) {

            // Set the title
            titleView.setText(cursor.getString(ArticleLoader.Query.TITLE));

            // Compose the byline content.
            String author = cursor.getString(ArticleLoader.Query.AUTHOR);
            String date = cursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            Date publishedDate = parsePublishedDate(date);

            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                date = DateUtils.getRelativeTimeSpanString(
                        publishedDate.getTime(),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString();
            } else {
                // If date is before 1902, just show the string
                date = outputFormat.format(publishedDate);
            }

            Spannable dateSpan = new SpannableString(date);
            dateSpan.setSpan(
                    new ForegroundColorSpan(getResources().getColor(R.color.secondaryTextColorLight)),
                    0, dateSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            Spannable authorSpan = new SpannableString(getString(R.string.by_author, author));
            authorSpan.setSpan(
                    new ForegroundColorSpan(getResources().getColor(R.color.md_white)),
                    0, authorSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            bylineView.setText(dateSpan);
            bylineView.append(authorSpan);

            // Load the backdrop and extract its palette to be used in the backdrop scrim
            String imgUrl = cursor.getString(ArticleLoader.Query.PHOTO_URL);
            Picasso.get().load(imgUrl).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                    // Set the image.
                    ivBackdrop.setImageBitmap(bitmap);

                    // Extract the palette.
                    Palette p = Palette.from(bitmap).generate();
                    int color = p.getDarkMutedColor(getResources().getColor(R.color.colorPrimaryDark));

                    // Create a gradient with that color
                    GradientDrawable gd = new GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM,
                            new int[]{0, color});
                    gd.setCornerRadius(0f);

                    // Set the gradient to the scrim.
                    vScrim.setBackground(gd);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) { /* empty */ }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) { /* empty */ }
            });

            // Set the article body.
            // For this, we are going to break the content into pages
            // and serve them to the pager adapter.
            String body = cursor.getString(ArticleLoader.Query.BODY);
            // Adjust the line breaks.
            body = body.replaceAll("(\\S.*?)\\R(.*?\\S)", "$1 $2");

            // Get some information about where to draw the text.
            View root = LayoutInflater.from(this).inflate(R.layout.fragment_article_detail, null);
            TextView tvBody = root.findViewById(R.id.tv_page_content);

            // Split the article body into pages.
            PageSplitter splitter = new PageSplitter(scrollView.getMeasuredWidth(), scrollView.getHeight(),
                    tvBody.getLineSpacingMultiplier(), 0);
            splitter.append(body, tvBody.getPaint());

            // Set the page adapter.
            mPagerAdapter.setPages(splitter.getPages());
        }
    }

    private Date parsePublishedDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            return new Date();
        }
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        private List<CharSequence> pageList = null;

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ArticleDetailFragment.newInstance(pageList.get(position), position, getCount());
        }

        @Override
        public int getCount() {
            return (pageList != null ? pageList.size() : 0);
        }

        void setPages(List<CharSequence> pages) {
            this.pageList = pages;
            notifyDataSetChanged();
        }
    }
}
