import java.awt.image.BufferedImage;

/**
 *
 * @author andrespillajo
 */
public class FiltroGris implements Runnable{
    private final BufferedImage imagen;
    private final int inicioFila;
    private final int finFila;
    
    public FiltroGris(BufferedImage imagen, int inicioFila, int finFila) {
        this.imagen = imagen;
        this.inicioFila = inicioFila;
        this.finFila = finFila;
    }
    
    @Override
    public void run() {
        for (int y = inicioFila; y < finFila; y++) {
            for (int x = 0; x < imagen.getWidth(); x++) {
                int pixel = imagen.getRGB(x, y);

                // Operaciones de bits para obtener componentes de color.
                int rojo = (pixel >> 16) & 0xff; // Desplazar 16 bits y obtener los últimos 8 bits.
                int verde = (pixel >> 8) & 0xff; // Desplazar 8 bits y obtener los últimos 8 bits.
                int azul = pixel & 0xff;        // Obtiene los últimos 8 bits.

                // Calcular promedio para escala de grises
                int gris = (rojo + verde + azul) / 3;

                // Nuevo color en escala de grises
                int nuevoPixel = (gris << 16) | (gris << 8) | gris;

                imagen.setRGB(x, y, nuevoPixel);
            }
        }
    }
    
}