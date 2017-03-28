package com.cacheserverdeploy.deploy;

import java.util.Random;

import com.cacheserverdeploy.push.NetFlow.Solution;
import com.filetool.main.Main;

public class Unit {

	int cost;
	int size; // 需要随机的服务器个数
	int range; // 随机范围，即网络节点数
	int[] server_location;
	Solution solution;

	public void init() {
		size = Main.NUM_CONSUMER - 1;
		range = Main.NUM_NET;
		server_location = new int[size];
		initLocation();
		System.out.println("初始化Unit...");
		while (!checkValid()) {
			initLocation();
		}
		calculateCost();
	}

	public void initLocation() {
		Random random = new Random();
		for (int i = 0; i < size; i++) {
			int tmp = random.nextInt(range);
			// 如果随机出来的位置权重小于平均值，则再随机一次，仅一次
			if (Deploy.weight[tmp] < Deploy.avg_weight)
				tmp = random.nextInt(range);
			int j = 0;
			while (j < i) {
				if (tmp == server_location[j]) {
					tmp = random.nextInt(range);
					if (Deploy.weight[tmp] < Deploy.avg_weight)
						tmp = random.nextInt(range);
					j = 0;
				} else {
					j++;
				}
			}
			server_location[i] = tmp;
		}
	}

	public boolean checkValid() {
		Deploy.flow.newServers(server_location);
		return Deploy.flow.meetDemands();
	}

	public void calculateCost() {
		solution = Deploy.flow.getSolution();
		cost = solution.getCost();
		System.out.println("Cost: " + cost);
	}

	public void printUnit(int n) {
		System.out.println(n);
		for (int location : server_location) {
			System.out.print(location + " ");
		}
		System.out.println();
	}

	public Unit deepCopy() {
		Unit copy = new Unit();
		copy.cost = this.cost;
		copy.size = this.size;
		copy.range = this.range;
		copy.server_location = this.server_location.clone();
		return copy;
	}

}
