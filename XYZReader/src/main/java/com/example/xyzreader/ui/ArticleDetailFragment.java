package com.example.xyzreader.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment {

    private static final String TAG = "ArticleDetailFragment";

    private static final String ARG_PAGE_CONTENT = "arg_page_content";
    private static final String ARG_PAGE_CURRENT = "arg_page_current";
    private static final String ARG_PAGE_COUNT = "arg_page_count";

    private String mPageContent = null;
    private int mPage = 0;
    private int mCount = 0;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(String content, int page, int pageCount) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_PAGE_CONTENT, content);
        arguments.putInt(ARG_PAGE_CURRENT, page);
        arguments.putInt(ARG_PAGE_COUNT, pageCount);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPageContent = getArguments().getString(ARG_PAGE_CONTENT);
            mPage = getArguments().getInt(ARG_PAGE_CURRENT);
            mCount = getArguments().getInt(ARG_PAGE_COUNT);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        TextView tvPageContent = rootView.findViewById(R.id.tv_page_content);

        // Show the article body as HTML
        String htmlText = mPageContent.replaceAll("(\r\n|\n)", "<br/>");
        tvPageContent.setText(Html.fromHtml(htmlText));

        TextView tvPageCount = rootView.findViewById(R.id.tv_page_count);
        tvPageCount.setText(String.format("%s/%s", mPage, mCount));

        return rootView;
    }
}
