package reconhecerdesenhos;

public class AnaliseMomentos {

    public double[] calcularMomentosInvariantes(Bolinha[] bolinhas) {
        // 1. Calcula momentos centrais
        double[] momentos = calcularMomentosCentrais(bolinhas);

        // 2. Calcula momentos invariantes (não mudam com rotação)
        double[] invariantes = new double[10];

        // Momentos de Hu (invariantes a rotação, escala e translação)
        invariantes[0] = calcularMomentoHu1(momentos);
        invariantes[1] = calcularMomentoHu2(momentos);
        invariantes[2] = calcularMomentoHu3(momentos);
        invariantes[3] = calcularMomentoHu4(momentos);
        invariantes[4] = calcularMomentoHu5(momentos);
        invariantes[5] = calcularMomentoHu6(momentos);
        invariantes[6] = calcularMomentoHu7(momentos);

        // Momentos adicionais para análise de forma
        invariantes[7] = calcularCircularidade(bolinhas);
        invariantes[8] = calcularElongacao(bolinhas);
        invariantes[9] = calcularCompactacao(bolinhas);

        return invariantes;
    }

    private double[] calcularMomentosCentrais(Bolinha[] bolinhas) {
        // Calcula centro de massa
        double centroX = 0, centroY = 0;
        int totalBolinhas = 0;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                centroX += b.x;
                centroY += b.y;
                totalBolinhas++;
            }
        }

        if (totalBolinhas == 0) {
            return new double[7]; // Retorna array de zeros
        }

        centroX /= totalBolinhas;
        centroY /= totalBolinhas;

        // Calcula momentos centrais
        double m00 = totalBolinhas; // Momento de ordem 0,0
        double m10 = 0, m01 = 0; // Momentos de ordem 1,0 e 0,1
        double m20 = 0, m02 = 0, m11 = 0; // Momentos de ordem 2
        double m30 = 0, m03 = 0, m21 = 0, m12 = 0; // Momentos de ordem 3

        for (Bolinha b : bolinhas) {
            if (b != null) {
                double dx = b.x - centroX;
                double dy = b.y - centroY;

                // Momentos de ordem 1
                m10 += dx;
                m01 += dy;

                // Momentos de ordem 2
                m20 += dx * dx;
                m02 += dy * dy;
                m11 += dx * dy;

                // Momentos de ordem 3
                m30 += dx * dx * dx;
                m03 += dy * dy * dy;
                m21 += dx * dx * dy;
                m12 += dx * dy * dy;
            }
        }

        return new double[] { m00, m10, m01, m20, m02, m11, m30, m03, m21, m12 };
    }

    private double calcularMomentoHu1(double[] momentos) {
        // Hu1 = η20 + η02
        double m20 = momentos[3];
        double m02 = momentos[4];
        double m00 = momentos[0];

        if (m00 == 0)
            return 0;

        double eta20 = m20 / (m00 * m00);
        double eta02 = m02 / (m00 * m00);

        return eta20 + eta02;
    }

    private double calcularMomentoHu2(double[] momentos) {
        // Hu2 = (η20 - η02)² + 4η11²
        double m20 = momentos[3];
        double m02 = momentos[4];
        double m11 = momentos[5];
        double m00 = momentos[0];

        if (m00 == 0)
            return 0;

        double eta20 = m20 / (m00 * m00);
        double eta02 = m02 / (m00 * m00);
        double eta11 = m11 / (m00 * m00);

        double diff = eta20 - eta02;
        return diff * diff + 4 * eta11 * eta11;
    }

    private double calcularMomentoHu3(double[] momentos) {
        // Hu3 = (η30 - 3η12)² + (3η21 - η03)²
        double m30 = momentos[6];
        double m03 = momentos[7];
        double m21 = momentos[8];
        double m12 = momentos[9];
        double m00 = momentos[0];

        if (m00 == 0)
            return 0;

        double eta30 = m30 / Math.pow(m00, 2.5);
        double eta03 = m03 / Math.pow(m00, 2.5);
        double eta21 = m21 / Math.pow(m00, 2.5);
        double eta12 = m12 / Math.pow(m00, 2.5);

        double term1 = eta30 - 3 * eta12;
        double term2 = 3 * eta21 - eta03;

        return term1 * term1 + term2 * term2;
    }

    private double calcularMomentoHu4(double[] momentos) {
        // Hu4 = (η30 + η12)² + (η21 + η03)²
        double m30 = momentos[6];
        double m03 = momentos[7];
        double m21 = momentos[8];
        double m12 = momentos[9];
        double m00 = momentos[0];

        if (m00 == 0)
            return 0;

        double eta30 = m30 / Math.pow(m00, 2.5);
        double eta03 = m03 / Math.pow(m00, 2.5);
        double eta21 = m21 / Math.pow(m00, 2.5);
        double eta12 = m12 / Math.pow(m00, 2.5);

        double term1 = eta30 + eta12;
        double term2 = eta21 + eta03;

        return term1 * term1 + term2 * term2;
    }

    private double calcularMomentoHu5(double[] momentos) {
        // Hu5 = (η30 - 3η12)(η30 + η12)[(η30 + η12)² - 3(η21 + η03)²] +
        // (3η21 - η03)(η21 + η03)[3(η30 + η12)² - (η21 + η03)²]
        double m30 = momentos[6];
        double m03 = momentos[7];
        double m21 = momentos[8];
        double m12 = momentos[9];
        double m00 = momentos[0];

        if (m00 == 0)
            return 0;

        double eta30 = m30 / Math.pow(m00, 2.5);
        double eta03 = m03 / Math.pow(m00, 2.5);
        double eta21 = m21 / Math.pow(m00, 2.5);
        double eta12 = m12 / Math.pow(m00, 2.5);

        double term1 = eta30 - 3 * eta12;
        double term2 = eta30 + eta12;
        double term3 = 3 * eta21 - eta03;
        double term4 = eta21 + eta03;

        double part1 = term1 * term2 * (term2 * term2 - 3 * term4 * term4);
        double part2 = term3 * term4 * (3 * term2 * term2 - term4 * term4);

        return part1 + part2;
    }

    private double calcularMomentoHu6(double[] momentos) {
        // Hu6 = (η20 - η02)[(η30 + η12)² - (η21 + η03)²] + 4η11(η30 + η12)(η21 + η03)
        double m20 = momentos[3];
        double m02 = momentos[4];
        double m11 = momentos[5];
        double m30 = momentos[6];
        double m03 = momentos[7];
        double m21 = momentos[8];
        double m12 = momentos[9];
        double m00 = momentos[0];

        if (m00 == 0)
            return 0;

        double eta20 = m20 / (m00 * m00);
        double eta02 = m02 / (m00 * m00);
        double eta11 = m11 / (m00 * m00);
        double eta30 = m30 / Math.pow(m00, 2.5);
        double eta03 = m03 / Math.pow(m00, 2.5);
        double eta21 = m21 / Math.pow(m00, 2.5);
        double eta12 = m12 / Math.pow(m00, 2.5);

        double term1 = eta20 - eta02;
        double term2 = eta30 + eta12;
        double term3 = eta21 + eta03;

        double part1 = term1 * (term2 * term2 - term3 * term3);
        double part2 = 4 * eta11 * term2 * term3;

        return part1 + part2;
    }

    private double calcularMomentoHu7(double[] momentos) {
        // Hu7 = (3η21 - η03)(η30 + η12)[(η30 + η12)² - 3(η21 + η03)²] -
        // (η30 - 3η12)(η21 + η03)[3(η30 + η12)² - (η21 + η03)²]
        double m30 = momentos[6];
        double m03 = momentos[7];
        double m21 = momentos[8];
        double m12 = momentos[9];
        double m00 = momentos[0];

        if (m00 == 0)
            return 0;

        double eta30 = m30 / Math.pow(m00, 2.5);
        double eta03 = m03 / Math.pow(m00, 2.5);
        double eta21 = m21 / Math.pow(m00, 2.5);
        double eta12 = m12 / Math.pow(m00, 2.5);

        double term1 = 3 * eta21 - eta03;
        double term2 = eta30 + eta12;
        double term3 = eta21 + eta03;
        double term4 = eta30 - 3 * eta12;

        double part1 = term1 * term2 * (term2 * term2 - 3 * term3 * term3);
        double part2 = term4 * term3 * (3 * term2 * term2 - term3 * term3);

        return part1 - part2;
    }

    private double calcularCircularidade(Bolinha[] bolinhas) {
        // Circularidade = 4π * área / perímetro²
        // Para bolinhas discretas, usamos o número de bolinhas como área
        // e estimamos o perímetro pela borda

        int totalBolinhas = 0;
        for (Bolinha b : bolinhas) {
            if (b != null)
                totalBolinhas++;
        }

        if (totalBolinhas < 3)
            return 0;

        // Estimativa simples de circularidade baseada na distribuição
        double centroX = 0, centroY = 0;
        for (Bolinha b : bolinhas) {
            if (b != null) {
                centroX += b.x;
                centroY += b.y;
            }
        }
        centroX /= totalBolinhas;
        centroY /= totalBolinhas;

        double somaDistancia = 0;
        for (Bolinha b : bolinhas) {
            if (b != null) {
                double distancia = Math.sqrt(Math.pow(b.x - centroX, 2) + Math.pow(b.y - centroY, 2));
                somaDistancia += distancia;
            }
        }

        double distanciaMedia = somaDistancia / totalBolinhas;
        double variancia = 0;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                double distancia = Math.sqrt(Math.pow(b.x - centroX, 2) + Math.pow(b.y - centroY, 2));
                variancia += Math.pow(distancia - distanciaMedia, 2);
            }
        }
        variancia /= totalBolinhas;

        // Baixa variância = mais circular
        return Math.max(0, 1 - variancia / (distanciaMedia * distanciaMedia));
    }

    private double calcularElongacao(Bolinha[] bolinhas) {
        // Elongação = eixo maior / eixo menor
        // Calcula usando momentos de segunda ordem

        double m20 = 0, m02 = 0, m11 = 0;
        double centroX = 0, centroY = 0;
        int totalBolinhas = 0;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                centroX += b.x;
                centroY += b.y;
                totalBolinhas++;
            }
        }

        if (totalBolinhas == 0)
            return 0;

        centroX /= totalBolinhas;
        centroY /= totalBolinhas;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                double dx = b.x - centroX;
                double dy = b.y - centroY;
                m20 += dx * dx;
                m02 += dy * dy;
                m11 += dx * dy;
            }
        }

        m20 /= totalBolinhas;
        m02 /= totalBolinhas;
        m11 /= totalBolinhas;

        // Calcula autovalores da matriz de covariância
        double delta = Math.sqrt(Math.pow(m20 - m02, 2) + 4 * m11 * m11);
        double lambda1 = (m20 + m02 + delta) / 2;
        double lambda2 = (m20 + m02 - delta) / 2;

        if (lambda2 <= 0)
            return 1; // Forma circular

        return Math.sqrt(lambda1 / lambda2);
    }

    private double calcularCompactacao(Bolinha[] bolinhas) {
        // Compactação = área / (perímetro² / 4π)
        // Para bolinhas discretas, usamos uma medida de densidade

        int totalBolinhas = 0;
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        for (Bolinha b : bolinhas) {
            if (b != null) {
                totalBolinhas++;
                minX = Math.min(minX, b.x);
                maxX = Math.max(maxX, b.x);
                minY = Math.min(minY, b.y);
                maxY = Math.max(maxY, b.y);
            }
        }

        if (totalBolinhas == 0)
            return 0;

        double area = (maxX - minX) * (maxY - minY);
        if (area == 0)
            return 1; // Forma pontual

        // Densidade = número de bolinhas / área ocupada
        return totalBolinhas / area;
    }
}