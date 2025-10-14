import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.concurrent.Semaphore; // Importe a classe Semaphore

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Simulação Sincronizada com Semáforo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // 1. Cria o painel que desenhará tudo
            PainelDeDesenho painel = new PainelDeDesenho();
            frame.add(painel);
            
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // --- COORDENAÇÃO CENTRAL ---

            // 2. Crie UMA ÚNICA instância do semáforo aqui.
            // O valor '0' significa que ele começa sem permissões. O trem será forçado
            // a esperar até que o empacotador libere a primeira permissão.
            Semaphore semaforoCompartilhado = new Semaphore(0);

            // 3. Crie as threads, injetando o MESMO semáforo em ambas.
            ThreadTrem tremThread = new ThreadTrem(painel, semaforoCompartilhado);
            ThreadEmpacotador empacotadorThread = new ThreadEmpacotador(painel, semaforoCompartilhado);
            
            // 4. Adicione os objetos gráficos de cada thread ao painel
            painel.adicionarObjetoParaDesenhar(tremThread.getObjetoGrafico());
            painel.adicionarObjetoParaDesenhar(empacotadorThread.getObjetoGrafico());
            
            // 5. Inicie as threads. Elas agora se comunicarão através do semáforo.
            tremThread.start();
            empacotadorThread.start();
        });
    }
}