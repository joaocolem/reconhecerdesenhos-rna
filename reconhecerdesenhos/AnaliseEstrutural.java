package reconhecerdesenhos;

import java.util.*;

public class AnaliseEstrutural {

    /**
     * Analisa o boneco palito baseado em grau e distância em nós para cada bolinha
     * Nova abordagem: cada bolinha é uma entrada com [grau,
     * distância_ao_nó_maior_grau]
     */
    public double[] analisarBonecoPalitoPorBolinha(Bolinha[] bolinhas) {
        // 1. Remove bolinhas desconectadas (ruído)
        Bolinha[] bolinhasFiltradas = removerBolinhasDesconectadas(bolinhas);

        // 2. Faz crop inteligente (centraliza e recorta área útil)
        Bolinha[] bolinhasCropadas = fazerCropInteligente(bolinhasFiltradas);

        // 3. Normaliza orientação (cabeça sempre para cima)
        Bolinha[] bolinhasNormalizadas = normalizarOrientacao(bolinhasCropadas);

        // 4. Calcula features por bolinha: [grau, distância_ao_nó_maior_grau]
        return calcularFeaturesPorBolinha(bolinhasNormalizadas);
    }

    /**
     * Calcula features para cada bolinha: grau e distância em nós ao nó de maior
     * grau
     * Retorna um vetor com 80 elementos (40 bolinhas * 2 features cada)
     */
    private double[] calcularFeaturesPorBolinha(Bolinha[] bolinhas) {
        int maxBolinhas = 40; // Máximo de bolinhas suportadas
        double[] features = new double[maxBolinhas * 2]; // 2 features por bolinha

        // Inicializa com zeros
        for (int i = 0; i < features.length; i++) {
            features[i] = 0.0;
        }

        // 1. Calcula o grau de cada bolinha
        int[] graus = calcularGrausBolinhas(bolinhas);

        // 2. Encontra o nó de maior grau (centro do boneco)
        int indiceMaiorGrau = encontrarNodoMaiorGrau(graus);

        if (indiceMaiorGrau == -1) {
            return features; // Nenhuma bolinha válida
        }

        // 3. Calcula distância em nós para cada bolinha até o nó de maior grau
        int[] distancias = calcularDistanciasEmNos(bolinhas, indiceMaiorGrau);

        // 4. Monta o vetor de features: [grau_1, dist_1, grau_2, dist_2, ...]
        int contadorBolinhas = 0;
        for (int i = 0; i < bolinhas.length && contadorBolinhas < maxBolinhas; i++) {
            if (bolinhas[i] != null) {
                features[contadorBolinhas * 2] = graus[i]; // Grau
                features[contadorBolinhas * 2 + 1] = distancias[i]; // Distância em nós
                contadorBolinhas++;
            }
        }

        // Debug
        System.out.println("Features por bolinha: " + contadorBolinhas + " bolinhas processadas");
        System.out.println("Nó de maior grau: índice " + indiceMaiorGrau + " com grau " + graus[indiceMaiorGrau]);

        return features;
    }

