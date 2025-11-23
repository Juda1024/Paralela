import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.imageio.ImageIO;

public class ImagenesHilosFinal {

    public static void main(String[] args) {
        try {
            // --- 1. CONFIGURACIÓN ---
            File carpetaEntrada = new File("dataset_browser");
            File carpetaSalida = new File("imagenes_grises_pool");

            if (!carpetaSalida.exists()) carpetaSalida.mkdirs();
            
            if (!carpetaEntrada.exists()) {
                System.out.println("Error: No existe la carpeta de entrada.");
                return;
            }

            File[] archivos = carpetaEntrada.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
                       lower.endsWith(".png") || lower.endsWith(".bmp");
            });

            if (archivos == null || archivos.length == 0) return;

            // --- 2. PREPARACIÓN DE LA COLA (POOL) ---
            Queue<File> colaDeArchivos = new ConcurrentLinkedQueue<>(Arrays.asList(archivos));

            int nucleos = Runtime.getRuntime().availableProcessors();
            
            System.out.println("=================================================");
            System.out.println(" INICIANDO PROCESAMIENTO");
            System.out.println(" Hilos Trabajadores: " + nucleos);
            System.out.println(" Imágenes a procesar: " + archivos.length);
            System.out.println("=================================================\n");

            long tiempoInicioTotal = System.nanoTime();

            // --- 3. CREACIÓN DE HILOS (CON NOMBRE PERSONALIZADO) ---
            Thread[] trabajadores = new Thread[nucleos];

            for (int i = 0; i < nucleos; i++) {
                // (i + 1) es para que empiece en "Hilo-1" y no "Hilo-0".
                trabajadores[i] = new Thread(new Trabajador(colaDeArchivos, carpetaSalida), "Hilo-" + (i + 1));
                
                trabajadores[i].start();
            }

            // --- 4. ESPERA (JOIN) ---
            for (Thread t : trabajadores) {
                t.join();
            }

            long tiempoFinTotal = System.nanoTime();
            long duracionTotal = (tiempoFinTotal - tiempoInicioTotal) / 1_000_000;

            System.out.println("\n=================================================");
            System.out.println(" PROCESO FINALIZADO");
            System.out.println(" Tiempo Total: " + duracionTotal + " ms");
            System.out.println("=================================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- CLASE INTERNA: EL TRABAJADOR ---
    static class Trabajador implements Runnable {
        private Queue<File> cola;
        private File carpetaSalida;

        public Trabajador(Queue<File> cola, File carpetaSalida) {
            this.cola = cola;
            this.carpetaSalida = carpetaSalida;
        }

        @Override
        public void run() {
            while (!cola.isEmpty()) {
                File archivo = cola.poll(); 

                if (archivo != null) {
                    try {
                        long inicio = System.nanoTime();
                        
                        BufferedImage imagen = ImageIO.read(archivo);
                        if (imagen == null) continue;

                        // Llamada a tu clase FiltroGris
                        FiltroGris miFiltro = new FiltroGris(imagen, 0, imagen.getHeight());
                        miFiltro.run(); 

                        String nombreSalida = archivo.getName().replace(".", "-gris.");
                        File destino = new File(carpetaSalida, nombreSalida);
                        ImageIO.write(imagen, "jpg", destino);

                        long fin = System.nanoTime();
                        
                        System.out.println("[" + Thread.currentThread().getName() + "] Procesó: " + archivo.getName() + 
                                           " (" + (fin - inicio) / 1_000_000 + "ms)");

                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            }
        }
    }
}