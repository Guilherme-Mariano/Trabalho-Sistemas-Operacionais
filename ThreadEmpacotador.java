// ThreadEmpacotador.java
import java.util.concurrent.Semaphore;
import javax.swing.SwingUtilities;
import java.util.Random;

public class ThreadEmpacotador extends Thread {

    private ObjetoGrafico empacotadorObj;
    private Box myBox;
    private Semaphore pacotesProntos;
    private Semaphore mutexArmazem;
    private Semaphore semaforoEspacoDisponivel;
    private PainelDeDesenho painel;
    private Random random;

    private Warehouse targetWarehouse;
    private int spawnX;
    private int spawnY;
    private int tempoArmazenamento; // Tempo GLOBAL para simularAcessoArmazem (em segundos)
    private int capacidadeMaximaM;
    private int tempoEmpacotamento; // Tempo INDIVIDUAL para simularTrabalho (em segundos)

    private final String[] framesAnimacao = {
        "/GameAsset/robot_ready.png",
        "/GameAsset/robot_hands_down.png"
    };

    private static final int SPAWN_Y = 430;
    private static final int SPAWN_X_INICIAL = 20;
    private static final int SPAWN_X_LARGURA = 300;
    private static final int MOVING_DURATION_SECONDS = 5;
    private static final int WORK_ANIMATION_STEP_MS = 100;

    // Construtor com 7 parâmetros
    public ThreadEmpacotador(PainelDeDesenho painel, Semaphore pacotesProntos, Semaphore mutexArmazem, Warehouse targetWarehouse, int tempoArmazenamento, int capacidadeMaximaM, Semaphore semaforoEspacoDisponivel, int tempoEmpacotamento) {
        this.painel = painel;
        this.pacotesProntos = pacotesProntos;
        this.mutexArmazem = mutexArmazem;
        this.targetWarehouse = targetWarehouse;
        this.tempoArmazenamento = tempoArmazenamento; // Armazena o tempo global
        this.capacidadeMaximaM = capacidadeMaximaM;
        this.semaforoEspacoDisponivel = semaforoEspacoDisponivel;
        this.tempoEmpacotamento = tempoEmpacotamento; // Armazena o tempo individual
        this.random = new Random();

        this.spawnX = SPAWN_X_INICIAL + random.nextInt(SPAWN_X_LARGURA);
        this.spawnY = SPAWN_Y;

        this.empacotadorObj = new ObjetoGrafico(this.spawnX, this.spawnY, 80, 80, framesAnimacao);
        this.myBox = new Box(painel, this.empacotadorObj);
    }

    public ObjetoGrafico getObjetoGrafico() {
        return this.empacotadorObj;
    }

    public Box getBox() {
        return this.myBox;
    }

    /** Método para simular espera ativa (busy-waiting) */
    private void busyWait(long milliseconds) {
        if (milliseconds <= 0) return;
        long startTime = System.currentTimeMillis();
        long endTime = startTime + milliseconds;
        while (System.currentTimeMillis() < endTime) {
            Thread.onSpinWait();
            if (Thread.currentThread().isInterrupted()) {
                System.err.println("ThreadEmpacotador " + getId() + " interrupted during busyWait.");
                return;
            }
        }
    }

