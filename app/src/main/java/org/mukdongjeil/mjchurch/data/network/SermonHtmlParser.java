package org.mukdongjeil.mjchurch.data.network;

import android.text.TextUtils;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class SermonHtmlParser {

    private static final String TAG = SermonHtmlParser.class.getSimpleName();

    private static final String LIST_CONTENT_CLASS = "contents bbs_list";
    private static final String LIST_LINK_CLASS = "list_link";

    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_SRC = "src";

    private static final String ELEMENT_TITLE = "bbs_ttl";
    private static final String ELEMENT_WRITER = "bbs_writer";
    private static final String ELEMENT_DATE = "bbs_date";
    private static final String ELEMENT_VIEW_COUNT = "bbs_count";
    private static final String ELEMENT_SUBSTANCE_P = "bbs_substance_p";

    private static SermonEntity fromHtml(final String bbsNo, final Element element) {
        int sermonType = 0;
        int viewCount;
        String title;
        String writer;
        String date;
        String content;
        String videoUrl;

        Element titleElement = element.getFirstElementByClass(ELEMENT_TITLE);
        title = titleElement.getTextExtractor().toString();
        Element writerElement = element.getFirstElementByClass(ELEMENT_WRITER);
        writer = writerElement.getTextExtractor().toString();
        Element dateElement = element.getFirstElementByClass(ELEMENT_DATE);
        date = dateElement.getTextExtractor().toString();
        Element viewCountElement = element.getFirstElementByClass(ELEMENT_VIEW_COUNT);
        viewCount = Integer.parseInt(viewCountElement.getTextExtractor().toString());

        Element temp = element.getFirstElementByClass(ELEMENT_SUBSTANCE_P);
        Element iframe = temp.getFirstElement(HTMLElementName.IFRAME);
        videoUrl = iframe != null ? iframe.getAttributeValue(ATTRIBUTE_SRC) : null;

        List<Element> contentElementList = temp.getAllElements(HTMLElementName.DIV);
        StringBuffer contentBuffer = new StringBuffer();
        for (Element elem : contentElementList) {
            contentBuffer.append(elem.getTextExtractor().toString()).append("\n");
        }

        content = contentBuffer.toString();

        return new SermonEntity(Integer.parseInt(bbsNo), sermonType, title, writer, date, viewCount,
                content, videoUrl);
    }

    @Nullable
    SermonEntity parse(final String bbsNo, final String html) {
        Source source = new Source(html);
        Element contentElement = source.getFirstElementByClass(LIST_CONTENT_CLASS);
        if (contentElement == null) {
            return null;
        }

        return fromHtml(bbsNo, contentElement);
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