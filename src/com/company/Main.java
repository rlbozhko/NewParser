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
        //TODO подключи логгер
        //TODO OOP
        String d = new Date(System.currentTimeMillis()).toString();
        final LinkedTransferQueue<Item> transferQueue = new LinkedTransferQueue<>();
        final Set<String> newUrls = new HashSet<>();
        final Set<String> oldUrls = new HashSet<>();
        final Set<Item> mainCacheItems = new HashSet<>();
        final List<String> badUrls = new ArrayList<>();
        final Set<String> cacheUrls = new HashSet<>();
        boolean flagCreateQueueConsumer = true;
        boolean bContinue = true;
        Arguments arguments = new Arguments(args);
        ThreadConsumer queueConsumer = new ThreadConsumer(transferQueue);
        Thread threadQueueConsumer = new Thread(queueConsumer);
        ExecutorService service = Executors.newFixedThreadPool(30);
        List<Future<Set<Item>>> futures = new ArrayList<>();
        Parser browsePage;
        int countReloadsTry = 0;
        Future<Set<Item>> future;

        if (checkArguments(arguments)) return;
        //newUrls.add("http://rozetka.com.ua/computers-notebooks/c80253/");
        //newUrls.add("http://google.com/");
        newUrls.add(arguments.getArg(0));
        threadQueueConsumer.start();

        while (bContinue || newUrls.size() > 0 || cacheUrls.size() > 0) {

            newUrls.addAll(cacheUrls);
            newUrls.removeAll(oldUrls);
            cacheUrls.clear();

            for (String urlBrowse : newUrls) {

                if (oldUrls.add(urlBrowse)) {
                    while (!mainContinue) {
                        Thread.sleep(2000);
                    }
                    //сейчас перед новым годом розетка перегружена(либо уже начала бороться с этим парсером)
                    // и ИНОГДА! не грузит норм. страницы с первого раза
                    //если не загружается главная-начальная страница то программа просто завершится
                    //Из-за периодичности ошибки checkAndReload не протестирован так что может и не помогает
                    browsePage = new Parser(urlBrowse).checkAndReload(5);

                    if (browsePage.getDom() == null) {
                        badUrls.add(browsePage.getUrl());
                    } else {
                        if ((Boolean) browsePage.jaxp("//*[@id=\"sort_price\"]", XPathConstants.BOOLEAN)) {
                            //TODO cacheItems.addALL(parseSortPrice(browsePa......
                            future =
                                    service.submit(new ThreadWorker(browsePage.getUrl(), arguments.getArg(1), arguments.getArg(2), transferQueue));
                            futures.add(future);

                            for (Future<Set<Item>> f : futures) {
                                if (f.isDone()) {
                                    mainCacheItems.addAll(f.get());
                                }
                            }
                        } else {
                            // TODO cacheUrls.addALL(getNewLinks(Parser browsePage));
                            // cacheUrls = Set<String> getNewLinks(Parser browsePage);
                            getNewLinks(cacheUrls, browsePage);
                        }
                    }
                }
            }
            bContinue = false;
            for (Future<Set<Item>> f : futures) {
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
        threadQueueConsumer.join();

        service.shutdown();
        if (oldUrls.size() == 1) {
            System.out.println("Сайт Rozetka перегружен попробуйте через несколько минут");
        }

        System.out.println(d);
        System.out.println(new Date(System.currentTimeMillis()).toString());

    }

    private static boolean checkArguments(Arguments arguments) {
        if (!arguments.isValidArguments()) {
            System.out.println(" Неправильные аргументы. Необходимые аргументы:");
            System.out.println(" ссылка на сайт: http://rozetka.com.ua/");
            System.out.println(" цена от(целое число): 1000");
            System.out.println(" цена до(целое число): 1100");
            System.out.println(" Пример аргументов: http://rozetka.com.ua/ 1000 1100");
            return true;
        }
        return false;
    }

    private static void getNewLinks(Set<String> cacheUrls, Parser browsePage) throws XPathExpressionException {
        //System.out.println("Новые линки");
        NodeList nodes = (NodeList) browsePage.jaxp("//a[contains(@href,'rozetka.com.ua')]/@href", XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            String href = (nodes.item(i).getNodeValue());
            if (ROZETKA_CATEGORY.matcher(href).matches()) {
                cacheUrls.add(href);
            }
        }
    }


}

