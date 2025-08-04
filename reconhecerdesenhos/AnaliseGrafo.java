package reconhecerdesenhos;

import java.util.*;

public class AnaliseGrafo {

    // Constante para o threshold de conexão
    private static final double THRESHOLD_CONEXAO = 30.0;

    public static class Componente {
        List<Bolinha> bolinhas;
        Set<Integer> indices;

        public Componente() {
            this.bolinhas = new ArrayList<>();
            this.indices = new HashSet<>();
        }

        public void adicionarBolinha(Bolinha b, int index) {
            bolinhas.add(b);
            indices.add(index);
        }

        public int size() {
            return bolinhas.size();
        }
    }

    public static class Grafo {
        boolean[][] adjacencia;
        int numVertices;

        public Grafo(int numVertices) {
            this.numVertices = numVertices;
            this.adjacencia = new boolean[numVertices][numVertices];
        }

        public void adicionarAresta(int i, int j) {
            if (i >= 0 && i < numVertices && j >= 0 && j < numVertices) {
                adjacencia[i][j] = true;
                adjacencia[j][i] = true; // Grafo não direcionado
            }
        }

        public boolean saoAdjacentes(int i, int j) {
            if (i >= 0 && i < numVertices && j >= 0 && j < numVertices) {
                return adjacencia[i][j];
            }
            return false;
        }

        public int calcularGrau(int vertice) {
            if (vertice < 0 || vertice >= numVertices)
                return 0;

            int grau = 0;
            for (int j = 0; j < numVertices; j++) {
                if (adjacencia[vertice][j]) {
                    grau++;
                }
            }
            return grau;
        }
    }

    public int[] analisarGrafoInvariante(Bolinha[] bolinhas) {
        // 1. Constrói grafo de conectividade
        Grafo grafo = construirGrafo(bolinhas);

        // 2. Encontra componentes conectados
        List<Componente> componentes = encontrarComponentes(grafo, bolinhas);

        // 3. Analisa propriedades invariantes
        return analisarPropriedadesInvariantes(componentes, grafo);
    }

