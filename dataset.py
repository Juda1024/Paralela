import os
import requests
import random
import time

# Configuramos los headers para parecer un navegador real evitando bloqueos
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
}

def obtener_url_picsum(w, h, seed):
    return f"https://picsum.photos/{w}/{h}?random={seed}"

def obtener_url_loremflickr(w, h, seed):
    # LoremFlickr usa palabras clave para variar, usamos 'all' o 'technology,nature'
    return f"https://loremflickr.com/{w}/{h}/all?lock={seed}"

def descargar_dataset_multiserver(cantidad_objetivo=100, carpeta_salida="dataset_imagenes"):
    if not os.path.exists(carpeta_salida):
        os.makedirs(carpeta_salida)

    resoluciones_base = [
        (1280, 720),   # HD
        (1920, 1080),  # Full HD
        (2560, 1440),  # 2K
        (3840, 2160)   # 4K
    ]

    # Fuentes disponibles
    fuentes = ["Picsum", "LoremFlickr"]

    archivos_existentes = len([name for name in os.listdir(carpeta_salida) if name.endswith('.jpg')])
    descargadas_exitosas = archivos_existentes

    print(f"--- INICIANDO DESCARGA MULTI-SERVIDOR ---")
    print(f"Imagenes actuales: {archivos_existentes}. Meta: {cantidad_objetivo}")
    print("---")

    while descargadas_exitosas < cantidad_objetivo:
        try:
            # Configuración aleatoria de imagen
            ancho, alto = random.choice(resoluciones_base)
            if random.choice([True, False]): 
                w_final, h_final = alto, ancho # Vertical
            else: 
                w_final, h_final = ancho, alto # Horizontal

            seed = random.randint(1, 100000)
            
            # Selección de servidor aleatorio para balancear carga
            servidor_actual = random.choice(fuentes)
            
            if servidor_actual == "Picsum":
                url = obtener_url_picsum(w_final, h_final, seed)
            else:
                url = obtener_url_loremflickr(w_final, h_final, seed)

            # Timeout extendido a 30s para resoluciones 4K
            respuesta = requests.get(url, headers=HEADERS, stream=True, timeout=30)
            
            # LoremFlickr a veces redirige, requests lo maneja, pero verificamos la URL final
            if respuesta.status_code == 200:
                numero_img = descargadas_exitosas + 1
                nombre_archivo = f"img_{numero_img:03d}_{w_final}x{h_final}.jpg"
                ruta_completa = os.path.join(carpeta_salida, nombre_archivo)

                with open(ruta_completa, 'wb') as f:
                    for chunk in respuesta.iter_content(1024):
                        f.write(chunk)
                
                descargadas_exitosas += 1
                print(f"[OK] ({servidor_actual}) {w_final}x{h_final} - Total: {descargadas_exitosas}")
                
                # Pausa pequeña
                time.sleep(1)
            else:
                print(f"[!] {servidor_actual} ocupado ({respuesta.status_code}). Probando otro...")
                time.sleep(1)

        except Exception as e:
            print(f"[REINTENTO] Timeout o error de red. Cambiando servidor...")
            time.sleep(2)

    print("---")
    print(f"Proceso terminado. Tienes {descargadas_exitosas} imagenes.")

if __name__ == "__main__":
    descargar_dataset_multiserver()