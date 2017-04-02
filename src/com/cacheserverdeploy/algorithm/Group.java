package com.cacheserverdeploy.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.cacheserverdeploy.algorithm.NetFlow.Solution;
import com.filetool.main.Main;

public class Group {
	private static final Random random = new Random();
	public static int GROUP_SIZE = 50; // 群体规模，即群体中个体的数量，一般为20~100
	public static double CROSS_RATE = 0.8; // 交叉概率，一般为0.4~0.9
	public static double VARIATION_RATE = 0.2; // 变异概率，一般为0.001~0.1
	public static double SELECT_RATE = 0.6; // 选择时保留的比例
	public static int MAX_GENERATION = 256; // 最大代数，一般为100~500
	public static int MAX_NO_CHANGE_GEN = MAX_GENERATION >> 3; // 连续多少代没有收敛，则跳出

	private List<Unit> group = new ArrayList<Unit>();

	public Group() {
		long start = System.currentTimeMillis();
		System.out.println("compute init time...");
		Unit initU = new Unit(Main.NUM_CONSUMER);
		initU.setServerLocation(Main.INIT_SERVERS);
		if (initU.isValid()) {
			initU.fillSolution();
		}
		group.add(initU);
		for (int i = 1; i < GROUP_SIZE; i++) {
			group.add(Unit.newRandomUnit(Main.NUM_NET));
		}
		System.out.println(System.currentTimeMillis() - start);
	}

	public void evolution() {
		int num_cross = (int) (GROUP_SIZE * CROSS_RATE / 2);
		int num_cross1 = (int) (GROUP_SIZE * (1 - CROSS_RATE));
		int num_cross2 = (int) (GROUP_SIZE * (1 - CROSS_RATE / 2));
		int num_select = (int) (GROUP_SIZE * SELECT_RATE);
		int num_abandon = GROUP_SIZE - num_select;

		int noChangeCount = 0;
		Unit lastBest = group.get(0); // 上一代最佳个体，初始化为group中的第一个unit

		for (int i = 0; i < MAX_GENERATION && noChangeCount < MAX_NO_CHANGE_GEN
				&& !Thread.currentThread().isInterrupted(); i++, noChangeCount++) {
			System.out.println("generation " + i);
			// 选择
			Collections.sort(group);
			if (lastBest.compareTo(group.get(0)) > 0) {
				noChangeCount = 0;
			}
			lastBest = group.get(0);
			if (Main.BEST_UNIT.compareTo(group.get(0)) > 0) {
				Main.BEST_UNIT = group.get(0);
			}
			System.out.println("Now Cost:" + group.get(0).getCost());
			for (int j = 0; j < num_abandon; j++) {
				group.set(j + num_select, group.get(j).clone());
			}
			// 交叉
			for (int j = 0; j < num_cross; j++) {
				cross(group.get(j + num_cross1), group.get(j + num_cross2));
			}
			// 变异
			variation(i);
			System.out.println("Best Cost: " + Main.BEST_UNIT.getCost());
		}

		Collections.sort(group);
		if (Main.BEST_UNIT.compareTo(group.get(0)) > 0) {
			Main.BEST_UNIT = group.get(0);
		}
	}

	private void cross(Unit u1, Unit u2) {
		// 考虑交叉时服务器位置的重复
		int size1 = u1.getSize();
		int size2 = u2.getSize();
		int range = Math.min(size1, size2);
		int crossPoint = random.nextInt(range);
		for (int i = 0; i < crossPoint; i++) {
			int temp1 = u1.getServerLocation()[i];
			int temp2 = u2.getServerLocation()[i];

			while (!checkRepeat(temp2, u1.getServerLocation(), 0, i)
					|| !checkRepeat(temp2, u1.getServerLocation(), crossPoint, size1)) {
				temp2 = random.nextInt(Main.NUM_NET);
			}
			u1.getServerLocation()[i] = temp2;
			while (!checkRepeat(temp1, u2.getServerLocation(), 0, i)
					|| !checkRepeat(temp1, u2.getServerLocation(), crossPoint, size2)) {
				temp1 = random.nextInt(Main.NUM_NET);
			}
			u2.getServerLocation()[i] = temp1;
		}
		if (u1.isValid()) {
			u1.fillSolution();
		} else {
			u1.setServerLocation(group.get(0).getServerLocation());
		}

		while (!u2.isValid()) {
			Unit.initServerLocation(u2, Main.NUM_NET);
		}
		u2.fillSolution();
	}

