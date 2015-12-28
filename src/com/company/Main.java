package com.company;

import com.company.entities.Item;
import org.htmlcleaner.XPatherException;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;


public class Main {

    volatile static Boolean mainContinue = true;
    private static final Pattern ROZETKA_CATEGORY = Pattern.compile(".*/c\\d*(/filter/|/)");

    public static void main(String[] args) throws IOException, XPatherException, ParserConfigurationException, XPathExpressionException, ExecutionException, InterruptedException {

        String d = new Date(System.currentTimeMillis()).toString();
        System.out.println(d);

        final LinkedTransferQueue<Item> transferQueue = new LinkedTransferQueue<>();

        final Set<String> newUrls = new HashSet<>();
        final Set<String> oldUrls = new HashSet<>();
        final Set<Item> mainCacheItems = new HashSet<>();
        final List<String> badUrls = new ArrayList<>();
        final Set<String> cacheUrls = new HashSet<>();

        boolean flagQueueConsumer = true;
        boolean bContinue = true;


        Arguments arguments = new Arguments(args);

        if (!arguments.isValidArguments()) {
            System.out.println(" Неправильные аргументы. Необходимые аргументы:");
            System.out.println(" ссылка на сайт: http://rozetka.com.ua/");
            System.out.println(" цена от(целое число): 1000");
            System.out.println(" цена до(целое число): 1100");
            System.out.println(" Пример аргументов: http://rozetka.com.ua/ 1000 1100");
            return;
        }

        newUrls.add(arguments.getArg(0));

        ExecutorService service = Executors.newFixedThreadPool(20);
        List<Future<Set<Item>>> futures = new ArrayList<>();

        while (bContinue || newUrls.size() > 0 || cacheUrls.size() > 0) {

            newUrls.addAll(cacheUrls);
            newUrls.removeAll(oldUrls);
            cacheUrls.clear();
            for (String urlBrowse : newUrls) {
                if (oldUrls.add(urlBrowse)) {
                    while (!mainContinue) {
                        Thread.sleep(1000);
                    }
                    Parser browsePage = new Parser(urlBrowse);
                    if (browsePage.getDom() == null) {
                        badUrls.add(browsePage.getUrl());
                    } else {
                        if ((Boolean) browsePage.jaxp("//*[@id=\"sort_price\"]", XPathConstants.BOOLEAN)) {
                            //TODO cacheItems.addALL(parseSortPrice(browsePa......
                            Future<Set<Item>> future =
                                    service.submit(new ThreadWorker(browsePage.getUrl(), arguments.getArg(1), arguments.getArg(2), transferQueue));
                            futures.add(future);

                            for (Future<Set<Item>> f : futures) {
                                bContinue = false;
                                if (f.isDone()) {
                                    mainCacheItems.addAll(f.get());
                                } else {
                                    bContinue = true;
                                }
                            }

                            if (flagQueueConsumer) {
                                ThreadConsumer queueConsumer1 = new ThreadConsumer(
                                        transferQueue);
                                new Thread(queueConsumer1).start();
                                flagQueueConsumer = false;
                            }

                        } else {
                            // TODO cacheUrls.addALL(getNewLinks(Parser browsePage));
                            // cacheUrls = Set<String> getNewLinks(Parser browsePage);
                            getNewLinks(cacheUrls, browsePage);
                        }
                    }
                }
            }

            for (Future<Set<Item>> f : futures) {
                bContinue = false;
                if (f.isDone()) {
                    mainCacheItems.addAll(f.get());
                } else {
                    bContinue = true;
                    if (newUrls.size() == 0 && cacheUrls.size() == 0) {
                        mainCacheItems.addAll(f.get());
                    }
                }
            }
        }
        transferQueue.transfer(new Item("FINAL", "STOP"));
        service.shutdown();
    }

    private static void getNewLinks(Set<String> cacheUrls, Parser browsePage) throws XPathExpressionException {
        NodeList nodes = (NodeList) browsePage.jaxp("//a[contains(@href,'rozetka.com.ua')]/@href", XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            String href = (nodes.item(i).getNodeValue());
            if (ROZETKA_CATEGORY.matcher(href).matches()) {
                cacheUrls.add(href);
            }
        }
    }


}
