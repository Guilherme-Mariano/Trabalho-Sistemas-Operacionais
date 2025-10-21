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
            
            // --- MUDANÇA: Cria Cidades E Armazéns independentes ---
            // Você pode ajustar as coordenadas para que fiquem um ao lado do outro
            // ou sobrepostos como preferir.
            
            // Estação A (Cidade + Armazém)
            CityObject cidadeA = new CityObject(painel, 50, 480); 
            Warehouse armazemA = new Warehouse(painel, 50, 400); // Um pouco deslocado

            // Estação B (Cidade + Armazém)
            CityObject cidadeB = new CityObject(painel, 900, 480);
            Warehouse armazemB = new Warehouse(painel, 900, 400); // Um pouco deslocado

            // --- Registra TODOS os objetos gráficos no painel ---
            painel.adicionarObjetoParaDesenhar(tremThread.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(empacotadorThread.getObjetoGrafico());
            
            // Adiciona Cidades e Armazéns ao painel
            // A ordem importa: desenhar a cidade primeiro faz ela ficar "atrás"
            painel.adicionarObjetoParaDesenhar(cidadeA.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(armazemA.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(cidadeB.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(armazemB.getObjetoGrafico());
            
            // --- Inicia as Threads ---
            tremThread.start();
            empacotadorThread.start();
        });
    }
}