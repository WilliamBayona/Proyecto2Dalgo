import java.util.*;
import java.lang.Math;

public class ProblemaP2Final {

    private int n; // Número de nodos
    private Map<Integer, Map<Integer, Integer>> originalGrafo; // Grafo original 
    private Map<Integer, Map<Integer, Integer>> grafo; // Grafo sobre el cual se corren las simulaciones
    private List<Integer> calculadoras; // Lista de celulas calculadoras

    // Mapeo de IDs de células a índices de nodos (Para acceder mas facil)
    private Map<Integer, Integer> idToNodeIndex;
    private Map<Integer, Integer> nodeIndexToId;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ProblemaP2 problema = new ProblemaP2();

        int casos = sc.nextInt();
        sc.nextLine(); // salto de línea

        // Guardamos en una lista todos los casos de prueba
        List<CasoDePrueba> listaDeCasos = new ArrayList<>();

        for (int c = 0; c < casos; c++) {
            // Leer n y d
            int n = sc.nextInt();
            double d = sc.nextDouble();
            sc.nextLine(); // salto de línea

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
            CasoDePrueba caso = new CasoDePrueba(celulas, d);
            listaDeCasos.add(caso);
        }

        // Procesar todos los casos de prueba y almacenar los resultados
        List<int[]> resultados = new ArrayList<>();
        for (CasoDePrueba caso : listaDeCasos) {
            int[] resultado = problema.resolverCaso(caso);
            resultados.add(resultado);
        }

