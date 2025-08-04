package reconhecerdesenhos;

import java.awt.Point;
import java.util.*;

public class AnaliseRelacional {

    // Constante para o threshold de conexão
    private static final double THRESHOLD_CONEXAO = 30.0;

    public static class Ponto {
        double x, y;

        public Ponto(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Grupo {
        List<Bolinha> bolinhas;
        Ponto centro;

        public Grupo() {
            this.bolinhas = new ArrayList<>();
        }

        public void adicionarBolinha(Bolinha b) {
            bolinhas.add(b);
            calcularCentro();
        }

        public int size() {
            return bolinhas.size();
        }

        private void calcularCentro() {
            if (bolinhas.isEmpty())
                return;

            double somaX = 0, somaY = 0;
            for (Bolinha b : bolinhas) {
                somaX += b.x;
                somaY += b.y;
            }
            centro = new Ponto(somaX / bolinhas.size(), somaY / bolinhas.size());
        }
    }

    public int[] analisarBonecoPalitoRelacional(Bolinha[] bolinhas) {
        // 1. Encontra o centro de massa geral
        Ponto centroGeral = calcularCentroMassa(bolinhas);

        // 2. Agrupa bolinhas por proximidade
        List<Grupo> grupos = agruparPorProximidade(bolinhas);

        // 3. Analisa relações entre grupos
        return analisarRelacoesGrupos(grupos, centroGeral);
    }

    private Ponto calcularCentroMassa(Bolinha[] bolinhas) {
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
            return new Ponto(0, 0);
        return new Ponto(somaX / contador, somaY / contador);
    }

    private List<Grupo> agruparPorProximidade(Bolinha[] bolinhas) {
        List<Grupo> grupos = new ArrayList<>();
        boolean[] visitado = new boolean[bolinhas.length];

        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null && !visitado[i]) {
                Grupo grupo = new Grupo();
                dfsAgrupar(bolinhas, i, grupo, visitado, THRESHOLD_CONEXAO); // Threshold de 50 pixels
                if (grupo.size() > 0) {
                    grupos.add(grupo);
                }
            }
        }

        return grupos;
    }

    private void dfsAgrupar(Bolinha[] bolinhas, int index, Grupo grupo, boolean[] visitado, double threshold) {
        if (index < 0 || index >= bolinhas.length || bolinhas[index] == null || visitado[index]) {
            return;
        }

        visitado[index] = true;
        grupo.adicionarBolinha(bolinhas[index]);

        // Procura bolinhas próximas
        for (int i = 0; i < bolinhas.length; i++) {
            if (bolinhas[i] != null && !visitado[i]) {
                double distancia = calcularDistancia(bolinhas[index], bolinhas[i]);
                if (distancia <= threshold) {
                    dfsAgrupar(bolinhas, i, grupo, visitado, threshold);
                }
            }
        }
    }

