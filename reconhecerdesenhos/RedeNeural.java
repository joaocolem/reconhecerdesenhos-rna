package reconhecerdesenhos;

import java.io.*;

public class RedeNeural {
    double y1 = 0;
    double h1 = 0;// dava para montar com vetores mas fiz assim para ficar mais fácil de explicar
    double w11 = 0;
    double h2 = 0;
    double w12 = 0; // 2,2,2,2,2,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0
    double[] v1k = { 3, 3, 2, 2, 2, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };// new double[20];
    double[] v2k = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1 };// new double[20];
    double[] xk = new double[20];
    int contUsos = 0;
    boolean emUso = false;
    int saida = 0;

    public RedeNeural(int[] projVertical, int[] projHorizontal) {
        // configura entrada
        for (int c = 0; c < 10; c++) {
            this.xk[c] = projVertical[c];
            this.xk[c + 10] = projHorizontal[c];
        }
        // gera modelo de forma aleatória
        /*
         * Random random = new Random();
         * for(int c = 0; c < v1k.length; c++) {
         * v1k[c] = random.nextDouble(0, 3);
         * v2k[c] = random.nextDouble(0, 3);
         * }
         */
        w11 = 1;
        w12 = 0.5;
        // Tenta carregar pesos do arquivo
        carregarPesos("pesos_rede.txt");
    }

    public void setEntrada(int[] projVertical, int[] projHorizontal) {
        // configura entrada
        for (int c = 0; c < 10; c++) {
            this.xk[c] = projVertical[c];
            this.xk[c + 10] = projHorizontal[c + 10];
        }
        contUsos = 0;
    }

    public int aplica(double limiar) {
        contUsos += 1;
        saida = 0;
        y1 = 0;
        h1 = 0;
        h2 = 0;
        for (int c = 0; c < v1k.length; c++) {
            h1 += v1k[c] * xk[c];
            h2 += v2k[c] * xk[c];
        }
        y1 = (h1 * w11 + h2 * w12);
        /*
         * String sx1 = "";
         * String sx2 = "";
         * for(int i = 0; i < xk.length/2; i++) {
         * sx1 += xk[i]+", ";
         * sx2 += xk[i+10]+", ";
         * }
         * System.out.println(sx1);
         * System.out.println(sx2);
         * System.out.println("h1: "+h1+"  h2: "+h2);
         */
        System.out.println("y1 = " + y1);
        // aplica função escada
        if (y1 > limiar) {
            saida = 1;
            return 1;
        }
        return 0;
    }

    public void treinar(int target, double limiar) {
        // 1. Calcula a saída atual
        aplica(limiar); // Atualiza y1, h1, h2

        // 2. Calcula o erro
        double erro = target - y1;

        // 3. Taxa de aprendizado
        double taxaAprendizado = 0.001;

        // 4. Ajusta os pesos
        for (int i = 0; i < v1k.length; i++) {
            v1k[i] += taxaAprendizado * erro * xk[i];
            v2k[i] += taxaAprendizado * erro * xk[i];
        }
        w11 += taxaAprendizado * erro * h1;
        w12 += taxaAprendizado * erro * h2;
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
        if (!f.exists())
            return; // Se não existe, mantém os valores padrão
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            w11 = Double.parseDouble(br.readLine());
            w12 = Double.parseDouble(br.readLine());
            for (int i = 0; i < v1k.length; i++)
                v1k[i] = Double.parseDouble(br.readLine());
            for (int i = 0; i < v2k.length; i++)
                v2k[i] = Double.parseDouble(br.readLine());
        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao carregar pesos: " + e.getMessage());
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