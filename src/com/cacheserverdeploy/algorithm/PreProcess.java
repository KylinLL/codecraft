package com.cacheserverdeploy.algorithm;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.filetool.main.Main;

public class PreProcess {

	public static int[] weight; // 所有节点的权重值，下标：节点编号，值：权重
	public static int[] elite; // 精英节点，值：节点编号，权重为前25%，NUM_NET >> 2
	public static int avg_weight;
	public static int num_elite;
	public static PriorityQueue<NetNode> queue;
	public static Set<Integer> consumer_servers = new HashSet<Integer>();

	public static void preProcessing() {
		num_elite = Main.NUM_NET >> 2;
		weight = new int[Main.NUM_NET];
		elite = new int[num_elite];
		queue = new PriorityQueue<NetNode>(num_elite + 1);
		for (int i = 0; i < Main.NUM_CONSUMER; i++) {
			consumer_servers.add(Main.CONSUMER[i][0]);
		}
		calculateWeight();
	}

	private static void calculateWeight() {

		int sum = 0;
		for (int i = 0; i < Main.NUM_NET; i++) {
			// weight[i] = 100 * getFlowSum(i) / (10 + getCostSum(i));
			weight[i] = BFSProcess(i);
			sum += weight[i];
			NetNode node = new NetNode(i, weight[i], 0, 1);
			queue.add(node);
			if (queue.size() > num_elite)
				queue.remove();
		}
		avg_weight = sum / Main.NUM_NET;
		int index = 0;
		for (NetNode node : queue) {
			elite[index++] = node.id;
		}
		for (int i : weight)
			System.out.print(i + " ");
		System.out.println();
	}

	private static int BFSProcess(int s) {
		Queue<NetNode> que = new ArrayDeque<NetNode>();
		boolean[] inq = new boolean[Main.NUM_NET];
		// 这里把weight当作cost
		que.add(new NetNode(s, 0, Integer.MAX_VALUE, 1));
		inq[s] = true;
		int sum_flow = 0;
		if (consumer_servers.contains(s)) {
			sum_flow = Main.CONSUMER[Main.CONSUMER_MAP.get(s)][1];
		}
		while (!que.isEmpty()) {
			NetNode top = que.poll();
			for (int i = 0; i < Main.NUM_NET; i++) {
				if (top.id != i && Main.MATRIX_NETWORK[top.id][i] != 0 && !inq[i]) {
					que.add(new NetNode(i, top.weight + Main.MATRIX_COST[top.id][i], Main.MATRIX_NETWORK[top.id][i],
							top.depth + 1));
					inq[i] = true;
					if (consumer_servers.contains(i)) {
						sum_flow += Math.min(top.flow, Main.MATRIX_NETWORK[top.id][i]) / (top.depth + top.weight);
					}
				}
			}
		}
		return sum_flow;
	}

	@SuppressWarnings("unused")
	private static int getFlowSum(int point) {
		int sum = 0;
		for (int i = 0; i < Main.NUM_NET; i++)
			sum += Main.MATRIX_NETWORK[point][i];
		for (int i = 0; i < Main.NUM_NET; i++)
			sum += Main.MATRIX_NETWORK[i][point];
		return sum;
	}

	@SuppressWarnings("unused")
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
		int flow;
		int depth;

		public NetNode(int id, int weight, int flow, int depth) {
			this.id = id;
			this.weight = weight;
			this.flow = flow;
			this.depth = depth;
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
