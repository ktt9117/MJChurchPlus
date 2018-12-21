package org.mukdongjeil.mjchurch;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        try {
//            URL welcomeUrl = NetworkUtils.getWelcomeUrl();
//            String html = NetworkUtils.getResponseFromHttpUrl(welcomeUrl);
//            IntroduceHtmlParser parser = new IntroduceHtmlParser();
//            List<IntroduceEntity> sermonList = parser.parse(html);

//            List<String> detailLinkList = parser.parseLinkList(html);
//            if (detailLinkList != null && detailLinkList.size() > 0) {
//                for (String detailLink : detailLinkList) {
//                    String bbsNo = detailLink.substring(detailLink.lastIndexOf("=") + 1);
//                    URL sermonDetailUrl = NetworkUtils.makeCompleteUrl(detailLink);
//                    String detailHtml = NetworkUtils.getResponseFromHttpUrl(sermonDetailUrl);
//
//                    SermonEntity entity = parser.parse(bbsNo, detailHtml);
//                    System.out.println("Html Parsing finished");
//                    sermonList.add(entity);
//                }
//            }
        } catch (Exception e) {
           e.printStackTrace();
        }


        assertEquals(4, 2 + 2);


    }


}