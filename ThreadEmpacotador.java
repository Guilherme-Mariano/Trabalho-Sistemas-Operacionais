// ThreadEmpacotador.java
import java.util.concurrent.Semaphore;
import javax.swing.SwingUtilities;
import java.util.Random;

public class ThreadEmpacotador extends Thread {

    private ObjetoGrafico empacotadorObj;
    private Box myBox; 
    private Semaphore pacotesProntos;
    private Semaphore mutexArmazem;
    private PainelDeDesenho painel;
    private Random random;
    
    private Warehouse targetWarehouse;
    private int spawnX;
    private int spawnY;
    private int workDuration; // Tempo em segundos

    private final String[] framesAnimacao = {
        "/GameAsset/robot_ready.png",
        "/GameAsset/robot_hands_down.png"
    };

    // Suas configurações
    private static final int SPAWN_Y = 430; 
    private static final int SPAWN_X_INICIAL = 20;
    private static final int SPAWN_X_LARGURA = 300;
    private static final int MOVING_DURATION_SECONDS = 5; // Duração da viagem em segundos
    private static final int WORK_ANIMATION_STEP_MS = 100; // Delay da animação de trabalho

    public ThreadEmpacotador(PainelDeDesenho painel, Semaphore pacotesProntos, Semaphore mutexArmazem, Warehouse targetWarehouse, int workDuration) {
        this.painel = painel;
        this.pacotesProntos = pacotesProntos;
        this.mutexArmazem = mutexArmazem;
        this.targetWarehouse = targetWarehouse;
        this.workDuration = workDuration; // Tempo em segundos
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
        long startTime = System.currentTimeMillis();
        long endTime = startTime + milliseconds;
        while (System.currentTimeMillis() < endTime) {
            Thread.onSpinWait(); 
            if (Thread.currentThread().isInterrupted()) {
                System.err.println("ThreadEmpacotador " + getId() + " interrompido durante busyWait.");
                return; 
            }
        }
    }

    /** Lógica de animação e trabalho com passos mais curtos. */
    private void simularTrabalho() {
        myBox.setVisible(true);
        SwingUtilities.invokeLater(() -> myBox.updatePosition(Box.State.BELOW));

        long totalWorkMs = this.workDuration * 1000;
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

    /** Anima o movimento por um tempo fixo, usando busyWait para o pacing visual. */
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

    /** Simula o acesso ao armazém COM animação e espera curta (DENTRO do mutex). */
    private void simularAcessoArmazem() {
         System.out.println("  [Thread " + this.getId() + "] ACESSOU armazém. Simulando armazenamento...");
         
         SwingUtilities.invokeLater(() -> empacotadorObj.setAnimationFrame(1)); // Hands down
         busyWait(250); // Metade do tempo
         if (Thread.currentThread().isInterrupted()) return; // Verifica interrupção
         SwingUtilities.invokeLater(() -> empacotadorObj.setAnimationFrame(0)); // Ready
         busyWait(250); // Outra metade do tempo
    }

    /** O run() com a ordem: Trabalha -> Acquire Mutex -> Move -> Simula Acesso -> Release Pacote -> Desaparece -> Release Mutex */
    @Override
    public void run() {
        if (Thread.currentThread().isInterrupted()) return; 
        System.out.println("Empacotador (Thread " + this.getId() + ") spawnou em (" + this.spawnX + ", " + this.spawnY + ")");
        
        // 1. Trabalha
        System.out.println("Empacotador (Thread " + this.getId() + ") trabalhando...");
        simularTrabalho(); 
        if (Thread.currentThread().isInterrupted()) return; 

        // 2. Tenta adquirir o mutex ANTES de se mover
        try {
            System.out.println("Empacotador (Thread " + this.getId() + ") esperando pelo mutex do armazém...");
            mutexArmazem.acquire(); 
             if (Thread.currentThread().isInterrupted()) {
                 // Se interrompido enquanto esperava, não precisa liberar o mutex
                 System.err.println("ThreadEmpacotador " + getId() + " interrompido enquanto esperava pelo mutex.");
                 return; 
             }
             
            // ----- INÍCIO DA SEÇÃO CRÍTICA -----
            System.out.println("Empacotador (Thread " + this.getId() + ") adquiriu mutex. Movendo para o armazém...");

            // 3. Move-se até o armazém (com caixa acima)
            int targetX = targetWarehouse.getObjetoGrafico().getX() + 10; 
            int targetY = targetWarehouse.getObjetoGrafico().getY() + 10;
            moveTo(targetX, targetY, Box.State.ABOVE); 
             // Verifica interrupção após mover
             if (Thread.currentThread().isInterrupted()) {
                 // Libera o mutex antes de sair se interrompido aqui
                 if (mutexArmazem.availablePermits() == 0) mutexArmazem.release(); 
                 return;
             }

            // 4. Simula o acesso/armazenamento DENTRO do mutex ANIMADO
            simularAcessoArmazem();
             // Verifica interrupção após simular acesso
            if (Thread.currentThread().isInterrupted()) {
                 if (mutexArmazem.availablePermits() == 0) mutexArmazem.release(); 
                 return;
            }

            // 5. Libera o semáforo para o trem APÓS armazenar
            pacotesProntos.release();
            System.out.println(">>> PACOTE PRONTO (Thread " + this.getId() + "). Total de caixas: " + pacotesProntos.availablePermits());

            // 6. Desaparece IMEDIATAMENTE após liberar o pacote ALTERAR
            System.out.println("Empacotador (Thread " + this.getId() + ") armazenou e desapareceu.");
            SwingUtilities.invokeLater(() -> {
                empacotadorObj.setVisible(false);
                myBox.setVisible(false); 
                painel.repaint(); 
            });
            
            // ----- FIM DA SEÇÃO CRÍTICA -----

        } catch (InterruptedException e) {
             Thread.currentThread().interrupt(); 
             System.err.println("ThreadEmpacotador " + getId() + " interrompido durante seção crítica.");
             // O finally cuidará de liberar o mutex
        } finally {
            // Garante que o mutex seja liberado, verificando se foi adquirido
            if (mutexArmazem.availablePermits() == 0) { 
                 mutexArmazem.release(); 
                 System.out.println("Empacotador (Thread " + this.getId() + ") liberou mutex e saiu do armazém.");
            } else {
                 System.out.println("Empacotador (Thread " + this.getId() + ") terminou sem precisar liberar mutex (provavelmente interrompido antes de acquire).");
            }
        }
        System.out.println("Empacotador (Thread " + this.getId() + ") terminou.");
    }
}