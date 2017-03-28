package com.cacheserverdeploy.deploy;

import com.cacheserverdeploy.push.NetFlow;
import com.cacheserverdeploy.push.NetFlow.Builder;
import com.filetool.main.Main;

public class Deploy {

	public static NetFlow flow;

	/**
	 * 你需要完成的入口 <功能详细描述>
	 * 
	 * @param graphContent
	 *            用例信息文件
	 * @return [参数说明] 输出结果信息
	 * @see [类、类#方法、类#成员]
	 */
	public static String[] deployServer(String[] graphContent) {

		/** do your work here **/
		Builder builder = NetFlow.builder(Main.NUM_NET);
		flow = builder.setCapacity(Main.MATRIX_NETWORK).setPrice(Main.MATRIX_COST)
				.setConsumers(Main.CONSUMER, Main.NUM_CONSUMER).setServeCost(Main.PRICE_PER_SERVER).getNetFlow();

		Group group = new Group();
		group.initGroup();
		group.evolution();

		String[] res = new String[Main.NUM_CONSUMER + 2];
		res[0] = new Integer(Main.NUM_CONSUMER).toString();
		res[1] = "\r\n";
		for (int i = 0; i < Main.NUM_CONSUMER; i++) {
			StringBuilder s = new StringBuilder();
			s.append(Main.CONSUMER[i][0]).append(" ").append(i).append(" ").append(Main.CONSUMER[i][1]);
			res[2 + i] = s.toString();
		}
		// return new String[]{"17","\r\n","0 8 0 20"};
		return res;
	}

}
