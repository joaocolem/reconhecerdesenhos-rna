# Relatório dos Processos - Sistema de Reconhecimento de Bonecos Palito

## 1. Visão Geral do Sistema

O sistema de reconhecimento de bonecos palito utiliza uma abordagem baseada em análise estrutural e redes neurais para identificar desenhos de bonecos palito. O sistema foi projetado para ser robusto, tolerante a variações no desenho e capaz de distinguir bonecos palito de outros tipos de desenhos.

### 1.1 Histórico e Evolução do Sistema

**Origem**: O sistema evoluiu a partir de uma rede neural prévia que detectava desenhos de árvores por meio de projeções verticais e horizontais. Esta rede inicial utilizava:

- **Projeções verticais**: Contagem de pontos em cada coluna da imagem
- **Projeções horizontais**: Contagem de pontos em cada linha da imagem
- **Arquitetura**: 20 entradas (10 projeções verticais + 10 projeções horizontais)

**Adaptação**: A partir do estudo desta rede neural original, foi implementada uma nova abordagem que utiliza fatores de grafo para o reconhecimento de bonecos palito. As principais decisões tomadas foram:

1. **Mudança de paradigma**: De análise por projeções para análise estrutural baseada em grafos
2. **Características mais robustas**: Substituição das projeções por características estruturais invariantes
3. **Arquitetura adaptada**: Rede neural modificada para trabalhar com 19 características de grafo

### 1.2 Decisões de Design Baseadas em Teoria de Grafos

O sistema utiliza extensivamente conceitos da teoria de grafos para análise estrutural:

- **Grafo de conectividade**: Cada bolinha é um vértice, conexões são arestas
- **Componentes conectados**: Identificação de partes do boneco
- **Análise de graus**: Distribuição de conexões por vértice
- **Caminhos e distâncias**: Análise de conectividade estrutural
- **Padrões de grafo**: Detecção de estrelas, linhas, árvores

## 2. Arquitetura do Sistema

### 2.1 Componentes Principais

- **ThreadBolinhaEx**: Interface gráfica e controle principal
- **AnaliseEstrutural**: Análise de características estruturais
- **AnaliseGrafo**: Análise baseada em teoria de grafos
- **AnaliseRelacional**: Análise de relações espaciais
- **RedeNeural**: Classificador neural (adaptado da rede de árvores)
- **Bolinha**: Representação de pontos do desenho

### 2.2 Fluxo de Processamento

```
Desenho → Captura de Pontos → Pré-processamento → Análise → Classificação → Resultado
```

## 3. Processos de Captura e Pré-processamento

### 3.1 Captura de Pontos (Bolinhas)

**Arquivo**: `ThreadBolinhaEx.java`

- **Processo**: Captura pontos do mouse durante o desenho
- **Características**:
  - Cada ponto é representado como uma `Bolinha` com coordenadas (x, y)
  - Cores aleatórias para visualização
  - Armazenamento em array de tamanho fixo

### 3.2 Remoção de Ruído

**Arquivo**: `AnaliseEstrutural.java` - método `removerBolinhasDesconectadas()`

- **Processo**: Identifica e remove bolinhas isoladas
- **Algoritmo**:
  1. Encontra componentes conectados usando DFS
  2. Mantém apenas o maior componente (boneco principal)
  3. Remove bolinhas desconectadas (ruído)

### 3.3 Crop Inteligente

**Arquivo**: `AnaliseEstrutural.java` - método `fazerCropInteligente()`

- **Processo**: Recorta a área útil do desenho
- **Características**:
  - Calcula limites do desenho (minX, maxX, minY, maxY)
  - Adiciona margem de 10 pixels
  - Transfere coordenadas para novo sistema

### 3.4 Normalização de Orientação

**Arquivo**: `AnaliseEstrutural.java` - método `normalizarOrientacao()`

