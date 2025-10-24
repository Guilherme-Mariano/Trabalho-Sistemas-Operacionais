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
    private int tempoArmazenamento; // Tempo GLOBAL (em segundos)
    private int capacidadeMaximaM;
    private int tempoEmpacotamento; // Tempo INDIVIDUAL (em segundos)

    private final String[] framesAnimacao = {
        "/GameAsset/robot_ready.png",
        "/GameAsset/robot_hands_down.png"
    };

    private static final int SPAWN_Y = 430;
    private static final int SPAWN_X_INICIAL = 20;
    private static final int SPAWN_X_LARGURA = 300;
    private static final float MOVE_PIXELS_PER_STEP = 2.0f; // Pixels a mover por atualização
    private static final int MOVE_STEP_DELAY_MS = 20;     // Delay entre atualizações (ms)
    private static final int WORK_ANIMATION_STEP_MS = 50; // Delay animação trabalho
    private static final int STORAGE_ANIMATION_DURATION_MS = 300; // Duração visual armaz.

    public ThreadEmpacotador(PainelDeDesenho painel, Semaphore pacotesProntos, Semaphore mutexArmazem, Warehouse targetWarehouse, int tempoArmazenamento, int capacidadeMaximaM, Semaphore semaforoEspacoDisponivel, int tempoEmpacotamento) {
        this.painel = painel;
        this.pacotesProntos = pacotesProntos;
        this.mutexArmazem = mutexArmazem;
        this.targetWarehouse = targetWarehouse;
        this.tempoArmazenamento = tempoArmazenamento;
        this.capacidadeMaximaM = capacidadeMaximaM;
        this.semaforoEspacoDisponivel = semaforoEspacoDisponivel;
        this.tempoEmpacotamento = tempoEmpacotamento;
        this.random = new Random();
        this.spawnX = SPAWN_X_INICIAL + random.nextInt(SPAWN_X_LARGURA);
        this.spawnY = SPAWN_Y;
        this.empacotadorObj = new ObjetoGrafico(this.spawnX, this.spawnY, 80, 80, framesAnimacao);
        this.myBox = new Box(painel, this.empacotadorObj);
    }

    public ObjetoGrafico getObjetoGrafico() { return this.empacotadorObj; }
    public Box getBox() { return this.myBox; }

    /** * Método busy-wait  */
    private void busyWait(long milliseconds) {
        if (milliseconds <= 0) return;
        long startTime = System.currentTimeMillis();
        long endTime = startTime + milliseconds;
        while (System.currentTimeMillis() < endTime) {
            if (Thread.currentThread().isInterrupted()) {
                System.err.println("ThreadEmpacotador " + getId() + " interrupted during busyWait.");
                return; 
            }
        }
    }

    /** Lógica de animação e trabalho com passos mais curtos. */
    private void simularTrabalho() {
        if (myBox == null || myBox.getObjetoGrafico() == null) {
             System.err.println("Erro: myBox ou seu ObjetoGrafico é nulo em simularTrabalho para Thread " + getId());
             return;
        }

        if (!myBox.getObjetoGrafico().isVisible()) {
            myBox.setVisible(true);
            SwingUtilities.invokeLater(() -> myBox.updatePosition(Box.State.BELOW));
        } else {
             SwingUtilities.invokeLater(() -> myBox.updatePosition(Box.State.BELOW));
        }

        long totalWorkMs = this.tempoEmpacotamento * 1000;
        int totalSteps = (int) (totalWorkMs / WORK_ANIMATION_STEP_MS);
        if (totalSteps <= 0) totalSteps = 1;

        for (int k = 0; k < totalSteps; k++) {
            if (Thread.currentThread().isInterrupted()) return;

            final int frameAtual = k % 2;

            SwingUtilities.invokeLater(() -> {
                if (empacotadorObj != null && myBox != null && painel != null) {
                    empacotadorObj.setAnimationFrame(frameAtual);
                    myBox.updatePosition(Box.State.BELOW);
                    painel.repaint();
                }
            });

            busyWait(WORK_ANIMATION_STEP_MS);
        }

        SwingUtilities.invokeLater(() -> {
             if (empacotadorObj != null && painel != null) {
                empacotadorObj.setAnimationFrame(0);
                painel.repaint();
             }
        });
    }

    /** Atualiza a posição do robô E da caixa na thread do Swing. */
    private void updatePositionOnEDT(int x, int y, Box.State boxState) {
        SwingUtilities.invokeLater(() -> {
            if (empacotadorObj == null || myBox == null || myBox.getObjetoGrafico() == null || painel == null) return;

            empacotadorObj.setLocation(x, y);
            if (myBox.getObjetoGrafico().isVisible()) {
                myBox.updatePosition(boxState);
            }
            painel.repaint();
        });
    }

    /** Movimento baseado em passo fixo. */
    private void moveTo(int targetX, int targetY, Box.State boxState) {
        if (empacotadorObj == null) return;

        int startX = empacotadorObj.getX();
        int startY = empacotadorObj.getY();
        float deltaX = targetX - startX;
        float deltaY = targetY - startY;

        System.out.println("  [Thread " + this.getId() + "] Movendo de ("+startX+","+startY+") para ("+targetX+","+targetY+"). DeltaY = " + deltaY);

        float totalDistance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (totalDistance < MOVE_PIXELS_PER_STEP) {
            updatePositionOnEDT(targetX, targetY, boxState);
            return;
        }

        int numberOfSteps = (int) (totalDistance / MOVE_PIXELS_PER_STEP);
        if (numberOfSteps <= 0) numberOfSteps = 1;

        float stepX = deltaX / numberOfSteps;
        float stepY = deltaY / numberOfSteps;

        if (myBox != null) myBox.setVisible(boxState == Box.State.ABOVE);
        if(boxState == Box.State.ABOVE && myBox != null) {
            SwingUtilities.invokeLater(() -> myBox.updatePosition(boxState));
        }

        float currentX = startX;
        float currentY = startY;

        for (int i = 0; i < numberOfSteps; i++) {
             if (Thread.currentThread().isInterrupted()) return;

            currentX += stepX;
            currentY += stepY;

            updatePositionOnEDT(Math.round(currentX), Math.round(currentY), boxState);

            busyWait(MOVE_STEP_DELAY_MS);
        }

        updatePositionOnEDT(targetX, targetY, boxState);
    }


    /** Animação visual de armazenamento + Espera restante. */
    private void simularAcessoArmazem() {
         System.out.println("  [Thread " + this.getId() + "] ACESSOU armazém. Simulando armazenamento ("+ this.tempoArmazenamento + "s total)...");

         if (myBox != null) myBox.setVisible(false);

         long animationStepMs = STORAGE_ANIMATION_DURATION_MS / 2;

         SwingUtilities.invokeLater(() -> { if(empacotadorObj != null) empacotadorObj.setAnimationFrame(1); });
         busyWait(animationStepMs);
         if (Thread.currentThread().isInterrupted()) return;

         SwingUtilities.invokeLater(() -> { if(empacotadorObj != null) empacotadorObj.setAnimationFrame(0); });
         busyWait(animationStepMs);
         if (Thread.currentThread().isInterrupted()) return;

         long totalStorageMs = this.tempoArmazenamento * 1000;
         long remainingWaitMs = totalStorageMs - STORAGE_ANIMATION_DURATION_MS;

         if (remainingWaitMs > 0) {
             System.out.println("  [Thread " + this.getId() + "] Animação visual concluída, esperando restante: " + remainingWaitMs + "ms");
             busyWait(remainingWaitMs);
         } else {
              System.out.println("  [Thread " + this.getId() + "] Tempo armazenamento <= duração animação visual, espera adicional pulada.");
         }

         SwingUtilities.invokeLater(() -> {
            if(empacotadorObj != null) empacotadorObj.setAnimationFrame(0);
         });
    }

    @Override
    public void run() {
        if (Thread.currentThread().isInterrupted()) return;
        System.out.println("Empacotador (Thread " + this.getId() + ") iniciado com tempo de empacotamento: " + this.tempoEmpacotamento + "s. Spawn em (" + this.spawnX + ", " + this.spawnY + ")");

        while (!Thread.currentThread().isInterrupted()) {

            // 1. Trabalha
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

                        // 3. Move para o armazém
                        int targetX = targetWarehouse.getObjetoGrafico().getX() + 10;
                        int targetY = targetWarehouse.getObjetoGrafico().getY() + 10;
                        moveTo(targetX, targetY, Box.State.ABOVE);
                         if (Thread.currentThread().isInterrupted()) break;

                        // 4. Simula acesso
                        simularAcessoArmazem();
                         if (Thread.currentThread().isInterrupted()) break;

                        // 5. Libera pacote
                        pacotesProntos.release();
                        System.out.println(">>> PACOTE ARMAZENADO (Thread " + this.getId() + "). Total: " + pacotesProntos.availablePermits());
                        stored = true;

                        // 6. Retorna INSTANTANEAMENTE ao spawn
                        System.out.println("Empacotador (Thread " + this.getId() + ") retornando instantaneamente ao spawn.");
                        SwingUtilities.invokeLater(() -> {
                            if (empacotadorObj != null && painel != null) {
                                empacotadorObj.setLocation(spawnX, spawnY);
                                painel.repaint();
                            }
                        });


                    } else {
                        System.out.println("Empacotador (Thread " + this.getId() + ") Armazém CHEIO ( >= " + capacidadeMaximaM + "). Liberando mutex e esperando.");
                    }
                    // ----- FIM DA SEÇÃO CRÍTICA -----

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
                        break; // Sai do loop interno
                    }
                }
            } // Fim do loop interno de tentativa de armazenamento

            if (Thread.currentThread().isInterrupted()) break;

        } // Fim do loop principal while(!interrupted)

        System.out.println("Empacotador (Thread " + this.getId() + ") terminando.");
        // Remove da tela ao terminar (por interrupção)
         SwingUtilities.invokeLater(() -> {
            if(painel != null) {
                if(empacotadorObj != null) painel.removerObjetoParaDesenhar(empacotadorObj);
                if(myBox != null && myBox.getObjetoGrafico() != null) painel.removerObjetoParaDesenhar(myBox.getObjetoGrafico());
            }
        });
    } // Fim do método run()
} // Fim da classe ThreadEmpacotador