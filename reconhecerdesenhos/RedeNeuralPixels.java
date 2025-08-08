package reconhecerdesenhos;

import java.io.*;

public class RedeNeuralPixels {
    private static final int NUM_ENTRADAS = 2500; // 50x50 pixels

    // pesos da rede neural (simplificados)
    private double w11 = 1.0;
    private double w12 = 0.5;
    private double[] v1k = new double[NUM_ENTRADAS];
    private double[] v2k = new double[NUM_ENTRADAS];

    // valores das camadas
    private double[] xk = new double[NUM_ENTRADAS]; // entrada (pixels)
    private double h1 = 0;
    private double h2 = 0;
    private double y1 = 0;

    // controle
    private int saida = 0;

    public RedeNeuralPixels() {
        inicializarPesos();
        carregarPesos("pesos_rede.txt");
    }

    /**
     * inicializa os pesos com valores padrão
     */
    private void inicializarPesos() {
        for (int i = 0; i < NUM_ENTRADAS; i++) {
            v1k[i] = 0.01;
            v2k[i] = 0.01;
        }

        w11 = 0.1;
        w12 = 0.1;
    }

    /**
     * define a entrada da rede neural (array de 2500 pixels)
     */
    public void setEntrada(double[] pixels) {
        if (pixels.length != NUM_ENTRADAS) {
            throw new IllegalArgumentException("Entrada deve ter " + NUM_ENTRADAS + " valores");
        }
        System.arraycopy(pixels, 0, xk, 0, NUM_ENTRADAS);
    }

    // aplica a rede neural com função escada
    public int aplica(double limiar) {
        y1 = 0;
        h1 = 0;
        h2 = 0;

        for (int c = 0; c < v1k.length; c++) {
            h1 += v1k[c] * xk[c];
            h2 += v2k[c] * xk[c];
        }

        y1 = (h1 * w11 + h2 * w12);

        System.out.println("y1 = " + y1);

        if (y1 > limiar) {
            return 1;
        }
        return 0;
    }

    // faz um passo de treinamento ajustando os pesos
    public void treinar(int target, double limiar) {
        aplica(0.5);

        double erro = target - y1;

        double taxaAprendizado = 0.0001;
        for (int i = 0; i < v1k.length; i++) {
            v1k[i] += taxaAprendizado * erro * xk[i];
            v2k[i] += taxaAprendizado * erro * xk[i];
        }
        w11 += taxaAprendizado * erro * h1;
        w12 += taxaAprendizado * erro * h2;

        salvarPesos("pesos_rede.txt");
    }

    // classifica usando função escada
    public boolean classificar() {
        return aplica(10.0) == 1;
    }

    // salva os pesos da rede neural
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

    // carrega os pesos da rede neural
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