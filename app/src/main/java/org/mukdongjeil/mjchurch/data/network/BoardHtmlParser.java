package org.mukdongjeil.mjchurch.data.network;

import android.text.TextUtils;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.data.database.entity.BoardEntity;
import org.mukdongjeil.mjchurch.data.database.entity.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;

public class BoardHtmlParser {

    private static final String TAG = BoardHtmlParser.class.getSimpleName();

    private static final String CONTENT_CLASS = "contents bbs_list";
    private static final String LIST_LINK_CLASS = "bbs_list_ul";

    private static final String ATTRIBUTE_HREF = "href";
    private static final String ELEMENT_WRITER = "bbs_writer";
    private static final String ELEMENT_DATE = "bbs_date";
    private static final String ELEMENT_VIEW_COUNT = "bbs_count";
    private static final String ELEMENT_SUBSTANCE_P = "bbs_substance_p";

    private static BoardEntity fromHtml(final String bbsNo, final Element element) {
        int viewCount;
        String writer;
        String date;
        String content;

        Element writerElement = element.getFirstElementByClass(ELEMENT_WRITER);
        writer = writerElement.getTextExtractor().toString();
        Element dateElement = element.getFirstElementByClass(ELEMENT_DATE);
        date = dateElement.getTextExtractor().toString();
        Element viewCountElement = element.getFirstElementByClass(ELEMENT_VIEW_COUNT);
        viewCount = Integer.parseInt(viewCountElement.getTextExtractor().toString());
        Element contentElement = element.getFirstElementByClass(ELEMENT_SUBSTANCE_P);
        content = contentElement.getTextExtractor().toString();

        User user = new User(null, writer, null);
        long timeMillis = convertTimeMillis(date);
        return new BoardEntity(bbsNo, user, timeMillis, 0, 0, viewCount, content);
    }

    private static long convertTimeMillis(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateStr);

            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Nullable
    public BoardEntity parse(final String bbsNo, final String html) {
        Source source = new Source(html);
        Element contentElement = source.getFirstElementByClass(CONTENT_CLASS);
        if (contentElement == null) {
            return null;
        }

        return fromHtml(bbsNo, contentElement);
    }

    @Nullable
    public List<String> parseLinkList(final String html) {
        Source source = new Source(html);
        Element contentElement = source.getFirstElementByClass(LIST_LINK_CLASS);
        if (contentElement == null) {
            return null;
        }

        List<String> detailLinkList = new ArrayList<>();
        for (Element elem : contentElement.getAllElements(HTMLElementName.A)) {
            String linkAttr = elem.getAttributeValue(ATTRIBUTE_HREF);
            if (TextUtils.isEmpty(linkAttr) == false) {
                detailLinkList.add(linkAttr);
            }
        }

        return detailLinkList;
    }
}