    private double calcularDistancia(Bolinha a, Bolinha b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    private int[] analisarRelacoesGrupos(List<Grupo> grupos, Ponto centroGeral) {
        int[] features = new int[30];

        if (grupos.isEmpty()) {
            return features; // Retorna array de zeros
        }

        // Encontra o grupo mais isolado (provavelmente a cabeça)
        Grupo cabeca = encontrarGrupoMaisIsolado(grupos);

        // Encontra o grupo mais central e vertical (provavelmente o tronco)
        Grupo tronco = encontrarGrupoMaisCentral(grupos, centroGeral);

        // Encontra grupos laterais (braços)
        List<Grupo> bracos = encontrarGruposLaterais(grupos, tronco);

        // Encontra grupos inferiores (pernas)
        List<Grupo> pernas = encontrarGruposInferiores(grupos, tronco);

        // Características baseadas em relações
        features[0] = cabeca != null ? cabeca.size() : 0;
        features[1] = tronco != null ? tronco.size() : 0;
        features[2] = bracos.size();
        features[3] = pernas.size();
        features[4] = calcularProporcao(cabeca, tronco);
        features[5] = verificarConectividade(cabeca, tronco);
        features[6] = verificarConectividade(tronco, bracos);
        features[7] = verificarConectividade(tronco, pernas);
        features[8] = calcularSimetria(bracos);
        features[9] = calcularSimetria(pernas);

        // Análise de posicionamento relativo
        features[10] = calcularPosicionamentoRelativo(cabeca, tronco);
        features[11] = calcularPosicionamentoRelativo(tronco, bracos);
        features[12] = calcularPosicionamentoRelativo(tronco, pernas);

        // Análise de tamanhos relativos
        features[13] = calcularProporcaoTamanhos(grupos);
        features[14] = calcularDensidadeGrupos(grupos);
        features[15] = calcularDistribuicaoEspacial(grupos, centroGeral);

        // Análise de conectividade geral
        features[16] = calcularConectividadeGeral(grupos);
        features[17] = calcularGrauMedio(grupos);
        features[18] = encontrarMaiorComponente(grupos);
        features[19] = calcularDiametro(grupos);

        // Análise de padrões estruturais
        features[20] = detectarPadraoHierarquico(grupos);
        features[21] = detectarPadraoSimetrico(grupos);
        features[22] = detectarPadraoCentralizado(grupos, centroGeral);
        features[23] = detectarPadraoRadial(grupos, centroGeral);
        features[24] = detectarPadraoLinear(grupos);

        // Análise de proporções específicas
        features[25] = calcularProporcaoCabecaCorpo(cabeca, tronco);
        features[26] = calcularProporcaoBracosTronco(bracos, tronco);
        features[27] = calcularProporcaoPernasTronco(pernas, tronco);
        features[28] = calcularProporcaoSimetria(bracos, pernas);
        features[29] = calcularProporcaoGeral(grupos);

        return features;
    }

    private Grupo encontrarGrupoMaisIsolado(List<Grupo> grupos) {
        if (grupos.isEmpty())
            return null;

        Grupo maisIsolado = grupos.get(0);
        double maiorDistancia = 0;

        for (Grupo grupo : grupos) {
            double distanciaMedia = calcularDistanciaMediaParaOutros(grupo, grupos);
            if (distanciaMedia > maiorDistancia) {
                maiorDistancia = distanciaMedia;
                maisIsolado = grupo;
            }
        }

        return maisIsolado;
    }

    private double calcularDistanciaMediaParaOutros(Grupo grupo, List<Grupo> todosGrupos) {
        if (todosGrupos.size() <= 1)
            return 0;

        double somaDistancia = 0;
        int contador = 0;

        for (Grupo outro : todosGrupos) {
            if (outro != grupo) {
                somaDistancia += calcularDistancia(grupo.centro, outro.centro);
                contador++;
            }
        }

        return contador > 0 ? somaDistancia / contador : 0;
    }

    private Grupo encontrarGrupoMaisCentral(List<Grupo> grupos, Ponto centroGeral) {
        if (grupos.isEmpty())
            return null;

        Grupo maisCentral = grupos.get(0);
        double menorDistancia = Double.MAX_VALUE;

        for (Grupo grupo : grupos) {
            double distancia = calcularDistancia(grupo.centro, centroGeral);
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                maisCentral = grupo;
            }
        }

        return maisCentral;
    }

    private List<Grupo> encontrarGruposLaterais(List<Grupo> grupos, Grupo tronco) {
        List<Grupo> laterais = new ArrayList<>();

        if (tronco == null)
            return laterais;

        for (Grupo grupo : grupos) {
            if (grupo != tronco) {
                double distanciaX = Math.abs(grupo.centro.x - tronco.centro.x);
                double distanciaY = Math.abs(grupo.centro.y - tronco.centro.y);

                // Grupos laterais estão mais distantes horizontalmente que verticalmente
                if (distanciaX > distanciaY && distanciaX > 30) {
                    laterais.add(grupo);
                }
            }
        }

        return laterais;
    }

    private List<Grupo> encontrarGruposInferiores(List<Grupo> grupos, Grupo tronco) {
        List<Grupo> inferiores = new ArrayList<>();

        if (tronco == null)
            return inferiores;

        for (Grupo grupo : grupos) {
            if (grupo != tronco) {
                double distanciaY = grupo.centro.y - tronco.centro.y;

                // Grupos inferiores estão abaixo do tronco
                if (distanciaY > 30) {
                    inferiores.add(grupo);
                }
            }
        }

        return inferiores;
    }

