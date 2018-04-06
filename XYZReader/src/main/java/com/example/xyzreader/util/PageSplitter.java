package com.example.xyzreader.util;


import android.content.Context;
import android.support.annotation.NonNull;

import com.example.xyzreader.R;

import java.util.ArrayList;
import java.util.List;

public final class PageSplitter {

    private int mMaxWords;
    private String mText;

    public PageSplitter(@NonNull Context context, @NonNull String text) {
        mMaxWords = context.getResources().getInteger(R.integer.detail_max_words_per_page);
        mText = text;
    }

    public List<String> getPages() {

        ArrayList<String> pageList = new ArrayList<>();
        String[] wordsArray = mText.split(" ");
        int idx = 0;

        while (idx < wordsArray.length) {

            StringBuilder builder = new StringBuilder();

            for (int i = 0; (i < mMaxWords) && (idx < wordsArray.length); i++) {
                builder.append(wordsArray[idx++]);
                builder.append(" ");
            }

            pageList.add(builder.toString());
        }

        return pageList;
    }
}
