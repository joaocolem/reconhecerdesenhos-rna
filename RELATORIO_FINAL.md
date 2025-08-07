# Relatório Final: Sistema de Reconhecimento de Bonecos Palito

## Projeto de Aprendizado de Máquina

**Disciplina:** Inteligência Artificial  
**Aluno:** [Nome do Aluno]  
**Data:** [Data Atual]

---

## 1. Introdução e Evolução do Projeto

### 1.1 Como Começou

O projeto começou como um sistema para reconhecer árvores usando **projeções de pixels**. A ideia era contar quantos pixels estavam ativos em cada linha e coluna da imagem.

### 1.2 Primeira Mudança: Detecção de Árvores

Mantivemos a mesma ideia de projeções, mas agora para detectar árvores. A rede neural tinha apenas 20 entradas (10 para linhas + 10 para colunas).

### 1.3 Segunda Mudança: Análise por Grafos

Como detectar árvores era muito complicado, tentamos uma abordagem usando **grafos**:

- Conectávamos bolinhas próximas
- Analisávamos as partes conectadas
- Extraíamos 20 características diferentes

**Problemas encontrados:**

- Código muito complexo (646 linhas!)
- Difícil de entender e consertar
- Resultados inconsistentes

### 1.4 Solução Final: Análise por Pixels

A solução atual é muito mais simples:

- **Recortamos** o desenho automaticamente
- **Redimensionamos** para 50x50 pixels
- **Analisamos** cada pixel diretamente
- **Classificamos** com uma rede neural simples

---

## 2. Processamento de Imagens

### 2.1 Detecção da Área de Interesse (Bounding Box)

O primeiro passo é encontrar exatamente onde está o desenho:

```java
public class BoundingBox {
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;

    public void atualizar(int x, int y) {
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
    }

    public int getLargura() { return maxX - minX; }
    public int getAltura() { return maxY - minY; }
}
```

**Processo:**

1. Percorremos todas as bolinhas desenhadas
2. Encontramos as coordenadas mínimas e máximas
3. Calculamos a área exata que contém o desenho

### 2.2 Criação da Imagem com Padding

Criamos uma imagem com espaço extra ao redor do desenho:

```java
private BufferedImage criarImagemDesenho(Bolinha[] bolinhas, BoundingBox bbox) {
    int padding = 20; // Espaço extra
    int largura = bbox.getLargura() + 2 * padding;
    int altura = bbox.getAltura() + 2 * padding;

    BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = imagem.createGraphics();

    // Fundo branco
    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, largura, altura);

    // Desenha as bolinhas
    g2d.setColor(Color.BLACK);
    for (Bolinha bolinha : bolinhas) {
        if (bolinha != null) {
            int x = bolinha.x - bbox.minX + padding;
            int y = bolinha.y - bbox.minY + padding;
            g2d.fillOval(x - 5, y - 5, 10, 10);
        }
    }

    return imagem;
}
```

### 2.3 Redimensionamento para 50x50

Convertemos a imagem para um tamanho padrão:

```java
private BufferedImage redimensionarImagem(BufferedImage original, int largura, int altura) {
    BufferedImage redimensionada = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = redimensionada.createGraphics();

    // Configuração para melhor qualidade
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Desenha a imagem redimensionada
    g2d.drawImage(original, 0, 0, largura, altura, null);
    g2d.dispose();

    return redimensionada;
}
```

### 2.4 Normalização de Pixels

Convertemos cada pixel para um valor entre 0 e 1:

```java
private double[] converterParaArray(BufferedImage imagem) {
    double[] pixels = new double[2500]; // 50x50 = 2500 pixels

    for (int y = 0; y < 50; y++) {
        for (int x = 0; x < 50; x++) {
            int rgb = imagem.getRGB(x, y);

            // Extrai componentes RGB
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            // Calcula intensidade média
            double intensidade = (r + g + b) / 3.0;

            // Normaliza e inverte (preto = 1.0, branco = 0.0)
            double normalizado = 1.0 - (intensidade / 255.0);

            pixels[y * 50 + x] = normalizado;
        }
    }

    return pixels;
}
```

**Por que invertemos?**

- Preto (desenho) = RGB(0,0,0) = intensidade 0 = normalizado 1.0
- Branco (fundo) = RGB(255,255,255) = intensidade 255 = normalizado 0.0
- Assim, pixels do desenho têm valor alto, pixels do fundo têm valor baixo

---

## 3. Treinamento da Rede Neural

### 3.1 Arquitetura da Rede