    private int calcularProporcao(Grupo a, Grupo b) {
        if (a == null || b == null || b.size() == 0)
            return 0;
        return (int) ((double) a.size() / b.size() * 10);
    }

    private int verificarConectividade(Grupo a, Grupo b) {
        if (a == null || b == null)
            return 0;
        double distancia = calcularDistancia(a.centro, b.centro);
        return distancia < 80 ? 1 : 0; // Conectado se distância < 80 pixels
    }

    private int verificarConectividade(Grupo central, List<Grupo> perifericos) {
        if (central == null || perifericos.isEmpty())
            return 0;

        int conectados = 0;
        for (Grupo periferico : perifericos) {
            if (verificarConectividade(central, periferico) == 1) {
                conectados++;
            }
        }
        return conectados;
    }

    private int calcularSimetria(List<Grupo> grupos) {
        if (grupos.size() < 2)
            return 0;

        // Verifica se há grupos em lados opostos
        int esquerda = 0, direita = 0;
        double centroX = 0;

        for (Grupo grupo : grupos) {
            centroX += grupo.centro.x;
        }
        centroX /= grupos.size();

        for (Grupo grupo : grupos) {
            if (grupo.centro.x < centroX)
                esquerda++;
            else
                direita++;
        }

        return Math.abs(esquerda - direita) <= 1 ? 1 : 0;
    }

