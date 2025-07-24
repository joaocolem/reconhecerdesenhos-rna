package reconhecerdesenhos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JButton;

/**
 * This is an extremely scaled-down sketching canvas; with it you
 * can only scribble thin black lines. For simplicity the window
 * contents are never refreshed when they are uncovered.
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
                /*
                 * if(contBolinhas >= 15) {
                 * cor = new Color(150, 75, 0);//marrom
                 * }
                 */
                bolinhasThread.bolinhas[contBolinhas] = new Bolinha("bolinha", cor, mouse_x - 15, mouse_y - 15);
                contBolinhas++;
            }
        });
        // Removido o botão Treinar do construtor
    }

    public void desenha(Bolinha[] bolinhas) {
        Graphics g = getGraphics();
        for (int c = 0; c < contBolinhas; c++) {
            Bolinha b = bolinhas[c];

            g.setColor(b.cor);
            g.fillOval(b.x, b.y, 30, 30);
        }
        if (contBolinhas >= bolinhas.length) {
            int[] projVert = this.getProjecaoVertical(bolinhasThread.bolinhas);
            int[] projHori = this.getProjecaoHorizontal(bolinhasThread.bolinhas);
            RedeNeural rn = new RedeNeural(projVert, projHori);
            ultimaRedeNeural = rn; // Guarda a última rede neural usada
            if (rn.contUsos == 0)
                rn.aplica(47);// 49
            if (rn.saida == 1 && rn.contUsos == 1) {
                g.setColor(Color.BLUE);
                g.drawString("É uma árvore!", 10, 10);
                System.out.println("é uma árvore");
                rn.contUsos++;
            } else if (rn.saida == 0 && rn.contUsos == 1) {
                g.setColor(Color.RED);
                g.drawString("Não é uma árvore!", 10, 10);
                System.out.println("Não é uma árvore");
                rn.contUsos++;
            }
        }

    }

    public int[] getProjecaoVertical(Bolinha[] bolinhas) {
        // projeção de largura 10
        int[] projecao = new int[10];
        int intervalo = this.getHeight() / 10;
        int indiceProj = 0;
        for (int i = 0; i < 10; i += 1) {
            int parteInicial = i * intervalo;
            int parteFinal = parteInicial + intervalo;
            for (Bolinha b : bolinhas) {
                // System.out.println("Faixa "+parteInicial+" - "+parteFinal);
                if (parteInicial <= b.y && b.y < parteFinal) {
                    projecao[indiceProj] += 1;
                    // System.out.println("Adicionando bolinha "+b.y+" em "+indiceProj);
                }
            }
            indiceProj++;
        }
        return projecao;
    }

    public int[] getProjecaoHorizontal(Bolinha[] bolinhas) {
        // projeção de largura 10
        int[] projecao = new int[10];
        int intervalo = this.getWidth() / 10;
        int indiceProj = 0;
        for (int i = 0; i < 10; i += 1) {
            for (Bolinha b : bolinhas) {
                int parteInicial = i * intervalo;
                int parteFinal = parteInicial + intervalo;
                if (parteInicial <= b.x && b.x < parteFinal) {
                    projecao[indiceProj] += 1;
                    // System.out.println("Adicionando bolinha "+b.x+" em "+indiceProj);
                }
            }
            indiceProj++;
        }
        return projecao;
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
        frame.setSize(200, 250);

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
        painelBotoes.add(btnTreinar);
        frame.getContentPane().add(painelBotoes, BorderLayout.SOUTH);

        frame.setVisible(true);
        bolinhasThread = new BolinhasThread(painel, 100);
        bolinhasThread.start();
    }
}