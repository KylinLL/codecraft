package com.cacheserverdeploy.deploy;

import com.filetool.main.Main;

public class Deploy {

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
		// for (int i = 0; i < graphContent.length; i++) {
		// System.out.println(graphContent[i]);
		// }
		Group group = new Group();
		group.initGroup();
		group.evolution();

		return new String[] { new Integer(Main.MIN_COST).toString(), "\r\n", "0 8 0 20" };
	}

}
