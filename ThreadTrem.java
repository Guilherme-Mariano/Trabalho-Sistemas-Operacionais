// ThreadTrem.java
import javax.swing.SwingUtilities;
import java.util.concurrent.Semaphore;

public class ThreadTrem extends Thread {
    
    // Tempo em SEGUNDOS
    private static final int TEMPO_VIAGEM_SEGUNDOS = 8; 
    private static final int TEMPO_CARGA_SEGUNDOS = 3;   

    // Delay entre frames da animação de movimento (em milissegundos)
    // Valores menores = mais suave (ex: 16ms ≈ 60 FPS, 20ms = 50 FPS)
    private static final long VISUAL_STEP_DELAY_MS = 20; 

    private ObjetoGrafico trainObj;
    private Carrier carrier; 
    private PainelDeDesenho painel;
    private Semaphore pacotesProntos;
    private int caixasNecessarias; 
    private Direcao direcaoAtual;

    public ThreadTrem(PainelDeDesenho painel, Semaphore pacotesProntos, int caixasNecessarias) {
        this.painel = painel;
        this.pacotesProntos = pacotesProntos;
        this.caixasNecessarias = caixasNecessarias;
        // Sua coordenada Y do trem
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
    
    /** Método para simular espera ativa (busy-waiting) */
    private void busyWait(long milliseconds) {
        // Previne espera negativa ou zero, que pode causar loop infinito
        if (milliseconds <= 0) return; 
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + milliseconds;
        while (System.currentTimeMillis() < endTime) {
            Thread.onSpinWait(); 
             if (Thread.currentThread().isInterrupted()) {
                System.err.println("ThreadTrem " + getId() + " interrupted during busyWait.");
                // Retorna imediatamente se interrompido
                return; 
            }
        }
    }
    
    /** Move o trem E o vagão. */
    private void moveGameObj(int x_step, int y_step) {
        // Executa a atualização na thread do Swing
        SwingUtilities.invokeLater(() -> {
            // Verifica se o objeto ainda existe (pode ter sido removido)
            if (trainObj != null && carrier != null && painel != null) {
                trainObj.setLocation(trainObj.getX() + x_step, trainObj.getY() + y_step);
                carrier.updatePosition(this.direcaoAtual); 
                painel.repaint();
            }
        });
    }

    /** Carga/descarga dura TEMPO_CARGA_SEGUNDOS usando busyWait */
    public void load_up() {
        System.out.println("Trem carregando...");
        carrier.setState(Carrier.State.FULL); 
        for (int i = 0; i < TEMPO_CARGA_SEGUNDOS; i++) {
            if (Thread.currentThread().isInterrupted()) return; 
            busyWait(1000); // Espera 1 segundo
        }
        System.out.println("Trem carregado.");
    }
    
    public void unload() {
        System.out.println("Trem descarregando...");
        carrier.setState(Carrier.State.EMPTY); 
        for (int i = 0; i < TEMPO_CARGA_SEGUNDOS; i++) {
             if (Thread.currentThread().isInterrupted()) return;
             busyWait(1000); // Espera 1 segundo
        }
        System.out.println("Trem descarregou.");
    }

    /** * Movimento mais suave com cálculo de passo preciso. */
    public void go_right() {
         if (Thread.currentThread().isInterrupted()) return; 
        this.direcaoAtual = Direcao.DIREITA;
        trainObj.setDirecao(this.direcaoAtual);
        carrier.setState(Carrier.State.FULL); 
        System.out.println("Trem viajando para a Direita...");

        int startX = trainObj.getX();
        // Define X de destino (perto do armazém B)
        int targetX = 1000 - trainObj.getLargura() - 50; 
        float totalDistanceX = targetX - startX;
        
        long totalDurationMs = TEMPO_VIAGEM_SEGUNDOS * 1000;
        // Calcula quantos passos visuais (atualizações) faremos no total
        int totalVisualSteps = (int) (totalDurationMs / VISUAL_STEP_DELAY_MS); 
        if (totalVisualSteps <= 0) totalVisualSteps = 1; // Evita divisão por zero

        // Calcula quanto mover em X a cada passo visual
        float stepXPerVisualUpdate = totalDistanceX / totalVisualSteps;

        long startTime = System.currentTimeMillis();

        // Loop pelos passos visuais
        for (int i = 0; i < totalVisualSteps; i++) {
             if (Thread.currentThread().isInterrupted()) return; 

            // Move a distância calculada para este passo
            moveGameObj(Math.round(stepXPerVisualUpdate), 0);

            // Espera o delay definido para o passo visual
            busyWait(VISUAL_STEP_DELAY_MS); 
        }
        
        // Garante a posição final exata após o loop
        trainObj.setLocation(targetX, trainObj.getY()); 
        // Atualiza o carrier na posição final
        SwingUtilities.invokeLater(() -> {
            if(carrier != null) carrier.updatePosition(this.direcaoAtual);
            if(painel != null) painel.repaint();
        });


        System.out.println("Trem chegou ao destino (Direita).");
    }

    /** CORREÇÃO: Movimento mais suave com cálculo de passo preciso. */
    public void go_left() {
         if (Thread.currentThread().isInterrupted()) return; 
        this.direcaoAtual = Direcao.ESQUERDA;
        trainObj.setDirecao(this.direcaoAtual);
        carrier.setState(Carrier.State.EMPTY); 
        System.out.println("Trem voltando para a Esquerda...");

        int startX = trainObj.getX();
        int targetX = 50; // Posição inicial A
        float totalDistanceX = targetX - startX;
        
        long totalDurationMs = TEMPO_VIAGEM_SEGUNDOS * 1000;
        int totalVisualSteps = (int) (totalDurationMs / VISUAL_STEP_DELAY_MS);
         if (totalVisualSteps <= 0) totalVisualSteps = 1;

        float stepXPerVisualUpdate = totalDistanceX / totalVisualSteps;
        
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalVisualSteps; i++) {
             if (Thread.currentThread().isInterrupted()) return; 

            moveGameObj(Math.round(stepXPerVisualUpdate), 0);
            busyWait(VISUAL_STEP_DELAY_MS); 
        }

        // Garante a posição final exata
        trainObj.setLocation(targetX, trainObj.getY());
        SwingUtilities.invokeLater(() -> {
             if(carrier != null) carrier.updatePosition(this.direcaoAtual);
             if(painel != null) painel.repaint();
        });
        
        System.out.println("Trem chegou à origem (Esquerda).");
    }

    @Override
    public void run() {
        // Define estado inicial visualmente
        trainObj.setDirecao(this.direcaoAtual); 
        carrier.setState(Carrier.State.EMPTY);
        SwingUtilities.invokeLater(() -> carrier.updatePosition(this.direcaoAtual));

        while (!Thread.currentThread().isInterrupted()) { 
            try {
                System.out.println("Trem: esperando por " + caixasNecessarias + " caixas... (Atuais: " + pacotesProntos.availablePermits() + ")");
                pacotesProntos.acquire(caixasNecessarias); 
                
                if (Thread.currentThread().isInterrupted()) break; 
                System.out.println("<<< " + caixasNecessarias + " CAIXAS RECEBIDAS! Trem iniciando ciclo de carga.");
                
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