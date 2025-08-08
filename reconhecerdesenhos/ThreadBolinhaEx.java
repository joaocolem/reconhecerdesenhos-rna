package reconhecerdesenhos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import java.io.File;

/**
 * reconhecedor de boneco palito - abordagem baseada em pixels
 * usa representação 50x50 pixels para reconhecimento
 */
public class ThreadBolinhaEx extends JPanel {
    static int contBolinhas = 0;
    RedeNeuralPixels ultimaRedeNeural = null; 
    AnalisePixel analisePixel = new AnalisePixel(); 
    boolean analiseJaFeita = false;

    static JFrame janelaProcessamento = null;
    static PainelProcessamento painelProcessamento = null;

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
                    cor = new Color(150, 75, 0);
                }
                bolinhasThread.bolinhas[contBolinhas] = new Bolinha("bolinha", cor, mouse_x - 15, mouse_y - 15);
                contBolinhas++;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent me) {
                int mouse_x = me.getX();
                int mouse_y = me.getY();
                if (contBolinhas >= bolinhasThread.bolinhas.length) {
                    return;
                }

                // verifica se já existe uma bolinha muito próxima (evita sobreposição)
                boolean muitoProximo = false;
                for (int i = 0; i < contBolinhas; i++) {
                    if (bolinhasThread.bolinhas[i] != null) {
                        double distancia = Math.sqrt(Math.pow(mouse_x - (bolinhasThread.bolinhas[i].x + 15), 2) +
                                Math.pow(mouse_y - (bolinhasThread.bolinhas[i].y + 15), 2));
                        if (distancia < 20) { // se tá muito próximo, não adiciona
                            muitoProximo = true;
                            break;
                        }
                    }
                }

                if (!muitoProximo) {
                    Color cor = Color.green;
                    if (SwingUtilities.isRightMouseButton(me)) {
                        cor = new Color(150, 75, 0);
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

        if (contBolinhas >= bolinhas.length && !analiseJaFeita) {
            analiseJaFeita = true;

            double[] pixels = analisePixel.analisarPixelInvariante(bolinhasThread.bolinhas);

            System.out.println("Análise baseada em pixels (50x50):");
            System.out.println("Total de pixels: " + pixels.length);

            int pixelsAtivos = 0;
            for (double pixel : pixels) {
                if (pixel > 0.1) { 
                    pixelsAtivos++;
                }
            }
            System.out.println("Pixels ativos: " + pixelsAtivos + " (" + (pixelsAtivos * 100.0 / pixels.length) + "%)");

            ultimaRedeNeural = new RedeNeuralPixels();
            ultimaRedeNeural.setEntrada(pixels);

            boolean ehBonecoPalito = ultimaRedeNeural.classificar();

            System.out.println("Classificação: " + (ehBonecoPalito ? "É um boneco palito" : "Não é um boneco palito"));

            if (ehBonecoPalito) {
                g.setColor(Color.BLUE);
                g.drawString("É um boneco palito!", 10, 20);
            } else {
                g.setColor(Color.RED);
                g.drawString("Não é um boneco palito!", 10, 20);
            }

            if (painelProcessamento != null) {
                painelProcessamento.atualizarVisualizacao(bolinhasThread.bolinhas, pixels);
            }
        }
    }

    /**
     * analisa se o desenho representa um boneco palito usando pixels 50x50
     */
    public double[] analisarBonecoPalitoPixels(Bolinha[] bolinhas) {
        return analisePixel.analisarPixelInvariante(bolinhas);
    }

    public static Color gerarCorAleatoriamente() {
        Random randColor = new Random();
        int r = randColor.nextInt(256);
        int g = randColor.nextInt(256);
        int b = 150 + randColor.nextInt(100);// randColor.nextInt(256);
        return new Color(r, g, b);
    }

    
    static class PainelProcessamento extends JPanel {
        private BufferedImage imagemOriginal = null;
        private BufferedImage imagem50x50 = null;
        private double[] pixels = null;

        public void atualizarVisualizacao(Bolinha[] bolinhasOriginais, double[] pixels) {
            this.pixels = pixels;

            AnalisePixel.BoundingBox bbox = new AnalisePixel.BoundingBox();
            for (Bolinha b : bolinhasOriginais) {
                if (b != null) {
                    bbox.atualizar(b.x, b.y);
                }
            }

            int largura = bbox.getLargura();
            int altura = bbox.getAltura();
            int padding = 20;
            imagemOriginal = new BufferedImage(largura + 2 * padding, altura + 2 * padding, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = imagemOriginal.createGraphics();

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, imagemOriginal.getWidth(), imagemOriginal.getHeight());

            g2d.setColor(Color.BLACK);
            for (Bolinha b : bolinhasOriginais) {
                if (b != null) {
                    int x = b.x - bbox.minX + padding;
                    int y = b.y - bbox.minY + padding;
                    g2d.fillOval(x - 5, y - 5, 10, 10);
                }
            }
            g2d.dispose();

            imagem50x50 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < 50; y++) {
                for (int x = 0; x < 50; x++) {
                    int index = y * 50 + x;
                    double valor = pixels[index];
                    int intensidade = (int) (255 * (1.0 - valor));
                    int rgb = (intensidade << 16) | (intensidade << 8) | intensidade;
                    imagem50x50.setRGB(x, y, rgb);
                }
            }

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int largura = getWidth();
            int altura = getHeight();

            int secaoLargura = largura / 2;

            // imagem original (crop)
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, secaoLargura, altura);
            g.setColor(Color.BLACK);
            g.drawString("Imagem Original (Crop)", 10, 20);

            if (imagemOriginal != null) {
                // escala a imagem para caber no painel
                double escalaX = (double) (secaoLargura - 40) / imagemOriginal.getWidth();
                double escalaY = (double) (altura - 40) / imagemOriginal.getHeight();
                double escala = Math.min(escalaX, escalaY);

                int novaLargura = (int) (imagemOriginal.getWidth() * escala);
                int novaAltura = (int) (imagemOriginal.getHeight() * escala);
                int offsetX = (secaoLargura - novaLargura) / 2;
                int offsetY = (altura - novaAltura) / 2 + 20;

                g.drawImage(imagemOriginal, offsetX, offsetY, novaLargura, novaAltura, null);
            }

            // imagem 50x50 pixels
            g.setColor(Color.WHITE);
            g.fillRect(secaoLargura, 0, secaoLargura, altura);
            g.setColor(Color.BLACK);
            g.drawString("Representação 50x50 Pixels", secaoLargura + 10, 20);

            if (imagem50x50 != null) {
                // 50x50 ampliada
                int tamanhoAmpliado = Math.min(secaoLargura - 40, altura - 40);
                int offsetX = secaoLargura + (secaoLargura - tamanhoAmpliado) / 2;
                int offsetY = (altura - tamanhoAmpliado) / 2 + 20;

                g.drawImage(imagem50x50, offsetX, offsetY, tamanhoAmpliado, tamanhoAmpliado, null);

                if (pixels != null) {
                    int pixelsAtivos = 0;
                    for (double pixel : pixels) {
                        if (pixel > 0.1)
                            pixelsAtivos++;
                    }
                    g.drawString("Pixels ativos: " + pixelsAtivos + "/2500", secaoLargura + 10, altura - 10);
                }
            }
        }
    }

    static ThreadBolinhaEx painel = new ThreadBolinhaEx();
    static BolinhasThread bolinhasThread;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);

        painel = new ThreadBolinhaEx();
        frame.getContentPane().add(painel, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel();

        JButton btnTreinar = new JButton("Treinar");
        btnTreinar.addActionListener(e -> {
            if (painel.ultimaRedeNeural != null) {
                // treina com target 10 (queremos que y1 se aproxime de 10)
                painel.ultimaRedeNeural.treinar(10, 0.0); // target 10, limiar não usado
                System.out.println("Treinamento realizado! Target: 10");
            } else {
                System.out.println("Nenhuma rede neural para treinar.");
            }
        });

        JButton btnReiniciar = new JButton("Reiniciar");
        btnReiniciar.addActionListener(e -> {
            contBolinhas = 0;
            for (int i = 0; i < bolinhasThread.bolinhas.length; i++) {
                bolinhasThread.bolinhas[i] = null;
            }
            painel.ultimaRedeNeural = null;
            painel.analiseJaFeita = false;

            Graphics g = painel.getGraphics();
            if (g != null) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, painel.getWidth(), painel.getHeight());
            }

            if (painelProcessamento != null) {
                painelProcessamento.atualizarVisualizacao(new Bolinha[0], new double[2500]);
            }

            System.out.println("Canvas reiniciado! Desenhe um novo boneco palito.");
        });

        painelBotoes.add(btnTreinar);
        painelBotoes.add(btnReiniciar);
        frame.getContentPane().add(painelBotoes, BorderLayout.SOUTH);

        janelaProcessamento = new JFrame("Processamento de Imagem - Abordagem Pixels");
        janelaProcessamento.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janelaProcessamento.setSize(800, 400);
        janelaProcessamento.setLocation(450, 100);

        painelProcessamento = new PainelProcessamento();
        janelaProcessamento.getContentPane().add(painelProcessamento);

        frame.setVisible(true);
        janelaProcessamento.setVisible(true);

        bolinhasThread = new BolinhasThread(painel, 100);
        bolinhasThread.start();
    }
}