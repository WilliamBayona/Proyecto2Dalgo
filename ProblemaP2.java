import java.util.*;
import java.lang.Math;

public class ProblemaP2 {

    private int n; // Número de nodos
    private Map<Integer, Map<Integer, Integer>> originalGrafo;
    private Map<Integer, Map<Integer, Integer>> grafo;
    private List<Integer> calculadoras;

    // Variables configurables para los porcentajes
    private double porcentajeMayorFlujo;
    private double porcentajeFlujoCercanoCapacidad;

    // Mapeo de IDs de células a índices de nodos
    private Map<Integer, Integer> idToNodeIndex;
    private Map<Integer, Integer> nodeIndexToId;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ProblemaP2 problema = new ProblemaP2();

        long tiempoInicioTotal = System.nanoTime(); // Tiempo inicial en nanosegundos para todo el programa

        int casos = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea

        List<CasoDePrueba> listaDeCasos = new ArrayList<>();

        for (int c = 0; c < casos; c++) {
            // Leer n y d
            int n = sc.nextInt();
            double d = sc.nextDouble();
            sc.nextLine(); // Consumir el salto de línea

            // Leer y almacenar la información de las células
            List<Celula> celulas = new ArrayList<>();

            for (int i = 0; i < n; i++) {
                String linea = sc.nextLine();
                String[] partes = linea.trim().split("\\s+");

                int id = Integer.parseInt(partes[0]);
                double x = Double.parseDouble(partes[1]);
                double y = Double.parseDouble(partes[2]);
                int tipo = Integer.parseInt(partes[3]);

                Set<String> peptidos = new HashSet<>();
                for (int j = 4; j < partes.length; j++) {
                    peptidos.add(partes[j]);
                }

                Celula celula = new Celula(id, x, y, tipo, peptidos);
                celulas.add(celula);
            }

            // Crear un objeto CasoDePrueba y agregarlo a la lista
            CasoDePrueba caso = new CasoDePrueba(celulas, d, n);
            listaDeCasos.add(caso);
        }

        // Procesar todos los casos de prueba y almacenar los resultados
        List<int[]> resultados = new ArrayList<>();
        int casoNumero = 1;
        for (CasoDePrueba caso : listaDeCasos) {
            long tiempoInicioCaso = System.nanoTime(); // Tiempo inicial por caso
            int[] resultado = problema.resolverCaso(caso);
            long tiempoFinCaso = System.nanoTime(); // Tiempo final por caso

            resultados.add(resultado);
            casoNumero++;
        }

        // Imprimir todos los resultados
        for (int[] resultado : resultados) {
            System.out.println(resultado[0] + " " + resultado[1] + " " + resultado[2]);
        }

