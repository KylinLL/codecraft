package com.cacheserverdeploy.algorithm;

import java.util.PriorityQueue;

import com.filetool.main.Main;

public class PreProcess {

	public static int[] weight; // 所有节点的权重值，下标：节点编号，值：权重
	public static int[] elite; // 精英节点，值：节点编号，权重为前25%，NUM_NET >> 2
	public static int avg_weight;
	public static int num_elite;
	public static PriorityQueue<NetNode> queue;

	public static void preProcessing() {
		num_elite = Main.NUM_NET >> 2;
		weight = new int[Main.NUM_NET];
		elite = new int[num_elite];
		queue = new PriorityQueue<NetNode>(num_elite + 1);
		calculateWeight();
	}

	private static void calculateWeight() {

		int sum = 0;
		for (int i = 0; i < Main.NUM_NET; i++) {
			weight[i] = 100 * getFlowSum(i) / (10 + getCostSum(i));
			sum += weight[i];
			NetNode node = new NetNode(i, weight[i]);
			queue.add(node);
			if (queue.size() > num_elite)
				queue.remove();
		}
		avg_weight = sum / Main.NUM_NET;
		int index = 0;
		for (NetNode node : queue) {
			elite[index++] = node.id;
		}
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

	private static class NetNode implements Comparable<NetNode> {
		int id;
		int weight;

		public NetNode(int id, int weight) {
			this.id = id;
			this.weight = weight;
		}

		@Override
		public int compareTo(NetNode o) {
			return this.weight - o.weight;
		}

		@Override
		public String toString() {
			return "[" + id + " " + weight + "]";
		}
	}

}
