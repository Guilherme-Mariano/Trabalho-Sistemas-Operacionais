// Main.java
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.concurrent.Semaphore;
import java.util.Random;

public class Main {

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
            Semaphore semaforoCaixasProntas = new Semaphore(0);
            Semaphore mutexArmazemA = new Semaphore(1); 

            // 1. Cria os objetos estáticos PRIMEIRO
            // As coordenadas aqui (Y=400) são cruciais.
            CityObject cidadeA = new CityObject(painel, 50, 480); 
            Warehouse armazemA = new Warehouse(painel, 50, 350); // Posição do armazém A
            CityObject cidadeB = new CityObject(painel, 900, 480);
            Warehouse armazemB = new Warehouse(painel, 900, 350);

            // 2. Cria a thread do Trem
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
                    while (true) {
                        int tempoDeTrabalho = 50; 
                        
                        // Passa todos os 5 argumentos
                        ThreadEmpacotador novoEmpacotador = new ThreadEmpacotador(
                            painel,                  
                            semaforoCaixasProntas,   
                            mutexArmazemA,           
                            armazemA,                
                            tempoDeTrabalho          
                        );
                        
                        painel.adicionarObjetoParaDesenhar(novoEmpacotador.getObjetoGrafico());
                        novoEmpacotador.start();

                        int delaySpawn = 2000 + spawnerRandom.nextInt(3000);
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