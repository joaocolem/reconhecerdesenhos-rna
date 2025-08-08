package reconhecerdesenhos;

import java.awt.*;
import java.awt.image.BufferedImage;

public class AnalisePixel {

    private static final int TAMANHO_SAIDA = 50;

    public static class BoundingBox {
        int minX, minY, maxX, maxY;

        public BoundingBox() {
            minX = Integer.MAX_VALUE;
            minY = Integer.MAX_VALUE;
            maxX = Integer.MIN_VALUE;
            maxY = Integer.MIN_VALUE;
        }

        public void atualizar(int x, int y) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        public int getLargura() {
            return maxX - minX + 1;
        }

        public int getAltura() {
            return maxY - minY + 1;
        }
    }

    /**
     * retorna um array de pixels normalizado para 50x50 a partir das bolinhas
     */
    public double[] analisarPixelInvariante(Bolinha[] bolinhas) {
        BoundingBox bbox = encontrarBoundingBox(bolinhas);
        BufferedImage imagem = criarImagemDesenho(bolinhas, bbox);
        BufferedImage imagemRedimensionada = redimensionarImagem(imagem, TAMANHO_SAIDA, TAMANHO_SAIDA);
        return converterParaArray(imagemRedimensionada);
    }

    // encontra o menor retângulo que contém todas as bolinhas
    private BoundingBox encontrarBoundingBox(Bolinha[] bolinhas) {
        BoundingBox bbox = new BoundingBox();

        for (Bolinha bolinha : bolinhas) {
            if (bolinha != null) {
                bbox.atualizar(bolinha.x, bolinha.y);
            }
        }

        return bbox;
    }

    // desenha as bolinhas em uma imagem
    private BufferedImage criarImagemDesenho(Bolinha[] bolinhas, BoundingBox bbox) {
        int largura = bbox.getLargura();
        int altura = bbox.getAltura();

        int padding = 20;
        BufferedImage imagem = new BufferedImage(largura + 2 * padding, altura + 2 * padding,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, imagem.getWidth(), imagem.getHeight());

        g2d.setColor(Color.BLACK);
        for (Bolinha bolinha : bolinhas) {
            if (bolinha != null) {
                int x = bolinha.x - bbox.minX + padding;
                int y = bolinha.y - bbox.minY + padding;
                g2d.fillOval(x - 5, y - 5, 10, 10);
            }
        }

        g2d.dispose();
        return imagem;
    }

    // redimensiona a imagem para o tamanho especificado
    private BufferedImage redimensionarImagem(BufferedImage original, int largura, int altura) {
        BufferedImage redimensionada = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = redimensionada.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(original, 0, 0, largura, altura, null);
        g2d.dispose();

        return redimensionada;
    }

    // converte a imagem para array de valores normalizados (0.0 a 1.0)
    private double[] converterParaArray(BufferedImage imagem) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        double[] pixels = new double[largura * altura];

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int rgb = imagem.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // inverte para que preto = 1.0
                double intensidade = (r + g + b) / 3.0;
                double normalizado = 1.0 - (intensidade / 255.0);

                pixels[y * largura + x] = normalizado;
            }
        }

        return pixels;
    }
}