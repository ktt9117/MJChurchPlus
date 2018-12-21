package org.mukdongjeil.mjchurch.data.network;

import android.text.TextUtils;
import android.util.Log;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.data.database.entity.TrainingEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class TrainingHtmlParser {
    private static final String TAG = TrainingHtmlParser.class.getSimpleName();

    private static final String MENU_LIST = "menu-left";
    private static final String TITLE_CLASS_NAME = "h2_box";
    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_SRC = "src";
    private static final String IMAGE_CONTAINER_CLASS_NAME = "expln";

    @Nullable
    public TrainingEntity parse(final String html) {
        Source source = new Source(html);
        try {
            Element contentElement = source.getFirstElementByClass(TITLE_CLASS_NAME);
            Element titleElement = contentElement.getFirstElement(HTMLElementName.H2);
            Element imgContainerElement = source.getFirstElementByClass(IMAGE_CONTAINER_CLASS_NAME);
            Element imgElement = imgContainerElement.getFirstElement(HTMLElementName.IMG);

            String title = titleElement.getTextExtractor().toString();
            String imgSrc = imgElement.getAttributeValue(ATTRIBUTE_SRC);
            if (TextUtils.isEmpty(imgSrc) == false) {
                return new TrainingEntity(title, NetworkUtils.BASE_URL + imgSrc);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "introduce entity parsing error! : " + e.getMessage());
        }

        return null;
    }

    @Nullable
    List<String> parseLinkList(final String html) {
        Source source = new Source(html);
        Element contentElement = source.getElementById(MENU_LIST);
        Log.i(TAG, "contentElement : " + contentElement);
        if (contentElement == null) {
            Log.e(TAG, "contentElement is null");
            return null;
        }

        List<String> list = new ArrayList<>();
        for (Element elem : contentElement.getAllElements(HTMLElementName.A)) {
            Log.i(TAG, "menu element : " + elem.toString());
            String linkAttr = elem.getAttributeValue(ATTRIBUTE_HREF);
            if (TextUtils.isEmpty(linkAttr) == false) {
                list.add(linkAttr);
            }
        }

        return list;
    }
}
