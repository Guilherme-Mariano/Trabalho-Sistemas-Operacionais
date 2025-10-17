// ThreadEmpacotador.java
import java.util.concurrent.Semaphore;
import javax.swing.SwingUtilities;

public class ThreadEmpacotador extends Thread {

    private ObjetoGrafico empacotadorObj;
    private Semaphore pacotesProntos;
    private PainelDeDesenho painel;

    private final String[] framesAnimacao = {
        "/GameAsset/robot_ready.png",
        "/GameAsset/robot_hands_down.png"
    };

    public ThreadEmpacotador(PainelDeDesenho painel, Semaphore pacotesProntos) {
        this.painel = painel;
        this.pacotesProntos = pacotesProntos;
        this.empacotadorObj = new ObjetoGrafico(20, 600, 150, 150, framesAnimacao);
    }
    
    public ObjetoGrafico getObjetoGrafico() {
        return this.empacotadorObj;
    }

    private void simularTrabalhoComAnimacao() {
        int totalPassos = framesAnimacao.length;

        for (int passo = 0; passo < totalPassos; passo++) {
            final int frameAtual = passo;
            SwingUtilities.invokeLater(() -> {
                empacotadorObj.setAnimationFrame(frameAtual);
                painel.repaint();
            });

            double soma = 0;
            for (int i = 0; i < 1000 / totalPassos; i++) {
                for (int j = 0; j < 5000; j++) {
                    soma = soma + Math.sin(i) * Math.cos(j);
                }
            }
        }
        
        SwingUtilities.invokeLater(() -> {
            empacotadorObj.setAnimationFrame(0); // Volta ao estado "pronto"
            painel.repaint();
        });
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("Empacotador trabalhando para criar um pacote...");
            simularTrabalhoComAnimacao();
            
            pacotesProntos.release();
            System.out.println(">>> PACOTE PRONTO. Itens disponíveis: " + pacotesProntos.availablePermits());
        }
    }
}