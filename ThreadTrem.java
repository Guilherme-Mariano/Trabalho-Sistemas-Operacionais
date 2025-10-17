import javax.swing.SwingUtilities;
import java.util.concurrent.Semaphore;

public class ThreadTrem extends Thread {

    private final int TRABALHO_DE_CARGA = 50;
    private final int DISTANCIA_VIAGEM = 400;

    private ObjetoGrafico trainObj;
    private PainelDeDesenho painel;
    private Semaphore pacotesProntos; // O semáforo compartilhado

    /**
     * O construtor recebe o painel e o semáforo compartilhado.
     */
    public ThreadTrem(PainelDeDesenho painel, Semaphore pacotesProntos) {
        this.painel = painel;
        this.trainObj = new ObjetoGrafico("/GameAsset/locomotive.png", 10, 370, 200, 120);
        this.pacotesProntos = pacotesProntos; // Armazena a referência
    }

    public ObjetoGrafico getObjetoGrafico() {
        return this.trainObj;
    }
    
    private void simularPassoDeTrabalho() {
        double soma = 0;
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 5000; j++) {
                soma = soma + Math.sin(i) * Math.cos(j);
            }
        }
    }
    
    private void moveGameObj(int x_step, int y_step) {
        SwingUtilities.invokeLater(() -> {
            trainObj.setLocation(trainObj.getX() + x_step, trainObj.getY() + y_step);
            painel.repaint();
        });
    }

    public void load_up() {
        System.out.println("Trem carregando...");
        for (int i = 0; i < TRABALHO_DE_CARGA; i++) {
            simularPassoDeTrabalho();
        }
        System.out.println("Trem carregado.");
    }
    
    public void unload() {
        System.out.println("Trem descarregando...");
        for (int i = 0; i < TRABALHO_DE_CARGA; i++) {
            simularPassoDeTrabalho();
        }
        System.out.println("Trem descarregou.");
    }

    public void go_right() {
        trainObj.setDirecao(Direcao.DIREITA);
        System.out.println("Trem viajando para a Direita...");
        for (int i = 0; i < DISTANCIA_VIAGEM; i++) {
            simularPassoDeTrabalho();
            moveGameObj(2, 0);
        }
        System.out.println("Trem chegou ao destino (Direita).");
    }

    public void go_left() {
        trainObj.setDirecao(Direcao.ESQUERDA);
        System.out.println("Trem voltando para a Esquerda...");
        for (int i = 0; i < DISTANCIA_VIAGEM; i++) {
            simularPassoDeTrabalho();
            moveGameObj(-2, 0);
        }
        System.out.println("Trem chegou à origem (Esquerda).");
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Trem: esperando por um pacote para poder carregar...");
                
                // Tenta obter uma permissão. Se nenhuma estiver disponível, a thread BLOQUEIA aqui.
                pacotesProntos.acquire();
                
                System.out.println("<<< PERMISSÃO RECEBIDA! Trem iniciando ciclo de carga.");

                // Uma vez que a permissão foi adquirida, o ciclo normal continua.
                load_up();
                go_right();
                unload();
                go_left();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread do trem foi interrompida.");
                break;
            }
        }
    }
}