    /**
     * Calcula o grau de cada bolinha (número de conexões diretas)
     */
    private int[] calcularGrausBolinhas(Bolinha[] bolinhas) {
        int[] graus = new int[bolinhas.length];

        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null) {
                int grau = 0;
                for (int j = 0; j < bolinhas.length; j++) {
                    if (i != j && bolinhas[j] != null) {
                        double distancia = calcularDistancia(bolinhas[i], bolinhas[j]);
                        if (distancia <= 40) { // Threshold para conexão
                            grau++;
                        }
                    }
                }
                graus[i] = grau;
            } else {
                graus[i] = 0;
            }
        }

        return graus;
    }

    /**
     * Encontra o índice da bolinha com maior grau
     */
    private int encontrarNodoMaiorGrau(int[] graus) {
        int indiceMaior = -1;
        int maiorGrau = -1;

        for (int i = 0; i < graus.length; i++) {
            if (graus[i] > maiorGrau) {
                maiorGrau = graus[i];
                indiceMaior = i;
            }
        }

        return indiceMaior;
    }

    /**
     * Calcula a distância em nós (número de saltos) de cada bolinha até o nó de
     * maior grau
     * Usa BFS (Breadth-First Search) para encontrar o caminho mais curto
     */
    private int[] calcularDistanciasEmNos(Bolinha[] bolinhas, int indiceNodoCentral) {
        int[] distancias = new int[bolinhas.length];
        boolean[] visitado = new boolean[bolinhas.length];

        // Inicializa com -1 (inacessível)
        for (int i = 0; i < distancias.length; i++) {
            distancias[i] = -1;
        }

        // BFS para calcular distâncias
        Queue<Integer> fila = new LinkedList<>();
        fila.add(indiceNodoCentral);
        visitado[indiceNodoCentral] = true;
        distancias[indiceNodoCentral] = 0; // Distância até si mesmo é 0

        while (!fila.isEmpty()) {
            int atual = fila.poll();

            // Visita todos os vizinhos
            for (int vizinho = 0; vizinho < bolinhas.length; vizinho++) {
                if (vizinho != atual && bolinhas[vizinho] != null && !visitado[vizinho]) {
                    // Verifica se são conectados
                    double distancia = calcularDistancia(bolinhas[atual], bolinhas[vizinho]);
                    if (distancia <= 40) { // Threshold para conexão
                        distancias[vizinho] = distancias[atual] + 1;
                        visitado[vizinho] = true;
                        fila.add(vizinho);
                    }
                }
            }
        }

        // Se alguma bolinha não foi visitada, significa que não está conectada
        // Define distância como um valor alto (ex: 999)
        for (int i = 0; i < distancias.length; i++) {
            if (distancias[i] == -1) {
                distancias[i] = 999; // Bolinha desconectada
            }
        }

        return distancias;
    }

    /**
     * Calcula o centro de massa das bolinhas
     */
    private double[] calcularCentroMassa(Bolinha[] bolinhas) {
        double somaX = 0, somaY = 0;
        int contador = 0;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                somaX += b.x;
                somaY += b.y;
                contador++;
            }
        }

        if (contador == 0)
            return new double[] { 0, 0 };
        return new double[] { somaX / contador, somaY / contador };
    }

    /**
     * Agrupa bolinhas por proximidade
     */
    private List<List<Bolinha>> agruparPorProximidade(Bolinha[] bolinhas) {
        List<List<Bolinha>> grupos = new ArrayList<>();
        boolean[] visitado = new boolean[bolinhas.length];

        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null && !visitado[i]) {
                List<Bolinha> grupo = new ArrayList<>();
                dfsAgrupar(bolinhas, i, grupo, visitado, 35.0); // Reduzido de 50.0 para 35.0
                if (!grupo.isEmpty()) {
                    grupos.add(grupo);
                }
            }
        }

        return grupos;
    }

    /**
     * DFS para agrupar bolinhas próximas
     */
    private void dfsAgrupar(Bolinha[] bolinhas, int index, List<Bolinha> grupo, boolean[] visitado, double threshold) {
        if (index < 0 || index >= bolinhas.length || bolinhas[index] == null || visitado[index]) {
            return;
        }

        visitado[index] = true;
        grupo.add(bolinhas[index]);

        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null && !visitado[i]) {
                double distancia = calcularDistancia(bolinhas[index], bolinhas[i]);
                if (distancia <= threshold) {
                    dfsAgrupar(bolinhas, i, grupo, visitado, threshold);
                }
            }
        }
    }

    /**
     * Calcula distância entre duas bolinhas
     */
    private double calcularDistancia(Bolinha a, Bolinha b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    /**
     * Encontra a parte mais alta (cabeça)
     */
    private int encontrarParteMaisAlta(List<List<Bolinha>> grupos) {
        if (grupos.isEmpty())
            return -1;

        int indiceMaisAlto = -1;
        double yMaisAlto = Double.MAX_VALUE;

        for (int i = 0; i < grupos.size(); i++) {
            double yMedio = calcularYMedio(grupos.get(i));
            if (yMedio < yMaisAlto) {
                yMaisAlto = yMedio;
                indiceMaisAlto = i;
            }
        }

        return indiceMaisAlto;
    }

    /**
     * Encontra a parte central (tronco)
     */
    private int encontrarParteCentral(List<List<Bolinha>> grupos, double[] centro) {
        if (grupos.isEmpty())
            return -1;

        int indiceMaisCentral = -1;
        double menorDistancia = Double.MAX_VALUE;

        for (int i = 0; i < grupos.size(); i++) {
            double[] centroGrupo = calcularCentroGrupo(grupos.get(i));
            double distancia = Math
                    .sqrt(Math.pow(centroGrupo[0] - centro[0], 2) + Math.pow(centroGrupo[1] - centro[1], 2));
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                indiceMaisCentral = i;
            }
        }

        return indiceMaisCentral;
    }

    /**
     * Encontra partes laterais (braços)
     */
    private List<Integer> encontrarPartesLaterais(List<List<Bolinha>> grupos, int indiceTronco) {
        List<Integer> laterais = new ArrayList<>();

        if (indiceTronco < 0 || indiceTronco >= grupos.size())
            return laterais;

        double[] centroTronco = calcularCentroGrupo(grupos.get(indiceTronco));

        for (int i = 0; i < grupos.size(); i++) {
            if (i != indiceTronco) {
                double[] centroGrupo = calcularCentroGrupo(grupos.get(i));
                double distanciaX = Math.abs(centroGrupo[0] - centroTronco[0]);
                double distanciaY = Math.abs(centroGrupo[1] - centroTronco[1]);

                // Braços estão mais distantes horizontalmente que verticalmente
                // E devem estar aproximadamente na mesma altura do tronco
                if (distanciaX > distanciaY && distanciaX > 25 && distanciaY < 40) {
                    laterais.add(i);
                }
            }
        }

        return laterais;
    }

    /**
     * Encontra partes inferiores (pernas)
     */
    private List<Integer> encontrarPartesInferiores(List<List<Bolinha>> grupos, int indiceTronco) {
        List<Integer> inferiores = new ArrayList<>();

        if (indiceTronco < 0 || indiceTronco >= grupos.size())
            return inferiores;

        double[] centroTronco = calcularCentroGrupo(grupos.get(indiceTronco));

        for (int i = 0; i < grupos.size(); i++) {
            if (i != indiceTronco) {
                double[] centroGrupo = calcularCentroGrupo(grupos.get(i));
                double distanciaY = centroGrupo[1] - centroTronco[1];
                double distanciaX = Math.abs(centroGrupo[0] - centroTronco[0]);

                // Pernas estão abaixo do tronco e não muito distantes horizontalmente
                if (distanciaY > 25 && distanciaX < 50) {
                    inferiores.add(i);
                }
            }
        }

        return inferiores;
    }

    /**
     * Calcula Y médio de um grupo
     */
    private double calcularYMedio(List<Bolinha> grupo) {
        double somaY = 0;
        for (Bolinha b : grupo) {
            somaY += b.y;
        }
        return somaY / grupo.size();
    }

    /**
     * Calcula centro de um grupo
     */
    private double[] calcularCentroGrupo(List<Bolinha> grupo) {
        double somaX = 0, somaY = 0;
        for (Bolinha b : grupo) {
            somaX += b.x;
            somaY += b.y;
        }
        return new double[] { somaX / grupo.size(), somaY / grupo.size() };
    }

    /**
     * Calcula tamanho total dos braços
     */
    private int calcularTamanhoTotalBracos(List<List<Bolinha>> grupos, List<Integer> indicesBracos) {
        int total = 0;
        for (int indice : indicesBracos) {
            total += grupos.get(indice).size();
        }
        return total;
    }

    /**
     * Calcula tamanho total das pernas
     */
    private int calcularTamanhoTotalPernas(List<List<Bolinha>> grupos, List<Integer> indicesPernas) {
        int total = 0;
        for (int indice : indicesPernas) {
            total += grupos.get(indice).size();
        }
        return total;
    }

    /**
     * Verifica conectividade entre cabeça e tronco
     */
    private int verificarConectividadeCabecaTronco(List<List<Bolinha>> grupos, int indiceCabeca, int indiceTronco) {
        if (indiceCabeca < 0 || indiceTronco < 0)
            return 0;

        double[] centroCabeca = calcularCentroGrupo(grupos.get(indiceCabeca));
        double[] centroTronco = calcularCentroGrupo(grupos.get(indiceTronco));

        double distancia = Math
                .sqrt(Math.pow(centroCabeca[0] - centroTronco[0], 2) + Math.pow(centroCabeca[1] - centroTronco[1], 2));
        return distancia < 80 ? 1 : 0;
    }

    /**
     * Verifica conectividade entre tronco e braços
     */
    private int verificarConectividadeTroncoBracos(List<List<Bolinha>> grupos, int indiceTronco,
            List<Integer> indicesBracos) {
        if (indiceTronco < 0 || indicesBracos.isEmpty())
            return 0;

        int conectados = 0;
        double[] centroTronco = calcularCentroGrupo(grupos.get(indiceTronco));

        for (int indice : indicesBracos) {
            double[] centroBraco = calcularCentroGrupo(grupos.get(indice));
            double distancia = Math.sqrt(
                    Math.pow(centroTronco[0] - centroBraco[0], 2) + Math.pow(centroTronco[1] - centroBraco[1], 2));
            if (distancia < 80)
                conectados++;
        }

        return conectados;
    }

    /**
     * Verifica conectividade entre tronco e pernas
     */
    private int verificarConectividadeTroncoPernas(List<List<Bolinha>> grupos, int indiceTronco,
            List<Integer> indicesPernas) {
        if (indiceTronco < 0 || indicesPernas.isEmpty())
            return 0;

        int conectados = 0;
        double[] centroTronco = calcularCentroGrupo(grupos.get(indiceTronco));

        for (int indice : indicesPernas) {
            double[] centroPerna = calcularCentroGrupo(grupos.get(indice));
            double distancia = Math.sqrt(
                    Math.pow(centroTronco[0] - centroPerna[0], 2) + Math.pow(centroTronco[1] - centroPerna[1], 2));
            if (distancia < 80)
                conectados++;
        }

        return conectados;
    }

    /**
     * Calcula simetria dos braços
     */
    private int calcularSimetriaBracos(List<List<Bolinha>> grupos, List<Integer> indicesBracos) {
        if (indicesBracos.size() != 2)
            return 0;

        double[] centroTronco = calcularCentroGrupo(grupos.get(0)); // Assumindo que o primeiro grupo é o tronco
        double[] centroBraco1 = calcularCentroGrupo(grupos.get(indicesBracos.get(0)));
        double[] centroBraco2 = calcularCentroGrupo(grupos.get(indicesBracos.get(1)));

        double distancia1 = Math.abs(centroBraco1[0] - centroTronco[0]);
        double distancia2 = Math.abs(centroBraco2[0] - centroTronco[0]);

        return Math.abs(distancia1 - distancia2) < 20 ? 1 : 0;
    }

    /**
     * Calcula simetria das pernas
     */
    private int calcularSimetriaPernas(List<List<Bolinha>> grupos, List<Integer> indicesPernas) {
        if (indicesPernas.size() != 2)
            return 0;

        double[] centroTronco = calcularCentroGrupo(grupos.get(0)); // Assumindo que o primeiro grupo é o tronco
        double[] centroPerna1 = calcularCentroGrupo(grupos.get(indicesPernas.get(0)));
        double[] centroPerna2 = calcularCentroGrupo(grupos.get(indicesPernas.get(1)));

        double distancia1 = Math.abs(centroPerna1[0] - centroTronco[0]);
        double distancia2 = Math.abs(centroPerna2[0] - centroTronco[0]);

        return Math.abs(distancia1 - distancia2) < 20 ? 1 : 0;
    }

    /**
     * Calcula proporção cabeça/corpo
     */
    private int calcularProporcaoCabecaCorpo(int indiceCabeca, int indiceTronco, List<List<Bolinha>> grupos) {
        if (indiceCabeca < 0 || indiceTronco < 0)
            return 0;

        int tamanhoCabeca = grupos.get(indiceCabeca).size();
        int tamanhoTronco = grupos.get(indiceTronco).size();

        return tamanhoTronco > 0 ? (tamanhoCabeca * 10) / tamanhoTronco : 0;
    }

    /**
     * Calcula proporção braços/pernas
     */
    private int calcularProporcaoBracosPernas(List<Integer> indicesBracos, List<Integer> indicesPernas,
            List<List<Bolinha>> grupos) {
        int totalBracos = calcularTamanhoTotalBracos(grupos, indicesBracos);
        int totalPernas = calcularTamanhoTotalPernas(grupos, indicesPernas);

        return totalPernas > 0 ? (totalBracos * 10) / totalPernas : 0;
    }

    /**
     * Verifica estrutura geral do boneco
     */
    private int verificarEstruturaGeral(List<List<Bolinha>> grupos, int indiceCabeca, int indiceTronco,
            List<Integer> indicesBracos, List<Integer> indicesPernas) {
        // Verifica se tem pelo menos cabeça, tronco e alguma extremidade
        boolean temCabeca = indiceCabeca >= 0;
        boolean temTronco = indiceTronco >= 0;
        boolean temExtremidades = !indicesBracos.isEmpty() || !indicesPernas.isEmpty();

        return (temCabeca && temTronco && temExtremidades) ? 1 : 0;
    }

    /**
     * Remove bolinhas que não estão conectadas ao boneco principal
     * Filtra ruído e bolinhas isoladas
     */
    private Bolinha[] removerBolinhasDesconectadas(Bolinha[] bolinhas) {
        // 1. Encontra o maior componente conectado (o boneco principal)
        List<List<Bolinha>> componentes = encontrarComponentesConectados(bolinhas);

        // 2. Identifica o maior componente
        List<Bolinha> maiorComponente = encontrarMaiorComponente(componentes);

        // 3. Filtra apenas as bolinhas do maior componente
        Bolinha[] bolinhasFiltradas = new Bolinha[bolinhas.length];
        int indice = 0;

        for (Bolinha b : maiorComponente) {
            bolinhasFiltradas[indice++] = b;
        }

        // Debug: mostra quantas bolinhas foram removidas
        int bolinhasRemovidas = 0;
        for (Bolinha b : bolinhas) {
            if (b != null)
                bolinhasRemovidas++;
        }
        bolinhasRemovidas -= maiorComponente.size();

        if (bolinhasRemovidas > 0) {
            System.out.println("Removidas " + bolinhasRemovidas + " bolinhas desconectadas (ruído)");
        }

        return bolinhasFiltradas;
    }

    /**
     * Encontra todos os componentes conectados
     */
    private List<List<Bolinha>> encontrarComponentesConectados(Bolinha[] bolinhas) {
        List<List<Bolinha>> componentes = new ArrayList<>();
        boolean[] visitado = new boolean[bolinhas.length];

        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null && !visitado[i]) {
                List<Bolinha> componente = new ArrayList<>();
                dfsComponente(bolinhas, i, componente, visitado, 60.0); // Threshold maior para conexão
                if (!componente.isEmpty()) {
                    componentes.add(componente);
                }
            }
        }

        return componentes;
    }

    /**
     * DFS para encontrar um componente conectado
     */
    private void dfsComponente(Bolinha[] bolinhas, int index, List<Bolinha> componente, boolean[] visitado,
            double threshold) {
        if (index < 0 || index >= bolinhas.length || bolinhas[index] == null || visitado[index]) {
            return;
        }

        visitado[index] = true;
        componente.add(bolinhas[index]);

        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null && !visitado[i]) {
                double distancia = calcularDistancia(bolinhas[index], bolinhas[i]);
                if (distancia <= threshold) {
                    dfsComponente(bolinhas, i, componente, visitado, threshold);
                }
            }
        }
    }

    /**
     * Encontra o maior componente (o boneco principal)
     */
    private List<Bolinha> encontrarMaiorComponente(List<List<Bolinha>> componentes) {
        if (componentes.isEmpty())
            return new ArrayList<>();

        List<Bolinha> maior = componentes.get(0);
        for (List<Bolinha> componente : componentes) {
            if (componente.size() > maior.size()) {
                maior = componente;
            }
        }

        return maior;
    }

    /**
     * Normaliza a orientação do boneco (cabeça sempre para cima)
     * Detecta qual lado é "cima" baseado na densidade e posição
     */
    private Bolinha[] normalizarOrientacao(Bolinha[] bolinhas) {
        if (contarBolinhasValidas(bolinhas) < 3) {
            return bolinhas; // Muito poucas bolinhas para normalizar
        }

        // 1. Calcula o centro de massa
        double[] centro = calcularCentroMassa(bolinhas);

        // 2. Divide o boneco em 4 quadrantes
        double[] densidades = calcularDensidadesQuadrantes(bolinhas, centro);

        // 3. Determina qual lado é "cima" baseado na densidade
        int ladoCima = determinarLadoCima(densidades);

        // 4. Rotaciona as bolinhas se necessário
        return rotacionarBolinhas(bolinhas, centro, ladoCima);
    }

    /**
     * Conta quantas bolinhas válidas existem
     */
    private int contarBolinhasValidas(Bolinha[] bolinhas) {
        int contador = 0;
        for (Bolinha b : bolinhas) {
            if (b != null)
                contador++;
        }
        return contador;
    }

    /**
     * Calcula densidades dos 4 quadrantes
     * [0] = superior-esquerdo, [1] = superior-direito
     * [2] = inferior-esquerdo, [3] = inferior-direito
     */
    private double[] calcularDensidadesQuadrantes(Bolinha[] bolinhas, double[] centro) {
        double[] densidades = new double[4];
        int[] contadores = new int[4];

        for (Bolinha b : bolinhas) {
            if (b != null) {
                int quadrante = determinarQuadrante(b, centro);
                contadores[quadrante]++;
            }
        }

        // Normaliza as densidades
        int total = contarBolinhasValidas(bolinhas);
        for (int i = 0; i < 4; i++) {
            densidades[i] = total > 0 ? (double) contadores[i] / total : 0;
        }

        return densidades;
    }

    /**
     * Determina em qual quadrante uma bolinha está
     */
    private int determinarQuadrante(Bolinha b, double[] centro) {
        boolean acima = b.y < centro[1];
        boolean esquerda = b.x < centro[0];

        if (acima && esquerda)
            return 0; // Superior-esquerdo
        if (acima && !esquerda)
            return 1; // Superior-direito
        if (!acima && esquerda)
            return 2; // Inferior-esquerdo
        return 3; // Inferior-direito
    }

    /**
     * Determina qual lado deve ser considerado "cima"
     * Baseado na densidade dos quadrantes superiores vs inferiores
     */
    private int determinarLadoCima(double[] densidades) {
        double densidadeSuperior = densidades[0] + densidades[1]; // Superior-esquerdo + Superior-direito
        double densidadeInferior = densidades[2] + densidades[3]; // Inferior-esquerdo + Inferior-direito

        // Se a parte superior tem mais densidade, está de cabeça para cima
        if (densidadeSuperior > densidadeInferior) {
            return 0; // Não precisa rotacionar
        } else {
            return 180; // Precisa rotacionar 180 graus
        }
    }

    /**
     * Rotaciona as bolinhas para normalizar a orientação
     */
    private Bolinha[] rotacionarBolinhas(Bolinha[] bolinhas, double[] centro, int anguloRotacao) {
        if (anguloRotacao == 0) {
            return bolinhas; // Não precisa rotacionar
        }

        Bolinha[] bolinhasRotacionadas = new Bolinha[bolinhas.length];

        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null) {
                // Rotaciona 180 graus em torno do centro
                double xRelativo = bolinhas[i].x - centro[0];
                double yRelativo = bolinhas[i].y - centro[1];

                double xRotacionado = centro[0] - xRelativo;
                double yRotacionado = centro[1] - yRelativo;

                bolinhasRotacionadas[i] = new Bolinha(
                        bolinhas[i].nome,
                        bolinhas[i].cor,
                        (int) xRotacionado,
                        (int) yRotacionado);
            }
        }

        System.out.println("Boneco rotacionado para normalizar orientação (cabeça para cima)");
        return bolinhasRotacionadas;
    }

    /**
     * Divide o boneco em regiões funcionais baseadas na posição
     * Cada região é uma lista de bolinhas próximas.
     */
    private List<List<Bolinha>> dividirEmRegioesFuncionais(Bolinha[] bolinhas) {
        // Encontra os limites do boneco
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                minX = Math.min(minX, b.x);
                maxX = Math.max(maxX, b.x);
                minY = Math.min(minY, b.y);
                maxY = Math.max(maxY, b.y);
            }
        }

        double largura = maxX - minX;
        double altura = maxY - minY;

        // Divide em 5 regiões: cabeça, tronco, braço esquerdo, braço direito, pernas
        List<List<Bolinha>> regioes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            regioes.add(new ArrayList<>());
        }

        // Classifica cada bolinha em uma região
        for (Bolinha b : bolinhas) {
            if (b != null) {
                int regiao = classificarBolinhaEmRegiao(b, minX, maxX, minY, maxY, largura, altura);
                regioes.get(regiao).add(b);
            }
        }

        // Remove regiões vazias
        List<List<Bolinha>> regioesNaoVazias = new ArrayList<>();
        for (List<Bolinha> regiao : regioes) {
            if (!regiao.isEmpty()) {
                regioesNaoVazias.add(regiao);
            }
        }

        return regioesNaoVazias;
    }

    /**
     * Classifica uma bolinha em uma das 5 regiões funcionais
     * 0 = cabeça, 1 = tronco, 2 = braço esquerdo, 3 = braço direito, 4 = pernas
     */
    private int classificarBolinhaEmRegiao(Bolinha b, double minX, double maxX, double minY, double maxY,
            double largura, double altura) {
        // Normaliza a posição da bolinha (0 a 1)
        double xNorm = (b.x - minX) / largura;
        double yNorm = (b.y - minY) / altura;

        // Cabeça: parte superior (y < 0.3)
        if (yNorm < 0.3) {
            return 0;
        }
        // Tronco: parte central (0.3 <= y < 0.7) e central (0.2 <= x <= 0.8)
        else if (yNorm < 0.7 && xNorm >= 0.2 && xNorm <= 0.8) {
            return 1;
        }
        // Braços: parte central vertical mas lateral horizontal
        else if (yNorm < 0.7) {
            if (xNorm < 0.2) {
                return 2; // Braço esquerdo
            } else if (xNorm > 0.8) {
                return 3; // Braço direito
            }
        }
        // Pernas: parte inferior (y >= 0.7)
        else {
            return 4;
        }

        // Se não se encaixou em nenhuma categoria específica, vai para tronco
        return 1;
    }

    /**
     * Calcula limites do desenho (minX, maxX, minY, maxY)
     */
    private double[] calcularLimites(Bolinha[] bolinhas) {
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                minX = Math.min(minX, b.x);
                maxX = Math.max(maxX, b.x);
                minY = Math.min(minY, b.y);
                maxY = Math.max(maxY, b.y);
            }
        }
        return new double[] { minX, maxX, minY, maxY };
    }

    /**
     * Calcula densidades de bolinhas por região (superior, central, inferior)
     */
    private int[] calcularDensidadesRegioes(Bolinha[] bolinhas, double[] limites) {
        double largura = limites[1] - limites[0];
        double altura = limites[3] - limites[2];

        double minY = limites[2];
        double maxY = limites[3];

        int superior = 0;
        int central = 0;
        int inferior = 0;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                double yNorm = (b.y - minY) / altura;
                if (yNorm < 0.3) {
                    superior++;
                } else if (yNorm < 0.7) {
                    central++;
                } else {
                    inferior++;
                }
            }
        }
        return new int[] { superior, central, inferior };
    }

    /**
     * Calcula distribuição horizontal (esquerda, centro, direita)
     */
    private int[] calcularDistribuicaoHorizontal(Bolinha[] bolinhas, double[] limites) {
        double largura = limites[1] - limites[0];
        double minX = limites[0];

        int esquerda = 0;
        int centro = 0;
        int direita = 0;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                double xNorm = (b.x - minX) / largura;
                if (xNorm < 0.2) {
                    esquerda++;
                } else if (xNorm < 0.8) {
                    centro++;
                } else {
                    direita++;
                }
            }
        }
        return new int[] { esquerda, centro, direita };
    }

    /**
     * Calcula simetria vertical
     */
    private int calcularSimetriaVertical(Bolinha[] bolinhas, double[] centro) {
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                minY = Math.min(minY, b.y);
                maxY = Math.max(maxY, b.y);
            }
        }

        double altura = maxY - minY;
        double yMedio = centro[1];

        return Math.abs(yMedio - (minY + altura / 2)) < 10 ? 1 : 0; // Tolerância de 10 pixels
    }

    /**
     * Calcula simetria horizontal
     */
    private int calcularSimetriaHorizontal(Bolinha[] bolinhas, double[] centro) {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                minX = Math.min(minX, b.x);
                maxX = Math.max(maxX, b.x);
            }
        }

        double largura = maxX - minX;
        double xMedio = centro[0];

        return Math.abs(xMedio - (minX + largura / 2)) < 10 ? 1 : 0; // Tolerância de 10 pixels
    }

    /**
     * Verifica estrutura geral do boneco
     */
    private int verificarEstruturaGeral(Bolinha[] bolinhas, int[] features) {
        // Verifica se tem pelo menos 3 bolinhas
        if (features[0] < 3) {
            return 0;
        }

        // Verifica se o centro de massa está razoavelmente centralizado
        double[] centro = calcularCentroMassa(bolinhas);
        double xMedio = centro[0];
        double largura = features[3];

        if (Math.abs(xMedio - (largura / 2)) > 10) { // Tolerância de 10 pixels
            return 0;
        }

        // Verifica se a altura é razoavelmente proporcional à largura
        double altura = features[4];
        if (altura > 0 && (altura * 1.5) < largura) { // Altura muito pequena em relação à largura
            return 0;
        }

        // Verifica se a densidade de bolinhas é razoável
        int[] densidades = calcularDensidadesRegioes(bolinhas, calcularLimites(bolinhas));
        if (densidades[0] < 1 || densidades[1] < 1 || densidades[2] < 1) { // Pelo menos uma bolinha em cada região
            return 0;
        }

        // Verifica se a distribuição horizontal é razoável
        int[] distribuicaoH = calcularDistribuicaoHorizontal(bolinhas, calcularLimites(bolinhas));
        if (distribuicaoH[0] < 1 || distribuicaoH[1] < 1 || distribuicaoH[2] < 1) { // Pelo menos uma bolinha em cada
                                                                                    // lado
            return 0;
        }

        // Verifica simetria vertical
        if (calcularSimetriaVertical(bolinhas, centro) == 0) {
            return 0;
        }

        // Verifica simetria horizontal
        if (calcularSimetriaHorizontal(bolinhas, centro) == 0) {
            return 0;
        }

        return 1;
    }

    /**
     * Faz crop inteligente - recorta apenas a área útil do desenho
     */
    private Bolinha[] fazerCropInteligente(Bolinha[] bolinhas) {
        // Encontra os limites do desenho
        double[] limites = calcularLimites(bolinhas);
        double minX = limites[0], maxX = limites[1];
        double minY = limites[2], maxY = limites[3];

        // Adiciona uma margem de 10 pixels
        double margem = 10.0;
        minX -= margem;
        maxX += margem;
        minY -= margem;
        maxY += margem;

        // Transfere as bolinhas para o novo sistema de coordenadas
        Bolinha[] bolinhasCropadas = new Bolinha[bolinhas.length];
        int indice = 0;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                // Ajusta as coordenadas para o novo sistema
                int novoX = (int) (b.x - minX);
                int novoY = (int) (b.y - minY);

                bolinhasCropadas[indice] = new Bolinha(b.nome, b.cor, novoX, novoY);
                indice++;
            }
        }

        System.out.println("Crop inteligente aplicado: " + (int) (maxX - minX) + "x" + (int) (maxY - minY));
        return bolinhasCropadas;
    }

    /**
     * Analisa o grafo de conectividade entre bolinhas
     */
    private int[] analisarGrafoConectividade(Bolinha[] bolinhas) {
        int numBolinhas = contarBolinhasValidas(bolinhas);
        int numConexoes = 0;
        int grauMaximo = 0;
        int grauMinimo = Integer.MAX_VALUE;

        // Calcula o grau de cada bolinha (número de conexões)
        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null) {
                int grau = 0;
                for (int j = 0; j < bolinhas.length; j++) {
                    if (i != j && bolinhas[j] != null) {
                        double distancia = calcularDistancia(bolinhas[i], bolinhas[j]);
                        if (distancia <= 40) { // Threshold para conexão
                            grau++;
                            numConexoes++;
                        }
                    }
                }
                grauMaximo = Math.max(grauMaximo, grau);
                grauMinimo = Math.min(grauMinimo, grau);
            }
        }

        // Divide por 2 porque cada conexão foi contada duas vezes
        numConexoes /= 2;

        // Calcula grau médio
        int grauMedio = numBolinhas > 0 ? (numConexoes * 2) / numBolinhas : 0;

        // Debug do grafo
        System.out.println(
                "Debug grafo: bolinhas=" + numBolinhas + ", conexoes=" + numConexoes + ", grauMedio=" + grauMedio);

        return new int[] { numBolinhas, numConexoes, grauMedio, grauMaximo, grauMinimo };
    }

    /**
     * Analisa padrões de conectividade
     */
    private int[] analisarPadroesConectividade(Bolinha[] bolinhas) {
        int extremidades = 0; // 1 conexão
        int linhas = 0; // 2 conexões
        int juncoes = 0; // 3+ conexões

        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null) {
                int grau = 0;
                for (int j = 0; j < bolinhas.length; j++) {
                    if (i != j && bolinhas[j] != null) {
                        double distancia = calcularDistancia(bolinhas[i], bolinhas[j]);
                        if (distancia <= 40) {
                            grau++;
                        }
                    }
                }

                if (grau == 1) {
                    extremidades++;
                } else if (grau == 2) {
                    linhas++;
                } else if (grau >= 3) {
                    juncoes++;
                }
            }
        }

        return new int[] { extremidades, linhas, juncoes };
    }

    /**
     * Calcula simetria baseada no grafo
     */
    private int calcularSimetriaGrafo(Bolinha[] bolinhas) {
        double[] centro = calcularCentroMassa(bolinhas);
        int simetricas = 0;
        int total = 0;

        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null) {
                total++;
                // Procura por uma bolinha simétrica
                boolean encontrouSimetrica = false;
                for (int j = 0; j < bolinhas.length; j++) {
                    if (i != j && bolinhas[j] != null) {
                        // Verifica se são simétricas em relação ao centro
                        double distanciaI = Math
                                .sqrt(Math.pow(bolinhas[i].x - centro[0], 2) + Math.pow(bolinhas[i].y - centro[1], 2));
                        double distanciaJ = Math
                                .sqrt(Math.pow(bolinhas[j].x - centro[0], 2) + Math.pow(bolinhas[j].y - centro[1], 2));

                        if (Math.abs(distanciaI - distanciaJ) < 20) { // Tolerância
                            encontrouSimetrica = true;
                            break;
                        }
                    }
                }
                if (encontrouSimetrica) {
                    simetricas++;
                }
            }
        }

        return total > 0 ? (simetricas * 10) / total : 0; // Retorna porcentagem * 10
    }

    /**
     * Verifica estrutura geral baseada em conectividade
     */
    private int verificarEstruturaGrafo(Bolinha[] bolinhas, int[] features) {
        // Verifica se tem pelo menos 3 bolinhas
        if (features[0] < 3) {
            return 0;
        }

        // Verifica se tem pelo menos 2 conexões (grafo conectado)
        if (features[2] < 2) {
            return 0;
        }

        // Verifica se tem extremidades (cabeça, mãos, pés) - pelo menos 1
        if (features[6] < 1) {
            return 0;
        }

        // Verifica se tem junções (tronco, ombros, quadris) - pelo menos 1
        if (features[8] < 1) {
            return 0;
        }

        // Verifica se a proporção é razoável (altura > largura para boneco palito)
        if (features[11] > 20) { // Muito largo - aumentado de 15 para 20
            return 0;
        }

        // Verifica se a densidade é razoável - removido limite superior
        if (features[12] < 50) { // Densidade muito baixa - ajustado para novo cálculo
            return 0;
        }

        // Debug da verificação
        System.out.println("Debug estrutura: extremidades=" + features[6] + ", juncoes=" + features[8] + ", proporcao="
                + features[11] + ", densidade=" + features[12]);

        return 1;
    }

    /**
     * Analisa o boneco palito baseado em crop inteligente e análise de grafo
     * Detecta padrões de conectividade entre bolinhas
     */
    public int[] analisarBonecoPalitoEstrutural(Bolinha[] bolinhas) {
        int[] features = new int[15];

        // 1. Remove bolinhas desconectadas (ruído)
        Bolinha[] bolinhasFiltradas = removerBolinhasDesconectadas(bolinhas);

        // 2. Faz crop inteligente (centraliza e recorta área útil)
        Bolinha[] bolinhasCropadas = fazerCropInteligente(bolinhasFiltradas);

        // 3. Normaliza orientação (cabeça sempre para cima)
        Bolinha[] bolinhasNormalizadas = normalizarOrientacao(bolinhasCropadas);

        // 4. Analisa padrões de conectividade (grafo)
        features[0] = contarBolinhasValidas(bolinhasNormalizadas); // Total de bolinhas

        // 5. Características do grafo de conectividade
        int[] caracteristicasGrafo = analisarGrafoConectividade(bolinhasNormalizadas);
        features[1] = caracteristicasGrafo[0]; // Número de nós (bolinhas)
        features[2] = caracteristicasGrafo[1]; // Número de arestas (conexões)
        features[3] = caracteristicasGrafo[2]; // Grau médio
        features[4] = caracteristicasGrafo[3]; // Grau máximo
        features[5] = caracteristicasGrafo[4]; // Grau mínimo

        // 6. Padrões de conectividade
        int[] padroesConectividade = analisarPadroesConectividade(bolinhasNormalizadas);
        features[6] = padroesConectividade[0]; // Bolinhas com 1 conexão (extremidades)
        features[7] = padroesConectividade[1]; // Bolinhas com 2 conexões (linhas)
        features[8] = padroesConectividade[2]; // Bolinhas com 3+ conexões (junções)

        // 7. Características geométricas do crop
        double[] limites = calcularLimites(bolinhasNormalizadas);
        features[9] = (int) (limites[1] - limites[0]); // Largura do crop
        features[10] = (int) (limites[3] - limites[2]); // Altura do crop

        // 8. Proporção largura/altura
        if (features[10] > 0) {
            features[11] = (features[9] * 10) / features[10]; // Proporção W/H * 10
        } else {
            features[11] = 0;
        }

        // 9. Densidade de bolinhas (bolinhas por área)
        double area = features[9] * features[10];
        if (area > 0) {
            features[12] = (int) ((features[0] * 10000) / area); // Densidade * 10000 (aumentado)
        } else {
            features[12] = 0;
        }

        // Debug da densidade
        System.out
                .println("Debug densidade: bolinhas=" + features[0] + ", area=" + area + ", densidade=" + features[12]);

        // 10. Simetria baseada no grafo
        features[13] = calcularSimetriaGrafo(bolinhasNormalizadas);

        // 11. Estrutura geral baseada em conectividade
        features[14] = verificarEstruturaGrafo(bolinhasNormalizadas, features);

        return features;
    }
}