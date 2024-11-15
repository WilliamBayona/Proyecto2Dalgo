import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ProblemaP2OLD {

    private int n; // Número de nodos
    private Map<Integer, Map<Integer, Integer>> originalGrafo;
    private Map<Integer, Map<Integer, Integer>> grafo;
    private ArrayList< Integer> calculadoras;
    

    public static void main(String[] args) {
        ProblemaP2 problema = new ProblemaP2();
        problema.n = 6; 
        problema.grafo = problema.originalGrafo;
      
    }

private boolean bfs(int source, int sink, int[] parent) {
        boolean[] visited = new boolean[this.n];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;

        while (!queue.isEmpty()) {
            int u = queue.poll();

            for (Map.Entry<Integer, Integer> entry : this.grafo.get(u).entrySet()) {
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
            // Encontrar el flujo máximo a través del camino encontrado cuello de botella
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, this.grafo.get(u).get(v));
            }

            // Actualizar las capacidades de las aristas y las aristas inversas
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                this.grafo.get(u).put(v, this.grafo.get(u).get(v) - pathFlow);
                this.grafo.get(v).put(u, this.grafo.get(v).getOrDefault(u, 0) + pathFlow);
            }

            maxFlow += pathFlow;
        }

        return maxFlow;
    }


      public Map<Integer, Map<Integer, Integer>> blockNode(int node) {
        
        this.grafo = originalGrafo;

        for (int i = 0; i < this.n; i++) {
            if (this.originalGrafo.get(i).containsKey(node)) {
                this.grafo.get(i).put(node, 0);
            }
            if (this.originalGrafo.get(node).containsKey(i)) {
                this.grafo.get(node).put(i, 0);
            }
        }
        return grafo;
    }

    //Funcion a llamar para encontrar el nodo a bloquear
    public int[] findBestNodeToBlock(int source, int sink, List<Integer> calculatorNodes) {
        //Encontrar flujo original
        int originalMaxFlow = this.edmondsKarp(source, sink);
        int minimoFlujo = originalMaxFlow;
        int bestNode = -1;

        for (int node : calculadoras) {
            grafo = this.blockNode(node);
            int newMaxFlow = this.edmondsKarp(source, sink);

            if (minimoFlujo > newMaxFlow ) {
                minimoFlujo = newMaxFlow;
                bestNode = node;
            }
        }

        return new int[]{bestNode,originalMaxFlow,minimoFlujo};
    }

   

}
