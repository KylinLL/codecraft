package com.filetool.main;

import java.io.File;
import java.util.Scanner;

import com.cacheserverdeploy.algorithm.Unit;
import com.cacheserverdeploy.deploy.Deploy;
import com.filetool.util.FileUtil;
import com.filetool.util.LogUtil;

/**
 * 
 * 工具入口
 * 
 * @version [版本号, 2017-1-9]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class Main {
    public static final int MAX_INT = Integer.MAX_VALUE >> 1;
    public static final int MAX_NODES = 1002;
    public static final int MAX_COMSUMERS_NODES = 501;
    public static int NUM_NET; // 网络节点
    public static int NUM_PATH; // 链路条数
    public static int NUM_CONSUMER; // 消费节点
    public static int PRICE_PER_SERVER; // 服务器部署成本
    public static int[][] MATRIX_NETWORK = new int[MAX_NODES][MAX_NODES]; // 带宽,capacity
    public static int[][] MATRIX_COST = new int[MAX_NODES][MAX_NODES]; // 带宽价格,price
    public static int[][] CONSUMER = new int[MAX_COMSUMERS_NODES][2]; // sink
    public static Unit BEST_UNIT;

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("please input args: graphFilePath, resultFilePath");
            return;
        }

        String graphFilePath = args[0];
        String resultFilePath = args[1];

        LogUtil.printLog("Begin");

        // 读取输入文件
        String[] graphContent = FileUtil.read(graphFilePath, null);
        read(graphFilePath);

        // 功能实现入口
        String[] resultContents = Deploy.deployServer(graphContent);

        // 写入输出文件
        if (hasResults(resultContents)) {
            FileUtil.write(resultFilePath, resultContents, false);
        } else {
            FileUtil.write(resultFilePath, new String[] { "NA" }, false);
        }
        LogUtil.printLog("End");
    }

    private static boolean hasResults(String[] resultContents) {
        if (resultContents == null) {
            return false;
        }
        for (String contents : resultContents) {
            if (contents != null && !contents.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static void read(String pathname) throws Exception {
        Scanner scan = new Scanner(new File(pathname));
        NUM_NET = scan.nextInt();
        NUM_PATH = scan.nextInt();
        NUM_CONSUMER = scan.nextInt();
        PRICE_PER_SERVER = scan.nextInt();
        // 初始解
        int MIN_COST = NUM_CONSUMER * PRICE_PER_SERVER;
        BEST_UNIT = new Unit();
        // TODO init best unit
        // BEST_UNIT = new Solution(MIN_COST, new ArrayList<Line>());

        for (int i = 0; i < NUM_PATH; i++) {
            int start = scan.nextInt();
            int end = scan.nextInt();
            int bandwidth = scan.nextInt();
            int cost = scan.nextInt();
            MATRIX_NETWORK[start][end] = bandwidth;
            MATRIX_NETWORK[end][start] = bandwidth;
            MATRIX_COST[start][end] = cost;
            MATRIX_COST[end][start] = cost;
        }
        for (int i = 0; i < NUM_CONSUMER; i++) {
            int id_consumer = scan.nextInt();
            int id_network = scan.nextInt();
            int bandwidth_need = scan.nextInt();
            CONSUMER[id_consumer][0] = id_network;
            CONSUMER[id_consumer][1] = bandwidth_need;
        }
        scan.close();
    }

}
