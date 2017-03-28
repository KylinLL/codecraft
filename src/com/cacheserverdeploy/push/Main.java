package com.cacheserverdeploy.push;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.cacheserverdeploy.push.NetFlow.Builder;

// https://www.topcoder.com/community/data-science/data-science-tutorials/push-relabel-approach-to-the-maximum-flow-problem/
// http://blog.csdn.net/bbbbaai/article/details/46538383
// http://codecraft.huawei.com/home/detail
// http://blog.csdn.net/hechenghai/article/details/42719715
// http://www.cppblog.com/Icyflame/archive/2009/06/24/88448.html
public class Main {
    public static final int MAX_NODE = 10;
    private Scanner scan;
    private int[][] capacity, left, price;
    private int vlen, elen, start, end;

    public Main() {
        capacity = new int[MAX_NODE][MAX_NODE];
        left = new int[MAX_NODE][MAX_NODE];
        price = new int[MAX_NODE][MAX_NODE];
    }

    public int[][] getCapacity() {
        return capacity;
    }

    public int[][] getPrice() {
        return price;
    }

    public int getVlen() {
        return vlen;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void init() {
        String path = System.getProperty("user.dir") + "/case1";
        try {
            scan = new Scanner(new FileInputStream(new File(path)));
            vlen = scan.nextInt();
            elen = scan.nextInt();
            start = scan.nextInt();
            end = scan.nextInt();
            for (int i = 0; i < elen; i++) {
                int from = scan.nextInt();
                int to = scan.nextInt();
                capacity[from][to] = scan.nextInt();
                left[from][to] = capacity[from][to];
                price[from][to] = scan.nextInt();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            scan.close();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        Main pr = new Main();
        pr.init();
        int[][] consumers = { { 4, 12 }, { 5, 3 } };
        int[] sources = { 1, 3 };
        Builder builder = NetFlow.builder(pr.getVlen());
        NetFlow flow = builder.setCapacity(pr.getCapacity()).setPrice(pr.getPrice()).setConsumers(consumers, consumers.length).setServeCost(1)
                .getNetFlow();
        flow.newServers(sources);
        if (flow.meetDemands()) {
            System.out.println("find max flow!");
            System.out.println(flow.getSolution());
        } else {
            System.out.println("cannot find max flow!");
        }

        int[] sources2 = { 1 };
        flow.newServers(sources2);
        if (flow.meetDemands()) {
            System.out.println("find max flow!");
            System.out.println(flow.getSolution());
        } else {
            System.out.println("cannot find max flow!");
        }
    }
}