	private boolean checkRepeat(int num, int[] source, int from, int to) {
		for (int i = from; i < to; i++) {
			if (num == source[i])
				return false;
		}
		return true;
	}

	private void variation(int gen) {
		// 一定机率减少服务器节点
		int flag = random.nextInt(GROUP_SIZE);
		// 在进化后期增大变异概率
		if (flag <= ((gen > MAX_GENERATION / 2) ? (3 * GROUP_SIZE * VARIATION_RATE) : (GROUP_SIZE * VARIATION_RATE))) {
			int choice = random.nextInt(10000); // 选择哪种变异方式
			int i = random.nextInt(GROUP_SIZE); // 确定发生变异的个体
			Unit u = group.get(i);
			int j = random.nextInt(u.getSize()); // 确定发生变异的位置
			int k = getLeastLoad(u);
			switch (choice % 5) {
			case 0:
				if (k >= u.getSize())
					k = j;
				minusServer(u, k);
				break;
			case 1:
				minusServer(u, j);
				break;
			case 2:
				if (k >= u.getSize())
					k = j;
				changeServer(u, k);
				break;
			case 3:
				changeServer(u, j);
				break;
			case 4:
				if (Main.NUM_CONSUMER - u.getSize() > 2) {
					plusServer(u);
				}
				break;
			default:
				break;
			}
		}
	}

	private int getLeastLoad(Unit u) {
		Solution solution = u.getSolution();
		int index = Main.NUM_NET;
		if (solution != null) {
			Map<Integer, Integer> workLoad = solution.getWorkLoad();
			int min = Integer.MAX_VALUE;
			for (int i : workLoad.keySet()) {
				if (min > workLoad.get(i)) {
					min = workLoad.get(i);
					index = i;
				}
			}
		}
		return index;
	}

	private void plusServer(Unit u) {
		int[] newSource = new int[u.getSize() + 1];
		int i = 0;
		for (int j : u.getServerLocation()) {
			newSource[i++] = j;
		}
		int tmp = random.nextInt(Main.NUM_NET);
		while (!checkRepeat(tmp, u.getServerLocation(), 0, u.getSize())) {
			tmp = random.nextInt(Main.NUM_NET);
		}
		newSource[i] = tmp;
		u.setServerLocation(newSource);
	}

	private void minusServer(Unit u, int index) {
		int[] oldSource = u.getServerLocation();
		int[] newSource = new int[u.getSize() - 1];
		for (int k = 0; k < u.getSize(); k++) {
			if (k < index)
				newSource[k] = oldSource[k];
			else if (k > index)
				newSource[k - 1] = oldSource[k];
		}
		u.setServerLocation(newSource);
		if (u.isValid()) {
			u.fillSolution();
			// u.setSize(u.getSize() - 1);
		} else {
			u.setServerLocation(oldSource);
		}
	}

	private void changeServer(Unit u, int index) {
		int oldServer = 0;
		oldServer = u.getServerLocation()[index];

		int tmp = random.nextInt(Main.NUM_NET);
		while (!checkRepeat(tmp, u.getServerLocation(), 0, u.getSize())) {
			tmp = random.nextInt(Main.NUM_NET);
		}
		u.getServerLocation()[index] = tmp;
		if (u.isValid()) {
			u.fillSolution();
		} else {
			u.getServerLocation()[index] = oldServer;
		}
	}

	public void printGroup() {
		for (Unit u : group) {
			System.out.println(u.getCost());
		}
	}

}