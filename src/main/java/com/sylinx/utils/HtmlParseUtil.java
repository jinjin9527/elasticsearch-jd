package com.sylinx.utils;

import com.sylinx.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParseUtil {

    public static void main(String[] args) throws IOException {

        HtmlParseUtil htmlParseUtil = new HtmlParseUtil();
        htmlParseUtil.parseJD("心理学").forEach(System.out::println);
    }

    public ArrayList<Content> parseJD(String keywords) throws IOException {
        String url = "https://search.jd.com/Search?keyword=" + keywords;

        Document document = Jsoup.parse(new URL(url), 30000);

        Element element = document.getElementById("J_goodsList");
        Elements goods_elements = element.getElementsByTag("li");
        ArrayList<Content> resultList = new ArrayList<>();
        for(Element e : goods_elements){
            String img = e.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = e.getElementsByClass("p-price").eq(0).text();
            String title = e.getElementsByClass("p-name").eq(0).text();

            Content content = new Content();
            content.setImg(img);
            content.setPrice(price);
            content.setTitle(title);
            resultList.add(content);
        }
        return resultList;
    }
}