- **Processo**: Alinha o boneco verticalmente (cabeça para cima)
- **Algoritmo**:
  1. Calcula centro de massa
  2. Encontra ângulo principal usando análise de eixos
  3. Rotaciona para alinhar com eixo vertical
  4. Verifica densidade de quadrantes para determinar orientação

## 4. Análise Estrutural

### 4.1 Constante de Conexão

**Valor**: `THRESHOLD_CONEXAO = 30.0`

- **Uso**: Distância máxima para considerar duas bolinhas conectadas
- **Aplicação**: Todos os algoritmos de conectividade

### 4.2 Análise por Bolinha

**Arquivo**: `AnaliseEstrutural.java` - método `analisarBonecoPalitoPorBolinha()`

**Características extraídas por bolinha**:

- **Grau**: Número de conexões diretas
- **Distância em nós**: Distância ao nó de maior grau (usando BFS)

**Saída**: Vetor de 80 elementos (40 bolinhas × 2 características)

### 4.3 Análise de Padrões Estruturais

**Arquivo**: `AnaliseEstrutural.java` - método `analisarBonecoPalitoPadroesEstruturais()`

**19 características extraídas**:

1. **Total de bolinhas** (0-50)
2. **Extremidades** - bolinhas com grau 1 (0-10)
3. **Linhas** - bolinhas com grau 2 (0-20)
4. **Junções simples** - bolinhas com grau 3 (0-10)
5. **Centros** - bolinhas com grau 4+ (0-5)
6. **Caminhos de 1 salto** (0-50)
7. **Caminhos de 2 saltos** (0-100)
8. **Caminhos de 3 saltos** (0-50)
9. **Caminhos de 4+ saltos** (0-30)
10. **Ramificações simples** (0-10)
11. **Ramificações complexas** (0-5)
12. **Cadeias lineares** (0-20)
13. **Largura do crop** (pixels)
14. **Altura do crop** (pixels)
15. **Proporção largura/altura** (×10)
16. **Densidade de bolinhas** (×10000)
17. **Simetria estrutural** (0-10)
18. **Estrutura geral** (0 ou 1)
19. **Tamanho do tronco** (bolinhas na região central)

### 4.4 Análise de Grafo

**Arquivo**: `AnaliseGrafo.java`

**Processos**:

- **Construção do grafo**: Conecta bolinhas próximas (threshold 30px)
- **Componentes conectados**: Identifica partes do boneco
- **Propriedades invariantes**: 20 características de grafo

**Características principais**:

- Número de componentes
- Grau médio
- Presença de ciclos
- Diâmetro do grafo
- Simetria
- Densidade
- Padrões (estrela, linha, árvore)

### 4.5 Análise Relacional

**Arquivo**: `AnaliseRelacional.java`

**Processos**:

- **Agrupamento por proximidade**: Agrupa bolinhas próximas
- **Análise de relações**: Estuda relações entre grupos
- **Padrões espaciais**: Detecta hierarquia, simetria, centralização

## 5. Rede Neural

### 5.1 Arquitetura

**Arquivo**: `RedeNeural.java`

- **Entrada**: 19 características estruturais normalizadas
- **Camada oculta 1**: 19 neurônios com pesos v1k[]
- **Camada oculta 2**: 19 neurônios com pesos v2k[]
- **Saída**: 1 neurônio com pesos w11, w12

### 5.2 Evolução da Arquitetura Neural

**Rede Original (Árvores)**:

- **Entrada**: 20 características (10 projeções verticais + 10 projeções horizontais)
- **Objetivo**: Detecção de desenhos de árvores
- **Método**: Análise por projeções de imagem

**Rede Adaptada (Bonecos Palito)**:

- **Entrada**: 19 características estruturais baseadas em grafos
- **Objetivo**: Detecção de bonecos palito
- **Método**: Análise estrutural e teoria de grafos

### 5.3 Normalização de Entrada

**Processo**: Cada característica é normalizada para [0,1]

```java
// Exemplos de normalização:
case 0: return Math.min(valor, 50) / 50.0;  // Total bolinhas
case 1: return Math.min(valor, 10) / 10.0;  // Extremidades
case 2: return Math.min(valor, 20) / 20.0;  // Linhas
```

