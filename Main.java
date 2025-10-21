// Main.java
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.concurrent.Semaphore;

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

            // --- Cria as Threads ---
            ThreadTrem tremThread = new ThreadTrem(painel, semaforoCompartilhado);
            ThreadEmpacotador empacotadorThread = new ThreadEmpacotador(painel, semaforoCompartilhado, 50);
            
            CityObject cidadeA = new CityObject(painel, 50, 400); 
            CityObject cidadeB = new CityObject(painel, 900, 400);

            // --- Registra TODOS os objetos gráficos no painel ---
            painel.adicionarObjetoParaDesenhar(tremThread.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(empacotadorThread.getObjetoGrafico());
            
            // --- NOVO: Adiciona as cidades ao painel ---
            painel.adicionarObjetoParaDesenhar(cidadeA.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(cidadeB.getObjetoGrafico());
            
            // --- Inicia as Threads ---
            tremThread.start();
            empacotadorThread.start();
        });
    }
}