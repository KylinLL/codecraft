package com.cacheserverdeploy.push;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class NetFlow {
	public static final int MAX_INT = Integer.MAX_VALUE >> 1;
	private final int[][] capacity;
	private final int[][] price;
	private final int[] sources;
	private final int[] sinks;
	private final int[] demands;
	private final int vlen;

	private final Strategy strategy;

	private NetFlow(Builder builder) {
		this.vlen = builder.vlen;
		this.capacity = builder.capacity;
		this.price = builder.price;
		this.sources = builder.sources;
		this.sinks = builder.sinks;
		this.demands = builder.demands;
		this.strategy = new AugmentPathStrategy();
	}

	public boolean meetDemands() {
		return strategy.meetDemands();
	}

	public Solution getSolution() {
		return strategy.getSolution();
	}

	public static Builder builder(int vlen) {
		return new Builder(vlen);
	}

	private abstract class Strategy {
		private int totalDemands;
		
		public Strategy() {
			totalDemands = addSuperSink();
			addSuperSource();
		}
		
		public boolean meetDemands(){
			return totalDemands == maxFlow();
		}

		private void addSuperSource() {
			for (int i = 0; i < sources.length; i++) {
				capacity[vlen][sources[i]] = MAX_INT;
				capacity[sources[i]][vlen] = 0;
				price[vlen][sources[i]] = 0;
				price[sources[i]][vlen] = MAX_INT;
			}
		}

		private int addSuperSink() {
			int total = 0;
			for (int i = 0; i < sinks.length; i++) {
				total += demands[i];
				capacity[sinks[i]][vlen + 1] = demands[i];
				capacity[vlen + 1][sinks[i]] = 0;
				price[sinks[i]][vlen + 1] = 0;
				price[vlen + 1][sinks[i]] = MAX_INT;
			}
			return total;
		}
		
		protected abstract int maxFlow();
		
		protected abstract Solution getSolution();
	}

	//https://www.topcoder.com/community/data-science/data-science-tutorials/push-relabel-approach-to-the-maximum-flow-problem/
	@SuppressWarnings("unused")
	private class PushRelabelStrategy extends Strategy {
		private final int[][] flow, left;
		private final int[] excess, height;
		private final int vertexLen, start, end;

		public PushRelabelStrategy() {
			super();
			vertexLen = vlen + 2;
			start = vlen;
			end = vlen + 1;
			flow = new int[vertexLen][vertexLen];
			left = new int[vertexLen][vertexLen];
			excess = new int[vertexLen];
			height = new int[vertexLen];
			for(int i=0;i<vertexLen;i++){
				for(int j=0;j<vertexLen;j++){
					left[i][j] = capacity[i][j];
				}
			}
		}

		private void preflow() {
			height[start] = vertexLen;
			for (int i = 0; i < vertexLen; i++) {
				if (capacity[start][i] > 0) {
					flow[start][i] = capacity[start][i];
					flow[i][start] = -capacity[start][i];
					excess[i] = capacity[start][i];
					excess[start] -= capacity[start][i];
					left[start][i] = capacity[start][i] - flow[start][i];
					left[i][start] = capacity[i][start] - flow[i][start];
				}
			}
		}

		private void push(int u, int v) {
			int push = Math.min(excess[u], left[u][v]);
			flow[u][v] += push;
			flow[v][u] = -flow[u][v];
			excess[u] -= push;
			excess[v] += push;
			left[u][v] = capacity[u][v] - flow[u][v];
			left[v][u] = capacity[v][u] - flow[v][u];
		}
		
		@Override
		protected int maxFlow() {
	        preflow();
	        Queue<Integer> queue = new LinkedList<>();
	        boolean[] active = new boolean[vertexLen];
	        int u, minHeight;
	        for (int i = 0; i < vertexLen; i++) {
	            if (capacity[start][i] > 0 && i != end) {
	                queue.offer(i);
	                active[i] = true;
	            }
	        }
	        while (!queue.isEmpty()) {
	            u = queue.peek();
	            minHeight = Integer.MAX_VALUE;
	            for (int i = 0; excess[u] > 0 && i < vertexLen; i++) {
	                if (left[u][i] > 0) {
	                    if (height[u] > height[i]) {
	                        push(u, i);
	                        if (!active[i] && i != start && i != end) {
	                            active[i] = true;
	                            queue.offer(i);
	                        }
	                    } else {
	                        minHeight = Math.min(minHeight, height[i]);
	                    }
	                }
	            }
	            if (excess[u] > 0) {
	                height[u] = 1 + minHeight;
	            } else {
	                active[u] = false;
	                queue.poll();
	            }
	        }
	        return excess[end];
	    }

		@Override
		public Solution getSolution() {
			
			return null;
		}


	}

	//http://blog.csdn.net/jarily/article/details/8613208
	private class AugmentPathStrategy extends Strategy {
		private final int[][] flow, left;
		private final int dist[],pre[];
		private final boolean inq[];
		private final Queue<Integer> queue;
		private final int vertexLen, start, end;
		private Solution solution;
		
		public AugmentPathStrategy(){
			super();
			vertexLen = vlen + 2;
			start = vlen;
			end = vlen + 1;
			dist = new int[vertexLen];
			pre = new int[vertexLen];
			inq = new boolean[vertexLen];
			flow = new int[vertexLen][vertexLen];
			left = new int[vertexLen][vertexLen];
			queue = new LinkedList<>();
			for(int i=0;i<vertexLen;i++){
				for(int j=0;j<vertexLen;j++){
					left[i][j] = capacity[i][j];
				}
			}
		}
		
		private void reset(){
			queue.clear();
			for(int i=0;i<vertexLen;i++){
				dist[i] = Integer.MAX_VALUE;
				pre[i] = -1;
				inq[i] = false;
			}
		}
		
		private void spfa(){
			reset();
			dist[start] = 0;
			queue.offer(start);
			inq[start] = true;
			int cur;
			while(!queue.isEmpty()){
				cur = queue.poll();
				inq[cur] = false;
				for(int i=0;i<vertexLen;i++){
					if(left[cur][i] == 0){
						continue;
					}
					if(price[cur][i] == MAX_INT){
						price[cur][i] = -price[i][cur];
					}
					if(dist[i] > dist[cur] + price[cur][i]){
						dist[i] = dist[cur] + price[cur][i];
						pre[i] = cur;
						if(!inq[i]){
							queue.offer(i);
							inq[i] = true;
						}
					}
				}
			}
		}
				
		@Override
		public Solution getSolution() {
			if(null == solution){
				maxFlow();
			}
			return solution;
		}

		@Override
		protected int maxFlow() {
			spfa();
			int minCost = 0;
			int maxFlow = 0;
			List<Line> lines = new ArrayList<>();
			while(pre[end]!=-1){
				int minCf = MAX_INT;
				int u = pre[end],v = end;
				LinkedList<Integer> path = new LinkedList<>();
				while(u!=-1){
					if(v != end){
						path.addFirst(v);
					}
					if(minCf > left[u][v]){
						minCf = left[u][v];
					}
					v = u;
					u = pre[v];
				}
				maxFlow += minCf;
				if(minCf != MAX_INT){
					lines.add(new Line(path,minCf));
				}
				u = pre[end];
				v = end;
				while(u!=-1){
					minCost += minCf * price[u][v];
					flow[u][v] += minCf;
					flow[v][u] = -flow[u][v];
					left[u][v] = capacity[u][v] - flow[u][v];
					left[v][u] = capacity[v][u] - flow[v][u];
					v =u;
					u = pre[v];
				}
				spfa();
			}
			solution = new Solution(minCost,lines);
			return maxFlow;
		}

	}
	
	public static class Line {
		private final List<Integer> vertexs;
		private final int volume;

		public Line(List<Integer> vertexs, int volume) {
			this.vertexs = vertexs;
			this.volume = volume;
		}

		public List<Integer> getVertexs() {
			return vertexs;
		}

		public int getVolume() {
			return volume;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(volume+": ");
			for(int i=0;i<vertexs.size();i++){
				builder.append(vertexs.get(i));
				if(i!=vertexs.size()-1){
					builder.append("->");
				}
			}
			builder.append("\n");
			return builder.toString();
		}
		
	}

	public static class Solution {
		private final int cost;
		private final List<Line> lines;

		public Solution(int cost, List<Line> lines) {
			this.cost = cost;
			this.lines = lines;
		}

		public int getCost() {
			return cost;
		}

		public List<Line> getLines() {
			return lines;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("minimum cost: "+cost+"\n");
			for(int i=0;i<lines.size();i++){
				builder.append((i+1)+") ");
				builder.append(lines.get(i));
			}
			return builder.toString();
		}
		
	}

	public static class Builder {
		private int vlen;
		private int[][] capacity;
		private int[][] price;
		private int[] sources;
		private int[] sinks;
		private int[] demands;

		private Builder(int vlen) {
			this.vlen = vlen;
		}

		public Builder setCapacity(int[][] capacity) {
			this.capacity = capacity;
			return this;
		}

		public Builder setPrice(int[][] price) {
			this.price = price;
			return this;
		}

		public Builder setSources(int[] sources) {
			this.sources = sources;
			return this;
		}

		public Builder setSinks(int[] sinks) {
			this.sinks = sinks;
			return this;
		}

		public Builder setDemands(int[] demands) {
			this.demands = demands;
			return this;
		}

		public Builder useAugmentPath() {
			return this;
		}

		public NetFlow getNetFlow() {
			return new NetFlow(this);
		}

	}
}