        long tiempoFinTotal = System.nanoTime(); // Tiempo final en nanosegundos para todo el programa
        System.out.printf("Tiempo total de ejecución: %.3f ms%n", (tiempoFinTotal - tiempoInicioTotal) / 1e6);
    }

    // Clase para representar un caso de prueba
    static class CasoDePrueba {
        List<Celula> celulas;
        double d;
        int n;

        public CasoDePrueba(List<Celula> celulas, double d, int n) {
            this.celulas = celulas;
            this.d = d;
            this.n = n;
        }
    }

    // Clase Celula
    static class Celula {
        int id;
        double x, y;
        int tipo; // 1: Iniciadora, 2: Calculadora, 3: Ejecutora
        Set<String> peptidos;
        int nodeInIndex;
        int nodeOutIndex;

        public Celula(int id, double x, double y, int tipo, Set<String> peptidos) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.tipo = tipo;
            this.peptidos = peptidos;
            this.nodeInIndex = -1;
            this.nodeOutIndex = -1;
        }
    }

    public int[] resolverCaso(CasoDePrueba caso) {
        List<Celula> celulas = caso.celulas;
        double d = caso.d;

        // Calcular los porcentajes dinámicamente
        calcularPorcentajesDinamicos(caso.n);

        // Mapear IDs de células a índices de nodos
        idToNodeIndex = new HashMap<>();
        nodeIndexToId = new HashMap<>();
        int nodeIndex = 0;

        for (Celula celula : celulas) {
            if (celula.tipo == 2) {
                celula.nodeInIndex = nodeIndex++;
                celula.nodeOutIndex = nodeIndex++;
            } else {
                celula.nodeInIndex = nodeIndex++;
                celula.nodeOutIndex = celula.nodeInIndex;
            }
            idToNodeIndex.put(celula.id, celula.nodeInIndex);
            nodeIndexToId.put(celula.nodeInIndex, celula.id);
        }

        n = nodeIndex;
        originalGrafo = new HashMap<>();
        calculadoras = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            originalGrafo.put(i, new HashMap<>());
        }

        int maxCapacity = 1_000_000;

        for (Celula celula : celulas) {
            if (celula.tipo == 2) {
                originalGrafo.get(celula.nodeInIndex).put(celula.nodeOutIndex, maxCapacity);
                calculadoras.add(celula.nodeInIndex);
            }
        }

        for (Celula origen : celulas) {
            int origenOutIndex = origen.nodeOutIndex;
            for (Celula destino : celulas) {
                if (origen == destino || !puedenComunicarse(origen.tipo, destino.tipo)) continue;

                double distancia = Math.hypot(origen.x - destino.x, origen.y - destino.y);

                if (distancia <= d + 1e-8) {
                    Set<String> peptidosCompartidos = new HashSet<>(origen.peptidos);
                    peptidosCompartidos.retainAll(destino.peptidos);
                    int capacidad = peptidosCompartidos.size();

                    if (capacidad > 0) {
                        int destinoInIndex = destino.nodeInIndex;
                        originalGrafo.get(origenOutIndex).put(destinoInIndex, capacidad);
                    }
                }
            }
        }

        int superSource = n;
        int superSink = n + 1;
        n = n + 2;

        originalGrafo.put(superSource, new HashMap<>());
        originalGrafo.put(superSink, new HashMap<>());

        for (Celula celula : celulas) {
            if (celula.tipo == 1) {
                originalGrafo.get(superSource).put(celula.nodeInIndex, maxCapacity);
            } else if (celula.tipo == 3) {
                originalGrafo.get(celula.nodeInIndex).put(superSink, maxCapacity);
            }
        }

        resetGrafo();
        return findBestNodeToBlock(superSource, superSink);
    }

    public int edmondsKarp(int source, int sink) {
        int[] parent = new int[this.n];
        int maxFlow = 0;

        while (this.bfs(source, sink, parent)) {
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, this.grafo.get(u).get(v));
            }

            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                this.grafo.get(u).put(v, this.grafo.get(u).get(v) - pathFlow);
                this.grafo.get(v).put(u, this.grafo.get(v).getOrDefault(u, 0) + pathFlow);
            }

            maxFlow += pathFlow;
        }

        return maxFlow;
    }

    private boolean bfs(int source, int sink, int[] parent) {
        boolean[] visited = new boolean[this.n];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        while (!queue.isEmpty()) {
            int u = queue.poll();

            for (Map.Entry<Integer, Integer> entry : this.grafo.get(u).entrySet()) {
                int v = entry.getKey();
                int capacity = entry.getValue();

                if (!visited[v] && capacity > 0) {
                    parent[v] = u;
                    visited[v] = true;
                    queue.add(v);
                    if (v == sink) return true;
                }
            }
        }

        return false;
    }

    public void resetGrafo() {
        this.grafo = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : originalGrafo.entrySet()) {
            this.grafo.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
    }

    public int[] findBestNodeToBlock(int source, int sink) {
        resetGrafo();
        int originalMaxFlow = edmondsKarp(source, sink);

        List<Integer> NodosCandidatos = findCandidateNodes();

        if (NodosCandidatos.isEmpty()) {
            return new int[]{-1, originalMaxFlow, originalMaxFlow};
        }

        int maxFlowReduction = 0;
        int bestNodeInIndex = -1;
        int minimoFlujo = originalMaxFlow;

        for (int nodeInIndex : NodosCandidatos) {
            resetGrafo();
            int nodeOutIndex = nodeInIndex + 1;

            grafo.get(nodeInIndex).put(nodeOutIndex, 0);

            int newMaxFlow = edmondsKarp(source, sink);

            int flowReduction = originalMaxFlow - newMaxFlow;

            if (flowReduction > maxFlowReduction) {
                maxFlowReduction = flowReduction;
                bestNodeInIndex = nodeInIndex;
                minimoFlujo = newMaxFlow;
            }
        }

        if (bestNodeInIndex == -1) {
            return new int[]{-1, originalMaxFlow, originalMaxFlow};
        }

        int bestNodeId = nodeIndexToId.get(bestNodeInIndex);
        return new int[]{bestNodeId, originalMaxFlow, minimoFlujo};
    }

    private List<Integer> findCandidateNodes() {
        Map<Integer, Integer> flowByNode = new HashMap<>();
        Map<Integer, Integer> capacityByNode = new HashMap<>();

        for (int nodeInIndex : calculadoras) {
            int nodeOutIndex = nodeInIndex + 1;

            int capacidadOriginal = originalGrafo.get(nodeInIndex).getOrDefault(nodeOutIndex, 0);
            int capacidadResidual = grafo.get(nodeInIndex).getOrDefault(nodeOutIndex, 0);
            int flujoEnviado = capacidadOriginal - capacidadResidual;

            flowByNode.put(nodeInIndex, flujoEnviado);
            capacityByNode.put(nodeInIndex, capacidadOriginal);
        }

        int cantidadMayorFlujo = Math.max(1, (int) (calculadoras.size() * porcentajeMayorFlujo));
        int cantidadFlujoCercano = Math.max(1, (int) (calculadoras.size() * porcentajeFlujoCercanoCapacidad));

        List<Integer> listaMayorFlujo = flowByNode.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(cantidadMayorFlujo)
                .map(Map.Entry::getKey)
                .toList();

        List<Integer> listaFlujoCercanoCapacidad = flowByNode.entrySet().stream()
                .sorted(Comparator.comparingDouble(entry ->
                        Math.abs(entry.getValue() - capacityByNode.get(entry.getKey()))))
                .limit(cantidadFlujoCercano)
                .map(Map.Entry::getKey)
                .toList();

        Set<Integer> nodosCandidatosSet = new HashSet<>();
        nodosCandidatosSet.addAll(listaMayorFlujo);
        nodosCandidatosSet.addAll(listaFlujoCercanoCapacidad);

        return new ArrayList<>(nodosCandidatosSet);
    }

    private void calcularPorcentajesDinamicos(int totalNodos) {
        // Parámetros ajustados a los valores objetivo
        double k = 1.0; // Escala inicial
        double a = 0.005; // Tasa de decrecimiento
        double c = 0.00000001; // Límite inferior
    
        // Calcular el porcentaje dinámico usando una función exponencial inversa
        porcentajeMayorFlujo = porcentajeFlujoCercanoCapacidad = k * Math.exp(-a * totalNodos) + c;
    
    }
    

    static boolean puedenComunicarse(int tipoOrigen, int tipoDestino) {
        if (tipoOrigen == 1 && tipoDestino == 2) return true;
        return tipoOrigen == 2 && (tipoDestino == 2 || tipoDestino == 3);
    }
}
