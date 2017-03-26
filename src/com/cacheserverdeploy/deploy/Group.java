package com.cacheserverdeploy.deploy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
		// TODO
	}

	private void variation(int n) {
		// TODO Auto-generated method stub

	}

	public void printGroup() {
		for (Unit u : group) {
			System.out.println(u.cost);
		}
	}

}