    /** Lógica de animação e trabalho usando tempoEmpacotamento. */
    private void simularTrabalho() {
        myBox.setVisible(true);
        SwingUtilities.invokeLater(() -> myBox.updatePosition(Box.State.BELOW));

        // Usa o tempo de EMPACOTAMENTO individual
        long totalWorkMs = this.tempoEmpacotamento * 1000;
        int totalSteps = (int) (totalWorkMs / WORK_ANIMATION_STEP_MS);
        if (totalSteps <= 0) totalSteps = 1;

        for (int k = 0; k < totalSteps; k++) {
            if (Thread.currentThread().isInterrupted()) return;

            final int frameAtual = k % 2;

            SwingUtilities.invokeLater(() -> {
                empacotadorObj.setAnimationFrame(frameAtual);
                myBox.updatePosition(Box.State.BELOW);
                painel.repaint();
            });

            busyWait(WORK_ANIMATION_STEP_MS);
        }

        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setAnimationFrame(0);
            painel.repaint();
        });
    }

    /** Atualiza a posição do robô E da caixa na thread do Swing. */
    private void updatePositionOnEDT(int x, int y, Box.State boxState) {
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setLocation(x, y);
            if (myBox.getObjetoGrafico().isVisible()) {
                myBox.updatePosition(boxState);
            }
            painel.repaint();
        });
    }

    /** Anima o movimento por um tempo fixo. */
    private void moveTo(int targetX, int targetY, Box.State boxState) {
        int startX = empacotadorObj.getX();
        int startY = empacotadorObj.getY();
        float deltaX = targetX - startX;
        float deltaY = targetY - startY;

        System.out.println("  [Thread " + this.getId() + "] Movendo de Y=" + startY + " para Y=" + targetY + ". DeltaY = " + deltaY);

        myBox.setVisible(boxState == Box.State.ABOVE);
        if(boxState == Box.State.ABOVE) {
            SwingUtilities.invokeLater(() -> myBox.updatePosition(boxState));
        }

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (MOVING_DURATION_SECONDS * 1000);
        long currentTime;
        long visualStepDelayMs = 50;

        while ((currentTime = System.currentTimeMillis()) < endTime) {
             if (Thread.currentThread().isInterrupted()) return;

            float progress = (float)(currentTime - startTime) / (MOVING_DURATION_SECONDS * 1000);

            float currentX = startX + (deltaX * progress);
            float currentY = startY + (deltaY * progress);

            updatePositionOnEDT(Math.round(currentX), Math.round(currentY), boxState);

            busyWait(visualStepDelayMs);
        }

        updatePositionOnEDT(targetX, targetY, boxState);
    }

    /** Simula o acesso ao armazém usando tempoArmazenamento. */
    private void simularAcessoArmazem() {
         System.out.println("  [Thread " + this.getId() + "] ACESSOU armazém. Simulando armazenamento ("+ this.tempoArmazenamento + "s)...");

         myBox.setVisible(false);

         // Usa o tempo de ARMAZENAMENTO global
         long totalStorageMs = this.tempoArmazenamento * 1000;
         long stepTimeMs = 250;
         long startTime = System.currentTimeMillis();

         while(System.currentTimeMillis() < startTime + totalStorageMs) {
             if (Thread.currentThread().isInterrupted()) return;

             SwingUtilities.invokeLater(() -> empacotadorObj.setAnimationFrame(1));
             busyWait(stepTimeMs);
             if (Thread.currentThread().isInterrupted()) return;

             SwingUtilities.invokeLater(() -> empacotadorObj.setAnimationFrame(0));
             busyWait(stepTimeMs);
         }
         SwingUtilities.invokeLater(() -> empacotadorObj.setAnimationFrame(0));
    }

    @Override
    public void run() {
        if (Thread.currentThread().isInterrupted()) return;
        System.out.println("Empacotador (Thread " + this.getId() + ") iniciado com tempo de empacotamento: " + this.tempoEmpacotamento + "s. Spawn em (" + this.spawnX + ", " + this.spawnY + ")");

        while (!Thread.currentThread().isInterrupted()) {

            // 1. Trabalha (usa tempoEmpacotamento)
            System.out.println("Empacotador (Thread " + this.getId() + ") trabalhando...");
            simularTrabalho();
            if (Thread.currentThread().isInterrupted()) break;

            // 2. Loop de tentativa de acesso e armazenamento
            boolean stored = false;
            while (!Thread.currentThread().isInterrupted() && !stored) {
                try {
                    System.out.println("Empacotador (Thread " + this.getId() + ") esperando pelo mutex do armazém...");
                    mutexArmazem.acquire();
                     if (Thread.currentThread().isInterrupted()) {
                         System.err.println("ThreadEmpacotador " + getId() + " interrompido esperando mutex.");
                         break;
                     }

                    // ----- INÍCIO DA SEÇÃO CRÍTICA -----
                    System.out.println("Empacotador (Thread " + this.getId() + ") adquiriu mutex. Verificando espaço...");

                    if (pacotesProntos.availablePermits() < capacidadeMaximaM) {
                        System.out.println("Empacotador (Thread " + this.getId() + ") Espaço disponível. Movendo para o armazém...");

                        // Move para o armazém
                        int targetX = targetWarehouse.getObjetoGrafico().getX() + 10;
                        int targetY = targetWarehouse.getObjetoGrafico().getY() + 10;
                        moveTo(targetX, targetY, Box.State.ABOVE);
                         if (Thread.currentThread().isInterrupted()) break;

                        // Simula acesso (usa tempoArmazenamento)
                        simularAcessoArmazem();
                         if (Thread.currentThread().isInterrupted()) break;

                        // Libera pacote
                        pacotesProntos.release();
                        System.out.println(">>> PACOTE ARMAZENADO (Thread " + this.getId() + "). Total: " + pacotesProntos.availablePermits());
                        stored = true; // Sai do loop interno

                        // Retorna ao spawn
                        System.out.println("Empacotador (Thread " + this.getId() + ") retornando instantaneamente ao spawn.");
                        SwingUtilities.invokeLater(() -> {
                            empacotadorObj.setLocation(spawnX, spawnY);
                            myBox.setVisible(true);
                            myBox.updatePosition(Box.State.BELOW);
                            painel.repaint();
                        });

                    } else {
                        System.out.println("Empacotador (Thread " + this.getId() + ") Armazém CHEIO ( >= " + capacidadeMaximaM + "). Liberando mutex e esperando.");
                    }
                    // ----- FIM DA SEÇÃO CRÍTICA ----- (Mutex liberado no finally)

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("ThreadEmpacotador " + getId() + " interrompido na seção crítica ou acquire.");
                    break;
                } finally {
                     if (mutexArmazem.availablePermits() == 0) {
                        mutexArmazem.release();
                        System.out.println("Empacotador (Thread " + this.getId() + ") liberou mutex.");
                     }
                }

                // Se NÃO conseguiu armazenar, espera por espaço FORA do mutex
                if (!stored && !Thread.currentThread().isInterrupted()) {
                    try {
                        System.out.println("Empacotador (Thread " + this.getId() + ") dormindo, esperando espaço...");
                        semaforoEspacoDisponivel.acquire();
                        System.out.println("Empacotador (Thread " + this.getId() + ") ACORDOU! Tentando acessar armazém novamente.");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("ThreadEmpacotador " + getId() + " interrompido enquanto esperava por espaço.");
                        break; 
                    }
                }
            } 

            if (Thread.currentThread().isInterrupted()) break;

        } 

        System.out.println("Empacotador (Thread " + this.getId() + ") terminando.");
        
         SwingUtilities.invokeLater(() -> {
            if(painel != null) {
                if(empacotadorObj != null) painel.removerObjetoParaDesenhar(empacotadorObj);
                if(myBox != null && myBox.getObjetoGrafico() != null) painel.removerObjetoParaDesenhar(myBox.getObjetoGrafico());
            }
        });
    }
}