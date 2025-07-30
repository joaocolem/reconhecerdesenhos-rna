# Reconhecedor de Boneco Palito - Rede Neural Simples

Este projeto é uma aplicação Java Swing para desenhar bolinhas em um painel e usar uma rede neural simples para reconhecer padrões de boneco palito (stick figure).

## Funcionalidades

- Desenhe bolinhas clicando no painel para formar um boneco palito.
- O sistema tenta reconhecer se o desenho é um boneco palito usando uma rede neural.
- Se o reconhecimento estiver errado, clique em **Treinar** para ajustar os pesos da rede neural.
- Os pesos da rede são salvos automaticamente em `pesos_rede.txt` e carregados ao iniciar o programa.

## Como rodar

1. Compile os arquivos Java:
   ```sh
   javac reconhecerdesenhos/reconhecerdesenhos/*.java
   ```
2. Execute a aplicação:
   ```sh
   java reconhecerdesenhos.ThreadBolinhaEx
   ```

## Como treinar a rede neural

- Desenhe um boneco palito usando até 40 bolinhas.
- Se o sistema não reconhecer corretamente, clique no botão **Treinar**.
- O sistema ajusta os pesos para que o padrão desenhado seja reconhecido como boneco palito.
- Os pesos são salvos automaticamente.

## Análise de Boneco Palito

O sistema agora usa uma análise mais sofisticada para reconhecer bonecos palito:

### Características Analisadas (20 features):

**Regiões Funcionais (1-10):**

- Cabeça (topo central)
- Pescoço
- Tronco superior
- Braço esquerdo
- Braço direito
- Tronco inferior
- Perna esquerda
- Perna direita
- Área central (tronco)
- Área periférica (membros)

**Análise de Estrutura (11-15):**

- Detecção de cabeça
- Detecção de tronco
- Detecção de braços
- Detecção de pernas
- Simetria do desenho

**Análise Avançada (16-20):**

- Conectividade entre bolinhas
- Proporções (cabeça vs corpo)
- Detecção de linhas retas
- Distribuição espacial
- Estrutura hierárquica

## Sobre os pesos

- Os pesos da rede neural são salvos no arquivo `pesos_rede.txt`.
- Ao iniciar o programa, se esse arquivo existir, os pesos são carregados dele.
- Se não existir, a rede usa os valores padrão definidos no código.

## Dicas para desenhar um boneco palito

1. **Cabeça**: 1-4 bolinhas no topo central
2. **Tronco**: 4+ bolinhas em linha vertical no centro
3. **Braços**: 2+ bolinhas em cada lado na altura do tronco
4. **Pernas**: 2+ bolinhas em cada lado na parte inferior
5. **Conectividade**: As bolinhas devem estar próximas umas das outras
6. **Simetria**: O desenho deve ser relativamente simétrico
7. **Proporções**: Cabeça pequena (até 4 bolinhas), corpo maior (12+ bolinhas)

## Observações

- A taxa de aprendizado pode ser ajustada no código da classe `RedeNeural`.
- Para melhores resultados, treine a rede com exemplos variados (boneco palito e não-boneco palito).
- O sistema é mais eficaz para bonecos palito do que as projeções anteriores usadas para árvores.
- Agora permite até 40 bolinhas para desenhos mais detalhados.

---

Desenvolvido para fins didáticos.
