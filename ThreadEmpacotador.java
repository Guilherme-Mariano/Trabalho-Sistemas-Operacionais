// ThreadEmpacotador.java
import java.util.concurrent.Semaphore;
import javax.swing.SwingUtilities;
import java.util.Random;

public class ThreadEmpacotador extends Thread {

    private ObjetoGrafico empacotadorObj;
    private Box myBox; // O objeto Box pertencente a este empacotador
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

    // Suas configurações de coordenadas e tamanho
    private static final int SPAWN_Y = 430; 
    private static final int SPAWN_X_INICIAL = 20;
    private static final int SPAWN_X_LARGURA = 300;
    private static final int MOVING_STEPS = 1000; // Seu número de passos

    public ThreadEmpacotador(PainelDeDesenho painel, Semaphore pacotesProntos, Semaphore mutexArmazem, Warehouse targetWarehouse, int workDuration) {
        this.painel = painel;
        this.pacotesProntos = pacotesProntos;
        this.mutexArmazem = mutexArmazem;
        this.targetWarehouse = targetWarehouse;
        this.workDuration = workDuration;
        this.random = new Random();

        this.spawnX = SPAWN_X_INICIAL + random.nextInt(SPAWN_X_LARGURA);
        this.spawnY = SPAWN_Y;
        
        // Seu tamanho (80x80)
        this.empacotadorObj = new ObjetoGrafico(this.spawnX, this.spawnY, 80, 80, framesAnimacao); 
        
        // Cria a caixa associada
        this.myBox = new Box(painel, this.empacotadorObj);
    }
    
    public ObjetoGrafico getObjetoGrafico() {
        return this.empacotadorObj;
    }
    
    // Getter para a caixa
    public Box getBox() {
        return this.myBox;
    }

    /** Lógica de animação e trabalho, mostrando a caixa abaixo. */
    private void simularTrabalho() {
        myBox.setVisible(true);
        SwingUtilities.invokeLater(() -> myBox.updatePosition(Box.State.BELOW));

        for (int k = 0; k < this.workDuration; k++) {
            final int frameAtual = k % 2; 
            
            SwingUtilities.invokeLater(() -> {
                empacotadorObj.setAnimationFrame(frameAtual);
                myBox.updatePosition(Box.State.BELOW); 
                painel.repaint();
            });

            // Simula passo de trabalho
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

    /** Simula um timer CPU-bound para a animação de movimento. */
    private void simularPassoDeMovimento() {
        double soma = 0;
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 1000; j++) {
                soma = soma + Math.sin(i) * Math.cos(j);
            }
        }
    }
    
    /** Atualiza a posição do robô E da caixa na thread do Swing. */
    private void updatePositionOnEDT(int x, int y, Box.State boxState) {
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setLocation(x, y);
            myBox.updatePosition(boxState); 
            painel.repaint();
        });
    }

    /** Anima o movimento do robô e da caixa juntos. */
    private void moveTo(int targetX, int targetY, Box.State boxState) {
        int startX = empacotadorObj.getX();
        int startY = empacotadorObj.getY();
        float deltaX = targetX - startX;
        float deltaY = targetY - startY;
        float stepX = deltaX / MOVING_STEPS;
        float stepY = deltaY / MOVING_STEPS;
        float currentX = startX;
        float currentY = startY;

        System.out.println("  [Thread " + this.getId() + "] Movendo de Y=" + startY + " para Y=" + targetY + ". DeltaY = " + deltaY);

        myBox.setVisible(true); 
        SwingUtilities.invokeLater(() -> myBox.updatePosition(boxState));

        for (int i = 0; i < MOVING_STEPS; i++) {
            currentX += stepX;
            currentY += stepY;
            updatePositionOnEDT(Math.round(currentX), Math.round(currentY), boxState);
            simularPassoDeMovimento();
        }
        updatePositionOnEDT(targetX, targetY, boxState); 
    }

    // O método simularArmazenamento não é mais necessário visualmente,
    // mas a lógica do mutex ainda precisa de um pequeno delay
    private void simularAcessoArmazem() {
         System.out.println("  [Thread " + this.getId() + "] ACESSOU armazém (logicamente).");
         // Pequena simulação de tempo para representar o acesso
         simularPassoDeMovimento(); 
    }


    /** O run() agora executa uma vez, desaparece ao chegar, e termina. */
    @Override
    public void run() {
        System.out.println("Empacotador (Thread " + this.getId() + ") spawnou em (" + this.spawnX + ", " + this.spawnY + ")");
        
        // 1. Trabalha (com a caixa visível abaixo)
        System.out.println("Empacotador (Thread " + this.getId() + ") trabalhando...");
        simularTrabalho(); 
        
        
        
        // 4. Lógica de Sincronização (acontece "invisivelmente" agora)
        try {
            System.out.println("Empacotador (Thread " + this.getId() + ") na fila do armazém (invisível)...");
            mutexArmazem.acquire(); 

            // Simula o tempo de acesso/armazenamento (sem animação visual)
            simularAcessoArmazem();
            
            // 2. Move-se até o armazém (com a caixa visível acima)
        System.out.println("Empacotador (Thread " + this.getId() + ") movendo para o armazém...");
        int targetX = targetWarehouse.getObjetoGrafico().getX() + 10; // Sua coordenada X
        int targetY = targetWarehouse.getObjetoGrafico().getY() + 10; // Sua coordenada Y
        // Faz mas sentido após mutex
        moveTo(targetX, targetY, Box.State.ABOVE); // Move com a caixa ACIMA

        // ---- MUDANÇA PRINCIPAL ----
        // 3. Chegou ao armazém -> Desaparece IMEDIATAMENTE
        System.out.println("Empacotador (Thread " + this.getId() + ") chegou e desapareceu.");
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setVisible(false);
            myBox.setVisible(false); // Esconde a caixa também
            painel.repaint(); // Garante que desapareçam visualmente
        });


            // Libera o semáforo para o trem
            pacotesProntos.release();
            System.out.println(">>> PACOTE PRONTO (Thread " + this.getId() + "). Total de caixas: " + pacotesProntos.availablePermits());

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutexArmazem.release(); 
            System.out.println("Empacotador (Thread " + this.getId() + ") saiu do armazém (invisível).");
        }

        // 5. REMOVIDO: Não há mais viagem de volta.

        // 6. REMOVIDO: Não precisa remover do painel, pois já está invisível.
        // A thread termina aqui naturalmente.
        System.out.println("Empacotador (Thread " + this.getId() + ") terminou.");
    }
}