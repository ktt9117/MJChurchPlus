package org.mukdongjeil.mjchurch.data.network;

import android.text.TextUtils;
import android.util.Log;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class SermonHtmlParser {

    private static final String TAG = SermonHtmlParser.class.getSimpleName();

    private static final String LIST_CONTENT_CLASS = "post-group";
    private static final String LIST_LINK_CLASS = "post-item-link";
    private static final String POST_HEADER_CLASS = "post-header";
    private static final String POST_BODY_CLASS = "post-body";

    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_SRC = "src";

    private static final String ELEMENT_TITLE = "post-title";
    private static final String ELEMENT_WRITER = "post-author";
    private static final String ELEMENT_DATE = "post-date";
    private static final String ELEMENT_VIEW_COUNT = "post-view";
    private static final String ELEMENT_FORMATTED_USER_INPUT = "formatted-user-input";

    private static SermonEntity fromHtml(final String bbsNo, final Element header, final Element body) {
        int sermonType = 0;
        int viewCount;
        String title;
        String writer;
        String date;
        String content;
        String videoUrl;

        Element titleElement = header.getFirstElementByClass(ELEMENT_TITLE);
        title = titleElement.getTextExtractor().toString();
        Element writerElement = header.getFirstElementByClass(ELEMENT_WRITER);
        writer = writerElement.getTextExtractor().toString();
        Element dateElement = header.getFirstElementByClass(ELEMENT_DATE);
        date = dateElement.getTextExtractor().toString();
        Element viewCountElement = header.getFirstElementByClass(ELEMENT_VIEW_COUNT);
        viewCount = Integer.parseInt(viewCountElement.getTextExtractor().toString());

        Element contentElement = body.getFirstElementByClass(ELEMENT_FORMATTED_USER_INPUT);
        Element iFrame = contentElement.getFirstElement(HTMLElementName.IFRAME);
        videoUrl = iFrame != null ? iFrame.getAttributeValue(ATTRIBUTE_SRC) : null;
        content = contentElement.getTextExtractor().toString();

        return new SermonEntity(Integer.parseInt(bbsNo), sermonType, title, writer, date, viewCount,
                content, videoUrl);
    }

    @Nullable
    SermonEntity parse(final String bbsNo, final String html) {
        Source source = new Source(html);
        Element headerElement = source.getFirstElementByClass(POST_HEADER_CLASS);
        Element bodyElement = source.getFirstElementByClass(POST_BODY_CLASS);
        if (headerElement == null || bodyElement == null) {
            Log.e(TAG, "headerElement or bodyElement is null");
            return null;
        }

        return fromHtml(bbsNo, headerElement, bodyElement);
    }

    @Nullable
    List<String> parseLinkList(final String html) {
        Source source = new Source(html);
        Element contentElement = source.getFirstElementByClass(LIST_CONTENT_CLASS);
        if (contentElement == null) {
            return null;
        }

        List<String> detailLinkList = new ArrayList<>();
        List<Element> linkList = contentElement.getAllElementsByClass(LIST_LINK_CLASS);
        for (Element link : linkList) {
            String linkAttr = link.getAttributeValue(ATTRIBUTE_HREF);
            if (!TextUtils.isEmpty(linkAttr)) {
                detailLinkList.add(linkAttr);
            }
        }

        return detailLinkList;
    }
}