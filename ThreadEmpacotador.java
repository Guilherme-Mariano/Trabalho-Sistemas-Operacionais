// ThreadEmpacotador.java
import java.util.concurrent.Semaphore;
import javax.swing.SwingUtilities;
import java.util.Random;

public class ThreadEmpacotador extends Thread {

    private ObjetoGrafico empacotadorObj;
    private Semaphore pacotesProntos;
    private Semaphore mutexArmazem;
    private PainelDeDesenho painel;
    private Random random;
    
    private Warehouse targetWarehouse;
    private int spawnX;
    private int spawnY;
    private int workDuration;

    private final String[] framesAnimacao = {
        "/GameAsset/robot_ready.png",
        "/GameAsset/robot_hands_down.png"
    };

    // As coordenadas aqui (Y=600) são cruciais.
    private static final int SPAWN_Y = 400;
    private static final int SPAWN_X_INICIAL = 20;
    private static final int SPAWN_X_LARGURA = 300;
    private static final int MOVING_STEPS = 1000;

    public ThreadEmpacotador(PainelDeDesenho painel, Semaphore pacotesProntos, Semaphore mutexArmazem, Warehouse targetWarehouse, int workDuration) {
        this.painel = painel;
        this.pacotesProntos = pacotesProntos;
        this.mutexArmazem = mutexArmazem;
        this.targetWarehouse = targetWarehouse;
        this.workDuration = workDuration;
        this.random = new Random();

        this.spawnX = SPAWN_X_INICIAL + random.nextInt(SPAWN_X_LARGURA);
        this.spawnY = SPAWN_Y;
        
        this.empacotadorObj = new ObjetoGrafico(this.spawnX, this.spawnY, 80, 80, framesAnimacao);
    }
    
    public ObjetoGrafico getObjetoGrafico() {
        return this.empacotadorObj;
    }

    private void simularTrabalho() {
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setAnimationFrame(1);
            painel.repaint();
        });

        for (int k = 0; k < this.workDuration; k++) {
            double soma = 0;
            for (int i = 0; i < 200; i++) { 
                for (int j = 0; j < 2000; j++) {
                    soma = soma + Math.sin(i) * Math.cos(j);
                }
            }
        }
        
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setAnimationFrame(0);
            painel.repaint();
        });
    }

    private void simularPassoDeMovimento() {
        double soma = 0;
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 1000; j++) {
                soma = soma + Math.sin(i) * Math.cos(j);
            }
        }
    }
    
    private void updatePositionOnEDT(int x, int y) {
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setLocation(x, y);
            painel.repaint();
        });
    }

    private void moveTo(int targetX, int targetY) {
        int startX = empacotadorObj.getX();
        int startY = empacotadorObj.getY();

        float deltaX = targetX - startX;
        float deltaY = targetY - startY;

        System.out.println("  [Thread " + this.getId() + "] Movendo de Y=" + startY + " para Y=" + targetY + ". DeltaY = " + deltaY);

        float stepX = deltaX / MOVING_STEPS;
        float stepY = deltaY / MOVING_STEPS;

        float currentX = startX;
        float currentY = startY;

        for (int i = 0; i < MOVING_STEPS; i++) {
            currentX += stepX;
            currentY += stepY;
            
            updatePositionOnEDT(Math.round(currentX), Math.round(currentY));
            simularPassoDeMovimento();
        }
        updatePositionOnEDT(targetX, targetY);
    }

    private void simularArmazenamento() {
        System.out.println("  [Thread " + this.getId() + "] ACESSOU. Armazenando...");
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setAnimationFrame(1);
            painel.repaint();
        });
        
        simularPassoDeMovimento();
        
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setAnimationFrame(0);
            painel.repaint();
        });
    }

    @Override
    public void run() {
        System.out.println("Empacotador (Thread " + this.getId() + ") spawnou em (" + this.spawnX + ", " + this.spawnY + ")");
        
        // 1. Trabalha (na posição de spawn)
        System.out.println("Empacotador (Thread " + this.getId() + ") trabalhando...");
        simularTrabalho(); 
        
        // 2. Move-se até o armazém
        System.out.println("Empacotador (Thread " + this.getId() + ") movendo para o armazém...");
        int targetX = targetWarehouse.getObjetoGrafico().getX() + 10; 
        int targetY = targetWarehouse.getObjetoGrafico().getY() + 10; // Y=400 + 100 = 500
        moveTo(targetX, targetY);

        // 3. Tenta acessar o armazém (lógica de mutex)
        try {
            System.out.println("Empacotador (Thread " + this.getId() + ") na fila do armazém...");
            mutexArmazem.acquire(); 

            simularArmazenamento();
            pacotesProntos.release();
            System.out.println(">>> PACOTE PRONTO (Thread " + this.getId() + "). Total de caixas: " + pacotesProntos.availablePermits());

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutexArmazem.release(); 
            System.out.println("Empacotador (Thread " + this.getId() + ") saiu do armazém.");
        }

        // 4. ADICIONADO: Move-se de volta para a posição de spawn
        System.out.println("Empacotador (Thread " + this.getId() + ") voltando ao ponto de spawn...");
        moveTo(this.spawnX, this.spawnY); // Y=500 -> Y=600

        // 5. Se descarta (remove da tela)
        System.out.println("Empacotador (Thread " + this.getId() + ") descartado.");
        SwingUtilities.invokeLater(() -> {
            painel.removerObjetoParaDesenhar(this.empacotadorObj);
        });
    }
}