        // Imprimir todos los resultados
        for (int[] resultado : resultados) {
            System.out.println(resultado[0] + " " + resultado[1] + " " + resultado[2]);
        }
    }

    // Clase para representar un caso de prueba
    static class CasoDePrueba {
        List<Celula> celulas; // Lista de células
        double d; // Distancia máxima de comunicación

        public CasoDePrueba(List<Celula> celulas, double d) {
            this.celulas = celulas;
            this.d = d;
        }
    }

    // Clase Celula
    static class Celula {
        int id; // Identificador
        double x, y; // Coordenadas
        int tipo; // 1: Iniciadora, 2: Calculadora, 3: Ejecutora
        Set<String> peptidos;
        int nodeInIndex; // Índice del nodo de entrada
        int nodeOutIndex; // Índice del nodo de salida

        // Constructor
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

    // Método para verificar si pueden comunicarse, retorna true si pueden, false en caso contrario
    static boolean puedenComunicarse(int tipoOrigen, int tipoDestino) {
        if (tipoOrigen == 1 && tipoDestino == 2) { // Iniciadora -> Calculadora
            return true;
        }
        if (tipoOrigen == 2 && (tipoDestino == 2 || tipoDestino == 3)) { // Calculadora -> Calculadora o Ejecutora
            return true;
        }
        return false;
    }

    // Método para resolver cada caso de prueba
    public int[] resolverCaso(CasoDePrueba caso) {
        List<Celula> celulas = caso.celulas;
        double d = caso.d;

        // Mapear IDs de células a índices de nodos
        idToNodeIndex = new HashMap<>();
        nodeIndexToId = new HashMap<>();
        int nodeIndex = 0;

        for (Celula celula : celulas) {
            if (celula.tipo == 2) {
                // Células calculadoras se dividen en dos nodos
                celula.nodeInIndex = nodeIndex++; 
                celula.nodeOutIndex = nodeIndex++;
            } else {
                // Otros tipos de células utilizan un solo nodo
                celula.nodeInIndex = nodeIndex++; 
                celula.nodeOutIndex = celula.nodeInIndex;
            }
            idToNodeIndex.put(celula.id, celula.nodeInIndex); // Mapear ID al nodo de entrada
            nodeIndexToId.put(celula.nodeInIndex, celula.id);
        }

        n = nodeIndex; // Actualizamos n al número total de nodos
        originalGrafo = new HashMap<>(); 
        calculadoras = new ArrayList<>();

        // Crear lista de adyacencia vacía
        for (int i = 0; i < n; i++) {
            originalGrafo.put(i, new HashMap<>());
        }

        // Conectar nodos de entrada y salida para células calculadoras
        int maxCapacity = Integer.MAX_VALUE;

        for (Celula celula : celulas) {
            if (celula.tipo == 2) {
                // Conectar nodo de entrada con nodo de salida
                originalGrafo.get(celula.nodeInIndex).put(celula.nodeOutIndex, maxCapacity);
                // Añadir la célula calculadora a la lista para el bloqueo
                calculadoras.add(celula.nodeInIndex);
            }
        }

        // Construcción de la cuadrícula
        double tamañoCelda = d;
        Map<Long, List<Celula>> grid = new HashMap<>();

        for (Celula celula : celulas) {
            long gridX = (long) (celula.x / tamañoCelda);
            long gridY = (long) (celula.y / tamañoCelda);
            long key = (gridX << 32) + gridY;
            grid.computeIfAbsent(key, k -> new ArrayList<>()).add(celula);
        }

        // Crear las aristas dirigidas según las reglas de comunicación
        for (Celula origen : celulas) {
            long gridX = (long) (origen.x / tamañoCelda);
            long gridY = (long) (origen.y / tamañoCelda);

            int origenOutIndex = origen.nodeOutIndex;

            for (long dx = -1; dx <= 1; dx++) {
                for (long dy = -1; dy <= 1; dy++) {
                    long vecinoX = gridX + dx;
                    long vecinoY = gridY + dy;
                    long keyVecino = (vecinoX << 32) + vecinoY;

                    if (grid.containsKey(keyVecino)) {
                        for (Celula destino : grid.get(keyVecino)) {
                            if (origen.id == destino.id) continue;

                            // Verificar si pueden comunicarse según las reglas
                            if (!puedenComunicarse(origen.tipo, destino.tipo)) {
                                continue;
                            }

                            double distancia = Math.hypot(origen.x - destino.x, origen.y - destino.y);

                            if (distancia <= d) {
                                Set<String> peptidosCompartidos = new HashSet<>(origen.peptidos);
                                peptidosCompartidos.retainAll(destino.peptidos);
                                int capacidad = peptidosCompartidos.size();

                                if (capacidad > 0) {
                                    int destinoInIndex = destino.nodeInIndex;

                                    // Agregar arista dirigida desde origenOutIndex a destinoInIndex
                                    originalGrafo.get(origenOutIndex).put(destinoInIndex, capacidad);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Identificar el super source y super sink
        int superSource = n;
        int superSink = n + 1;
        n = n + 2; // Actualizar el número total de nodos

        // Añadir los nuevos nodos al grafo
        originalGrafo.put(superSource, new HashMap<>());
        originalGrafo.put(superSink, new HashMap<>());

        // Conectar el super source a todas las células iniciadoras
        for (Celula celula : celulas) {
            if (celula.tipo == 1) {
                int celulaIndex = celula.nodeInIndex;
                originalGrafo.get(superSource).put(celulaIndex, maxCapacity);
            }
        }

        // Conectar todas las células ejecutoras al super sink
        for (Celula celula : celulas) {
            if (celula.tipo == 3) {
                int celulaIndex = celula.nodeInIndex;
                originalGrafo.get(celulaIndex).put(superSink, maxCapacity);
            }
        }

        // Clonar el grafo original para usarlo en los cálculos
        resetGrafo();

        // Llamar a la función para encontrar el mejor nodo a bloquear
        int[] resultado = findBestNodeToBlock(superSource, superSink, calculadoras);

        // Devolver el resultado
        return resultado;
    }

    // Métodos auxiliares (bfs, edmondsKarp, blockNode, findBestNodeToBlock, resetGrafo)

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

    public void blockNode(int nodeInIndex) {
        // Bloquear la arista entre nodeInIndex y nodeOutIndex
        this.grafo.get(nodeInIndex).put(nodeInIndex + 1, 0); 
    }

    public void resetGrafo() {
        this.grafo = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : originalGrafo.entrySet()) {
            this.grafo.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
    }

    public int[] findBestNodeToBlock(int source, int sink, List<Integer> calculatorNodes) {
        resetGrafo();
        int originalMaxFlow = this.edmondsKarp(source, sink);

        int minimoFlujo = originalMaxFlow;
        int bestNodeInIndex = -1;

        for (int nodeInIndex : calculatorNodes) {
            resetGrafo();
            blockNode(nodeInIndex);

            int newMaxFlow = this.edmondsKarp(source, sink);

            if (newMaxFlow < minimoFlujo) {
                minimoFlujo = newMaxFlow;
                bestNodeInIndex = nodeInIndex;
            } else if (newMaxFlow == minimoFlujo) {
                int currentBestNodeId = nodeIndexToId.get(bestNodeInIndex);
                int newNodeId = nodeIndexToId.get(nodeInIndex);
                if (newNodeId > currentBestNodeId) {
                    bestNodeInIndex = nodeInIndex;
                }
            }
        }

        int bestNodeId = nodeIndexToId.get(bestNodeInIndex);

        return new int[]{bestNodeId, originalMaxFlow, minimoFlujo};
    }
}
