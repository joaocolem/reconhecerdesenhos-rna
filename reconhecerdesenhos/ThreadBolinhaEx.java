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

    // Janela para mostrar o processamento de imagem
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
            int[] featuresEstruturais = this.analisarBonecoPalitoPadroesEstruturais(bolinhasThread.bolinhas);

            // Debug: mostra as características estruturais do boneco palito
            System.out.println("Características estruturais do boneco palito:");
            String[] nomes = {
                    "Total bolinhas", "Extremidades (grau 1)", "Linhas (grau 2)", "Junções (grau 3)",
                    "Centros (grau 4+)",
                    "Caminhos 1 salto", "Caminhos 2 saltos", "Caminhos 3 saltos", "Caminhos 4+ saltos",
                    "Ramificações simples", "Ramificações complexas", "Cadeias lineares",
                    "Largura crop", "Altura crop", "Proporção W/H", "Densidade", "Simetria estrutural",
                    "Estrutura padrões", "Tamanho do tronco"
            };

            for (int i = 0; i < featuresEstruturais.length; i++) {
                System.out.println(nomes[i] + ": " + featuresEstruturais[i]);
            }
            System.out.println();

            // Atualiza a visualização do processamento
            if (painelProcessamento != null) {
                painelProcessamento.atualizarVisualizacao(bolinhasThread.bolinhas);
            }

            RedeNeural rn = new RedeNeural(featuresEstruturais);
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
     * Analisa se o desenho representa um boneco palito usando processamento de
     * imagem
     * Nova abordagem: matriz binária, contornos, momentos de imagem
     */
    public int[] analisarBonecoPalitoProcessamentoImagem(Bolinha[] bolinhas) {
        AnaliseEstrutural analise = new AnaliseEstrutural();
        return analise.analisarBonecoPalitoProcessamentoImagem(bolinhas);
    }

    /**
     * Analisa se o desenho representa um boneco palito usando padrões estruturais
     * (mantido para compatibilidade)
     * Nova abordagem: análise de caminhos, distribuição de graus e padrões de
     * ramificação
     */
    public int[] analisarBonecoPalitoPadroesEstruturais(Bolinha[] bolinhas) {
        AnaliseEstrutural analise = new AnaliseEstrutural();
        return analise.analisarBonecoPalitoPadroesEstruturais(bolinhas);
    }

    /**
     * Analisa se o desenho representa um boneco palito usando features por bolinha
     * (mantido para compatibilidade)
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
     * Painel para mostrar o processamento de imagem
     */
    static class PainelProcessamento extends JPanel {
        private Bolinha[] bolinhasCropadas = null;
        private Bolinha[] bolinhasNormalizadas = null;
        private int[][] matrizBinaria = null;

        public void atualizarVisualizacao(Bolinha[] bolinhasOriginais) {
            AnaliseEstrutural analise = new AnaliseEstrutural();

            // 1. Remove bolinhas desconectadas
            Bolinha[] bolinhasFiltradas = analise.removerBolinhasDesconectadas(bolinhasOriginais);

            // 2. Faz crop inteligente
            bolinhasCropadas = analise.fazerCropInteligente(bolinhasFiltradas);

            // 3. Para análise de grafos, não precisamos normalizar orientação
            bolinhasNormalizadas = bolinhasCropadas;

            // 4. Converte para matriz binária para visualização
            matrizBinaria = analise.converterParaMatrizBinaria(bolinhasNormalizadas);

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int largura = getWidth();
            int altura = getHeight();

            // Divide o painel em 3 seções
            int secaoLargura = largura / 3;

            // Seção 1: Desenho original (crop)
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, secaoLargura, altura);
            g.setColor(Color.BLACK);
            g.drawString("Crop", 10, 20);

            if (bolinhasCropadas != null) {
                desenharBolinhas(g, bolinhasCropadas, 0, 0, secaoLargura, altura);
            }

            // Seção 2: Grafo de conectividade
            g.setColor(Color.WHITE);
            g.fillRect(secaoLargura, 0, secaoLargura, altura);
            g.setColor(Color.BLACK);
            g.drawString("Grafo", secaoLargura + 10, 20);

            if (bolinhasNormalizadas != null) {
                desenharGrafoConectividade(g, bolinhasNormalizadas, secaoLargura, 0, secaoLargura, altura);
            }

            // Seção 3: Matriz binária
            g.setColor(Color.WHITE);
            g.fillRect(2 * secaoLargura, 0, secaoLargura, altura);
            g.setColor(Color.BLACK);
            g.drawString("Matriz Binária", 2 * secaoLargura + 10, 20);

            if (matrizBinaria != null) {
                desenharMatrizBinaria(g, matrizBinaria, 2 * secaoLargura, 0, secaoLargura, altura);
            }
        }

        private void desenharBolinhas(Graphics g, Bolinha[] bolinhas, int offsetX, int offsetY, int largura,
                int altura) {
            if (bolinhas == null)
                return;

            // Encontra limites para escalar
            double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

            for (Bolinha b : bolinhas) {
                if (b != null) {
                    minX = Math.min(minX, b.x);
                    maxX = Math.max(maxX, b.x);
                    minY = Math.min(minY, b.y);
                    maxY = Math.max(maxY, b.y);
                }
            }

            double escalaX = (largura - 40) / (maxX - minX + 1);
            double escalaY = (altura - 40) / (maxY - minY + 1);
            double escala = Math.min(escalaX, escalaY);

            for (Bolinha b : bolinhas) {
                if (b != null) {
                    int x = offsetX + 20 + (int) ((b.x - minX) * escala);
                    int y = offsetY + 20 + (int) ((b.y - minY) * escala);

                    g.setColor(b.cor);
                    g.fillOval(x, y, 8, 8);
                }
            }
        }

        private void desenharGrafoConectividade(Graphics g, Bolinha[] bolinhas, int offsetX, int offsetY, int largura,
                int altura) {
            if (bolinhas == null)
                return;

            // Encontra limites para escalar
            double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

            for (Bolinha b : bolinhas) {
                if (b != null) {
                    minX = Math.min(minX, b.x);
                    maxX = Math.max(maxX, b.x);
                    minY = Math.min(minY, b.y);
                    maxY = Math.max(maxY, b.y);
                }
            }

            double escalaX = (largura - 40) / (maxX - minX + 1);
            double escalaY = (altura - 40) / (maxY - minY + 1);
            double escala = Math.min(escalaX, escalaY);

            // Desenha as bolinhas
            for (Bolinha b : bolinhas) {
                if (b != null) {
                    int x = offsetX + 20 + (int) ((b.x - minX) * escala);
                    int y = offsetY + 20 + (int) ((b.y - minY) * escala);

                    g.setColor(b.cor);
                    g.fillOval(x, y, 8, 8);
                }
            }

            // Desenha as conexões (arestas do grafo)
            g.setColor(Color.RED);
            for (int i = 0; i < bolinhas.length; i++) {
                if (bolinhas[i] != null) {
                    for (int j = i + 1; j < bolinhas.length; j++) {
                        if (bolinhas[j] != null) {
                            double distancia = Math.sqrt(Math.pow(bolinhas[i].x - bolinhas[j].x, 2) +
                                    Math.pow(bolinhas[i].y - bolinhas[j].y, 2));
                            if (distancia <= 25) { // Threshold reduzido de 40 para 25
                                int x1 = offsetX + 20 + (int) ((bolinhas[i].x - minX) * escala);
                                int y1 = offsetY + 20 + (int) ((bolinhas[i].y - minY) * escala);
                                int x2 = offsetX + 20 + (int) ((bolinhas[j].x - minX) * escala);
                                int y2 = offsetY + 20 + (int) ((bolinhas[j].y - minY) * escala);

                                g.drawLine(x1 + 4, y1 + 4, x2 + 4, y2 + 4);
                            }
                        }
                    }
                }
            }
        }

        private void desenharMatrizBinaria(Graphics g, int[][] matriz, int offsetX, int offsetY, int largura,
                int altura) {
            if (matriz == null || matriz.length == 0)
                return;

            int matrizAltura = matriz.length;
            int matrizLargura = matriz[0].length;

            int pixelLargura = (largura - 40) / matrizLargura;
            int pixelAltura = (altura - 40) / matrizAltura;

            for (int y = 0; y < matrizAltura; y++) {
                for (int x = 0; x < matrizLargura; x++) {
                    int pixelX = offsetX + 20 + x * pixelLargura;
                    int pixelY = offsetY + 20 + y * pixelAltura;

                    if (matriz[y][x] == 1) {
                        g.setColor(Color.BLACK);
                    } else {
                        g.setColor(Color.WHITE);
                    }

                    g.fillRect(pixelX, pixelY, pixelLargura, pixelAltura);
                }
            }
        }
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

            // Limpa a visualização de processamento
            if (painelProcessamento != null) {
                painelProcessamento.atualizarVisualizacao(new Bolinha[0]);
            }

            System.out.println("Canvas reiniciado! Desenhe um novo boneco palito.");
        });

        painelBotoes.add(btnTreinar);
        painelBotoes.add(btnReiniciar);
        frame.getContentPane().add(painelBotoes, BorderLayout.SOUTH);

        // Cria a janela de processamento
        janelaProcessamento = new JFrame("Processamento de Imagem");
        janelaProcessamento.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janelaProcessamento.setSize(600, 300);
        janelaProcessamento.setLocation(450, 100);

        painelProcessamento = new PainelProcessamento();
        janelaProcessamento.getContentPane().add(painelProcessamento);

        frame.setVisible(true);
        janelaProcessamento.setVisible(true);

        bolinhasThread = new BolinhasThread(painel, 100);
        bolinhasThread.start();
    }
}