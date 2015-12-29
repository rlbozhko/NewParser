package com.company;

import com.company.entities.Item;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedTransferQueue;


public class ThreadWorker implements Callable<Set<Item>> {

    private String url;
    String minPrice;
    String maxPrice;
    Set<Item> threadCacheItems;
    private LinkedTransferQueue<Item> linkedTransferQueue;

    public ThreadWorker(String url, String minPrice, String maxPrice, LinkedTransferQueue<Item> linkedTransferQueue) {
        this.url = url;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.threadCacheItems = new HashSet<>();
        this.linkedTransferQueue = linkedTransferQueue;
    }

    public Set<Item> call() throws Exception {
        //System.out.println("ThreadWorker");
        parseSortPrice(url, minPrice, maxPrice);
        return new HashSet<Item>(threadCacheItems);
    }

    public void parseSortPrice(String url, String minPrice, String maxPrice) throws ParserConfigurationException, XPatherException, XPathExpressionException, InterruptedException {
        int page = 0;
        Parser mainPage;
        TagNode blockWithGoods;
        String name;
        String price;

        threadCacheItems.clear();
        do {
            page++;
            String sortedUrl = url + "page=" + page + ";" + "price=" + minPrice.trim() + "-" + maxPrice.trim() + "/";
            mainPage = new Parser(sortedUrl);
            if (mainPage.getDom() == null) {
                blockWithGoods = null;
            } else {
                blockWithGoods = mainPage.findOneNode("//*[@id='block_with_goods']/div[1]");
                if (blockWithGoods != null) {
                    //TagNode[] goods = mainPage.findAllNodes("//div[@class="g-i-tile-i-title clearfix"]/a/text()", blockWithGoods);
                    NodeList nodes = (NodeList) mainPage.jaxp("//a[contains(@onclick,'goodsTitleClick')]", XPathConstants.NODESET);
                    TagNode[] prices = mainPage.findAllNodes("//div[@class='g-price-uah']", blockWithGoods);

                    for (int i = 0; i < nodes.getLength(); i++) {
                        name = (nodes.item(i).getTextContent()).trim().replaceAll("\n", "");
                        price = mainPage.findText("/text()", prices[i]).trim().replaceAll("&thinsp;", "").replaceAll("\n", "");
                        threadCacheItems.add(new Item(name, price));
                        if (linkedTransferQueue.size() < 95) {
                            linkedTransferQueue.add(new Item(name, price));
                        } else {
                            linkedTransferQueue.transfer(new Item(name, price));
                        }
                    }
                }
            }
        }
        // Продолжать цикл если на странице есть //div[@name="more_goods"]
        while (blockWithGoods != null && mainPage.findOneNode("//div[@name=\"more_goods\"]", blockWithGoods) != null);
        //  while (blockWithGoods != null && (Boolean) mainPage.jaxp("//*[@id='block_with_goods']/div[1]//div[@name=\"more_goods\"]", XPathConstants.BOOLEAN));
    }
}
