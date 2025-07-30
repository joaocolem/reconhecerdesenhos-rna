package reconhecerdesenhos;

import java.awt.Rectangle;
import java.util.*;

public class AnaliseSimples {

    /**
     * Analisa o boneco palito de forma simples e robusta
     * 1. Corta a imagem para manter apenas o desenho
     * 2. Normaliza a orientação (parte mais cheia para cima)
     * 3. Vetoriza em uma grade simples
     * 4. Retorna vetor de características
     */
    public int[] analisarBonecoPalitoSimples(Bolinha[] bolinhas) {
        // 1. Encontra a bounding box do desenho
        Rectangle bbox = encontrarBoundingBox(bolinhas);
        if (bbox == null) {
            return new int[25]; // Retorna zeros se não há bolinhas
        }

        // 2. Cria uma grade 5x5 da área do desenho
        boolean[][] grade = criarGrade(bolinhas, bbox);

        // 3. Normaliza a orientação (rotação)
        boolean[][] gradeNormalizada = normalizarOrientacao(grade);

        // 4. Vetoriza a grade normalizada
        return vetorizarGrade(gradeNormalizada);
    }

    /**
     * Encontra a bounding box que contém todas as bolinhas
     */
    private Rectangle encontrarBoundingBox(Bolinha[] bolinhas) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        boolean encontrouBolinha = false;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                minX = Math.min(minX, b.x);
                minY = Math.min(minY, b.y);
                maxX = Math.max(maxX, b.x + 30); // +30 para incluir o tamanho da bolinha
                maxY = Math.max(maxY, b.y + 30);
                encontrouBolinha = true;
            }
        }

        if (!encontrouBolinha) {
            return null;
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Cria uma grade 5x5 representando o desenho
     */
    private boolean[][] criarGrade(Bolinha[] bolinhas, Rectangle bbox) {
        boolean[][] grade = new boolean[5][5];

        // Calcula o tamanho de cada célula da grade
        double cellWidth = bbox.width / 5.0;
        double cellHeight = bbox.height / 5.0;

        // Para cada bolinha, marca as células correspondentes na grade
        for (Bolinha b : bolinhas) {
            if (b != null) {
                // Calcula posição relativa na grade
                int gridX = (int) ((b.x - bbox.x) / cellWidth);
                int gridY = (int) ((b.y - bbox.y) / cellHeight);

                // Garante que está dentro dos limites
                gridX = Math.max(0, Math.min(4, gridX));
                gridY = Math.max(0, Math.min(4, gridY));

                grade[gridY][gridX] = true;
            }
        }

        return grade;
    }

    /**
     * Normaliza a orientação - gira para que a parte mais cheia fique para cima
     */
    private boolean[][] normalizarOrientacao(boolean[][] grade) {
        // Calcula a "densidade" de cada lado
        int[] densidades = calcularDensidadesLados(grade);

        // Encontra o lado com maior densidade
        int maxDensidade = 0;
        int ladoMaisCheio = 0; // 0=cima, 1=direita, 2=baixo, 3=esquerda

        for (int i = 0; i < 4; i++) {
            if (densidades[i] > maxDensidade) {
                maxDensidade = densidades[i];
                ladoMaisCheio = i;
            }
        }

        // Rotaciona a grade para que o lado mais cheio fique para cima
        return rotacionarGrade(grade, ladoMaisCheio);
    }

    /**
     * Calcula a densidade de cada lado da grade
     */
    private int[] calcularDensidadesLados(boolean[][] grade) {
        int[] densidades = new int[4];

        // Cima (primeira linha)
        for (int x = 0; x < 5; x++) {
            if (grade[0][x])
                densidades[0]++;
        }

        // Direita (última coluna)
        for (int y = 0; y < 5; y++) {
            if (grade[y][4])
                densidades[1]++;
        }

        // Baixo (última linha)
        for (int x = 0; x < 5; x++) {
            if (grade[4][x])
                densidades[2]++;
        }

        // Esquerda (primeira coluna)
        for (int y = 0; y < 5; y++) {
            if (grade[y][0])
                densidades[3]++;
        }

        return densidades;
    }

    /**
     * Rotaciona a grade para que o lado mais cheio fique para cima
     */
    private boolean[][] rotacionarGrade(boolean[][] grade, int rotacoes) {
        boolean[][] resultado = new boolean[5][5];

        // Copia a grade original
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                resultado[y][x] = grade[y][x];
            }
        }

        // Aplica as rotações necessárias
        for (int r = 0; r < rotacoes; r++) {
            resultado = rotacionar90Graus(resultado);
        }

        return resultado;
    }

    /**
     * Rotaciona a grade 90 graus no sentido horário
     */
    private boolean[][] rotacionar90Graus(boolean[][] grade) {
        boolean[][] rotacionada = new boolean[5][5];

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                rotacionada[x][4 - y] = grade[y][x];
            }
        }

        return rotacionada;
    }

    /**
     * Converte a grade normalizada em um vetor de características
     */
    private int[] vetorizarGrade(boolean[][] grade) {
        int[] vetor = new int[25];
        int index = 0;

        // Converte a grade 5x5 em um vetor linear
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                vetor[index++] = grade[y][x] ? 1 : 0;
            }
        }

        return vetor;
    }

    /**
     * Método auxiliar para debug - imprime a grade
     */
    public void imprimirGrade(boolean[][] grade) {
        System.out.println("Grade 5x5:");
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                System.out.print(grade[y][x] ? "X " : ". ");
            }
            System.out.println();
        }
        System.out.println();
    }
}