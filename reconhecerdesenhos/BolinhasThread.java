package reconhecerdesenhos;

import javax.swing.JOptionPane;

public class BolinhasThread extends Thread {
    ThreadBolinhaEx painel;
    int tempo;
    Bolinha[] bolinhas = new Bolinha[20];// a melhor forma na verdade seria utilizar um ArrayList ou Vector
    Bolinha obstaculo = null;
    int contBolinhas = 0;// número total de bolinhas adiconadas ao vetor

    public BolinhasThread(ThreadBolinhaEx painel, int tempo) {
        this.painel = painel;
        this.tempo = tempo;
    }

    public void run() {
        JOptionPane.showMessageDialog(null, "Desenhe uma árvore com 20 bolinhas!");
        while (true) {
            try {
                painel.desenha(bolinhas);
                Thread.sleep(tempo);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}