package reconhecerdesenhos;

import java.io.*;

public class RedeNeural {
    double y1 = 0;
    double h1 = 0;
    double w11 = 0;
    double h2 = 0;
    double w12 = 0; // Pesos da camada de saída
    double[] v1k = new double[80]; // Pesos da primeira camada oculta (80 características por bolinha)
    double[] v2k = new double[80]; // Pesos da segunda camada oculta (80 características por bolinha)
    double[] xk = new double[80]; // Entrada - características por bolinha [grau, dist, grau, dist, ...]
    int contUsos = 0;
    boolean emUso = false;
    int saida = 0;

    public RedeNeural(double[] featuresPorBolinha) {
        // configura entrada com as características por bolinha
        for (int c = 0; c < 80; c++) {
            this.xk[c] = normalizarCaracteristicaPorBolinha(featuresPorBolinha[c], c);
        }

        // Inicializa pesos com valores padrão
        inicializarPesosPadrao();

        // Tenta carregar pesos do arquivo
        carregarPesos("pesos_rede.txt");
    }

    public RedeNeural(int[] featuresEstruturais) {
        // configura entrada com as características estruturais (mantido para
        // compatibilidade)
        for (int c = 0; c < 15; c++) {
            this.xk[c] = normalizarCaracteristica(featuresEstruturais[c], c);
        }

        // Inicializa pesos com valores padrão
        inicializarPesosPadrao();

        // Tenta carregar pesos do arquivo
        carregarPesos("pesos_rede.txt");
    }

    public RedeNeural(int[] projVertical, int[] projHorizontal) {
        // configura entrada com as projeções (mantido para compatibilidade)
        for (int c = 0; c < 10; c++) {
            this.xk[c] = projVertical[c];
            this.xk[c + 10] = projHorizontal[c];
        }

        // Inicializa pesos com valores padrão
        inicializarPesosPadrao();

        // Tenta carregar pesos do arquivo
        carregarPesos("pesos_rede.txt");
    }

    /**
     * Inicializa pesos com valores padrão para características por bolinha
     */
    private void inicializarPesosPadrao() {
        w11 = 0.1; // Reduzido drasticamente para evitar overflow
        w12 = 0.1; // Reduzido drasticamente para evitar overflow

        // Inicializa v1k com valores muito conservadores
        for (int i = 0; i < 80; i++) {
            v1k[i] = 0.1; // Valores muito baixos para evitar overflow
        }

        // Inicializa v2k com valores muito conservadores
        for (int i = 0; i < 80; i++) {
            v2k[i] = 0.1; // Valores muito baixos para evitar overflow
        }
    }

    public void setEntrada(double[] featuresPorBolinha) {
        // configura entrada com as características por bolinha normalizadas
        for (int c = 0; c < 80; c++) {
            this.xk[c] = normalizarCaracteristicaPorBolinha(featuresPorBolinha[c], c);
        }
        contUsos = 0;
    }

    public void setEntrada(int[] featuresEstruturais) {
        // configura entrada com as características estruturais normalizadas (mantido
        // para compatibilidade)
        for (int c = 0; c < 15; c++) {
            this.xk[c] = normalizarCaracteristica(featuresEstruturais[c], c);
        }
        contUsos = 0;
    }

    /**
     * Normaliza cada característica por bolinha para evitar overflow
     */
    private double normalizarCaracteristicaPorBolinha(double valor, int indice) {
        // Para cada bolinha: [grau, distância_em_nós]
        // Grau: 0-10 (normalmente 1-4 para boneco palito)
        // Distância: 0-999 (0 = nó central, 999 = desconectado)

        if (indice % 2 == 0) {
            // É um grau (índices pares: 0, 2, 4, ...)
            return Math.min(valor, 10) / 10.0; // Normaliza grau para 0-1
        } else {
            // É uma distância (índices ímpares: 1, 3, 5, ...)
            if (valor >= 999) {
                return 1.0; // Bolinha desconectada
            } else {
                return Math.min(valor, 10) / 10.0; // Normaliza distância para 0-1 (máx 10 saltos)
            }
        }
    }

