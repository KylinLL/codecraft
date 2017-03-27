package com.cacheserverdeploy.deploy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Group {

	public static int GROUP_SIZE = 100; // 群体规模，即群体中个体的数量，一般为20~100
	public static double CROSS_RATE = 0.8; // 交叉概率，一般为0.4~0.9
	public static double VARIATION_RATE = 0.1; // 变异概率，一般为0.001~0.1
	public static double SELECT_RATE = 0.6; // 选择时保留的比例
	public static int MAX_GENERATION = 100; // 最大代数，一般为100~500

	List<Unit> group = new ArrayList<>();

	public void initGroup() {
		for (int i = 0; i < GROUP_SIZE; i++) {
			Unit u = new Unit();
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

		for (int i = 0; i < MAX_GENERATION; i++) {
			// 选择
			sort();
			for (int j = 0; j < num_abandon; j++) {
				group.set(j + num_select, group.get(j));
			}
			// 交叉
			for (int j = 0; j < num_cross; j++) {
				cross(group.get(j + num_cross1), group.get(j + num_cross2));
			}
			// 变异
			variation(i);
		}
		sort();
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
		// TODO 考虑交叉时服务器位置的重复
		int range = Math.min(u1.size, u2.size);
		int crossPoint = new Random().nextInt(range);
		Random random = new Random();
		for (int i = 0; i < crossPoint; i++) {
			int temp1 = u1.server_location.get(i);
			int temp2 = u2.server_location.get(i);

			while (!checkRepeat(temp2, u1.server_location, crossPoint)) {
				temp2 = random.nextInt(range);
			}
			u1.server_location.set(i, temp2);
			while (!checkRepeat(temp1, u2.server_location, crossPoint)) {
				temp1 = random.nextInt(range);
			}
			u2.server_location.set(i, temp1);
		}
		if (u1.checkValid()) {
			u1.calculateCost();
		} else {
			u1.server_location = group.get(0).server_location;
		}
		if (u2.checkValid()) {
			u2.calculateCost();
		} else {
			u2.server_location = group.get(0).server_location;
		}
	}

	private boolean checkRepeat(int num, List<Integer> list, int index) {
		for (int i = index; i < list.size(); i++) {
			if (num == list.get(i))
				return false;
		}
		return true;
	}

	private void variation(int gen) {
		// TODO 一定机率减少服务器节点
		Random random = new Random();
		int flag = random.nextInt(GROUP_SIZE);
		// 在进化后期增大变异概率
		if (flag <= ((gen > MAX_GENERATION / 2) ? (5 * GROUP_SIZE * VARIATION_RATE) : (GROUP_SIZE * VARIATION_RATE))) {
			int i = random.nextInt(GROUP_SIZE); // 确定发生变异的个体
			Unit u = group.get(i);
			int j = random.nextInt(u.size); // 确定发生变异的位置
			Integer rm = u.server_location.remove(j);
			if (u.checkValid())
				u.calculateCost();
			else
				u.server_location.add(rm);
		}
	}

	public void printGroup() {
		for (Unit u : group) {
			System.out.println(u.cost);
		}
	}

}