```java
public class RedeNeuralPixels {
    private static final int NUM_ENTRADAS = 2500; // 50x50 pixels

    // Pesos da rede neural
    private double w11 = 0.1;
    private double w12 = 0.1;
    private double[] v1k = new double[NUM_ENTRADAS];
    private double[] v2k = new double[NUM_ENTRADAS];

    // Valores das camadas
    private double[] xk = new double[NUM_ENTRADAS]; // entrada (pixels)
    private double h1 = 0;
    private double h2 = 0;
    private double y1 = 0;
}
```

### 3.2 Inicialização dos Pesos

```java
private void inicializarPesos() {
    // Pesos pequenos para evitar explosão de valores
    for (int i = 0; i < NUM_ENTRADAS; i++) {
        v1k[i] = 0.01;
        v2k[i] = 0.01;
    }
    w11 = 0.1;
    w12 = 0.1;
}
```

**Por que pesos pequenos?**

- Evita que os valores y1 fiquem muito grandes
- Permite convergência mais estável
- Previne overflow numérico

### 3.3 Forward Pass (Cálculo da Saída)

```java
public int aplica(double limiar) {
    y1 = 0; h1 = 0; h2 = 0;

    // Calcula h1 e h2 (camadas intermediárias)
    for (int c = 0; c < v1k.length; c++) {
        h1 += v1k[c] * xk[c];
        h2 += v2k[c] * xk[c];
    }

    // Calcula y1 (saída final)
    y1 = (h1 * w11 + h2 * w12);

    System.out.println("y1 = " + y1);

    // Função escada
    if (y1 > limiar) {
        return 1; // É boneco palito
    }
    return 0; // Não é boneco palito
}
```

### 3.4 Algoritmo de Treinamento

```java
public void treinar(int target, double limiar) {
    // 1. Calcula a saída atual
    aplica(0.5); // Usa limiar interno para treinamento

    // 2. Calcula o erro
    double erro = target - y1;

    // 3. Taxa de aprendizado
    double taxaAprendizado = 0.0001;

    // 4. Ajusta os pesos (backpropagation simplificado)
    for (int i = 0; i < v1k.length; i++) {
        v1k[i] += taxaAprendizado * erro * xk[i];
        v2k[i] += taxaAprendizado * erro * xk[i];
    }
    w11 += taxaAprendizado * erro * h1;
    w12 += taxaAprendizado * erro * h2;

    // 5. Salva os pesos
    salvarPesos("pesos_rede.txt");
}
```

### 3.5 Persistência dos Pesos

```java
public void salvarPesos(String caminho) {
    try (PrintWriter pw = new PrintWriter(new FileWriter(caminho))) {
        pw.println(w11);
        pw.println(w12);
        for (double v : v1k) pw.println(v);
        for (double v : v2k) pw.println(v);
    } catch (IOException e) {
        System.out.println("Erro ao salvar pesos: " + e.getMessage());
    }
}

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
        // ... carrega outros pesos ...
    } catch (IOException | NumberFormatException e) {
        System.out.println("Erro ao carregar pesos: " + e.getMessage());
    }
}
```

---

## 4. Como Funciona o Sistema

### 4.1 Fluxo Completo

1. **Desenho:** O usuário desenha um boneco palito
2. **Crop:** O sistema recorta automaticamente o desenho
3. **Redimensionamento:** Converte para 50x50 pixels
4. **Normalização:** Cada pixel vira um número entre 0 e 1
5. **Classificação:** Rede neural decide se é boneco palito
6. **Treinamento:** Ajusta pesos baseado no resultado

### 4.2 Rede Neural

A rede neural é bem simples:

- **2500 entradas:** Uma para cada pixel (50x50)
- **1 saída:** 1 = é boneco palito, 0 = não é

```java
// Exemplo simplificado
public boolean classificar() {
    double resultado = calcularResultado();
    return resultado > 10.0; // Se maior que 10, é boneco palito
}
```

---

## 5. Tecnologias Usadas

- **Java:** Linguagem principal
- **Swing:** Interface gráfica
- **AWT:** Desenho e processamento de imagens

---

## 6. Código Principal

### 6.1 Classe Principal

