import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

public class ImagenesEficientes {

    public static void main(String[] args) {
        try {
            // --- 1. CONFIGURACIÓN DE RUTAS ---
            File carpetaEntrada = new File("dataset_browser");
            File carpetaSalida = new File("imagenes_grises_optimizado");

            if (!carpetaSalida.exists()) carpetaSalida.mkdirs();

            if (!carpetaEntrada.exists() || !carpetaEntrada.isDirectory()) {
                System.out.println("Error: No existe la carpeta de entrada.");
                return;
            }

            File[] archivos = carpetaEntrada.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
                       lower.endsWith(".png") || lower.endsWith(".bmp");
            });

            if (archivos == null || archivos.length == 0) return;

            // --- 2. OPTIMIZACIÓN: DETECCIÓN DE NÚCLEOS ---
            // Preguntamos al sistema cuántos hilos puede manejar realmente
            int nucleos = Runtime.getRuntime().availableProcessors();
            
            System.out.println("=================================================");
            System.out.println(" INICIANDO PROCESAMIENTO OPTIMIZADO");
            System.out.println(" CPU Núcleos detectados: " + nucleos);
            System.out.println(" Imágenes a procesar: " + archivos.length);
            System.out.println(" Estrategia: Paralelismo por archivo + Acceso directo a memoria");
            System.out.println("=================================================\n");

            long tiempoInicioTotal = System.nanoTime();

            // --- 3. OPTIMIZACIÓN: THREAD POOL (Pool de Hilos) ---
            // Creamos un administrador de hilos que reutiliza los trabajadores
            ExecutorService pool = Executors.newFixedThreadPool(nucleos);

            // --- 4. OPTIMIZACIÓN: PARALELISMO A NIVEL DE IMAGEN ---
            // Enviamos cada imagen como una tarea independiente al pool
            for (File archivo : archivos) {
                pool.submit(new TareaImagen(archivo, carpetaSalida));
            }

            // Ordenamos el cierre del pool (no acepta más tareas, pero termina las actuales)
            pool.shutdown();

            // Esperamos a que todas las tareas terminen (timeout de 1 hora por seguridad)
            try {
                if (!pool.awaitTermination(1, TimeUnit.HOURS)) {
                    System.err.println("El proceso tardó demasiado y fue forzado a detenerse.");
                    pool.shutdownNow();
                }
            } catch (InterruptedException ex) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
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

    // --- CLASE INTERNA PARA PROCESAR CADA IMAGEN ---
    static class TareaImagen implements Runnable {
        private File archivoEntrada;
        private File carpetaSalida;

        public TareaImagen(File archivoEntrada, File carpetaSalida) {
            this.archivoEntrada = archivoEntrada;
            this.carpetaSalida = carpetaSalida;
        }

        @Override
        public void run() {
            try {
                BufferedImage imagenOriginal = ImageIO.read(archivoEntrada);
                if (imagenOriginal == null) return;

                // --- 5. OPTIMIZACIÓN: ACCESO DIRECTO A MEMORIA ---
                BufferedImage imagenRapida = new BufferedImage(
                    imagenOriginal.getWidth(), 
                    imagenOriginal.getHeight(), 
                    BufferedImage.TYPE_INT_RGB
                );

                // CORRECCIÓN AQUÍ: Cambié 'g' por 'graficos'
                Graphics graficos = imagenRapida.getGraphics();
                graficos.drawImage(imagenOriginal, 0, 0, null);
                graficos.dispose();

                // Obtenemos el array de píxeles directamente
                int[] pixeles = ((DataBufferInt) imagenRapida.getRaster().getDataBuffer()).getData();

                // Procesamos el array
                for (int i = 0; i < pixeles.length; i++) {
                    int p = pixeles[i];
                    
                    // Ahora 'g' ya no da error porque 'graficos' tiene otro nombre
                    int r = (p >> 16) & 0xff;
                    int g = (p >> 8) & 0xff; 
                    int b = p & 0xff;

                    // Fórmula de luminosidad
                    int gris = (int)(r * 0.299 + g * 0.587 + b * 0.114);

                    // Reconstruimos el píxel
                    pixeles[i] = (0xff << 24) | (gris << 16) | (gris << 8) | gris;
                }

                // Guardamos
                String nombreSalida = archivoEntrada.getName();
                File destino = new File(carpetaSalida, "opt_" + nombreSalida);
                ImageIO.write(imagenRapida, "jpg", destino);

            } catch (Exception e) {
                System.err.println("Error en " + archivoEntrada.getName() + ": " + e.getMessage());
            }
        
        
        }
    }
}