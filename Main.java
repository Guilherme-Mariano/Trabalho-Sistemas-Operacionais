<<<<<<< HEAD
// Main.java
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.concurrent.Semaphore;
=======
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.concurrent.Semaphore; // Importe a classe Semaphore
>>>>>>> 2ce907bba5992559b8fc15134deb373092eb7007

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Simulação Sincronizada com Semáforo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

<<<<<<< HEAD
=======
            // 1. Cria o painel que desenhará tudo
>>>>>>> 2ce907bba5992559b8fc15134deb373092eb7007
            PainelDeDesenho painel = new PainelDeDesenho();
            frame.add(painel);
            
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // --- COORDENAÇÃO CENTRAL ---

<<<<<<< HEAD
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
=======
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
>>>>>>> 2ce907bba5992559b8fc15134deb373092eb7007
            tremThread.start();
            empacotadorThread.start();
        });
    }
}