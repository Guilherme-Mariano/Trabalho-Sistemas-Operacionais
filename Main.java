// Main.java
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.concurrent.Semaphore;
import java.util.Random;

public class Main {

    // REQUISITO: O trem só pode sair com N = 30 caixas.
    private static final int N_CAIXAS_PARA_PARTIDA = 30;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Simulação Sincronizada com Semáforo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            PainelDeDesenho painel = new PainelDeDesenho();
            frame.add(painel);
            
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // --- COORDENAÇÃO CENTRAL ---

            // REQUISITO: Semáforo para contar as caixas prontas (o trem espera por N=30)
            Semaphore semaforoCaixasProntas = new Semaphore(0);

            // REQUISITO: Semáforo mutex (de acesso exclusivo) para a área de armazenamento
            // Começa com 1 permissão, significando "disponível".
            Semaphore mutexArmazemA = new Semaphore(1); 

            // 1. Cria os objetos estáticos PRIMEIRO
            CityObject cidadeA = new CityObject(painel, 50, 480); 
            Warehouse armazemA = new Warehouse(painel, 50, 400); // Posição do armazém A
            CityObject cidadeB = new CityObject(painel, 900, 480);
            Warehouse armazemB = new Warehouse(painel, 900, 400);

            // 2. Cria a thread do Trem
            // Passa o semáforo de caixas e o número N necessário
            ThreadTrem tremThread = new ThreadTrem(painel, semaforoCaixasProntas, N_CAIXAS_PARA_PARTIDA);
            
            // 3. Registra os objetos estáticos e o trem
            painel.adicionarObjetoParaDesenhar(tremThread.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(cidadeA.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(armazemA.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(cidadeB.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(armazemB.getObjetoGrafico());
            
            // 4. Cria a thread "Spawner" para os empacotadores
            Thread empacotadorSpawner = new Thread(() -> {
                Random spawnerRandom = new Random();
                try {
                    // REQUISITO: Loop para criar múltiplos empacotadores
                    while (true) {
                        int tempoDeTrabalho = 50; 
                        
                        // REQUISITO: Construtor agora passa os 5 argumentos
                        ThreadEmpacotador novoEmpacotador = new ThreadEmpacotador(
                            painel,                  // 1. O painel
                            semaforoCaixasProntas,   // 2. O semáforo de contagem
                            mutexArmazemA,           // 3. O semáforo mutex
                            armazemA,                // 4. O armazém de destino
                            tempoDeTrabalho          // 5. O tempo de trabalho
                        );
                        
                        painel.adicionarObjetoParaDesenhar(novoEmpacotador.getObjetoGrafico());
                        novoEmpacotador.start();

                        // Delay de spawn mais curto (2-5 segundos) para ver múltiplos robôs
                        int delaySpawn = 2000 + spawnerRandom.nextInt(3000);
                        Thread.sleep(delaySpawn);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            // 5. Inicia as threads principais
            tremThread.start();
            empacotadorSpawner.start(); // Inicia o "Spawner"
        });
    }
}