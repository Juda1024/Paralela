import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author andrespillajo
 */
public class ImagenesSinHilos {

    public static void main(String[] args) {
        try {
            // Cargar la imagen desde un archivo
            File archivoEntrada = new File("C:\\Users\\Hewlett Packard\\Desktop\\Carpeta General\\ESPE U\\4 Nivel\\Comp_Paralela\\Escala_Grises\\SinHilos\\Goats-maximili.jpg"); // Cambia "imagen.png" por la ruta de tu imagen
            BufferedImage imagen = ImageIO.read(archivoEntrada);
            
            if (imagen == null) {
                System.out.println("No se pudo cargar la imagen. Verifica la ruta y el formato.");
                return;
            }
            
            // Obtener dimensiones de la imagen
            int ancho = imagen.getWidth();
            int alto = imagen.getHeight();
            
            System.out.println("Procesando imagen de " + ancho + "x" + alto + " píxeles.");

            long inicio = System.nanoTime(); // Registrar tiempo inicial
            
            // Recorrer cada píxel de la imagen
            for (int y = 0; y < alto; y++) {
                for (int x = 0; x < ancho; x++) {
                    // Obtener el valor ARGB del píxel
                    int pixel = imagen.getRGB(x, y);

                    // Extraer componentes de color
                    int alpha = (pixel >> 24) & 0xff; // Componente Alpha
                    int red = (pixel >> 16) & 0xff;   // Componente Rojo
                    int green = (pixel >> 8) & 0xff;  // Componente Verde
                    int blue = pixel & 0xff;          // Componente Azul

                    // Calcular el promedio para escala de grises
                    int gris = (red + green + blue) / 3;

                    // Crear el nuevo color en escala de grises
                    int nuevoPixel = (alpha << 24) | (gris << 16) | (gris << 8) | gris;

                    // Asignar el nuevo color al píxel
                    imagen.setRGB(x, y, nuevoPixel);
                }
            }
            
            long fin = System.nanoTime(); // Registrar tiempo final

            // Guardar la imagen resultante
            File archivoSalida = new File("C:\\\\Users\\\\Hewlett Packard\\\\Desktop\\\\Carpeta General\\\\ESPE U\\\\4 Nivel\\\\Comp_Paralela\\\\Escala_Grises\\\\SinHilos\\\\Goats-maximili-gris.jpg"); // Cambia "imagen_gris.png" por la ruta de tu imagen
            ImageIO.write(imagen, "jpg", archivoSalida);

            System.out.println("Imagen convertida a escala de grises y guardada como 'imagen_gris.jpg'.");
            System.out.println("Tiempo de ejecución: " + (fin - inicio) / 1_000_000 + " ms");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}