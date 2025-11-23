import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;


public class ImagenesHilos {

    public static void main(String[] args) {
        try {
            // --- CONFIGURACIÓN DE RUTAS ---
            File carpetaEntrada = new File("dataset_browser");
            File carpetaSalida = new File("imagenes_grises_concurrente");

            if (!carpetaSalida.exists()) {
                carpetaSalida.mkdirs();
            }

            if (!carpetaEntrada.exists() || !carpetaEntrada.isDirectory()) {
                System.out.println("Error: No existe la carpeta 'dataset_browser'. Ejecuta primero el script de Python.");
                return;
            }

            // Filtrar solo imágenes
            File[] archivos = carpetaEntrada.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
                       lower.endsWith(".png") || lower.endsWith(".bmp");
            });

            if (archivos == null || archivos.length == 0) {
                System.out.println("La carpeta está vacía.");
                return;
            }

            System.out.println("=================================================");
            System.out.println(" INICIANDO PROCESAMIENTO CONCURRENTE (4 HILOS/IMG) ");
            System.out.println(" Cantidad de imágenes: " + archivos.length);
            System.out.println("=================================================\n");

            // --- CRONÓMETRO GLOBAL (Inicio) ---
            long tiempoInicioTotal = System.nanoTime();

            // Bucle principal que recorre el dataset
            for (File archivoEntrada : archivos) {
                try {
                    // --- CRONÓMETRO INDIVIDUAL (Inicio) ---
                    long tiempoInicioImagen = System.nanoTime();

                    BufferedImage imagen = ImageIO.read(archivoEntrada);
                    if (imagen == null) continue;

                    int altura = imagen.getHeight();
                    
                    // --- LOGICA CONCURRENTE ---
                    int numeroHilos = 8; // Dividimos la imagen en 4 franjas
                    Thread[] hilos = new Thread[numeroHilos];
                    int filasPorHilo = altura / numeroHilos;
                    
                    // Crear y lanzar los hilos
                    for (int i = 0; i < numeroHilos; i++) {
                        int inicioFila = i * filasPorHilo;
                        int finFila = (i == numeroHilos - 1) ? altura : inicioFila + filasPorHilo;
                
                        hilos[i] = new Thread(new FiltroGris(imagen, inicioFila, finFila));
                        hilos[i].start();
                    }

                    // Esperar a los hilos (Barrera de Sincronización)
                    for (Thread hilo : hilos) {
                        hilo.join();
                    }

                    // Guardar imagen resultante
                    String nombreSalida = archivoEntrada.getName().replace(".", "-gris-conc.");
                    File archivoDestino = new File(carpetaSalida, nombreSalida);
                    ImageIO.write(imagen, "jpg", archivoDestino);

                    // --- CRONÓMETRO INDIVIDUAL (Fin) ---
                    long tiempoFinImagen = System.nanoTime();
                    long duracionImagen = (tiempoFinImagen - tiempoInicioImagen) / 1_000_000; // a ms

                    // IMPRESIÓN INDIVIDUAL
                    System.out.println(String.format("Imagen: %-30s | Hilos: 4 | Tiempo: %4d ms", 
                            archivoEntrada.getName(), duracionImagen));

                } catch (Exception e) {
                    System.out.println("Error procesando " + archivoEntrada.getName() + ": " + e.getMessage());
                }
            }

            // --- CRONÓMETRO GLOBAL (Fin) ---
            long tiempoFinTotal = System.nanoTime();
            long duracionTotal = (tiempoFinTotal - tiempoInicioTotal) / 1_000_000; // a ms

            System.out.println("\n=================================================");
            System.out.println(" PROCESO FINALIZADO");
            System.out.println(" Tiempo Total de Ejecución: " + duracionTotal + " ms");
            System.out.println(" Imágenes guardadas en: " + carpetaSalida.getAbsolutePath());
            System.out.println("=================================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}