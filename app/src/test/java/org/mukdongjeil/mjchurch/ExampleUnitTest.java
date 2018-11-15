package org.mukdongjeil.mjchurch;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTagType;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {


        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        StringBuffer sb = new StringBuffer();
        try {
            URL url = getClass().getResource("test.txt");
            System.out.println("url path : " + url.getPath());
            File file = new File(url.getPath());

            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                sb.append(temp);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
                if (fileReader != null) fileReader.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        boolean bbs_substance = false;
        Source source = new Source(sb.toString());
        List<Element> elements = source.getAllElements();
        List<Element> bbsSubstanceElements = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            Element elem = elements.get(i);
            if (elem.getStartTag().getTagType() == StartTagType.COMMENT && elem.toString().equals("<!--S:bbs_substance-->")) {
                bbs_substance = true;
            } else if (elem.getStartTag().getTagType() == StartTagType.COMMENT && elem.toString().equals("<!--E:bbs_substance-->")) {
                bbs_substance = false;
            }

            if (bbs_substance && elem.getName().equals(HTMLElementName.DIV) && !elem.isEmptyElementTag()) {
                bbsSubstanceElements.add(elem);
            }
        }


        StringBuffer contents = new StringBuffer();
        for (int i = 0; i < bbsSubstanceElements.size(); i++) {
            Element elem = bbsSubstanceElements.get(i);
            contents.append(elem.getTextExtractor());
        }

        System.out.println(contents.toString());

        assertEquals(4, 2 + 2);


    }


}