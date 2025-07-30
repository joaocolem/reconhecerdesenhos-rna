package reconhecerdesenhos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import java.io.File;

/**
 * Reconhecedor de Boneco Palito - Abordagem Simples
 * Usa grade 5x5 normalizada para reconhecimento
 */
public class ThreadBolinhaEx extends JPanel {
    static int contBolinhas = 0;
    RedeNeural ultimaRedeNeural = null; // Guarda a última rede neural usada

    public ThreadBolinhaEx() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                int mouse_x = me.getX();
                int mouse_y = me.getY();
                if (contBolinhas >= bolinhasThread.bolinhas.length) {
                    return;
                }
                Color cor = Color.green;
                if (SwingUtilities.isRightMouseButton(me)) {
                    cor = new Color(150, 75, 0);// marrom
                }
                bolinhasThread.bolinhas[contBolinhas] = new Bolinha("bolinha", cor, mouse_x - 15, mouse_y - 15);
                contBolinhas++;
            }
        });

        // Adiciona listener para desenho contínuo
        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent me) {
                int mouse_x = me.getX();
                int mouse_y = me.getY();
                if (contBolinhas >= bolinhasThread.bolinhas.length) {
                    return;
                }

                // Verifica se já existe uma bolinha muito próxima (evita sobreposição)
                boolean muitoProximo = false;
                for (int i = 0; i < contBolinhas; i++) {
                    if (bolinhasThread.bolinhas[i] != null) {
                        double distancia = Math.sqrt(Math.pow(mouse_x - (bolinhasThread.bolinhas[i].x + 15), 2) +
                                Math.pow(mouse_y - (bolinhasThread.bolinhas[i].y + 15), 2));
                        if (distancia < 20) { // Se está muito próximo, não adiciona
                            muitoProximo = true;
                            break;
                        }
                    }
                }

                if (!muitoProximo) {
                    Color cor = Color.green;
                    if (SwingUtilities.isRightMouseButton(me)) {
                        cor = new Color(150, 75, 0);// marrom
                    }
                    bolinhasThread.bolinhas[contBolinhas] = new Bolinha("bolinha", cor, mouse_x - 15, mouse_y - 15);
                    contBolinhas++;
                }
            }
        });
    }

    public void desenha(Bolinha[] bolinhas) {
        Graphics g = getGraphics();
        for (int c = 0; c < contBolinhas; c++) {
            Bolinha b = bolinhas[c];
            g.setColor(b.cor);
            g.fillOval(b.x, b.y, 30, 30);
        }

        if (contBolinhas >= bolinhas.length) {
            double[] featuresPorBolinha = this.analisarBonecoPalitoPorBolinha(bolinhasThread.bolinhas);

            // Debug: mostra as características por bolinha
            System.out.println("Características por bolinha (grau, distância_em_nós):");
            int numBolinhas = 0;
            for (int i = 0; i < featuresPorBolinha.length; i += 2) {
                if (featuresPorBolinha[i] > 0 || featuresPorBolinha[i + 1] > 0) {
                    System.out.println("Bolinha " + numBolinhas + ": grau=" + featuresPorBolinha[i] +
                            ", distância=" + featuresPorBolinha[i + 1]);
                    numBolinhas++;
                }
            }
            System.out.println("Total de bolinhas processadas: " + numBolinhas);
            System.out.println();

            RedeNeural rn = new RedeNeural(featuresPorBolinha);
            ultimaRedeNeural = rn; // Guarda a última rede neural usada
            if (rn.contUsos == 0)
                rn.aplica(47);// 49
            if (rn.saida == 1 && rn.contUsos == 1) {
                g.setColor(Color.BLUE);
                g.drawString("É um boneco palito!", 10, 10);
                System.out.println("é um boneco palito");
                rn.contUsos++;
            } else if (rn.saida == 0 && rn.contUsos == 1) {
                g.setColor(Color.RED);
                g.drawString("Não é um boneco palito!", 10, 10);
                System.out.println("Não é um boneco palito");
                rn.contUsos++;
            }
        }
    }

    /**
     * Analisa se o desenho representa um boneco palito usando features por bolinha
     * Cada bolinha tem: [grau, distância_em_nós_ao_nó_maior_grau]
     */
    public double[] analisarBonecoPalitoPorBolinha(Bolinha[] bolinhas) {
        AnaliseEstrutural analise = new AnaliseEstrutural();
        return analise.analisarBonecoPalitoPorBolinha(bolinhas);
    }

    /**
     * Analisa se o desenho representa um boneco palito usando análise estrutural
     * (mantido para compatibilidade)
     * Detecta: cabeça, tronco, braços, pernas e suas relações
     */
    public int[] analisarBonecoPalitoEstrutural(Bolinha[] bolinhas) {
        AnaliseEstrutural analise = new AnaliseEstrutural();
        return analise.analisarBonecoPalitoEstrutural(bolinhas);
    }

    public static Color gerarCorAleatoriamente() {
        Random randColor = new Random();
        int r = randColor.nextInt(256);
        int g = randColor.nextInt(256);
        int b = 150 + randColor.nextInt(100);// randColor.nextInt(256);
        return new Color(r, g, b);
    }

    /**
     * A tester method that embeds the panel in a frame so you can
     * run it as an application.
     */
    static ThreadBolinhaEx painel = new ThreadBolinhaEx();
    static BolinhasThread bolinhasThread;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);

        painel = new ThreadBolinhaEx();
        frame.getContentPane().add(painel, BorderLayout.CENTER);

        // Cria o painel de botões
        JPanel painelBotoes = new JPanel();

        JButton btnTreinar = new JButton("Treinar");
        btnTreinar.addActionListener(e -> {
            if (painel.ultimaRedeNeural != null) {
                painel.ultimaRedeNeural.treinar(47, 47);
                System.out.println("Treinamento realizado!");
                painel.ultimaRedeNeural.printModelo(); // Mostra os pesos atualizados
            } else {
                System.out.println("Nenhuma rede neural para treinar.");
            }
        });

        JButton btnReiniciar = new JButton("Reiniciar");
        btnReiniciar.addActionListener(e -> {
            // Limpa o canvas
            contBolinhas = 0;
            for (int i = 0; i < bolinhasThread.bolinhas.length; i++) {
                bolinhasThread.bolinhas[i] = null;
            }
            painel.ultimaRedeNeural = null;

            // Limpa a tela
            Graphics g = painel.getGraphics();
            if (g != null) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, painel.getWidth(), painel.getHeight());
            }

            System.out.println("Canvas reiniciado! Desenhe um novo boneco palito.");
        });

        painelBotoes.add(btnTreinar);
        painelBotoes.add(btnReiniciar);
        frame.getContentPane().add(painelBotoes, BorderLayout.SOUTH);

        frame.setVisible(true);
        bolinhasThread = new BolinhasThread(painel, 100);
        bolinhasThread.start();
    }
}