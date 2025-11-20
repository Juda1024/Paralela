import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImagenesSinHilos {

    public static void main(String[] args) {
        try {
            File carpetaEntrada = new File("dataset_browser");
            File carpetaSalida = new File("Imagenes_grises_secuencial");
            
            if (!carpetaSalida.exists()) {
                carpetaSalida.mkdirs();
            }
            
            if (!carpetaEntrada.exists() || !carpetaEntrada.isDirectory()) {
                System.out.println("Error: No se encuentra la carpeta 'dataset_browser'");
                return;
            }
            
            // Obtener lista de archivos de imagen
            File[] archivos = carpetaEntrada.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
                       lower.endsWith(".png") || lower.endsWith(".bmp");
            });
            
            if (archivos == null || archivos.length == 0) {
                System.out.println("No se encontraron imagenes en la carpeta 'dataset_browser'");
                return;
            }
            
            System.out.println("Encontradas " + archivos.length + " imagenes para procesar");
            
            long inicioTotal = System.nanoTime(); // Tiempo total de procesamiento
            
            // Procesar cada imagen
            for (File archivoEntrada : archivos) {
                try {
                    System.out.println("\nProcesando: " + archivoEntrada.getName());
                    
                    BufferedImage imagen = ImageIO.read(archivoEntrada);
                    
                    if (imagen == null) {
                        System.out.println("No se pudo cargar la imagen: " + archivoEntrada.getName());
                        continue;
                    }
                    
                    // Obtener dimensiones de la imagen
                    int ancho = imagen.getWidth();
                    int alto = imagen.getHeight();
                    
                    System.out.println("Dimensiones: " + ancho + "x" + alto + " pixeles");

                    long inicio = System.nanoTime(); 
                    
                    
                    for (int y = 0; y < alto; y++) {
                        for (int x = 0; x < ancho; x++) {
                            // Obtener el valor ARGB del píxel
                            int pixel = imagen.getRGB(x, y);

                            int alpha = (pixel >> 24) & 0xff; 
                            int red = (pixel >> 16) & 0xff;   
                            int green = (pixel >> 8) & 0xff;  
                            int blue = pixel & 0xff;          
                            int gris = (red + green + blue) / 3;

    
                            int nuevoPixel = (alpha << 24) | (gris << 16) | (gris << 8) | gris;

                            // Asignar el nuevo color al píxel
                            imagen.setRGB(x, y, nuevoPixel);
                        }
                    }
                    
                    long fin = System.nanoTime(); // Tiempo final por imagen

                    // Guardar la imagen resultante
                    String nombreSalida = archivoEntrada.getName().replace(".", "-gris.");
                    File archivoSalida = new File(carpetaSalida, nombreSalida);
                    ImageIO.write(imagen, "jpg", archivoSalida);

                    System.out.println("Imagen convertida: " + archivoSalida.getName());
                    System.out.println("Tiempo de esta imagen: " + (fin - inicio) / 1_000_000 + " ms");
                    
                } catch (Exception e) {
                    System.out.println("Error procesando " + archivoEntrada.getName() + ": " + e.getMessage());
                }
            }
            
            long finTotal = System.nanoTime();
            System.out.println("\n--- PROCESAMIENTO SECUENCIAL COMPLETADO ---");
            System.out.println("Total de imagenes procesadas: " + archivos.length);
            System.out.println("Tiempo total SECUENCIAL: " + (finTotal - inicioTotal) / 1_000_000 + " ms");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}