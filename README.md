# Reconhecedor de Desenhos - Rede Neural Simples

Este projeto é uma aplicação Java Swing para desenhar bolinhas em um painel e usar uma rede neural simples para reconhecer padrões (por exemplo, uma árvore).

## Funcionalidades

- Desenhe bolinhas clicando no painel.
- O sistema tenta reconhecer se o desenho é uma árvore usando uma rede neural.
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

- Desenhe uma árvore (ou outro padrão) usando 20 bolinhas.
- Se o sistema não reconhecer corretamente, clique no botão **Treinar**.
- O sistema ajusta os pesos para que o padrão desenhado seja reconhecido como árvore.
- Os pesos são salvos automaticamente.

## Sobre os pesos

- Os pesos da rede neural são salvos no arquivo `pesos_rede.txt`.
- Ao iniciar o programa, se esse arquivo existir, os pesos são carregados dele.
- Se não existir, a rede usa os valores padrão definidos no código.

## Observações

- A taxa de aprendizado pode ser ajustada no código da classe `RedeNeural`.
- Para melhores resultados, treine a rede com exemplos variados (árvore e não-árvore).

---

Desenvolvido para fins didáticos.
