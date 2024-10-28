import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ProblemaP2 {

    private int n; // Número de nodos
    private Map<Integer, Map<Integer, Integer>> graph;
    

    public static void main(String[] args) {
        int n = 6; // Número de nodos en la red
      
    }

private boolean bfs(int source, int sink, int[] parent) {
        boolean[] visited = new boolean[this.n];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;

        while (!queue.isEmpty()) {
            int u = queue.poll();

            for (Map.Entry<Integer, Integer> entry : this.graph.get(u).entrySet()) {
                int v = entry.getKey();
                int capacity = entry.getValue();

                if (!visited[v] && capacity > 0) {
                    parent[v] = u;
                    visited[v] = true;
                    queue.add(v);
                    if (v == sink) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int edmondsKarp(int source, int sink) {
        int[] parent = new int[this.n];
        int maxFlow = 0;

        while (this.bfs(source, sink, parent)) {
            // Encontrar el flujo máximo a través del camino encontrado
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, this.graph.get(u).get(v));
            }

            // Actualizar las capacidades de las aristas y las aristas inversas
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                this.graph.get(u).put(v, this.graph.get(u).get(v) - pathFlow);
                this.graph.get(v).put(u, this.graph.get(v).getOrDefault(u, 0) + pathFlow);
            }

            maxFlow += pathFlow;
        }

        return maxFlow;
    }

   

}
