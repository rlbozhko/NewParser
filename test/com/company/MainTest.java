package com.company;

import com.company.entities.Item;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.HashSet;
import java.util.Set;

public class MainTest {
    public String urLL1, minPrice, maxPrice;
    private Set<Item> cacheItems;

    @Before
    public void setUp() throws Exception {
        //SSD
        // urLL1 = "http://hard.rozetka.com.ua/ssd/c80109/";
        // гладильные доски
        // urLL1 = "http://rozetka.com.ua/pressboards/c185692/";

        // urLL1 = "http://rozetka.com.ua/svarochnoe-oborudovanie/c152563/";
        urLL1 = "http://hard.rozetka.com.ua/ssd/c80109/";

        minPrice = "1000";
        maxPrice = "2000";
        cacheItems = new HashSet<>();

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void ParseSortPrice() throws ParserConfigurationException, XPatherException, XPathExpressionException {
        int page = 0;
        Parser mainPage;
        TagNode blockWithGoods;
        do {
            page++;
            String sortedUrl = urLL1 + "page=" + page + ";" + "price=" + minPrice.trim() + "-" + maxPrice.trim() + "/";
            System.out.println(sortedUrl);


            mainPage = new Parser(sortedUrl);

            blockWithGoods = mainPage.findOneNode("//*[@id='block_with_goods']/div[1]");
            if (blockWithGoods != null) {
                //     TagNode[] goods = mainPage.findAllNodes("//a[contains(@onclick,'goodsTitleClick')]", blockWithGoods);

                NodeList nodes = (NodeList) mainPage.jaxp("//a[contains(@onclick,'goodsTitleClick')]", XPathConstants.NODESET);
                TagNode[] prices = mainPage.findAllNodes("//div[@class='g-price-uah']", blockWithGoods);
                String name;
                String price;


                for (int i = 0; i < nodes.getLength(); i++) {
                    name = (nodes.item(i).getTextContent()).trim();
                    price = mainPage.findText("/text()", prices[i]).trim().replaceAll("&thinsp;", "");

                    System.out.println(name);
                    System.out.println(price);
                    cacheItems.add(new Item(name, price));
                }
            }
        }
        // Продолжать цикл если на странице есть //div[@name="more_goods"]
        while (blockWithGoods != null && mainPage.findOneNode("//div[@name=\"more_goods\"]", blockWithGoods) != null);
        //  while (blockWithGoods != null && (Boolean) mainPage.jaxp("//*[@id='block_with_goods']/div[1]//div[@name=\"more_goods\"]", XPathConstants.BOOLEAN));
    }
}
