package com.cacheserverdeploy.deploy;

import java.util.Arrays;
import java.util.List;

import com.cacheserverdeploy.algorithm.Group;
import com.cacheserverdeploy.algorithm.NetFlow;
import com.cacheserverdeploy.algorithm.NetFlow.Builder;
import com.cacheserverdeploy.algorithm.NetFlow.Line;
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

		List<Line> lines = Main.BEST_UNIT.getSolution().getLines();
		String[] content = new String[Main.NUM_CONSUMER + 2];
		content[0] = String.valueOf(lines.size());
		content[1] = "";

		for (int i = 0; i < lines.size(); i++) {
			content[2 + i] = lines.get(i).toString();
		}
		System.out.println("Server count init: " + (Main.NUM_CONSUMER - 1));
		System.out.println("Server count: " + Main.BEST_UNIT.getSize());
		System.out.println(Arrays.toString(flow.getSources()));
		return content;
	}

	private static void calculateWeight() {
		weight = new int[Main.NUM_NET];
		int sum = 0;
		for (int i = 0; i < Main.NUM_NET; i++) {
			weight[i] = 1000 * getFlowSum(i) / (100 + getCostSum(i));
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
