// Main.java
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.concurrent.Semaphore;
import java.util.Random;

public class Main {

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

            Semaphore semaforoCompartilhado = new Semaphore(0);

            // 1. Cria os objetos estáticos PRIMEIRO
            CityObject cidadeA = new CityObject(painel, 50, 480); 
            Warehouse armazemA = new Warehouse(painel, 50, 400); // Posição do armazém A
            CityObject cidadeB = new CityObject(painel, 900, 480);
            Warehouse armazemB = new Warehouse(painel, 900, 400);

            // 2. Cria a thread do Trem
            ThreadTrem tremThread = new ThreadTrem(painel, semaforoCompartilhado);
            
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
                    while (true) {
                        int tempoDeTrabalho = 50; 
                        
                        // Garante que o construtor correto (4 argumentos) está sendo chamado
                        ThreadEmpacotador novoEmpacotador = new ThreadEmpacotador(
                            painel,                  // 1. O painel
                            semaforoCompartilhado,   // 2. O semáforo
                            armazemA,                // 3. O armazém de destino
                            tempoDeTrabalho          // 4. O tempo de trabalho
                        );
                        
                        painel.adicionarObjetoParaDesenhar(novoEmpacotador.getObjetoGrafico());
                        novoEmpacotador.start();

                        int delaySpawn = 10000 + spawnerRandom.nextInt(10000);
                        Thread.sleep(delaySpawn);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            // 5. Inicia as threads principais
            tremThread.start();
            empacotadorSpawner.start();
        });
    }
}