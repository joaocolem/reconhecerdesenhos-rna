package reconhecerdesenhos;

import java.awt.Color;
import java.util.Random;

public class Bolinha {
    String nome;
    Color cor;
    int x = 0;
    int y = 0;
    int cont = 0;
    int contColisoes = 0;
    int velocidade = 0;
    boolean acertou = false;
    int pontos = 0;
    int pontosGeral = 0;

    public Bolinha(String nome, Color cor, int x, int y) {
        this.nome = nome;
        this.cor = cor;
        this.x = x;
        this.y = y;
        Random random = new Random();
        this.velocidade = random.nextInt(1, 6);
    }
}