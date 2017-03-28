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
		// int cnt = 0;
		System.out.println("初始化Unit...");
		while (!checkValid()) {
			// System.out.println("kkkkkkkkkkkkkkkkkk" + cnt++);
			initLocation();
		}
		calculateCost();
	}

	public void initLocation() {
		Random random = new Random();
		for (int i = 0; i < size; i++) {
			int tmp = random.nextInt(range);
			int j = 0;
			while (j < i) {
				if (tmp == server_location[j]) {
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
		// int[] source = new int[server_location.size()];
		// for (int i = 0; i < server_location.size(); i++) {
		// source[i] = server_location.get(i);
		// }
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
		copy.server_location = this.server_location;
		return copy;
	}

}
