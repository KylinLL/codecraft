package com.cacheserverdeploy.algorithm;

import java.util.Random;

import com.cacheserverdeploy.algorithm.NetFlow.Solution;
import com.cacheserverdeploy.deploy.Deploy;
import com.filetool.main.Main;

public class Unit implements Comparable<Unit>, Cloneable {
	private static final Random random = new Random();
	private int size; // 需要随机的服务器个数
	private int[] serverLocation;
	private Solution solution;

	public Unit() {
		size = Main.NUM_CONSUMER - 1;
		serverLocation = new int[size];
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int[] getServerLocation() {
		return serverLocation;
	}

	public void setServerLocation(int[] server_location) {
		this.serverLocation = server_location;
	}

	public boolean isValid() {
		return Deploy.flow.newServers(serverLocation).meetDemands();
	}

	public int getCost() {
		return solution == null ? Main.MAX_INT : solution.getCost();
	}

	public Solution getSolution() {
		return solution;
	}

	public void setSolution(Solution solution) {
		this.solution = solution;
	}

	public void fillSolution() {
		setSolution(Deploy.flow.getSolution());
	}

	@Override
	public int compareTo(Unit o) {
		return this.getCost() - o.getCost();
	}

	/*
	 * 拷贝函数，注意solution為null。
	 */
	@Override
	public Unit clone() {
		Unit ret = null;
		try {
			ret = (Unit) super.clone();
			ret.setSolution(null);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static Unit newRandomUnit(int range) {
		System.out.println("初始化Unit...");
		Unit ret = new Unit();
		initServerLocation(ret, range);
		while (!ret.isValid()) {
			initServerLocation(ret, range);
		}
		ret.fillSolution();
		return ret;
	}

	public static void initServerLocation(Unit unit, int range) {
		int[] server_location = unit.getServerLocation();
		for (int i = 0; i < unit.getSize(); i++) {
			int tmp = random.nextInt(range);
			// 如果随机出来的位置权重小于平均值，则再随机一次，仅一次
//			if (Deploy.weight[tmp] < Deploy.avg_weight)
//				tmp = random.nextInt(range);
			int j = 0;
			while (j < i) {
				if (tmp == server_location[j]) {
					tmp = random.nextInt(range);
//					if (Deploy.weight[tmp] < Deploy.avg_weight)
//						tmp = random.nextInt(range);
					j = 0;
				} else {
					j++;
				}
			}
			server_location[i] = tmp;
		}
	}

}