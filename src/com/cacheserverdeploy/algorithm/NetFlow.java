package com.cacheserverdeploy.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.filetool.main.Main;

public class NetFlow {
    private final int[][] capacity;
    private final int[][] price;
    private final int[] sinks;
    private final int[] sinksId;
    private final int[] demands;
    private final int vlen, superSink, superSource, vertexLen;
    private final int perServerCost;
    private final int totalDemands;

    private AugmentPathStrategy strategy;
    private int[] sources, sourcesId;

    private NetFlow(Builder builder) {
        this.vlen = builder.vlen;
        this.superSink = vlen + 1;
        this.superSource = vlen;
        this.vertexLen = vlen + 2;
        this.capacity = builder.capacity;
        this.price = builder.price;
        this.sinks = builder.sinks;
        this.sinksId = builder.sinksId;
        this.demands = builder.demands;
        this.perServerCost = builder.perServerCost;

        // this.info = new NetShortestInfo();
        this.totalDemands = initSink();
    }

    private int initSink() {
        int total = 0;
        for (int i = 0; i < sinks.length; i++) {
            total += demands[i];
            capacity[sinks[i]][superSink] = demands[i];
            capacity[superSink][sinks[i]] = 0;
            price[sinks[i]][superSink] = 0;
            price[superSink][sinks[i]] = Main.MAX_INT;
        }
        return total;
    }

    public boolean meetDemands() {
        return strategy.maxFlow();
    }

    public Solution getSolution() {
        return strategy.getSolution();
    }

    public int[] getSources() {
        return sources;
    }

    public NetFlow newServers(int[] newSources) {
        for (int i = 0; i < vlen; i++) {
            capacity[superSource][i] = 0;
            capacity[i][superSource] = 0;
            price[superSource][i] = 0;
            price[i][superSource] = 0;
        }
        this.sources = newSources;
        sourcesId = new int[vlen];
        for (int i = 0; i < sources.length; i++) {
            capacity[superSource][sources[i]] = Main.MAX_INT;
            capacity[sources[i]][superSource] = 0;
            price[superSource][sources[i]] = 0;
            price[sources[i]][superSource] = Main.MAX_INT;
            sourcesId[sources[i]] = i;
        }
        this.strategy = new AugmentPathStrategy();
        return this;
    }

    public static Builder builder(int vlen) {
        return new Builder(vlen);
    }

    // http://blog.csdn.net/jarily/article/details/8613208
    private class AugmentPathStrategy {
        private final int[][] flow, left;
        private final int cost[], pre[];
        private final boolean visited[];
        private final PriorityQueue<Integer> queue;
        private Solution solution;

        public AugmentPathStrategy() {
            cost = new int[vertexLen];
            pre = new int[vertexLen];
            visited = new boolean[vertexLen];
            queue = new PriorityQueue<Integer>(vertexLen, new Comparator<Integer>() {
                @Override
                public int compare(Integer p1, Integer p2) {
                    return cost[p1] - cost[p2];
                }
            });
            flow = new int[vertexLen][vertexLen];
            left = new int[vertexLen][vertexLen];
            for (int i = 0; i < vertexLen; i++) {
                for (int j = 0; j < vertexLen; j++) {
                    left[i][j] = capacity[i][j];
                }
            }
        }

        private void reset() {
            queue.clear();
            for (int i = 0; i < vertexLen; i++) {
                cost[i] = Main.MAX_INT;
                if (i == superSource) {
                    cost[i] = 0;
                }
                queue.offer(i);
                pre[i] = -1;
                visited[i] = false;
            }
        }

        private void dijkstra() {
            reset();
            int cur = -1;
            while (!queue.isEmpty()) {
                cur = queue.poll();
                visited[cur] = true;
                if (cur == superSink) {
                    break;
                }
                for (int i = 0; i < vertexLen; i++) {
                    if (!visited[i] && left[cur][i] > 0 && cost[i] > cost[cur] + price[cur][i]) {
                        queue.remove(i);
                        cost[i] = cost[cur] + price[cur][i];
                        queue.offer(i);
                        pre[i] = cur;
                    }
                }
            }
        }

        public Solution getSolution() {
            return solution;
        }

        protected boolean maxFlow() {
            dijkstra();
            int minCost = 0;
            int maxFlow = 0;
            List<Line> lines = new ArrayList<Line>();
            Map<Integer, Integer> map = new HashMap<Integer,Integer>();
            while (pre[superSink] != -1) {
                int minCf = Main.MAX_INT;
                int u = pre[superSink], v = superSink;
                LinkedList<Integer> path = new LinkedList<Integer>();
                while (u != -1) {
                    if (v != superSink) {
                        path.addFirst(v);
                    } else {
                        path.addFirst(sinksId[u]);
                    }
                    if (minCf > left[u][v]) {
                        minCf = left[u][v];
                    }
                    v = u;
                    u = pre[v];
                }
                if (minCf != Main.MAX_INT) {
                    maxFlow += minCf;
                    lines.add(new Line(path, minCf));
                    Integer server = path.peek();
                    if (!map.containsKey(sourcesId[server])) {
                        map.put(sourcesId[server], 0);
                    }
                    map.put(sourcesId[server], map.get(sourcesId[server]) + minCf);
                }
                u = pre[superSink];
                v = superSink;
                while (u != -1) {
                    minCost += minCf * price[u][v];
                    flow[u][v] += minCf;
                    left[u][v] = capacity[u][v] - flow[u][v];
                    v = u;
                    u = pre[v];
                }
                dijkstra();
            }
            boolean ret = (maxFlow == totalDemands);
            if (ret) {
                minCost += sources.length * perServerCost;
                solution = new Solution(minCost, lines, map);
            }
            return ret;
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
            for (int i = 0; i < vertexs.size(); i++) {
                builder.append(vertexs.get(i));
                builder.append(" ");
            }
            builder.append(volume);
            return builder.toString();
        }

    }

    public static class Solution {
        private final int cost;
        private final List<Line> lines;
        private final Map<Integer, Integer> workLoad;

        public Solution(int cost, List<Line> lines, Map<Integer, Integer> work) {
            this.cost = cost;
            this.lines = lines;
            this.workLoad = work;
        }

        public int getCost() {
            return cost;
        }

        public List<Line> getLines() {
            return lines;
        }

        public Map<Integer, Integer> getWorkLoad() {
            return workLoad;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("minimum cost: " + cost + "\n");
            for (int i = 0; i < lines.size(); i++) {
                builder.append((i + 1) + ") ");
                builder.append(lines.get(i) + "\n");
            }
            return builder.toString();
        }

    }

    public static class Builder {
        private int vlen;
        private int[][] capacity;
        private int[][] price;
        private int[] sinks;
        private int[] demands;
        private int[] sinksId;
        private int perServerCost;

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

        public Builder setConsumers(int[][] consumers, int num) {
            this.sinks = new int[num];
            this.demands = new int[num];
            this.sinksId = new int[Main.MAX_NODES];
            for (int i = 0; i < num; i++) {
                sinks[i] = consumers[i][0];
                sinksId[sinks[i]] = i;
                demands[i] = consumers[i][1];
            }
            return this;
        }

        public Builder setServeCost(int cost) {
            perServerCost = cost;
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