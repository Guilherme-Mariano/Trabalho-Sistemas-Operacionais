// ThreadTrem.java
import javax.swing.SwingUtilities;
import java.util.concurrent.Semaphore;

public class ThreadTrem extends Thread {

    private final int tempoViagemSegundos;
    // Valores Default
    private static final int TEMPO_CARGA_SEGUNDOS = 3;
    private static final long VISUAL_STEP_DELAY_MS = 20;

    private ObjetoGrafico trainObj;
    private Carrier carrier;
    private PainelDeDesenho painel;
    private Semaphore pacotesProntos;
    private int caixasNecessarias; // N
    private Semaphore semaforoEspacoDisponivel; // Para acordar empacotadores
    private Direcao direcaoAtual;

    public ThreadTrem(PainelDeDesenho painel, Semaphore pacotesProntos, int caixasNecessarias, int tempoViagemSegundos, Semaphore semaforoEspacoDisponivel) {
        this.painel = painel;
        this.pacotesProntos = pacotesProntos;
        this.caixasNecessarias = caixasNecessarias; // N
        this.tempoViagemSegundos = tempoViagemSegundos; 
        this.semaforoEspacoDisponivel = semaforoEspacoDisponivel; // Vai dar um release para os empacotadores

        this.trainObj = new ObjetoGrafico("/GameAsset/locomotive.png", 50, 350, 120, 80);
        this.carrier = new Carrier(painel, this.trainObj);
        this.direcaoAtual = Direcao.DIREITA;
        this.trainObj.setDirecao(this.direcaoAtual);
    }

    public ObjetoGrafico getObjetoGrafico() {
        return this.trainObj;
    }

    public Carrier getCarrier() {
        return this.carrier;
    }

    /** * BusyWait. */
    private void busyWait(long milliseconds) {
        if (milliseconds <= 0) return;
        long startTime = System.currentTimeMillis();
        long endTime = startTime + milliseconds;
        double sum =0;
        while (System.currentTimeMillis() < endTime) {
            if (Thread.currentThread().isInterrupted()) {
                System.err.println("ThreadTrem " + getId() + " interrupted during busyWait.");
                Thread.currentThread().interrupt(); 
                return;
            }
            sum = sum + Math.sin(endTime);
        }
    }

    /** Move o trem com vagão. */
    private void moveGameObj(int x_step, int y_step) {
        SwingUtilities.invokeLater(() -> {
            if (trainObj != null && carrier != null && painel != null) {
                trainObj.setLocation(trainObj.getX() + x_step, trainObj.getY() + y_step);
                carrier.updatePosition(this.direcaoAtual);
                painel.repaint();
            }
        });
    }

    /** Carga usando busyWait */
    public void load_up() {
        System.out.println("Trem carregando...");
        carrier.setState(Carrier.State.FULL);
        for (int i = 0; i < TEMPO_CARGA_SEGUNDOS; i++) {
            if (Thread.currentThread().isInterrupted()) return;
            busyWait(1000); // Espera 1 segundo
        }
        System.out.println("Trem carregado.");
    }

    /** Descarga usando busyWait */
    public void unload() {
        System.out.println("Trem descarregando...");
        carrier.setState(Carrier.State.EMPTY);
        for (int i = 0; i < TEMPO_CARGA_SEGUNDOS; i++) {
             if (Thread.currentThread().isInterrupted()) return;
             busyWait(1000); // Espera 1 segundo
        }
        System.out.println("Trem descarregou.");
    }

    /** Movimento usa tempoViagemSegundos, com pacing visual via busyWait. */
    public void go_right() {
         if (Thread.currentThread().isInterrupted()) return;
        this.direcaoAtual = Direcao.DIREITA;
        trainObj.setDirecao(this.direcaoAtual);
        carrier.setState(Carrier.State.FULL);
        System.out.println("Trem viajando para a Direita...");

        int startX = trainObj.getX();
        int targetX = 1000 - trainObj.getLargura() - 50;
        float totalDistanceX = targetX - startX;

        long totalDurationMs = this.tempoViagemSegundos * 1000;
        int totalVisualSteps = (int) (totalDurationMs / VISUAL_STEP_DELAY_MS);
        if (totalVisualSteps <= 0) totalVisualSteps = 1;

        float stepXPerVisualUpdate = totalDistanceX / totalVisualSteps;

        // PARTE RELEVANTE DA ANIMAÇÃO DO TREM 
        for (int i = 0; i < totalVisualSteps; i++) {
             if (Thread.currentThread().isInterrupted()) return;

            // MOVEGAMEOBJ ESTÁ A PASSO DO busyWait
            moveGameObj(Math.round(stepXPerVisualUpdate), 0);
            busyWait(VISUAL_STEP_DELAY_MS);
        }

        trainObj.setLocation(targetX, trainObj.getY());
        SwingUtilities.invokeLater(() -> {
            if(carrier != null) carrier.updatePosition(this.direcaoAtual);
            if(painel != null) painel.repaint();
        });

        System.out.println("Trem chegou ao destino (Direita).");
    }

    public void go_left() {
         if (Thread.currentThread().isInterrupted()) return;
        this.direcaoAtual = Direcao.ESQUERDA;
        trainObj.setDirecao(this.direcaoAtual);
        carrier.setState(Carrier.State.EMPTY);
        System.out.println("Trem voltando para a Esquerda...");

        int startX = trainObj.getX();
        int targetX = 50;
        float totalDistanceX = targetX - startX;

        long totalDurationMs = this.tempoViagemSegundos * 1000;
        int totalVisualSteps = (int) (totalDurationMs / VISUAL_STEP_DELAY_MS);
         if (totalVisualSteps <= 0) totalVisualSteps = 1;

        float stepXPerVisualUpdate = totalDistanceX / totalVisualSteps;

        for (int i = 0; i < totalVisualSteps; i++) {
             if (Thread.currentThread().isInterrupted()) return;
            moveGameObj(Math.round(stepXPerVisualUpdate), 0);
            busyWait(VISUAL_STEP_DELAY_MS);
        }

        trainObj.setLocation(targetX, trainObj.getY());
        SwingUtilities.invokeLater(() -> {
             if(carrier != null) carrier.updatePosition(this.direcaoAtual);
             if(painel != null) painel.repaint();
        });

        System.out.println("Trem chegou à origem (Esquerda).");
    }

    @Override
    public void run() {
        trainObj.setDirecao(this.direcaoAtual);
        carrier.setState(Carrier.State.EMPTY);
        SwingUtilities.invokeLater(() -> carrier.updatePosition(this.direcaoAtual));

        while (!Thread.currentThread().isInterrupted()) {
            try {
                System.out.println("Trem: esperando por " + caixasNecessarias + " caixas... (Atuais: " + pacotesProntos.availablePermits() + ")");
                pacotesProntos.acquire(caixasNecessarias);

                if (Thread.currentThread().isInterrupted()) break;
                System.out.println("<<< " + caixasNecessarias + " CAIXAS RECEBIDAS! Trem partindo.");

                System.out.println("Trem: Sinalizando que " + caixasNecessarias + " espaços estão disponíveis.");
                semaforoEspacoDisponivel.release(caixasNecessarias);

                // load_up();
                go_right();
                 if (Thread.currentThread().isInterrupted()) break;
                unload();
                 if (Thread.currentThread().isInterrupted()) break;
                go_left();
                 if (Thread.currentThread().isInterrupted()) break;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread do trem foi interrompida enquanto esperava por caixas.");
                break;
            }
        }
         System.out.println("Thread do trem terminando.");
    }
}