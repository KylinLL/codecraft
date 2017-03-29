package com.cacheserverdeploy.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.filetool.main.Main;

public class NetFlow {
    private final int[][] capacity;
    private final int[][] price;
    private final int[] sinks;
    private final int[] sinksId;
    private final int[] demands;
    private final int vlen;
    private final int perServerCost;

    private Strategy strategy;
    private int[] sources;
    private Set<Integer> givenServers = new HashSet<>();

    private NetFlow(Builder builder) {
        this.vlen = builder.vlen;
        this.capacity = builder.capacity;
        this.price = builder.price;
        this.sinks = builder.sinks;
        this.sinksId = builder.sinksId;
        this.demands = builder.demands;
        this.perServerCost = builder.perServerCost;
    }

    public boolean meetDemands() {
        return strategy.meetDemands();
    }

    public Solution getSolution() {
        System.out.println("COST: " + (null == strategy.getSolution() ? "NULL" : "" + strategy.getSolution().getCost()));
        return strategy.getSolution();
    }

    public int[] getSources() {
        return sources;
    }

    public NetFlow newServers(int[] newSources) {
        for (int i = 0; i < vlen; i++) {
            capacity[vlen][i] = 0;
            capacity[i][vlen] = 0;
            price[vlen][i] = 0;
            price[i][vlen] = 0;
        }
        givenServers.clear();
        for (int i = 0; i < newSources.length; i++) {
            givenServers.add(newSources[i]);
        }
        this.sources = newSources;
        this.strategy = new AugmentPathStrategy();
        return this;
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

        public boolean meetDemands() {
            return totalDemands == maxFlow();
        }

        protected abstract void addSuperSource();

        protected abstract int addSuperSink();

        protected abstract int maxFlow();

        public abstract Solution getSolution();
    }

    // http://blog.csdn.net/jarily/article/details/8613208
    private class AugmentPathStrategy extends Strategy {
        private final int[][] flow, left;
        private final int cost[], pre[];
        private final boolean visited[];
        private final int vertexLen, start, end;
        private Solution solution;

        public AugmentPathStrategy() {
            super();
            vertexLen = vlen + 2;
            start = vlen;
            end = vlen + 1;
            cost = new int[vertexLen];
            pre = new int[vertexLen];
            visited = new boolean[vertexLen];
            flow = new int[vertexLen][vertexLen];
            left = new int[vertexLen][vertexLen];
            for (int i = 0; i < vertexLen; i++) {
                for (int j = 0; j < vertexLen; j++) {
                    left[i][j] = capacity[i][j];
                }
            }
        }

        private void reset() {
            for (int i = 0; i < vertexLen; i++) {
                cost[i] = Main.MAX_INT;
                pre[i] = -1;
                visited[i] = false;
            }
        }

        private void dijkstra() {
            reset();
            cost[start] = 0;
            int cur = start;
            while (cur != end) {
                visited[cur] = true;
                for (int i = 0; i < vertexLen; i++) {
                    if (!visited[i] && left[cur][i] > 0 && cost[i] > cost[cur] + price[cur][i]) {
                        cost[i] = cost[cur] + price[cur][i];
                        pre[i] = cur;
                    }
                }
                int minCost = Main.MAX_INT, pos = -1;
                for (int i = 0; i < vertexLen; i++) {
                    if (!visited[i] && minCost > cost[i]) {
                        minCost = cost[i];
                        pos = i;
                    }
                }
                if (pos != -1) {
                    cur = pos;
                } else {
                    break;
                }
            }
        }

        @Override
        public Solution getSolution() {
            if (null == solution) {
                maxFlow();
            }
            return solution;
        }

        @Override
        protected int maxFlow() {
            dijkstra();
            int minCost = 0;
            int maxFlow = 0;
            List<Line> lines = new ArrayList<Line>();
            while (pre[end] != -1) {
                int minCf = Main.MAX_INT;
                int u = pre[end], v = end;
                LinkedList<Integer> path = new LinkedList<Integer>();
                while (u != -1) {
                    if (v != end) {
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
                    givenServers.remove(path.peek());
                }
                u = pre[end];
                v = end;
                while (u != -1) {
                    minCost += minCf * price[u][v];
                    flow[u][v] += minCf;
                    left[u][v] = capacity[u][v] - flow[u][v];
                    v = u;
                    u = pre[v];
                }
                dijkstra();
            }
            minCost += sources.length * perServerCost;
            solution = new Solution(minCost, lines,givenServers);
            return maxFlow;
        }

        @Override
        protected void addSuperSource() {
            for (int i = 0; i < sources.length; i++) {
                capacity[vlen][sources[i]] = Main.MAX_INT;
                capacity[sources[i]][vlen] = 0;
                price[vlen][sources[i]] = 0;
                price[sources[i]][vlen] = Main.MAX_INT;
            }
        }

        @Override
        protected int addSuperSink() {
            int total = 0;
            for (int i = 0; i < sinks.length; i++) {
                total += demands[i];
                capacity[sinks[i]][vlen + 1] = demands[i];
                capacity[vlen + 1][sinks[i]] = 0;
                price[sinks[i]][vlen + 1] = 0;
                price[vlen + 1][sinks[i]] = Main.MAX_INT;
            }
            return total;
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
        private final Set<Integer> remain;

        public Solution(int cost, List<Line> lines,Set<Integer> ra) {
            this.cost = cost;
            this.lines = lines;
            this.remain = ra;
        }

        public int getCost() {
            return cost;
        }

        public List<Line> getLines() {
            return lines;
        }
        
        public Set<Integer> getRemain() {
            return remain;
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