### 5.4 Função de Ativação

**Processo**: Função escada (step function)

```java
if (y1 > limiar) {
    saida = 1;  // É boneco palito
    return 1;
}
return 0;       // Não é boneco palito
```

### 5.5 Threshold de Classificação

**Valor atual**: **140**

- **Histórico**: 49 → 47 → 140
- **Efeito**: Maior rigor na classificação

### 5.6 Treinamento

**Processo**: Aprendizado supervisionado

- **Target**: 47 (valor desejado para bonecos palito)
- **Limiar**: 140 (threshold de classificação)
- **Algoritmo**: Backpropagation simplificado

## 6. Visualização

### 6.1 Interface Principal

**Arquivo**: `ThreadBolinhaEx.java`

- **Canvas de desenho**: Captura pontos do mouse
- **Botões**: Treinar e Reiniciar
- **Feedback visual**: Texto colorido indicando resultado

### 6.2 Painel de Processamento

**Arquivo**: `ThreadBolinhaEx.java` - classe `PainelProcessamento`

**Duas seções**:

1. **Crop**: Visualização do desenho após crop inteligente
2. **Grafo**: Visualização das conexões entre bolinhas

**Removido**: Seção de matriz binária (conforme solicitado)

## 7. Persistência de Dados

### 7.1 Pesos da Rede Neural

**Arquivo**: `pesos_rede.txt`

- **Formato**: Um peso por linha
- **Carregamento**: Automático na inicialização
- **Salvamento**: Manual via botão "Treinar"

### 7.2 Estrutura dos Pesos

```
w11
w12
v1k[0] a v1k[18]  (19 pesos)
v2k[0] a v2k[18]  (19 pesos)
```

## 8. Algoritmos Específicos

### 8.1 Cálculo de Graus

```java
for (int i = 0; i < bolinhas.length; i++) {
    if (bolinhas[i] != null) {
        int grau = 0;
        for (int j = 0; j < bolinhas.length; j++) {
            if (i != j && bolinhas[j] != null) {
                double distancia = calcularDistancia(bolinhas[i], bolinhas[j]);
                if (distancia <= THRESHOLD_CONEXAO) {
                    grau++;
                }
            }
        }
        graus[i] = grau;
    }
}
```

### 8.2 BFS para Distâncias

```java
Queue<Integer> fila = new LinkedList<>();
fila.add(indiceNodoCentral);
visitado[indiceNodoCentral] = true;
distancias[indiceNodoCentral] = 0;

while (!fila.isEmpty()) {
    int atual = fila.poll();
    // Visita vizinhos não visitados
}
```

### 8.3 DFS para Componentes

```java
private void dfsComponente(Bolinha[] bolinhas, int index, List<Bolinha> componente,
                          boolean[] visitado, double threshold) {
    visitado[index] = true;
    componente.add(bolinhas[index]);

    for (int i = 0; i < bolinhas.length; i++) {
        if (bolinhas[i] != null && !visitado[i]) {
            double distancia = calcularDistancia(bolinhas[index], bolinhas[i]);
            if (distancia <= threshold) {
                dfsComponente(bolinhas, i, componente, visitado, threshold);
            }
        }
    }
}
```

## 9. Configurações e Parâmetros

### 9.1 Thresholds

- **Conexão**: 30.0 pixels
- **Classificação**: 140.0
- **Target treinamento**: 47.0

### 9.2 Limites de Normalização

- **Total bolinhas**: 0-50
- **Extremidades**: 0-10
- **Linhas**: 0-20
- **Junções**: 0-10
- **Centros**: 0-5

### 9.3 Margens e Tolerâncias

- **Crop**: 10 pixels
- **Simetria**: 20 pixels
- **Conectividade**: 80 pixels

## 10. Melhorias Implementadas

### 10.1 Remoção de Matriz Binária