    /**
     * Normaliza cada característica estrutural para evitar overflow (mantido para
     * compatibilidade)
     */
    private double normalizarCaracteristica(int valor, int indice) {
        // Normaliza baseado no tipo de característica
        switch (indice) {
            case 0: // Total bolinhas (0-50)
                return Math.min(valor, 50) / 50.0;
            case 1: // Nós do grafo (0-50)
                return Math.min(valor, 50) / 50.0;
            case 2: // Arestas do grafo (0-100)
                return Math.min(valor, 100) / 100.0;
            case 3: // Grau médio (0-10)
                return Math.min(valor, 10) / 10.0;
            case 4: // Grau máximo (0-20)
                return Math.min(valor, 20) / 20.0;
            case 5: // Grau mínimo (0-5)
                return Math.min(valor, 5) / 5.0;
            case 6: // Extremidades (0-10)
                return Math.min(valor, 10) / 10.0;
            case 7: // Linhas (0-20)
                return Math.min(valor, 20) / 20.0;
            case 8: // Junções (0-50)
                return Math.min(valor, 50) / 50.0;
            case 9: // Largura crop (0-200)
                return Math.min(valor, 200) / 200.0;
            case 10: // Altura crop (0-400)
                return Math.min(valor, 400) / 400.0;
            case 11: // Proporção W/H (0-30)
                return Math.min(valor, 30) / 30.0;
            case 12: // Densidade (0-1000)
                return Math.min(valor, 1000) / 1000.0;
            case 13: // Simetria grafo (0-10)
                return valor / 10.0;
            case 14: // Estrutura grafo (0-1)
                return valor;
            default:
                return Math.min(valor, 100) / 100.0; // Normalização padrão
        }
    }

    public void setEntrada(int[] projVertical, int[] projHorizontal) {
        // configura entrada com as projeções (mantido para compatibilidade)
        for (int c = 0; c < 10; c++) {
            this.xk[c] = projVertical[c];
            this.xk[c + 10] = projHorizontal[c];
        }
        contUsos = 0;
    }

    public int aplica(double limiar) {
        contUsos += 1;
        saida = 0;
        y1 = 0;
        h1 = 0;
        h2 = 0;

        // Verifica se há valores NaN nos pesos
        if (Double.isNaN(w11) || Double.isNaN(w12) || Double.isInfinite(w11) || Double.isInfinite(w12)) {
            System.out.println("Pesos inválidos detectados, reinicializando...");
            w11 = 0.1;
            w12 = 0.1;
        }

        // Verifica se há valores NaN nas entradas
        for (int c = 0; c < v1k.length; c++) {
            if (Double.isNaN(xk[c]) || Double.isInfinite(xk[c])) {
                System.out.println("Entrada inválida detectada na posição " + c + ", zerando...");
                xk[c] = 0.0;
            }
        }

        for (int c = 0; c < v1k.length; c++) {
            // Verifica se os pesos são válidos
            if (Double.isNaN(v1k[c]) || Double.isInfinite(v1k[c]))
                v1k[c] = 0.1;
            if (Double.isNaN(v2k[c]) || Double.isInfinite(v2k[c]))
                v2k[c] = 0.1;

            // Verifica se os valores intermediários não estão explodindo
            double produto1 = v1k[c] * xk[c];
            double produto2 = v2k[c] * xk[c];

            // Verifica se os produtos são válidos
            if (Double.isNaN(produto1) || Double.isInfinite(produto1) || Math.abs(produto1) > 100) {
                System.out.println("Produto inválido detectado em v1k[" + c + "], limitando...");
                produto1 = Math.signum(produto1) * 100; // Limita a 100
            }
            if (Double.isNaN(produto2) || Double.isInfinite(produto2) || Math.abs(produto2) > 100) {
                System.out.println("Produto inválido detectado em v2k[" + c + "], limitando...");
                produto2 = Math.signum(produto2) * 100; // Limita a 100
            }

            h1 += produto1;
            h2 += produto2;
        }

        // Verifica se h1 e h2 são válidos
        if (Double.isNaN(h1) || Double.isInfinite(h1) || Math.abs(h1) > 1000) {
            System.out.println("h1 inválido detectado, limitando...");
            h1 = Math.signum(h1) * 1000; // Limita a 1000
        }
        if (Double.isNaN(h2) || Double.isInfinite(h2) || Math.abs(h2) > 1000) {
            System.out.println("h2 inválido detectado, limitando...");
            h2 = Math.signum(h2) * 1000; // Limita a 1000
        }

        y1 = (h1 * w11 + h2 * w12);

        // Debug: mostra valores intermediários
        System.out.println("Debug - h1: " + h1 + ", h2: " + h2 + ", w11: " + w11 + ", w12: " + w12);

        // Verifica se o resultado é válido
        if (Double.isNaN(y1) || Double.isInfinite(y1)) {
            System.out.println("Resultado inválido detectado, reinicializando pesos...");
            System.out.println("y1 = " + y1 + " (h1=" + h1 + ", h2=" + h2 + ", w11=" + w11 + ", w12=" + w12 + ")");
            reinicializarPesos();
            y1 = 0.0;
        }

        System.out.println("y1 = " + y1);
        // aplica função escada
        if (y1 > limiar) {
            saida = 1;
            return 1;
        }
        return 0;
    }

