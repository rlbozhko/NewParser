package com.company;

import com.company.entities.Item;

import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.LinkedTransferQueue;


public class ThreadConsumer implements Runnable {

    private LinkedTransferQueue<Item> linkedTransferQueue;

    public ThreadConsumer(LinkedTransferQueue<Item> linkedTransferQueue) {
        this.linkedTransferQueue = linkedTransferQueue;
    }

    @Override
    public void run() {
        try {
            //System.out.println("ThreadConsumer");
            doSmth();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doSmth() throws InterruptedException {
        //строка Thread.sleep(10000); не влияет на производительность т.к. поиск первых товаров
        // происходит около 10-ти секунд, однако это страховка что
        // linkedTransferQueue будет проинициализирована
        // Thread.sleep(10000);
        Scanner in = new Scanner(System.in);
        int i = 0;
        int k = 0;
        Item item = linkedTransferQueue.take();
        Item itemTerminator = new Item("FINAL", "STOP");

        while (!item.equals(itemTerminator)) {
            Main.mainContinue = true;
            i++;
            System.out.println((k++) + "\n" + item);
            item = linkedTransferQueue.take();
            if (i >= 100) {
                Main.mainContinue = false;
                System.out.println("\nContinue? Press 1 to Exit or any other number to continue");
                try {
                    if (in.nextInt() == 1) {
                        System.exit(0);
                    } else {
                        i = 0;
                    }
                } catch (InputMismatchException e) {
                    e.printStackTrace();
                    i = 0;
                    in.next();
                }
            }
        }

    }
}