    private Grafo construirGrafo(Bolinha[] bolinhas) {
        int numVertices = 0;
        for (Bolinha b : bolinhas) {
            if (b != null)
                numVertices++;
        }

        Grafo grafo = new Grafo(numVertices);

        // Mapeia índices das bolinhas não-nulas
        int[] indices = new int[bolinhas.length];
        int contador = 0;
        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null) {
                indices[i] = contador++;
            } else {
                indices[i] = -1;
            }
        }

        // Adiciona arestas entre bolinhas próximas
        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] == null)
                continue;

            for (int j = i + 1; j < bolinhas.length; j++) {
                if (bolinhas[j] == null)
                    continue;

                double distancia = calcularDistancia(bolinhas[i], bolinhas[j]);
                if (distancia <= THRESHOLD_CONEXAO) { // Threshold de 50 pixels
                    grafo.adicionarAresta(indices[i], indices[j]);
                }
            }
        }

        return grafo;
    }

    private double calcularDistancia(Bolinha a, Bolinha b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    private List<Componente> encontrarComponentes(Grafo grafo, Bolinha[] bolinhas) {
        List<Componente> componentes = new ArrayList<>();
        boolean[] visitado = new boolean[grafo.numVertices];

        // Mapeia índices das bolinhas não-nulas
        int[] indices = new int[bolinhas.length];
        int contador = 0;
        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null) {
                indices[i] = contador++;
            } else {
                indices[i] = -1;
            }
        }

        for (int i = 0; i < grafo.numVertices; i++) {
            if (!visitado[i]) {
                Componente componente = new Componente();
                dfsComponente(grafo, i, visitado, componente, bolinhas, indices);
                if (componente.size() > 0) {
                    componentes.add(componente);
                }
            }
        }

        return componentes;
    }

    private void dfsComponente(Grafo grafo, int vertice, boolean[] visitado,
            Componente componente, Bolinha[] bolinhas, int[] indices) {
        visitado[vertice] = true;

        // Encontra a bolinha correspondente ao vértice
        for (int i = 0; i < bolinhas.length; i++) {
            if (indices[i] == vertice && bolinhas[i] != null) {
                componente.adicionarBolinha(bolinhas[i], i);
                break;
            }
        }

        // Visita vértices adjacentes
        for (int j = 0; j < grafo.numVertices; j++) {
            if (grafo.saoAdjacentes(vertice, j) && !visitado[j]) {
                dfsComponente(grafo, j, visitado, componente, bolinhas, indices);
            }
        }
    }

    private int[] analisarPropriedadesInvariantes(List<Componente> componentes, Grafo grafo) {
        int[] features = new int[20];

        if (componentes.isEmpty()) {
            return features; // Retorna array de zeros
        }

        // Propriedades que não dependem de orientação
        features[0] = componentes.size(); // Número de partes
        features[1] = encontrarMaiorComponente(componentes); // Tamanho da maior parte
        features[2] = calcularGrauMedio(grafo); // Conectividade média
        features[3] = detectarCiclos(grafo); // Presença de ciclos
        features[4] = calcularDiametro(componentes); // Distância máxima

        // Análise de proporções relativas
        features[5] = calcularProporcaoMaiorMenor(componentes);
        features[6] = calcularProporcaoCentralPeriferico(componentes);
        features[7] = detectarSimetria(componentes);
        features[8] = calcularDensidade(componentes);
        features[9] = detectarRegularidade(componentes);

        // Análise de conectividade específica
        features[10] = calcularConectividadeInterna(componentes);
        features[11] = calcularConectividadeExterna(componentes);
        features[12] = detectarEstrela(grafo, componentes);
        features[13] = detectarLinha(grafo, componentes);
        features[14] = detectarArvore(grafo, componentes);

        // Análise de distribuição
        features[15] = calcularDistribuicaoUniforme(componentes);
        features[16] = calcularDistribuicaoHierarquica(componentes);
        features[17] = calcularDistribuicaoRadial(componentes);
        features[18] = calcularDistribuicaoLinear(componentes);
        features[19] = calcularDistribuicaoCluster(componentes);

        return features;
    }

    private int encontrarMaiorComponente(List<Componente> componentes) {
        if (componentes.isEmpty())
            return 0;

        int maior = 0;
        for (Componente componente : componentes) {
            if (componente.size() > maior) {
                maior = componente.size();
            }
        }

        return maior;
    }

    private int calcularGrauMedio(Grafo grafo) {
        if (grafo.numVertices == 0)
            return 0;

        int somaGraus = 0;
        for (int i = 0; i < grafo.numVertices; i++) {
            somaGraus += grafo.calcularGrau(i);
        }

        return somaGraus / grafo.numVertices;
    }

    private int detectarCiclos(Grafo grafo) {
        // Detecta se há ciclos no grafo
        boolean[] visitado = new boolean[grafo.numVertices];
        boolean[] pilhaRecursao = new boolean[grafo.numVertices];

        for (int i = 0; i < grafo.numVertices; i++) {
            if (!visitado[i]) {
                if (dfsCiclo(grafo, i, visitado, pilhaRecursao)) {
                    return 1; // Ciclo encontrado
                }
            }
        }

        return 0; // Sem ciclos
    }

    private boolean dfsCiclo(Grafo grafo, int vertice, boolean[] visitado, boolean[] pilhaRecursao) {
        visitado[vertice] = true;
        pilhaRecursao[vertice] = true;

        for (int j = 0; j < grafo.numVertices; j++) {
            if (grafo.saoAdjacentes(vertice, j)) {
                if (!visitado[j]) {
                    if (dfsCiclo(grafo, j, visitado, pilhaRecursao)) {
                        return true;
                    }
                } else if (pilhaRecursao[j]) {
                    return true; // Ciclo encontrado
                }
            }
        }

        pilhaRecursao[vertice] = false;
        return false;
    }

    private int calcularDiametro(List<Componente> componentes) {
        if (componentes.size() < 2)
            return 0;

        double maiorDistancia = 0;
        for (int i = 0; i < componentes.size(); i++) {
            for (int j = i + 1; j < componentes.size(); j++) {
                double distancia = calcularDistanciaComponentes(componentes.get(i), componentes.get(j));
                if (distancia > maiorDistancia) {
                    maiorDistancia = distancia;
                }
            }
        }

        return (int) maiorDistancia;
    }

    private double calcularDistanciaComponentes(Componente a, Componente b) {
        if (a.bolinhas.isEmpty() || b.bolinhas.isEmpty())
            return 0;

        double menorDistancia = Double.MAX_VALUE;
        for (Bolinha ba : a.bolinhas) {
            for (Bolinha bb : b.bolinhas) {
                double distancia = calcularDistancia(ba, bb);
                if (distancia < menorDistancia) {
                    menorDistancia = distancia;
                }
            }
        }

        return menorDistancia;
    }

    private int calcularProporcaoMaiorMenor(List<Componente> componentes) {
        if (componentes.size() < 2)
            return 0;

        int maior = 0, menor = Integer.MAX_VALUE;
        for (Componente componente : componentes) {
            if (componente.size() > maior)
                maior = componente.size();
            if (componente.size() < menor)
                menor = componente.size();
        }

        return menor > 0 ? maior / menor : 0;
    }

    private int calcularProporcaoCentralPeriferico(List<Componente> componentes) {
        if (componentes.size() < 2)
            return 0;

        // Calcula centro de massa geral
        double centroX = 0, centroY = 0;
        int totalBolinhas = 0;

        for (Componente componente : componentes) {
            for (Bolinha b : componente.bolinhas) {
                centroX += b.x;
                centroY += b.y;
                totalBolinhas++;
            }
        }

        if (totalBolinhas == 0)
            return 0;
        centroX /= totalBolinhas;
        centroY /= totalBolinhas;

        // Conta componentes centrais vs periféricos
        int centrais = 0, perifericos = 0;
        for (Componente componente : componentes) {
            double distanciaMedia = 0;
            for (Bolinha b : componente.bolinhas) {
                distanciaMedia += Math.sqrt(Math.pow(b.x - centroX, 2) + Math.pow(b.y - centroY, 2));
            }
            distanciaMedia /= componente.size();

            if (distanciaMedia < 100)
                centrais++;
            else
                perifericos++;
        }

        return perifericos > 0 ? centrais / perifericos : centrais;
    }

    private int detectarSimetria(List<Componente> componentes) {
        if (componentes.size() < 2)
            return 0;

        // Calcula centro de massa
        double centroX = 0, centroY = 0;
        int totalBolinhas = 0;

        for (Componente componente : componentes) {
            for (Bolinha b : componente.bolinhas) {
                centroX += b.x;
                centroY += b.y;
                totalBolinhas++;
            }
        }

        if (totalBolinhas == 0)
            return 0;
        centroX /= totalBolinhas;
        centroY /= totalBolinhas;

        // Verifica simetria em relação ao centro
        int esquerda = 0, direita = 0;
        for (Componente componente : componentes) {
            double centroComponenteX = 0;
            for (Bolinha b : componente.bolinhas) {
                centroComponenteX += b.x;
            }
            centroComponenteX /= componente.size();

            if (centroComponenteX < centroX)
                esquerda++;
            else
                direita++;
        }

        return Math.abs(esquerda - direita) <= 1 ? 1 : 0;
    }

    private int calcularDensidade(List<Componente> componentes) {
        if (componentes.isEmpty())
            return 0;

        int totalBolinhas = 0;
        for (Componente componente : componentes) {
            totalBolinhas += componente.size();
        }

        return totalBolinhas / componentes.size();
    }

    private int detectarRegularidade(List<Componente> componentes) {
        if (componentes.size() < 2)
            return 0;

        // Verifica se os componentes têm tamanhos similares
        int primeiroTamanho = componentes.get(0).size();
        for (Componente componente : componentes) {
            if (Math.abs(componente.size() - primeiroTamanho) > 2) {
                return 0; // Irregular
            }
        }

        return 1; // Regular
    }

    private int calcularConectividadeInterna(List<Componente> componentes) {
        if (componentes.isEmpty())
            return 0;

        int totalConexoes = 0;
        for (Componente componente : componentes) {
            if (componente.size() > 1) {
                totalConexoes += componente.size() - 1; // N-1 conexões para conectar N bolinhas
            }
        }

        return totalConexoes;
    }

    private int calcularConectividadeExterna(List<Componente> componentes) {
        if (componentes.size() < 2)
            return 0;

        // Conta conexões entre componentes
        int conexoes = 0;
        for (int i = 0; i < componentes.size(); i++) {
            for (int j = i + 1; j < componentes.size(); j++) {
                if (calcularDistanciaComponentes(componentes.get(i), componentes.get(j)) < 80) {
                    conexoes++;
                }
            }
        }

        return conexoes;
    }

    private int detectarEstrela(Grafo grafo, List<Componente> componentes) {
        if (componentes.size() < 2)
            return 0;

        // Procura por um vértice central com alto grau
        for (int i = 0; i < grafo.numVertices; i++) {
            if (grafo.calcularGrau(i) >= componentes.size() - 1) {
                return 1; // Padrão estrela detectado
            }
        }

        return 0;
    }

    private int detectarLinha(Grafo grafo, List<Componente> componentes) {
        if (componentes.size() < 3)
            return 0;

        // Verifica se há um caminho linear
        int verticesGrau2 = 0;
        int verticesGrau1 = 0;

        for (int i = 0; i < grafo.numVertices; i++) {
            int grau = grafo.calcularGrau(i);
            if (grau == 1)
                verticesGrau1++;
            else if (grau == 2)
                verticesGrau2++;
        }

        // Padrão linear: 2 vértices grau 1, resto grau 2
        return (verticesGrau1 == 2 && verticesGrau2 >= componentes.size() - 2) ? 1 : 0;
    }

    private int detectarArvore(Grafo grafo, List<Componente> componentes) {
        if (componentes.size() < 2)
            return 0;

        // Verifica se é uma árvore (sem ciclos e conectado)
        if (detectarCiclos(grafo) == 0 && calcularConectividadeExterna(componentes) > 0) {
            return 1;
        }

        return 0;
    }

    private int calcularDistribuicaoUniforme(List<Componente> componentes) {
        if (componentes.size() < 3)
            return 0;

        // Verifica se os componentes estão distribuídos uniformemente
        double[] distancias = new double[componentes.size()];
        for (int i = 0; i < componentes.size(); i++) {
            for (int j = i + 1; j < componentes.size(); j++) {
                distancias[i] += calcularDistanciaComponentes(componentes.get(i), componentes.get(j));
            }
        }

        // Calcula variância das distâncias
        double media = 0;
        for (double d : distancias)
            media += d;
        media /= distancias.length;

        double variancia = 0;
        for (double d : distancias)
            variancia += Math.pow(d - media, 2);
        variancia /= distancias.length;

        return variancia < 1000 ? 1 : 0; // Baixa variância = distribuição uniforme
    }

    private int calcularDistribuicaoHierarquica(List<Componente> componentes) {
        if (componentes.size() < 3)
            return 0;

        // Verifica se há um componente central conectado a outros
        for (Componente componente : componentes) {
            int conexoes = 0;
            for (Componente outro : componentes) {
                if (outro != componente && calcularDistanciaComponentes(componente, outro) < 80) {
                    conexoes++;
                }
            }
            if (conexoes >= 2)
                return 1; // Padrão hierárquico
        }

        return 0;
    }

    private int calcularDistribuicaoRadial(List<Componente> componentes) {
        if (componentes.size() < 3)
            return 0;

        // Calcula centro de massa
        double centroX = 0, centroY = 0;
        int totalBolinhas = 0;

        for (Componente componente : componentes) {
            for (Bolinha b : componente.bolinhas) {
                centroX += b.x;
                centroY += b.y;
                totalBolinhas++;
            }
        }

        if (totalBolinhas == 0)
            return 0;
        centroX /= totalBolinhas;
        centroY /= totalBolinhas;

        // Verifica se os componentes estão distribuídos radialmente
        int radiais = 0;
        for (Componente componente : componentes) {
            double distanciaMedia = 0;
            for (Bolinha b : componente.bolinhas) {
                distanciaMedia += Math.sqrt(Math.pow(b.x - centroX, 2) + Math.pow(b.y - centroY, 2));
            }
            distanciaMedia /= componente.size();

            if (distanciaMedia > 50)
                radiais++;
        }

        return radiais >= componentes.size() / 2 ? 1 : 0;
    }

    private int calcularDistribuicaoLinear(List<Componente> componentes) {
        if (componentes.size() < 3)
            return 0;

        // Verifica se os componentes estão alinhados
        double[] xs = new double[componentes.size()];
        double[] ys = new double[componentes.size()];

        for (int i = 0; i < componentes.size(); i++) {
            double centroX = 0, centroY = 0;
            for (Bolinha b : componentes.get(i).bolinhas) {
                centroX += b.x;
                centroY += b.y;
            }
            xs[i] = centroX / componentes.get(i).size();
            ys[i] = centroY / componentes.get(i).size();
        }

        // Calcula correlação linear
        double correlacao = calcularCorrelacao(xs, ys);
        return Math.abs(correlacao) > 0.7 ? 1 : 0;
    }

    private int calcularDistribuicaoCluster(List<Componente> componentes) {
        if (componentes.size() < 2)
            return 0;

        // Verifica se há clusters (grupos próximos)
        int clusters = 0;
        for (int i = 0; i < componentes.size(); i++) {
            for (int j = i + 1; j < componentes.size(); j++) {
                if (calcularDistanciaComponentes(componentes.get(i), componentes.get(j)) < 60) {
                    clusters++;
                }
            }
        }

        return clusters > 0 ? 1 : 0;
    }

    private double calcularCorrelacao(double[] x, double[] y) {
        if (x.length != y.length || x.length < 2)
            return 0;

        double mediaX = 0, mediaY = 0;
        for (int i = 0; i < x.length; i++) {
            mediaX += x[i];
            mediaY += y[i];
        }
        mediaX /= x.length;
        mediaY /= y.length;

        double numerador = 0, denominadorX = 0, denominadorY = 0;
        for (int i = 0; i < x.length; i++) {
            double dx = x[i] - mediaX;
            double dy = y[i] - mediaY;
            numerador += dx * dy;
            denominadorX += dx * dx;
            denominadorY += dy * dy;
        }

        if (denominadorX == 0 || denominadorY == 0)
            return 0;
        return numerador / Math.sqrt(denominadorX * denominadorY);
    }
}