package com.ssn.framework.foundation;

import android.content.res.ColorStateList;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.TextAppearanceSpan;

/**
 * Created by lingminjun on 15/8/4.
 */
public class AttributeStringBuilder {
    private SpannableStringBuilder builder;
    public AttributeStringBuilder() {builder = new SpannableStringBuilder();}

    public void append(CharSequence text) {
        if (TextUtils.isEmpty(text)) {return;}
        builder.append(text);
    }

    public void append(CharSequence text, int colorId) {
        if (TextUtils.isEmpty(text)) {return;}
        builder.append(text);
        if (colorId != 0) {
            int end = builder.length();
            int start = end - text.length();
            builder.setSpan(new ForegroundColorSpan(Res.color(colorId)),start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void append(CharSequence text, int colorId, int fontSize) {
        if (TextUtils.isEmpty(text)) {return;}
        builder.append(text);
        if (colorId != 0) {
            int end = builder.length();
            int start = end - text.length();


            /*
            XmlResourceParser xppcolor=getResources().getXml (R.color.color);
            try {
                csl= ColorStateList.createFromXml(getResources(),xppcolor);
            }catch(XmlPullParserException e){
                // TODO: handle exception
                e.printStackTrace();
            }catch(IOException e){
                // TODO: handle exception
                e.printStackTrace();
            }
            */

            int[] colors = new int[] { Res.color(colorId) };
            int[][] states = new int[1][];
            states[0] = new int[] {};
            ColorStateList colorList = new ColorStateList(states, colors);

            builder.setSpan(new TextAppearanceSpan("monospace", android.graphics.Typeface.BOLD_ITALIC, Density.sp2px(fontSize), colorList, colorList), start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void appendStyle(CharSequence text, int styleId) {
        if (TextUtils.isEmpty(text)) {return;}
        builder.append(text);
        if (styleId != 0) {
            int end = builder.length();
            int start = end - text.length();
            builder.setSpan(new TextAppearanceSpan(Res.context(),styleId),start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void appendDeleteLine(CharSequence text) {
        if (TextUtils.isEmpty(text)) {return;}
        builder.append(text);

        int end = builder.length();
        int start = end - text.length();
        builder.setSpan(new StrikethroughSpan(),start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void append(CharSequence text, Object what) {
        if (TextUtils.isEmpty(text)) {return;}
        builder.append(text);
        if (what != null) {
            int end = builder.length();
            int start = end - text.length();
            builder.setSpan(what, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public Spannable toSpannable() {return builder;}
}