    /**
     * Reinicializa os pesos com valores padrão
     */
    public void reinicializarPesos() {
        inicializarPesosPadrao();
        System.out.println("Pesos reinicializados com valores padrão (80 características por bolinha).");
    }

    public void treinar(int target, double limiar) {
        // 1. Calcula a saída atual
        aplica(limiar); // Atualiza y1, h1, h2

        // 2. Calcula o erro
        double erro = target - y1;

        // Verifica se o erro é válido
        if (Double.isNaN(erro) || Double.isInfinite(erro)) {
            System.out.println("Erro inválido detectado no treinamento, abortando...");
            return;
        }

        // 3. Taxa de aprendizado
        double taxaAprendizado = 0.02; // Taxa muito baixa para evitar overflow

        // 4. Ajusta os pesos
        for (int i = 0; i < v1k.length; i++) {
            double delta1 = taxaAprendizado * erro * xk[i];
            double delta2 = taxaAprendizado * erro * xk[i];

            // Verifica se os deltas são válidos
            if (!Double.isNaN(delta1) && !Double.isInfinite(delta1)) {
                v1k[i] += delta1;
            }
            if (!Double.isNaN(delta2) && !Double.isInfinite(delta2)) {
                v2k[i] += delta2;
            }
        }

        double deltaW11 = taxaAprendizado * erro * h1;
        double deltaW12 = taxaAprendizado * erro * h2;

        // Verifica se os deltas são válidos
        if (!Double.isNaN(deltaW11) && !Double.isInfinite(deltaW11)) {
            w11 += deltaW11;
        }
        if (!Double.isNaN(deltaW12) && !Double.isInfinite(deltaW12)) {
            w12 += deltaW12;
        }

        salvarPesos("pesos_rede.txt");
    }

    public void salvarPesos(String caminho) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(caminho))) {
            pw.println(w11);
            pw.println(w12);
            for (double v : v1k)
                pw.println(v);
            for (double v : v2k)
                pw.println(v);
        } catch (IOException e) {
            System.out.println("Erro ao salvar pesos: " + e.getMessage());
        }
    }

    public void carregarPesos(String caminho) {
        File f = new File(caminho);
        if (!f.exists()) {
            System.out.println("Arquivo de pesos não encontrado, usando valores padrão.");
            return; // Se não existe, mantém os valores padrão
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linha = br.readLine();
            if (linha != null)
                w11 = Double.parseDouble(linha);

            linha = br.readLine();
            if (linha != null)
                w12 = Double.parseDouble(linha);

            // Verifica se os pesos principais são válidos e não muito grandes
            if (Double.isNaN(w11) || Double.isInfinite(w11) || Math.abs(w11) > 10) {
                System.out.println("Pesos corrompidos detectados! Deletando arquivo e usando valores padrão.");
                f.delete();
                reinicializarPesos();
                return;
            }
            if (Double.isNaN(w12) || Double.isInfinite(w12) || Math.abs(w12) > 10) {
                System.out.println("Pesos corrompidos detectados! Deletando arquivo e usando valores padrão.");
                f.delete();
                reinicializarPesos();
                return;
            }

            for (int i = 0; i < v1k.length; i++) {
                linha = br.readLine();
                if (linha != null) {
                    double valor = Double.parseDouble(linha);
                    if (Double.isNaN(valor) || Double.isInfinite(valor) || Math.abs(valor) > 10) {
                        System.out.println("Pesos corrompidos detectados! Deletando arquivo e usando valores padrão.");
                        f.delete();
                        reinicializarPesos();
                        return;
                    } else {
                        v1k[i] = valor;
                    }
                }
            }

            for (int i = 0; i < v2k.length; i++) {
                linha = br.readLine();
                if (linha != null) {
                    double valor = Double.parseDouble(linha);
                    if (Double.isNaN(valor) || Double.isInfinite(valor) || Math.abs(valor) > 10) {
                        System.out.println("Pesos corrompidos detectados! Deletando arquivo e usando valores padrão.");
                        f.delete();
                        reinicializarPesos();
                        return;
                    } else {
                        v2k[i] = valor;
                    }
                }
            }

            System.out.println("Pesos carregados com sucesso do arquivo.");

        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao carregar pesos: " + e.getMessage());
            System.out.println("Deletando arquivo corrompido e usando valores padrão.");
            f.delete();
            reinicializarPesos();
        }
    }

    public void printModelo() {
        System.out.println("b1.redeNeural.w11 = " + w11 + ";");
        System.out.println("b1.redeNeural.w12 = " + w12 + ";");
        for (int i = 0; i < v1k.length; i++) {
            System.out.println("b1.redeNeural.v1k[" + i + "] = " + v1k[i] + ";");
            System.out.println("b1.redeNeural.v2k[" + i + "] = " + v2k[i] + ";");
        }
    }
}
