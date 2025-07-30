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
                        if (distancia <= 25) { // Threshold reduzido de 40 para 25
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
                    if (distancia <= 25) { // Threshold reduzido de 40 para 25
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
                dfsAgrupar(bolinhas, i, grupo, visitado, 25.0); // Reduzido de 35.0 para 25.0
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
    public Bolinha[] removerBolinhasDesconectadas(Bolinha[] bolinhas) {
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
     * Versão melhorada: detecta qualquer ângulo de rotação
     */
    public Bolinha[] normalizarOrientacao(Bolinha[] bolinhas) {
        if (contarBolinhasValidas(bolinhas) < 3) {
            return bolinhas; // Muito poucas bolinhas para normalizar
        }

        // 1. Calcula o centro de massa
        double[] centro = calcularCentroMassa(bolinhas);

        // 2. Encontra a direção principal do boneco
        double anguloPrincipal = encontrarAnguloPrincipal(bolinhas, centro);

        // 3. Rotaciona para alinhar com o eixo vertical
        return rotacionarBolinhas(bolinhas, centro, anguloPrincipal);
    }

    /**
     * Encontra o ângulo principal do boneco usando análise de eixos
     */
    private double encontrarAnguloPrincipal(Bolinha[] bolinhas, double[] centro) {
        // Calcula a matriz de covariância para encontrar os eixos principais
        double m20 = 0, m02 = 0, m11 = 0;
        int total = 0;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                double dx = b.x - centro[0];
                double dy = b.y - centro[1];
                m20 += dx * dx;
                m02 += dy * dy;
                m11 += dx * dy;
                total++;
            }
        }

        if (total == 0)
            return 0;

        // Normaliza os momentos
        m20 /= total;
        m02 /= total;
        m11 /= total;

        // Calcula o ângulo do eixo principal
        double angulo = 0.5 * Math.atan2(2 * m11, m20 - m02);

        // Converte para graus
        double anguloGraus = Math.toDegrees(angulo);

        // Normaliza para o intervalo [0, 180]
        while (anguloGraus < 0)
            anguloGraus += 180;
        while (anguloGraus >= 180)
            anguloGraus -= 180;

        // Determina se precisa rotacionar 180° para cabeça ficar para cima
        double[] densidades = calcularDensidadesQuadrantes(bolinhas, centro);
        double densidadeSuperior = densidades[0] + densidades[1];
        double densidadeInferior = densidades[2] + densidades[3];

        // Se a parte inferior tem mais densidade, rotaciona 180°
        if (densidadeInferior > densidadeSuperior) {
            anguloGraus += 180;
        }

        return anguloGraus;
    }

    /**
     * Rotaciona as bolinhas para normalizar a orientação
     * Versão melhorada: suporta qualquer ângulo
     */
    private Bolinha[] rotacionarBolinhas(Bolinha[] bolinhas, double[] centro, double anguloRotacao) {
        if (Math.abs(anguloRotacao) < 5) { // Tolerância de 5 graus
            return bolinhas; // Não precisa rotacionar
        }

        Bolinha[] bolinhasRotacionadas = new Bolinha[bolinhas.length];
        double anguloRad = Math.toRadians(anguloRotacao);

        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null) {
                // Coordenadas relativas ao centro
                double xRelativo = bolinhas[i].x - centro[0];
                double yRelativo = bolinhas[i].y - centro[1];

                // Aplica rotação
                double xRotacionado = xRelativo * Math.cos(anguloRad) - yRelativo * Math.sin(anguloRad);
                double yRotacionado = xRelativo * Math.sin(anguloRad) + yRelativo * Math.cos(anguloRad);

                // Volta para coordenadas absolutas
                int novoX = (int) (centro[0] + xRotacionado);
                int novoY = (int) (centro[1] + yRotacionado);

                bolinhasRotacionadas[i] = new Bolinha(
                        bolinhas[i].nome,
                        bolinhas[i].cor,
                        novoX,
                        novoY);
            }
        }

        System.out.println("Boneco rotacionado " + anguloRotacao + "° para normalizar orientação");
        return bolinhasRotacionadas;
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
    public Bolinha[] fazerCropInteligente(Bolinha[] bolinhas) {
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
                        if (distancia <= 25) { // Threshold reduzido de 40 para 25
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
                        if (distancia <= 25) {
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
     * Analisa o boneco palito baseado em padrões estruturais de conectividade
     * Nova abordagem: análise de caminhos, distribuição de graus e padrões de
     * ramificação
     */
    public int[] analisarBonecoPalitoPadroesEstruturais(Bolinha[] bolinhas) {
        int[] features = new int[19]; // 19 características estruturais (adicionado tamanho do tronco)

        // 1. Remove bolinhas desconectadas (ruído)
        Bolinha[] bolinhasFiltradas = removerBolinhasDesconectadas(bolinhas);

        // 2. Faz crop inteligente (centraliza e recorta área útil)
        Bolinha[] bolinhasCropadas = fazerCropInteligente(bolinhasFiltradas);

        // 3. Normaliza orientação (cabeça sempre para cima)
        Bolinha[] bolinhasNormalizadas = normalizarOrientacao(bolinhasCropadas);

        // 4. Análise de padrões estruturais
        features[0] = contarBolinhasValidas(bolinhasNormalizadas); // Total de bolinhas

        // 5. Distribuição de graus (quantas bolinhas com cada grau)
        int[] distribuicaoGraus = calcularDistribuicaoGraus(bolinhasNormalizadas);
        features[1] = distribuicaoGraus[0]; // Bolinhas com grau 1 (extremidades)
        features[2] = distribuicaoGraus[1]; // Bolinhas com grau 2 (linhas)
        features[3] = distribuicaoGraus[2]; // Bolinhas com grau 3 (junções)
        features[4] = distribuicaoGraus[3]; // Bolinhas com grau 4+ (centros)

        // 6. Análise de caminhos de diferentes comprimentos
        int[] caminhos = analisarCaminhos(bolinhasNormalizadas);
        features[5] = caminhos[0]; // Caminhos de 1 salto
        features[6] = caminhos[1]; // Caminhos de 2 saltos
        features[7] = caminhos[2]; // Caminhos de 3 saltos
        features[8] = caminhos[3]; // Caminhos de 4+ saltos

        // 7. Padrões de ramificação
        int[] ramificacoes = analisarPadroesRamificacao(bolinhasNormalizadas);
        features[9] = ramificacoes[0]; // Pontos de ramificação simples (grau 3)
        features[10] = ramificacoes[1]; // Pontos de ramificação complexa (grau 4+)
        features[11] = ramificacoes[2]; // Cadeias lineares (sequências de grau 2)

        // 8. Características geométricas do crop
        double[] limites = calcularLimites(bolinhasNormalizadas);
        features[12] = (int) (limites[1] - limites[0]); // Largura do crop
        features[13] = (int) (limites[3] - limites[2]); // Altura do crop

        // 9. Proporção largura/altura
        if (features[13] > 0) {
            features[14] = (features[12] * 10) / features[13]; // Proporção W/H * 10
        } else {
            features[14] = 0;
        }

        // 10. Densidade de bolinhas (bolinhas por área)
        double area = features[12] * features[13];
        if (area > 0) {
            features[15] = (int) ((features[0] * 10000) / area); // Densidade * 10000
        } else {
            features[15] = 0;
        }

        // 11. Simetria estrutural
        features[16] = calcularSimetriaEstrutural(bolinhasNormalizadas);

        // 12. Estrutura geral baseada em padrões
        features[17] = verificarEstruturaPadroes(bolinhasNormalizadas, features);

        // 13. Tamanho do tronco (NOVA FEATURE)
        features[18] = calcularTamanhoTronco(bolinhasNormalizadas);

        return features;
    }

    /**
     * Analisa o boneco palito baseado em crop inteligente e análise de grafo
     * Detecta padrões de conectividade entre bolinhas (mantido para
     * compatibilidade)
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

    /**
     * Analisa o boneco palito usando processamento de imagem simples
     * Nova abordagem: matriz binária, contornos, momentos de imagem
     */
    public int[] analisarBonecoPalitoProcessamentoImagem(Bolinha[] bolinhas) {
        int[] features = new int[8]; // 8 características simples

        // 1. Remove bolinhas desconectadas (ruído)
        Bolinha[] bolinhasFiltradas = removerBolinhasDesconectadas(bolinhas);

        // 2. Faz crop inteligente (centraliza e recorta área útil)
        Bolinha[] bolinhasCropadas = fazerCropInteligente(bolinhasFiltradas);

        // 3. Normaliza orientação (cabeça sempre para cima)
        Bolinha[] bolinhasNormalizadas = normalizarOrientacao(bolinhasCropadas);

        // 4. Converte para matriz binária
        int[][] matrizBinaria = converterParaMatrizBinaria(bolinhasNormalizadas);

        // 5. Calcula características de processamento de imagem
        features[0] = contarBolinhasValidas(bolinhasNormalizadas); // Total de bolinhas

        // 6. Análise de contorno
        int[] contorno = analisarContorno(matrizBinaria);
        features[1] = contorno[0]; // Perímetro do contorno
        features[2] = contorno[1]; // Área do contorno

        // 7. Momentos de imagem (orientação-invariante)
        double[] momentos = calcularMomentosImagem(matrizBinaria);
        features[3] = (int) (momentos[0] * 1000); // Momento de área normalizado
        features[4] = (int) (momentos[1] * 1000); // Excentricidade

        // 8. Características geométricas simples
        double[] limites = calcularLimites(bolinhasNormalizadas);
        features[5] = (int) (limites[1] - limites[0]); // Largura
        features[6] = (int) (limites[3] - limites[2]); // Altura

        // 9. Proporção simples
        if (features[6] > 0) {
            features[7] = (features[5] * 10) / features[6]; // Proporção W/H * 10
        } else {
            features[7] = 0;
        }

        return features;
    }

    /**
     * Calcula a distribuição de graus das bolinhas
     * Retorna: [grau1, grau2, grau3, grau4+]
     */
    private int[] calcularDistribuicaoGraus(Bolinha[] bolinhas) {
        int[] distribuicao = new int[4]; // [grau1, grau2, grau3, grau4+]

        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null) {
                int grau = 0;
                for (int j = 0; j < bolinhas.length; j++) {
                    if (i != j && bolinhas[j] != null) {
                        double distancia = calcularDistancia(bolinhas[i], bolinhas[j]);
                        if (distancia <= 25) { // Threshold reduzido de 40 para 25
                            grau++;
                        }
                    }
                }

                if (grau == 1) {
                    distribuicao[0]++; // Extremidades
                } else if (grau == 2) {
                    distribuicao[1]++; // Linhas
                } else if (grau == 3) {
                    distribuicao[2]++; // Junções simples
                } else if (grau >= 4) {
                    distribuicao[3]++; // Centros/ramificações complexas
                }
            }
        }

        return distribuicao;
    }

    /**
     * Analisa caminhos de diferentes comprimentos
     * Retorna: [caminhos1salto, caminhos2saltos, caminhos3saltos, caminhos4+saltos]
     */
    private int[] analisarCaminhos(Bolinha[] bolinhas) {
        int[] caminhos = new int[4];

        // Para cada bolinha, calcula caminhos de diferentes comprimentos
        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null) {
                int[] distancias = calcularDistanciasEmNos(bolinhas, i);

                for (int j = 0; j < bolinhas.length; j++) {
                    if (i != j && bolinhas[j] != null) {
                        int distancia = distancias[j];
                        if (distancia == 1) {
                            caminhos[0]++;
                        } else if (distancia == 2) {
                            caminhos[1]++;
                        } else if (distancia == 3) {
                            caminhos[2]++;
                        } else if (distancia >= 4 && distancia < 999) {
                            caminhos[3]++;
                        }
                    }
                }
            }
        }

        // Divide por 2 porque cada caminho foi contado duas vezes
        for (int i = 0; i < caminhos.length; i++) {
            caminhos[i] /= 2;
        }

        return caminhos;
    }

    /**
     * Analisa padrões de ramificação
     * Retorna: [ramificacoesSimples, ramificacoesComplexas, cadeiasLineares]
     */
    private int[] analisarPadroesRamificacao(Bolinha[] bolinhas) {
        int[] padroes = new int[3];

        // Encontra cadeias lineares (sequências de bolinhas com grau 2)
        int cadeiasLineares = encontrarCadeiasLineares(bolinhas);
        padroes[2] = cadeiasLineares;

        // Ramificações simples e complexas já calculadas em calcularDistribuicaoGraus
        int[] distribuicao = calcularDistribuicaoGraus(bolinhas);
        padroes[0] = distribuicao[2]; // Grau 3 = ramificações simples
        padroes[1] = distribuicao[3]; // Grau 4+ = ramificações complexas

        return padroes;
    }

    /**
     * Encontra cadeias lineares (sequências de bolinhas com grau 2)
     */
    private int encontrarCadeiasLineares(Bolinha[] bolinhas) {
        int cadeias = 0;
        boolean[] visitado = new boolean[bolinhas.length];

        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null && !visitado[i]) {
                int grau = calcularGrauBolinha(bolinhas, i);
                if (grau == 2) {
                    // Encontra o comprimento da cadeia linear
                    int comprimento = medirCadeiaLinear(bolinhas, i, visitado);
                    if (comprimento >= 2) { // Pelo menos 2 bolinhas em sequência
                        cadeias += comprimento - 1; // Número de conexões na cadeia
                    }
                }
            }
        }

        return cadeias;
    }

    /**
     * Calcula o grau de uma bolinha específica
     */
    private int calcularGrauBolinha(Bolinha[] bolinhas, int indice) {
        int grau = 0;
        for (int j = 0; j < bolinhas.length; j++) {
            if (indice != j && bolinhas[j] != null) {
                double distancia = calcularDistancia(bolinhas[indice], bolinhas[j]);
                if (distancia <= 25) {
                    grau++;
                }
            }
        }
        return grau;
    }

    /**
     * Mede o comprimento de uma cadeia linear a partir de uma bolinha
     */
    private int medirCadeiaLinear(Bolinha[] bolinhas, int inicio, boolean[] visitado) {
        int comprimento = 0;
        int atual = inicio;

        while (atual != -1 && !visitado[atual] && bolinhas[atual] != null) {
            visitado[atual] = true;
            comprimento++;

            // Encontra o próximo na cadeia (deve ter grau 2 e não visitado)
            int proximo = -1;
            for (int i = 0; i < bolinhas.length; i++) {
                if (i != atual && bolinhas[i] != null && !visitado[i]) {
                    double distancia = calcularDistancia(bolinhas[atual], bolinhas[i]);
                    if (distancia <= 25 && calcularGrauBolinha(bolinhas, i) == 2) {
                        proximo = i;
                        break;
                    }
                }
            }
            atual = proximo;
        }

        return comprimento;
    }

    /**
     * Calcula simetria estrutural baseada em padrões de conectividade
     */
    private int calcularSimetriaEstrutural(Bolinha[] bolinhas) {
        double[] centro = calcularCentroMassa(bolinhas);
        int simetricas = 0;
        int total = 0;

        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null) {
                total++;
                int grauI = calcularGrauBolinha(bolinhas, i);

                // Procura por uma bolinha simétrica com mesmo grau
                boolean encontrouSimetrica = false;
                for (int j = 0; j < bolinhas.length; j++) {
                    if (i != j && bolinhas[j] != null) {
                        int grauJ = calcularGrauBolinha(bolinhas, j);

                        if (grauI == grauJ) {
                            // Verifica se são simétricas em relação ao centro
                            double distanciaI = Math.sqrt(Math.pow(bolinhas[i].x - centro[0], 2) +
                                    Math.pow(bolinhas[i].y - centro[1], 2));
                            double distanciaJ = Math.sqrt(Math.pow(bolinhas[j].x - centro[0], 2) +
                                    Math.pow(bolinhas[j].y - centro[1], 2));

                            if (Math.abs(distanciaI - distanciaJ) < 20) { // Tolerância
                                encontrouSimetrica = true;
                                break;
                            }
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
     * Verifica estrutura geral baseada em padrões estruturais
     */
    private int verificarEstruturaPadroes(Bolinha[] bolinhas, int[] features) {
        // Verifica se tem pelo menos 3 bolinhas
        if (features[0] < 3) {
            return 0;
        }

        // Verifica se tem extremidades (cabeça, mãos, pés) - pelo menos 1
        if (features[1] < 1) {
            return 0;
        }

        // Verifica se tem junções (tronco, ombros) - pelo menos 1
        if (features[3] < 1) {
            return 0;
        }

        // Verifica se tem caminhos de diferentes comprimentos
        if (features[5] < 2) { // Pelo menos 2 caminhos de 1 salto
            return 0;
        }

        // Verifica se a proporção é razoável (altura > largura para boneco palito)
        if (features[14] > 20) { // Muito largo
            return 0;
        }

        // Verifica se a densidade é razoável
        if (features[15] < 50) { // Densidade muito baixa
            return 0;
        }

        // Debug da verificação
        System.out.println("Debug estrutura: extremidades=" + features[1] + ", juncoes=" + features[3] +
                ", caminhos1=" + features[5] + ", proporcao=" + features[14] + ", densidade=" + features[15]);

        return 1;
    }

    /**
     * Converte bolinhas para matriz binária
     */
    public int[][] converterParaMatrizBinaria(Bolinha[] bolinhas) {
        // Encontra limites
        double[] limites = calcularLimites(bolinhas);
        int minX = (int) limites[0];
        int maxX = (int) limites[1];
        int minY = (int) limites[2];
        int maxY = (int) limites[3];

        int largura = maxX - minX + 1;
        int altura = maxY - minY + 1;

        // Cria matriz binária
        int[][] matriz = new int[altura][largura];

        // Preenche com bolinhas (1 = bolinha, 0 = fundo)
        for (Bolinha b : bolinhas) {
            if (b != null) {
                int x = (int) b.x - minX;
                int y = (int) b.y - minY;

                // Marca a bolinha e sua vizinhança (dilatação simples)
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dx = -2; dx <= 2; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < largura && ny >= 0 && ny < altura) {
                            matriz[ny][nx] = 1;
                        }
                    }
                }
            }
        }

        return matriz;
    }

    /**
     * Analisa contorno da imagem binária
     */
    private int[] analisarContorno(int[][] matriz) {
        int altura = matriz.length;
        int largura = matriz[0].length;

        int perimetro = 0;
        int area = 0;

        // Calcula área (pixels brancos)
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if (matriz[y][x] == 1) {
                    area++;

                    // Verifica se é borda (tem vizinho branco)
                    boolean isBorda = false;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx >= 0 && nx < largura && ny >= 0 && ny < altura) {
                                if (matriz[ny][nx] == 0) {
                                    isBorda = true;
                                    break;
                                }
                            }
                        }
                        if (isBorda)
                            break;
                    }

                    if (isBorda) {
                        perimetro++;
                    }
                }
            }
        }

        return new int[] { perimetro, area };
    }

    /**
     * Calcula momentos de imagem (orientação-invariante)
     */
    private double[] calcularMomentosImagem(int[][] matriz) {
        int altura = matriz.length;
        int largura = matriz[0].length;

        // Calcula centro de massa
        double somaX = 0, somaY = 0, area = 0;

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if (matriz[y][x] == 1) {
                    somaX += x;
                    somaY += y;
                    area++;
                }
            }
        }

        if (area == 0) {
            return new double[] { 0, 0 };
        }

        double centroX = somaX / area;
        double centroY = somaY / area;

        // Calcula momentos centrais
        double m20 = 0, m02 = 0, m11 = 0;

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if (matriz[y][x] == 1) {
                    double dx = x - centroX;
                    double dy = y - centroY;
                    m20 += dx * dx;
                    m02 += dy * dy;
                    m11 += dx * dy;
                }
            }
        }

        // Normaliza momentos
        m20 /= area;
        m02 /= area;
        m11 /= area;

        // Calcula características invariantes
        double momentoArea = Math.sqrt(m20 + m02); // Momento de área
        double excentricidade = Math.sqrt(4 * m11 * m11 + (m20 - m02) * (m20 - m02)) / (m20 + m02);

        if (Double.isNaN(excentricidade)) {
            excentricidade = 0;
        }

        return new double[] { momentoArea, excentricidade };
    }

    /**
     * Calcula o tamanho do tronco (número de bolinhas na região central)
     */
    private int calcularTamanhoTronco(Bolinha[] bolinhas) {
        if (contarBolinhasValidas(bolinhas) < 3) {
            return 0; // Muito poucas bolinhas para ter tronco
        }

        // 1. Calcula o centro de massa do boneco
        double[] centro = calcularCentroMassa(bolinhas);

        // 2. Calcula os limites do boneco
        double[] limites = calcularLimites(bolinhas);
        double largura = limites[1] - limites[0];
        double altura = limites[3] - limites[2];

        // 3. Define a região do tronco (parte central do boneco)
        // Tronco: região central vertical (30% a 70% da altura) e central horizontal
        // (20% a 80% da largura)
        double minY = limites[2] + altura * 0.3; // 30% da altura
        double maxY = limites[2] + altura * 0.7; // 70% da altura
        double minX = limites[0] + largura * 0.2; // 20% da largura
        double maxX = limites[0] + largura * 0.8; // 80% da largura

        // 4. Conta bolinhas na região do tronco
        int bolinhasTronco = 0;
        for (Bolinha b : bolinhas) {
            if (b != null) {
                // Verifica se a bolinha está na região do tronco
                if (b.x >= minX && b.x <= maxX && b.y >= minY && b.y <= maxY) {
                    bolinhasTronco++;
                }
            }
        }

        // Debug
        System.out.println("Tronco: " + bolinhasTronco + " bolinhas na região central");
        System.out
                .println("  Região: X[" + (int) minX + "-" + (int) maxX + "] Y[" + (int) minY + "-" + (int) maxY + "]");

        return bolinhasTronco;
    }
}