    private double calcularDistancia(Ponto a, Ponto b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    // Métodos auxiliares para as outras características
    private int calcularPosicionamentoRelativo(Grupo a, Grupo b) {
        if (a == null || b == null)
            return 0;
        double distancia = calcularDistancia(a.centro, b.centro);
        return distancia < 100 ? 1 : 0;
    }

    private int calcularPosicionamentoRelativo(Grupo central, List<Grupo> perifericos) {
        if (central == null || perifericos.isEmpty())
            return 0;
        int bemPosicionados = 0;
        for (Grupo periferico : perifericos) {
            if (calcularPosicionamentoRelativo(central, periferico) == 1) {
                bemPosicionados++;
            }
        }
        return bemPosicionados;
    }

    private int calcularProporcaoTamanhos(List<Grupo> grupos) {
        if (grupos.size() < 2)
            return 0;

        int maior = 0, menor = Integer.MAX_VALUE;
        for (Grupo grupo : grupos) {
            if (grupo.size() > maior)
                maior = grupo.size();
            if (grupo.size() < menor)
                menor = grupo.size();
        }

        return menor > 0 ? maior / menor : 0;
    }

    private int calcularDensidadeGrupos(List<Grupo> grupos) {
        if (grupos.isEmpty())
            return 0;

        int totalBolinhas = 0;
        for (Grupo grupo : grupos) {
            totalBolinhas += grupo.size();
        }

        return totalBolinhas / grupos.size();
    }

    private int calcularDistribuicaoEspacial(List<Grupo> grupos, Ponto centro) {
        if (grupos.isEmpty())
            return 0;

        double somaDistancia = 0;
        for (Grupo grupo : grupos) {
            somaDistancia += calcularDistancia(grupo.centro, centro);
        }

        return (int) (somaDistancia / grupos.size());
    }

    private int calcularConectividadeGeral(List<Grupo> grupos) {
        if (grupos.size() < 2)
            return 0;

        int conexoes = 0;
        for (int i = 0; i < grupos.size(); i++) {
            for (int j = i + 1; j < grupos.size(); j++) {
                if (verificarConectividade(grupos.get(i), grupos.get(j)) == 1) {
                    conexoes++;
                }
            }
        }

        return conexoes;
    }

    private int calcularGrauMedio(List<Grupo> grupos) {
        if (grupos.isEmpty())
            return 0;
        return calcularConectividadeGeral(grupos) / grupos.size();
    }

    private int encontrarMaiorComponente(List<Grupo> grupos) {
        if (grupos.isEmpty())
            return 0;

        int maior = 0;
        for (Grupo grupo : grupos) {
            if (grupo.size() > maior) {
                maior = grupo.size();
            }
        }

        return maior;
    }

    private int calcularDiametro(List<Grupo> grupos) {
        if (grupos.size() < 2)
            return 0;

        double maiorDistancia = 0;
        for (int i = 0; i < grupos.size(); i++) {
            for (int j = i + 1; j < grupos.size(); j++) {
                double distancia = calcularDistancia(grupos.get(i).centro, grupos.get(j).centro);
                if (distancia > maiorDistancia) {
                    maiorDistancia = distancia;
                }
            }
        }

        return (int) maiorDistancia;
    }

    private int detectarPadraoHierarquico(List<Grupo> grupos) {
        if (grupos.size() < 3)
            return 0;

        // Verifica se há um grupo central conectado a outros
        for (Grupo grupo : grupos) {
            int conexoes = 0;
            for (Grupo outro : grupos) {
                if (outro != grupo && verificarConectividade(grupo, outro) == 1) {
                    conexoes++;
                }
            }
            if (conexoes >= 2)
                return 1; // Padrão hierárquico detectado
        }

        return 0;
    }

    private int detectarPadraoSimetrico(List<Grupo> grupos) {
        return calcularSimetria(grupos);
    }

    private int detectarPadraoCentralizado(List<Grupo> grupos, Ponto centro) {
        if (grupos.isEmpty())
            return 0;

        int centralizados = 0;
        for (Grupo grupo : grupos) {
            if (calcularDistancia(grupo.centro, centro) < 100) {
                centralizados++;
            }
        }

        return centralizados >= grupos.size() / 2 ? 1 : 0;
    }

    private int detectarPadraoRadial(List<Grupo> grupos, Ponto centro) {
        if (grupos.size() < 3)
            return 0;

        int radiais = 0;
        for (Grupo grupo : grupos) {
            if (calcularDistancia(grupo.centro, centro) > 50) {
                radiais++;
            }
        }

        return radiais >= grupos.size() / 2 ? 1 : 0;
    }

    private int detectarPadraoLinear(List<Grupo> grupos) {
        if (grupos.size() < 3)
            return 0;

        // Verifica se os grupos estão alinhados
        double[] xs = new double[grupos.size()];
        double[] ys = new double[grupos.size()];

        for (int i = 0; i < grupos.size(); i++) {
            xs[i] = grupos.get(i).centro.x;
            ys[i] = grupos.get(i).centro.y;
        }

        // Calcula correlação linear
        double correlacao = calcularCorrelacao(xs, ys);
        return Math.abs(correlacao) > 0.7 ? 1 : 0;
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

    private int calcularProporcaoCabecaCorpo(Grupo cabeca, Grupo tronco) {
        return calcularProporcao(cabeca, tronco);
    }

    private int calcularProporcaoBracosTronco(List<Grupo> bracos, Grupo tronco) {
        if (tronco == null || bracos.isEmpty())
            return 0;

        int totalBracos = 0;
        for (Grupo braco : bracos) {
            totalBracos += braco.size();
        }

        return tronco.size() > 0 ? totalBracos / tronco.size() : 0;
    }

    private int calcularProporcaoPernasTronco(List<Grupo> pernas, Grupo tronco) {
        if (tronco == null || pernas.isEmpty())
            return 0;

        int totalPernas = 0;
        for (Grupo perna : pernas) {
            totalPernas += perna.size();
        }

        return tronco.size() > 0 ? totalPernas / tronco.size() : 0;
    }

    private int calcularProporcaoSimetria(List<Grupo> bracos, List<Grupo> pernas) {
        int simetriaBracos = calcularSimetria(bracos);
        int simetriaPernas = calcularSimetria(pernas);
        return simetriaBracos + simetriaPernas;
    }

    private int calcularProporcaoGeral(List<Grupo> grupos) {
        if (grupos.size() < 2)
            return 0;

        int totalBolinhas = 0;
        for (Grupo grupo : grupos) {
            totalBolinhas += grupo.size();
        }

        return totalBolinhas / grupos.size();
    }
}