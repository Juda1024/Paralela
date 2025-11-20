import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

<<<<<<< HEAD
/**
 * Procesamiento Concurrente de un Dataset completo.
 * Aplica la lógica de hilos (FiltroGris) a cada imagen de la carpeta.
 */
=======

>>>>>>> 3a1eef2fc2fe95430e5850a1fcc6711cb0cd8ea1
public class ImagenesHilos {

    public static void main(String[] args) {
        try {
            // --- 1. CONFIGURACIÓN DE CARPETAS (Igual que en el secuencial) ---
            File carpetaEntrada = new File("dataset_browser");
            File carpetaSalida = new File("imagenes_grises_concurrente"); // Nombre solicitado en el PDF

            // Crear carpeta de salida si no existe
            if (!carpetaSalida.exists()) {
                carpetaSalida.mkdirs();
            }

            // Validar entrada
            if (!carpetaEntrada.exists() || !carpetaEntrada.isDirectory()) {
                System.out.println("Error: No se encuentra la carpeta 'dataset_browser'");
                return;
            }

            // Obtener imágenes
            File[] archivos = carpetaEntrada.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
                       lower.endsWith(".png") || lower.endsWith(".bmp");
            });

            if (archivos == null || archivos.length == 0) {
                System.out.println("No hay imágenes para procesar.");
                return;
            }

            System.out.println("Iniciando procesamiento CONCURRENTE de " + archivos.length + " imágenes.");
            System.out.println("Estrategia: 4 Hilos por cada imagen (Paralelismo de Datos).");

            long inicioTotal = System.nanoTime(); // Cronómetro global

            // --- 2. BUCLE PARA RECORRER EL DATASET ---
            for (File archivoEntrada : archivos) {
                try {
                    BufferedImage imagen = ImageIO.read(archivoEntrada);
                    
                    if (imagen == null) continue;

                    int altura = imagen.getHeight();
                    // int ancho = imagen.getWidth(); // No es crítico para dividir filas

                    // --- 3. LÓGICA CONCURRENTE (Tu código original adaptado) ---
                    int numeroHilos = 4; 
                    Thread[] hilos = new Thread[numeroHilos];
                    int filasPorHilo = altura / numeroHilos;
                    
                    // Creamos y lanzamos los hilos para ESTA imagen
                    for (int i = 0; i < numeroHilos; i++) {
                        int inicioFila = i * filasPorHilo;
                        int finFila = (i == numeroHilos - 1) ? altura : inicioFila + filasPorHilo;

                        // Usamos tu clase FiltroGris existente
                        hilos[i] = new Thread(new FiltroGris(imagen, inicioFila, finFila));
                        hilos[i].start();
                    }

                    // Esperamos a que los 4 hilos terminen esta imagen (Barrera)
                    for (Thread hilo : hilos) {
                        hilo.join();
                    }

                    // --- 4. GUARDADO DE LA IMAGEN PROCESADA ---
                    String nombreSalida = archivoEntrada.getName().replace(".", "-gris-conc.");
                    File archivoDestino = new File(carpetaSalida, nombreSalida);
                    
                    // Guardamos (esto se hace en el hilo principal secuencialmente, el disco es el límite)
                    ImageIO.write(imagen, "jpg", archivoDestino);
                    
                    // Opcional: Imprimir progreso para no desesperar
                    // System.out.println("Procesada: " + archivoDestino.getName());

                } catch (Exception e) {
                    System.out.println("Error en archivo " + archivoEntrada.getName() + ": " + e.getMessage());
                }
            }

            long finTotal = System.nanoTime();
            
            // --- 5. REPORTE FINAL ---
            System.out.println("\n--- PROCESAMIENTO CONCURRENTE COMPLETADO ---");
            System.out.println("Total imágenes: " + archivos.length);
            System.out.println("Tiempo Total: " + (finTotal - inicioTotal) / 1_000_000 + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}