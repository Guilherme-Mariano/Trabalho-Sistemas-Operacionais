<<<<<<< HEAD
import java.util.concurrent.Semaphore;
import javax.swing.SwingUtilities;
import java.util.Random; // Importe a classe Random
=======
// ThreadEmpacotador.java
import java.util.concurrent.Semaphore;
import javax.swing.SwingUtilities;
>>>>>>> 2ce907bba5992559b8fc15134deb373092eb7007

public class ThreadEmpacotador extends Thread {

    private ObjetoGrafico empacotadorObj;
    private Semaphore pacotesProntos;
    private PainelDeDesenho painel;
<<<<<<< HEAD
    private Random random; // Objeto para gerar números aleatórios
    private int work_time;
=======
>>>>>>> 2ce907bba5992559b8fc15134deb373092eb7007

    private final String[] framesAnimacao = {
        "/GameAsset/robot_ready.png",
        "/GameAsset/robot_hands_down.png"
    };

<<<<<<< HEAD
    // Constantes para a área de spawn
    private static final int SPAWN_Y = 600;
    private static final int SPAWN_X_INICIAL = 20;
    private static final int SPAWN_X_LARGURA = 300; // Ele vai sortear um X entre 20 e 320

    public ThreadEmpacotador(PainelDeDesenho painel, Semaphore pacotesProntos, int work_time) {
        this.painel = painel;
        this.pacotesProntos = pacotesProntos;
        this.random = new Random();
        this.work_time = work_time;

        // MUDANÇA: Sorteia uma posição X aleatória dentro da área de spawn
        int xAleatorio = SPAWN_X_INICIAL + random.nextInt(SPAWN_X_LARGURA);
        
        this.empacotadorObj = new ObjetoGrafico(xAleatorio, SPAWN_Y, 150, 150, framesAnimacao);
=======
    public ThreadEmpacotador(PainelDeDesenho painel, Semaphore pacotesProntos) {
        this.painel = painel;
        this.pacotesProntos = pacotesProntos;
        this.empacotadorObj = new ObjetoGrafico(20, 600, 150, 150, framesAnimacao);
>>>>>>> 2ce907bba5992559b8fc15134deb373092eb7007
    }
    
    public ObjetoGrafico getObjetoGrafico() {
        return this.empacotadorObj;
    }

<<<<<<< HEAD
    
    private void simularTrabalho() {
        // O número de passos de trabalho é igual ao número de frames de animação
        int totalPassos = framesAnimacao.length;

        // Itera por cada passo/frame
        for (int passo = 0; passo < totalPassos; passo++) {
            
            // 1. Atualiza a imagem para o frame de trabalho atual
=======
    private void simularTrabalhoComAnimacao() {
        int totalPassos = framesAnimacao.length;

        for (int passo = 0; passo < totalPassos; passo++) {
>>>>>>> 2ce907bba5992559b8fc15134deb373092eb7007
            final int frameAtual = passo;
            SwingUtilities.invokeLater(() -> {
                empacotadorObj.setAnimationFrame(frameAtual);
                painel.repaint();
            });

<<<<<<< HEAD
            // 2. Realiza UMA PARTE do trabalho (CPU-bound)
            // Este laço é a "pausa" que dá tempo para vermos o frame
            double soma = 0;
            for (int i = 0; i < this.work_time*100; i++) {
=======
            double soma = 0;
            for (int i = 0; i < 1000 / totalPassos; i++) {
>>>>>>> 2ce907bba5992559b8fc15134deb373092eb7007
                for (int j = 0; j < 5000; j++) {
                    soma = soma + Math.sin(i) * Math.cos(j);
                }
            }
        }
        
<<<<<<< HEAD
        // 3. DEPOIS que todo o trabalho terminou, volta ao frame inicial
=======
>>>>>>> 2ce907bba5992559b8fc15134deb373092eb7007
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setAnimationFrame(0); // Volta ao estado "pronto"
            painel.repaint();
        });
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("Empacotador trabalhando para criar um pacote...");
<<<<<<< HEAD
            
            // Chama o método de trabalho corrigido
            simularTrabalho(); 
            
            // Libera o semáforo APÓS o trabalho e a animação terminarem
=======
            simularTrabalhoComAnimacao();
            
>>>>>>> 2ce907bba5992559b8fc15134deb373092eb7007
            pacotesProntos.release();
            System.out.println(">>> PACOTE PRONTO. Itens disponíveis: " + pacotesProntos.availablePermits());
        }
    }
}