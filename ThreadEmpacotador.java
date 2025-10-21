// ThreadEmpacotador.java
import java.util.concurrent.Semaphore;
import javax.swing.SwingUtilities;
import java.util.Random;

public class ThreadEmpacotador extends Thread {

    private ObjetoGrafico empacotadorObj;
    private Semaphore pacotesProntos;
    private PainelDeDesenho painel;
    private Random random;
    
    private Warehouse targetWarehouse; // O armazém para onde o robô deve ir
    private int spawnX; // Posição X inicial (aleatória)
    private int spawnY; // Posição Y inicial (fixa)
    private int workDuration; // Parâmetro de tempo de trabalho

    private final String[] framesAnimacao = {
        "/GameAsset/robot_ready.png",
        "/GameAsset/robot_hands_down.png"
    };

    private static final int SPAWN_Y = 600;
    private static final int SPAWN_X_INICIAL = 20;
    private static final int SPAWN_X_LARGURA = 300;
    private static final int MOVING_STEPS = 100; // Passos para a animação de movimento

    public ThreadEmpacotador(PainelDeDesenho painel, Semaphore pacotesProntos, Warehouse targetWarehouse, int workDuration) {
        this.painel = painel;
        this.pacotesProntos = pacotesProntos;
        this.targetWarehouse = targetWarehouse; // Armazena o alvo
        this.workDuration = workDuration; // Armazena o tempo de trabalho
        this.random = new Random();

        this.spawnX = SPAWN_X_INICIAL + random.nextInt(SPAWN_X_LARGURA);
        this.spawnY = SPAWN_Y;
        
        this.empacotadorObj = new ObjetoGrafico(this.spawnX, this.spawnY, 150, 150, framesAnimacao);
    }
    
    public ObjetoGrafico getObjetoGrafico() {
        return this.empacotadorObj;
    }

    /** Lógica de animação e trabalho. */
    private void simularTrabalho() {
        // Frame 0 = ready, Frame 1 = hands_down
        
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setAnimationFrame(1); // Frame 1 = hands_down
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
            empacotadorObj.setAnimationFrame(0); // Frame 0 = ready
            painel.repaint();
        });
    }

    /** Simula um timer CPU-bound para a animação de movimento. */
    private void simularPassoDeMovimento() {
        double soma = 0;
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 1000; j++) {
                soma = soma + Math.sin(i) * Math.cos(j);
            }
        }
    }
    
    /** Atualiza a posição do objeto na thread do Swing. */
    private void updatePositionOnEDT(int x, int y) {
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setLocation(x, y);
            painel.repaint();
        });
    }

    /** Anima o movimento do robô de um ponto a outro. */
    private void moveTo(int targetX, int targetY) {
        int startX = empacotadorObj.getX();
        int startY = empacotadorObj.getY();

        float deltaX = targetX - startX;
        float deltaY = targetY - startY;

        // Adicionando logs para depuração
        System.out.println("  [Thread " + this.getId() + "] Movendo de (" + startX + ", " + startY + ") para (" + targetX + ", " + targetY + ")");
        System.out.println("  [Thread " + this.getId() + "] DeltaX: " + deltaX + ", DeltaY: " + deltaY);

        // CORREÇÃO: O passo é a distância total (delta) dividida pelo número de passos (MOVING_STEPS)
        float stepX = deltaX / MOVING_STEPS;
        float stepY = deltaY / MOVING_STEPS;

        System.out.println("  [Thread " + this.getId() + "] StepX: " + stepX + ", StepY: " + stepY);

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

    /** O run() agora executa uma vez e a thread morre. */
    @Override
    public void run() {
        System.out.println("Empacotador (Thread " + this.getId() + ") spawnou em (" + this.spawnX + ", " + this.spawnY + ")");
        
        // 1. Trabalha (na posição de spawn)
        System.out.println("Empacotador (Thread " + this.getId() + ") trabalhando...");
        simularTrabalho(); 
        
        // 2. Move-se até o armazém
        System.out.println("Empacotador (Thread " + this.getId() + ") movendo para o armazém...");
        
        // CORREÇÃO: Usa o targetWarehouse para obter as coordenadas dinamicamente
        int targetX = targetWarehouse.getObjetoGrafico().getX(); 
        int targetY = targetWarehouse.getObjetoGrafico().getY();
        
        moveTo(targetX, targetY);

        // 3. Libera o semáforo para o trem
        pacotesProntos.release();
        System.out.println(">>> PACOTE PRONTO (Thread " + this.getId() + "). Itens: " + pacotesProntos.availablePermits());

        // 4. Se descarta (remove da tela)
        System.out.println("Empacotador (Thread " + this.getId() + ") descartado.");
        SwingUtilities.invokeLater(() -> {
            painel.removerObjetoParaDesenhar(this.empacotadorObj);
        });
    }
}