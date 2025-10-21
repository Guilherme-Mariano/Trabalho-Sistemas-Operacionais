// ThreadEmpacotador.java
import java.util.concurrent.Semaphore;
import javax.swing.SwingUtilities;
import java.util.Random;

public class ThreadEmpacotador extends Thread {

    private ObjetoGrafico empacotadorObj;
    private Semaphore pacotesProntos;   // Semáforo de contagem
    private Semaphore mutexArmazem;     // Semáforo de acesso exclusivo
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

    private static final int SPAWN_Y = 600;
    private static final int SPAWN_X_INICIAL = 20;
    private static final int SPAWN_X_LARGURA = 300;
    private static final int MOVING_STEPS = 100;

    // REQUISITO: Construtor aceitando os 5 parâmetros
    public ThreadEmpacotador(PainelDeDesenho painel, Semaphore pacotesProntos, Semaphore mutexArmazem, Warehouse targetWarehouse, int workDuration) {
        this.painel = painel;
        this.pacotesProntos = pacotesProntos;
        this.mutexArmazem = mutexArmazem; // Armazena o mutex
        this.targetWarehouse = targetWarehouse;
        this.workDuration = workDuration;
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

    /** REQUISITO: Simula o ato de armazenar (protegido pelo mutex). */
    private void simularArmazenamento() {
        System.out.println("  [Thread " + this.getId() + "] ACESSOU. Armazenando...");
        // Mostra a animação de "armazenando"
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setAnimationFrame(1); // Frame 1 = hands_down
            painel.repaint();
        });
        
        // Simula um pequeno tempo de trabalho para o ato de armazenar
        simularPassoDeMovimento(); // Reutiliza o timer de movimento
        
        // Volta para a animação "pronto"
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setAnimationFrame(0); // Frame 0 = ready
            painel.repaint();
        });
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
        int targetX = targetWarehouse.getObjetoGrafico().getX() + 20; 
        int targetY = targetWarehouse.getObjetoGrafico().getY() + 100;
        moveTo(targetX, targetY);

        // 3. REQUISITO: Tenta acessar o armazém (lógica de mutex)
        try {
            System.out.println("Empacotador (Thread " + this.getId() + ") na fila do armazém...");
            mutexArmazem.acquire(); // Tenta pegar o "cadeado" do armazém

            // ----- INÍCIO DA SEÇÃO CRÍTICA -----
            // Apenas uma thread por vez pode executar este bloco
            
            simularArmazenamento();
            
            // 4. Libera o semáforo para o trem (adiciona +1 caixa)
            pacotesProntos.release();
            System.out.println(">>> PACOTE PRONTO (Thread " + this.getId() + "). Total de caixas: " + pacotesProntos.availablePermits());
            
            // ----- FIM DA SEÇÃO CRÍTICA -----

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutexArmazem.release(); // Libera o "cadeado" para o próximo
            System.out.println("Empacotador (Thread " + this.getId() + ") saiu do armazém.");
        }

        // 5. Se descarta (remove da tela)
        System.out.println("Empacotador (Thread " + this.getId() + ") descartado.");
        SwingUtilities.invokeLater(() -> {
            painel.removerObjetoParaDesenhar(this.empacotadorObj);
        });
        // A thread morre aqui
    }
}