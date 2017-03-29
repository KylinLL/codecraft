package com.cacheserverdeploy.deploy;

import com.cacheserverdeploy.algorithm.Group;
import com.cacheserverdeploy.algorithm.NetFlow;
import com.cacheserverdeploy.algorithm.NetFlow.Builder;
import com.filetool.main.Main;

public class Deploy {

	public static NetFlow flow;
	public static int[] weight; // 所有节点的权重值，初始化时优先去权重高的节点选择
	public static int avg_weight;

    /**
     * 你需要完成的入口 <功能详细描述>
     * 
     * @param graphContent
     *            用例信息文件
     * @return [参数说明] 输出结果信息
     * @see [类、类#方法、类#成员]
     */
    public static String[] deployServer(String[] graphContent) {

        /** do your work here **/
        Builder builder = NetFlow.builder(Main.NUM_NET);
        flow = builder.setCapacity(Main.MATRIX_NETWORK).setPrice(Main.MATRIX_COST)
                .setConsumers(Main.CONSUMER, Main.NUM_CONSUMER).setServeCost(Main.PRICE_PER_SERVER).getNetFlow();

        calculateWeight();
        Group group = new Group();
        group.evolution();

		String[] res = new String[Main.NUM_CONSUMER + 2];
		res[0] = new Integer(Main.NUM_CONSUMER).toString();
		res[1] = "";
		for (int i = 0; i < Main.NUM_CONSUMER; i++) {
			StringBuilder s = new StringBuilder();
			s.append(Main.CONSUMER[i][0]).append(" ").append(i).append(" ").append(Main.CONSUMER[i][1]);
			res[2 + i] = s.toString();
		}
		// return new String[]{"17","\r\n","0 8 0 20"};
		return res;
	}

	private static void calculateWeight() {
		weight = new int[Main.NUM_NET];
		int sum = 0;
		for (int i = 0; i < Main.NUM_NET; i++) {
			weight[i] = 10 * getFlowSum(i) / (1 + getCostSum(i));
			sum += weight[i];
		}
		avg_weight = sum / Main.NUM_NET;
	}

	private static int getFlowSum(int point) {
		int sum = 0;
		for (int i = 0; i < Main.NUM_NET; i++)
			sum += Main.MATRIX_NETWORK[point][i];
		for (int i = 0; i < Main.NUM_NET; i++)
			sum += Main.MATRIX_NETWORK[i][point];
		return sum;
	}

	private static int getCostSum(int point) {
		int sum = 0;
		for (int i = 0; i < Main.NUM_NET; i++)
			sum += Main.MATRIX_COST[point][i];
		for (int i = 0; i < Main.NUM_NET; i++)
			sum += Main.MATRIX_COST[i][point];
		return sum;
	}

}
