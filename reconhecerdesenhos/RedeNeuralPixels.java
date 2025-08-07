package reconhecerdesenhos;

import java.io.*;

public class RedeNeuralPixels {
    private static final int NUM_ENTRADAS = 2500; // 50x50 pixels

    // Pesos da rede neural (simplificados)
    private double w11 = 1.0;
    private double w12 = 0.5;
    private double[] v1k = new double[NUM_ENTRADAS];
    private double[] v2k = new double[NUM_ENTRADAS];

    // Valores das camadas
    private double[] xk = new double[NUM_ENTRADAS]; // entrada (pixels)
    private double h1 = 0;
    private double h2 = 0;
    private double y1 = 0;

    // Controle
    private int saida = 0;

    public RedeNeuralPixels() {
        inicializarPesos();
        carregarPesos("pesos_rede.txt");
    }

    /**
     * Inicializa os pesos com valores padrão
     */
    private void inicializarPesos() {
        // Inicializa v1k e v2k com valores pequenos e balanceados
        for (int i = 0; i < NUM_ENTRADAS; i++) {
            v1k[i] = 0.01; // Valores pequenos para evitar explosão
            v2k[i] = 0.01;
        }

        w11 = 0.1; // Pesos pequenos
        w12 = 0.1;
    }

    /**
     * Define a entrada da rede neural (array de 2500 pixels)
     */
    public void setEntrada(double[] pixels) {
        if (pixels.length != NUM_ENTRADAS) {
            throw new IllegalArgumentException("Entrada deve ter " + NUM_ENTRADAS + " valores");
        }
        System.arraycopy(pixels, 0, xk, 0, NUM_ENTRADAS);
    }

    /**
     * Aplica a rede neural com função escada
     */
    public int aplica(double limiar) {
        y1 = 0;
        h1 = 0;
        h2 = 0;

        // Calcula h1 e h2
        for (int c = 0; c < v1k.length; c++) {
            h1 += v1k[c] * xk[c];
            h2 += v2k[c] * xk[c];
        }

        // Calcula y1
        y1 = (h1 * w11 + h2 * w12);

        System.out.println("y1 = " + y1);

        // Aplica função escada
        if (y1 > limiar) {
            return 1;
        }
        return 0;
    }

    /**
     * Treina a rede neural com target e limiar
     */
    public void treinar(int target, double limiar) {
        // 1. Calcula a saída atual (usa limiar padrão para treinamento)
        aplica(0.5); // Atualiza y1, h1, h2

        // 2. Calcula o erro (target = 10, queremos que y1 se aproxime de 10)
        double erro = target - y1;

        // 3. Taxa de aprendizado aumentada para ser mais efetiva
        double taxaAprendizado = 0.0001; // Taxa maior para convergência mais rápida
        // 4. Ajusta os pesos
        for (int i = 0; i < v1k.length; i++) {
            v1k[i] += taxaAprendizado * erro * xk[i];
            v2k[i] += taxaAprendizado * erro * xk[i];
        }
        w11 += taxaAprendizado * erro * h1;
        w12 += taxaAprendizado * erro * h2;

        salvarPesos("pesos_rede.txt");
    }

    /**
     * Classifica usando função escada
     */
    public boolean classificar() {
        return aplica(10.0) == 1; // Limiar ajustado para 10
    }

    /**
     * Salva os pesos da rede neural
     */
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

    /**
     * Carrega os pesos da rede neural
     */
    public void carregarPesos(String caminho) {
        File f = new File(caminho);
        if (!f.exists()) {
            System.out.println("Arquivo de pesos não encontrado, usando valores padrão.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linha = br.readLine();
            if (linha != null) {
                w11 = Double.parseDouble(linha.trim());
            }

            linha = br.readLine();
            if (linha != null) {
                w12 = Double.parseDouble(linha.trim());
            }

            for (int i = 0; i < v1k.length; i++) {
                linha = br.readLine();
                if (linha != null) {
                    v1k[i] = Double.parseDouble(linha.trim());
                }
            }
            for (int i = 0; i < v2k.length; i++) {
                linha = br.readLine();
                if (linha != null) {
                    v2k[i] = Double.parseDouble(linha.trim());
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao carregar pesos: " + e.getMessage());
            System.out.println("Usando valores padrão.");
        }
    }
}