```java
public class ThreadBolinhaEx extends JPanel {
    // Controle para analisar apenas uma vez
    boolean analiseJaFeita = false;

    public void desenha(Bolinha[] bolinhas) {
        // Desenha as bolinhas

        // Quando termina o desenho, analisa
        if (contBolinhas >= 100 && !analiseJaFeita) {
            analiseJaFeita = true;

            // Converte desenho para pixels
            double[] pixels = analisePixel.analisarPixelInvariante(bolinhas);

            // Classifica
            boolean ehBonecoPalito = redeNeural.classificar();

            // Mostra resultado
            if (ehBonecoPalito) {
                g.drawString("É um boneco palito!", 10, 20);
            } else {
                g.drawString("Não é um boneco palito!", 10, 20);
            }
        }
    }
}
```

### 6.2 Análise de Pixels

```java
public class AnalisePixel {
    public double[] analisarPixelInvariante(Bolinha[] bolinhas) {
        // 1. Encontra a área do desenho
        BoundingBox bbox = encontrarBoundingBox(bolinhas);

        // 2. Cria uma imagem
        BufferedImage imagem = criarImagemDesenho(bolinhas, bbox);

        // 3. Redimensiona para 50x50
        BufferedImage imagem50x50 = redimensionarImagem(imagem, 50, 50);

        // 4. Converte para array de números
        return converterParaArray(imagem50x50);
    }
}
```

---

## 7. Resultados dos Testes

### 7.1 Configurações

- **Tamanho da imagem:** 50x50 pixels
- **Target de treinamento:** 10
- **Limiar de classificação:** 10
- **Taxa de aprendizado:** 0.0001

### 7.2 Resultados

| Teste | Pixels Ativos | Resultado | Classificação       | Sucesso |
| ----- | ------------- | --------- | ------------------- | ------- |
| 1     | 133 (5.32%)   | 86.30     | É boneco palito     | ✅      |
| 2     | 104 (4.16%)   | 68.08     | É boneco palito     | ✅      |
| 3     | 144 (5.76%)   | 95.22     | É boneco palito     | ✅      |
| 4     | 88 (3.52%)    | -2.167    | Não é boneco palito | ❌      |
| 5     | 125 (5.0%)    | -3.102    | Não é boneco palito | ❌      |

**Taxa de acerto:** 60% (3 de 5 casos)

---

## 8. Imagens dos Resultados

### 8.1 Casos de Sucesso

[ESPAÇO PARA PRINTS - Casos onde funcionou bem]

**O que funcionou:**

- Bonecos palito bem desenhados
- Proporções adequadas
- Tamanho médio (4-6% de pixels ativos)
- Resultado > 10

### 8.2 Casos de Falha

[ESPAÇO PARA PRINTS - Casos onde falhou]

**O que não funcionou:**

- Desenhos muito pequenos
- Proporções estranhas
- Estrutura confusa
- Resultado < 10

### 8.3 Interface do Sistema

[ESPAÇO PARA PRINTS - Como a tela se parece]

**O que tem na tela:**

- Área para desenhar
- Botão "Treinar"
- Botão "Reiniciar"
- Janela mostrando o processamento

### 8.4 Processamento da Imagem

[ESPAÇO PARA PRINTS - Janela de processamento]

**O que mostra:**

- Desenho original recortado
- Imagem 50x50 pixels
- Quantos pixels estão ativos
- Resultado da classificação

---

## 9. O que Deu Errado

### 9.1 Problemas de Treinamento

- **Oscilações excessivas:** Valores y1 variando muito entre treinamentos
- **Convergência lenta:** Taxa de aprendizado muito baixa inicialmente
- **Overfitting:** Rede neural memorizando desenhos específicos

### 9.2 Problemas de Classificação

- **Sensibilidade ao tamanho:** Desenhos muito pequenos sendo rejeitados
- **Dependência da proporção:** Bonecos com proporções não convencionais falhando
- **Limiar fixo:** Classificação binária rígida (10.0)

---

## 10. Conclusões

### 10.1 O que Aprendemos

1. **Análise por pixels é melhor:** Mais simples que grafos
2. **Pré-processamento é importante:** Crop e redimensionamento ajudam muito
3. **Interface gráfica é essencial:** Facilita testes e demonstração
4. **Treinamento precisa de ajustes:** Taxa de aprendizado é crítica

### 10.2 Limitações

1. **Classificação muito rígida:** Só aceita ou rejeita
2. **Depende do tamanho:** Desenhos muito pequenos ou grandes falham
3. **Pode "decorar":** Às vezes memoriza desenhos específicos

### 10.3 Como Melhorar

1. **Mais exemplos:** Treinar com mais bonecos palito
2. **Classificação flexível:** Aceitar graus de similaridade
3. **Melhor normalização:** Lidar melhor com tamanhos diferentes
4. **Interface web:** Versão online

---

**Fim do Relatório**
