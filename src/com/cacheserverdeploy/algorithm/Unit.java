package com.cacheserverdeploy.algorithm;

import java.util.Random;
import java.util.Set;

import com.cacheserverdeploy.algorithm.NetFlow.Solution;
import com.cacheserverdeploy.deploy.Deploy;
import com.filetool.main.Main;

public class Unit implements Comparable<Unit>, Cloneable {
	private static final Random random = new Random();
	// private int size; // 需要随机的服务器个数
	private int[] serverLocation;
	private Solution solution;

	public Unit() {
		int size = Main.NUM_CONSUMER - 1;
		serverLocation = new int[size];
	}

	public int getSize() {
		return serverLocation.length;
	}

	// public void setSize(int size) {
	// this.size = size;
	// }

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

		if (solution != null && solution.getRemain() != null && !solution.getRemain().isEmpty()) {
			Set<Integer> remain = solution.getRemain();
			int size = remain.size();
			int[] newSource = new int[serverLocation.length - size];
			int i = 0;
			for (int location : serverLocation) {
				if (!remain.contains(location)) {
					newSource[i++] = location;
				}
			}
			serverLocation = newSource;
		}
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
//		System.out.println("初始化Unit...");
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
			int tmp = randomLocation(unit, i, range);
			int j = 0;
			while (j < i) {
				if (tmp == server_location[j]) {
					tmp = randomLocation(unit, i, range);
					j = 0;
				} else {
					j++;
				}
			}
			server_location[i] = tmp;
		}
	}

	private static int randomLocation(Unit unit, int index, int range) {
		if (index < unit.getSize() >> 2) {
			int i = random.nextInt(PreProcess.num_elite);
			return PreProcess.elite[i];
		}
		int location = random.nextInt(range);
		// 如果随机出来的位置权重小于平均值，则再随机一次，仅一次
		if (PreProcess.weight[location] < PreProcess.avg_weight)
			location = random.nextInt(range);
		return location;
	}

}