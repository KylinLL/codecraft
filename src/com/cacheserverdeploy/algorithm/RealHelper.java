package com.cacheserverdeploy.algorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.cacheserverdeploy.algorithm.NetFlow.Builder;

// https://www.topcoder.com/community/data-science/data-science-tutorials/push-relabel-approach-to-the-maximum-flow-problem/
// http://blog.csdn.net/bbbbaai/article/details/46538383
// http://codecraft.huawei.com/home/detail
// http://blog.csdn.net/hechenghai/article/details/42719715
// http://www.cppblog.com/Icyflame/archive/2009/06/24/88448.html
@Deprecated
public class RealHelper {
	private Scanner scan;
	private int[][] capacity, price, consumers;
	private int vlen, elen, clen, serverPrice;

	public RealHelper() {

	}

	public Scanner getScan() {
		return scan;
	}

	public void setScan(Scanner scan) {
		this.scan = scan;
	}

	public int[][] getConsumers() {
		return consumers;
	}

	public void setConsumers(int[][] consumers) {
		this.consumers = consumers;
	}

	public int getElen() {
		return elen;
	}

	public void setElen(int elen) {
		this.elen = elen;
	}

	public int getClen() {
		return clen;
	}

	public void setClen(int clen) {
		this.clen = clen;
	}

	public int getServerPrice() {
		return serverPrice;
	}

	public void setServerPrice(int serverPrice) {
		this.serverPrice = serverPrice;
	}

	public void setCapacity(int[][] capacity) {
		this.capacity = capacity;
	}

	public void setPrice(int[][] price) {
		this.price = price;
	}

	public void setVlen(int vlen) {
		this.vlen = vlen;
	}

	public int[][] getCapacity() {
		return capacity;
	}

	public int[][] getPrice() {
		return price;
	}

	public int getVlen() {
		return vlen;
	}

	public void init() {
		String path = System.getProperty("user.dir") + "/case0.txt";
		try {
			scan = new Scanner(new FileInputStream(new File(path)));
			vlen = scan.nextInt();
			elen = scan.nextInt();
			clen = scan.nextInt();
			serverPrice = scan.nextInt();

			capacity = new int[vlen + 2][vlen + 2];
			price = new int[vlen + 2][vlen + 2];
			consumers = new int[clen][2];

			for (int i = 0; i < elen; i++) {
				int from = scan.nextInt();
				int to = scan.nextInt();
				capacity[from][to] = scan.nextInt();
				price[from][to] = scan.nextInt();
				capacity[to][from] = capacity[from][to];
				price[to][from] = price[from][to];
			}

			int id = -1;
			for (int i = 0; i < clen; i++) {
				id = scan.nextInt();
				consumers[id][0] = scan.nextInt();
				consumers[id][1] = scan.nextInt();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			scan.close();
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		RealHelper pr = new RealHelper();
		pr.init();
		Builder builder = NetFlow.builder(pr.getVlen());
		NetFlow flow = builder.setCapacity(pr.getCapacity())
				.setPrice(pr.getPrice()).setConsumers(pr.getConsumers(), pr.getClen())
				.setServeCost(pr.getServerPrice()).getNetFlow();
	}
}