- **Removido**: `converterParaMatrizBinaria()`
- **Removido**: `analisarContorno()`
- **Removido**: `calcularMomentosImagem()`
- **Removido**: Visualização de matriz binária

### 10.2 Constante de Threshold

- **Implementado**: `THRESHOLD_CONEXAO = 30.0`
- **Aplicado**: Todos os algoritmos de conectividade
- **Benefício**: Facilita ajustes futuros

### 10.3 Interface Simplificada

- **Antes**: 3 seções (Crop, Grafo, Matriz Binária)
- **Depois**: 2 seções (Crop, Grafo)
- **Benefício**: Interface mais limpa e focada

## 11. Performance e Robustez

### 11.1 Tratamento de Erros

- **Validação de entrada**: Verificação de valores NaN/Infinito
- **Limitação de valores**: Prevenção de overflow
- **Reinicialização automática**: Em caso de pesos inválidos

### 11.2 Otimizações

- **Pré-processamento**: Remove ruído antes da análise
- **Normalização**: Evita problemas de escala
- **Crop inteligente**: Reduz área de processamento

## 12. Decisões de Design e Justificativas

### 12.1 Uso de Teoria de Grafos

**Decisão**: Utilizar conceitos de teoria de grafos para análise estrutural

**Justificativas**:

- **Invariância**: Características de grafo são invariantes a rotação e escala
- **Robustez**: Menos sensível a variações no desenho
- **Estrutura**: Captura a conectividade essencial do boneco palito
- **Matemática**: Base teórica sólida para análise

**Conceitos aplicados**:

- **Grafo de conectividade**: Modela a estrutura do boneco
- **Componentes conectados**: Identifica partes funcionais
- **Análise de graus**: Caracteriza pontos de junção e extremidades
- **Caminhos**: Mede conectividade estrutural

### 12.2 Adaptação da Rede Neural

**Decisão**: Adaptar rede neural de detecção de árvores para bonecos palito

**Justificativas**:

- **Experiência prévia**: Rede já treinada para reconhecimento de formas
- **Arquitetura similar**: Ambos trabalham com características estruturais
- **Eficiência**: Reutilização de código e conhecimento
- **Validação**: Confirma eficácia da abordagem neural

**Adaptações realizadas**:

- **Entrada**: De 20 projeções para 19 características de grafo
- **Características**: De análise de imagem para análise estrutural
- **Treinamento**: Ajuste para novo conjunto de dados

### 12.3 Características Estruturais Escolhidas

**Decisão**: Usar 19 características específicas baseadas em grafos

**Justificativas**:

- **Completude**: Cobre aspectos estruturais essenciais
- **Discriminatividade**: Diferenciam bonecos palito de outros desenhos
- **Normalização**: Valores limitados para evitar overflow
- **Interpretabilidade**: Cada característica tem significado claro

## 13. Conclusões

O sistema implementa uma abordagem robusta e multi-dimensional para reconhecimento de bonecos palito, combinando:

1. **Análise estrutural** baseada em grafos
2. **Características geométricas** normalizadas
3. **Rede neural** adaptada de sistema prévio de detecção de árvores
4. **Pré-processamento** para remoção de ruído
5. **Interface visual** para feedback imediato

### 13.1 Contribuições Principais

- **Evolução de paradigma**: De análise por projeções para análise estrutural
- **Aplicação de teoria de grafos**: Uso inovador de conceitos de grafos em reconhecimento de formas
- **Adaptação de rede neural**: Reutilização eficiente de arquitetura prévia
- **Características robustas**: 19 características estruturais invariantes

### 13.2 Benefícios da Abordagem

- **Modularidade**: Arquitetura permite fácil manutenção e extensão
- **Robustez**: Menos sensível a variações no desenho
- **Eficiência**: Algoritmos otimizados para boa performance
- **Precisão**: Combinação de múltiplas análises para classificação precisa

A arquitetura modular permite fácil manutenção e extensão, enquanto os algoritmos implementados garantem boa performance e precisão na classificação.
