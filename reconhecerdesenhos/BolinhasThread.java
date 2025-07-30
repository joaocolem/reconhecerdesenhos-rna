package reconhecerdesenhos;

import javax.swing.JOptionPane;

public class BolinhasThread extends Thread {
    ThreadBolinhaEx painel;
    int tempo;
    Bolinha[] bolinhas = new Bolinha[40];// a melhor forma na verdade seria utilizar um ArrayList ou Vector
    Bolinha obstaculo = null;
    int contBolinhas = 0;// número total de bolinhas adiconadas ao vetor

    public BolinhasThread(ThreadBolinhaEx painel, int tempo) {
        this.painel = painel;
        this.tempo = tempo;
    }

    public void run() {
        JOptionPane.showMessageDialog(null, "Desenhe um boneco palito com até 40 bolinhas!");
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