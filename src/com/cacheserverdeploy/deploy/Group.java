package com.cacheserverdeploy.deploy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.filetool.main.Main;

public class Group {

	public static int GROUP_SIZE = 100; // 群体规模，即群体中个体的数量，一般为20~100
	public static double CROSS_RATE = 0.8; // 交叉概率，一般为0.4~0.9
	public static double VARIATION_RATE = 0.1; // 变异概率，一般为0.001~0.1
	public static double SELECT_RATE = 0.6; // 选择时保留的比例
	public static int MAX_GENERATION = 100; // 最大代数，一般为100~500

	List<Unit> group = new ArrayList<>();
	Random random = new Random();

	public void initGroup() {
		for (int i = 0; i < GROUP_SIZE; i++) {
			Unit u = new Unit();
			u.init();
			u.printUnit(i);
			group.add(u);
		}
	}

	public void evolution() {
		int num_cross = (int) (GROUP_SIZE * CROSS_RATE / 2);
		int num_cross1 = (int) (GROUP_SIZE * (1 - CROSS_RATE));
		int num_cross2 = (int) (GROUP_SIZE * (1 - CROSS_RATE / 2));
		int num_select = (int) (GROUP_SIZE * SELECT_RATE);
		int num_abandon = GROUP_SIZE - num_select;
		int nochange = 0;

		for (int i = 0; i < MAX_GENERATION && nochange < MAX_GENERATION >> 1; i++, nochange++) {
			System.out.println("generation " + i);
			// 选择
			sort();
			if (Main.MIN_COST > group.get(0).cost) {
				Main.MIN_COST = group.get(0).cost;
				Main.BEST_SOLUTION = group.get(0).solution;
				nochange = 0;
			}
			for (int j = 0; j < num_abandon; j++) {
				group.set(j + num_select, group.get(j).deepCopy());
			}
			// 交叉
			for (int j = 0; j < num_cross; j++) {
				cross(group.get(j + num_cross1), group.get(j + num_cross2));
			}
			// 变异
			variation(i);
			System.out.println("Min Cost: " + Main.MIN_COST);
		}
		sort();
		if (Main.MIN_COST > group.get(0).cost) {
			Main.MIN_COST = group.get(0).cost;
			Main.BEST_SOLUTION = group.get(0).solution;
		}
	}

	private void sort() {
		Collections.sort(group, new Comparator<Unit>() {
			@Override
			public int compare(Unit o1, Unit o2) {
				return o1.cost - o2.cost;
			}
		});
	}

	private void cross(Unit u1, Unit u2) {
		// 考虑交叉时服务器位置的重复
		int size1 = u1.size;
		int size2 = u2.size;
		int range = Math.min(size1, size2);
		int crossPoint = random.nextInt(range);
		for (int i = 0; i < crossPoint; i++) {
			int temp1 = u1.server_location[i];
			int temp2 = u2.server_location[i];

			while (!checkRepeat(temp2, u1.server_location, size1, crossPoint)) {
				temp2 = random.nextInt(Main.NUM_NET);
			}
			u1.server_location[i] = temp2;
			while (!checkRepeat(temp1, u2.server_location, size2, crossPoint)) {
				temp1 = random.nextInt(Main.NUM_NET);
			}
			u2.server_location[i] = temp1;
		}
		if (u1.checkValid()) {
			u1.calculateCost();
		} else {
			u1.server_location = group.get(0).server_location.clone();
		}

		while (!u2.checkValid()) {
			u2.initLocation();
		}
		u2.calculateCost();
	}

	private boolean checkRepeat(int num, int[] source, int size, int index) {
		for (int i = index; i < size; i++) {
			if (num == source[i])
				return false;
		}
		return true;
	}

	private void variation(int gen) {
		// 改变或减少服务器节点
		int flag = random.nextInt(GROUP_SIZE);
		// 在进化后期增大变异概率
		if (flag <= ((gen > MAX_GENERATION / 2) ? (5 * GROUP_SIZE * VARIATION_RATE) : (GROUP_SIZE * VARIATION_RATE))) {
			int i = random.nextInt(GROUP_SIZE); // 确定发生变异的个体
			Unit u = group.get(i);
			int j = random.nextInt(u.size); // 确定发生变异的位置
			if (flag % 2 == 0)
				minusServer(u, j);
			else
				changeServer(u, j);
		}
	}

	private void minusServer(Unit u, int index) {

		int[] oldSource = u.server_location;
		int[] newSource = new int[u.size - 1];
		for (int k = 0; k < u.size; k++) {
			if (k < index)
				newSource[k] = oldSource[k];
			else if (k > index)
				newSource[k - 1] = oldSource[k];
		}
		u.server_location = newSource;
		if (u.checkValid()) {
			u.calculateCost();
			u.size--;
		} else {
			u.server_location = oldSource;
		}
	}

	private void changeServer(Unit u, int index) {

		int oldServer = u.server_location[index];
		u.server_location[index] = random.nextInt(Main.NUM_NET);

		if (u.checkValid()) {
			u.calculateCost();
		} else {
			u.server_location[index] = oldServer;
		}
	}

	public void printGroup() {
		for (Unit u : group) {
			System.out.println(u.cost);
		}
	}

}
