package com.company;

import com.company.entities.Item;

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
            doSmth();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doSmth() throws InterruptedException {

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
                System.out.println("\nContinue? Press 1 to Exit or any other character to continue");
                if (in.nextInt() == 1) {
                    System.exit(0);
                } else {
                    i = 0;
                }
            }
        }

    }
}
