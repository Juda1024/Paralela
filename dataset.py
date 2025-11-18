import os
import time
import requests
import random
import glob
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from webdriver_manager.chrome import ChromeDriverManager

# --- CONFIGURACIÓN ---
META_TOTAL = 100
CARPETA_SALIDA = "dataset_browser"

TEMAS_VARIADOS = [
    "tropical beach", "colorful abstract", 
    "underwater coral","street art graffiti", "hot air balloons", "festival colors", "vibrant flowers", "neon car", "technology lights",
    "sunset mountains", "stained glass", "macro eye", "liquid color"
]

RESOLUCIONES = {
    "4K": 3840,
    "2K": 2560,
    "FHD": 1920,
    "HD": 1280
}

def iniciar_navegador():
    options = webdriver.ChromeOptions()
    options.add_argument("--start-maximized")
    options.add_argument("--disable-notifications")
    # options.add_argument("--headless") # Descomenta si no quieres ver el navegador
    driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)
    return driver

def completar_dataset():
    if not os.path.exists(CARPETA_SALIDA):
        os.makedirs(CARPETA_SALIDA)
    
    # 1. CONTAR LO QUE YA EXISTE
    # Buscamos todos los archivos .jpg en la carpeta
    archivos_existentes = glob.glob(os.path.join(CARPETA_SALIDA, "*.jpg"))
    cantidad_actual = len(archivos_existentes)
    
    faltantes = META_TOTAL - cantidad_actual
    
    print(f"--- ESTADO ACTUAL ---")
    print(f"Tienes: {cantidad_actual} imágenes.")
    print(f"Meta:   {META_TOTAL} imágenes.")
    
    if faltantes <= 0:
        print("¡Felicidades! Ya tienes el dataset completo (o más). No hace falta descargar nada.")
        return

    print(f"--> Faltan {faltantes} imágenes. Iniciando búsqueda para completarlas...")
    print("---------------------")

    driver = iniciar_navegador()
    urls_encontradas = set()
    
    temas_por_usar = TEMAS_VARIADOS.copy()
    random.shuffle(temas_por_usar)
    
    # 2. BUCLE DE BÚSQUEDA (Solo hasta encontrar los enlaces FALTANTES)
    while len(urls_encontradas) < faltantes:
        
        if not temas_por_usar:
            temas_por_usar = TEMAS_VARIADOS.copy()
            random.shuffle(temas_por_usar)
        
        tema_actual = temas_por_usar.pop()
        print(f"Buscando '{tema_actual}'... (Necesitamos {faltantes - len(urls_encontradas)} más)")
        
        try:
            driver.get(f"https://unsplash.com/s/photos/{tema_actual.replace(' ', '-')}")
            time.sleep(3) 

            # Scroll para ver más fotos
            for _ in range(6):
                driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
                time.sleep(1)
            
            imagenes = driver.find_elements(By.CSS_SELECTOR, "img.I7OuT")
            if not imagenes:
                imagenes = driver.find_elements(By.TAG_NAME, "img")

            for img in imagenes:
                src = img.get_attribute("src")
                if src and "images.unsplash.com" in src and "profile" not in src:
                    base_url = src.split("?")[0]
                    urls_encontradas.add(base_url)
                    
                    # Si ya tenemos suficientes enlaces para llenar el hueco, paramos
                    if len(urls_encontradas) >= faltantes:
                        break
            
        except Exception as e:
            print(f"Error menor buscando: {e}")

    driver.quit()
    print(f"--- LINKS RECOLECTADOS. DESCARGANDO {len(urls_encontradas)} FOTOS ---")

    # 3. DESCARGA CONTINUADA
    # Empezamos a numerar desde donde nos quedamos (ej. si hay 56, la primera será la 57)
    indice_inicial = cantidad_actual + 1
    lista_urls = list(urls_encontradas)
    random.shuffle(lista_urls) # Mezclar para variedad

    for i, url_base in enumerate(lista_urls):
        try:
            numero_actual = indice_inicial + i
            
            # Si por alguna razón nos pasamos, paramos
            if numero_actual > META_TOTAL:
                break

            nombre_res, ancho = random.choice(list(RESOLUCIONES.items()))
            url_final = f"{url_base}?w={ancho}&q=85&fm=jpg&fit=max"
            
            print(f"Guardando img_{numero_actual:03d} ({nombre_res})...")
            
            respuesta = requests.get(url_final, timeout=10)
            
            if respuesta.status_code == 200:
                nombre_archivo = f"img_{numero_actual:03d}_{nombre_res}.jpg"
                ruta = os.path.join(CARPETA_SALIDA, nombre_archivo)
                
                with open(ruta, 'wb') as f:
                    f.write(respuesta.content)
            
            time.sleep(0.2)
            
        except Exception as e:
            print(f"Error descargando una imagen: {e}")

    print("---")
    total_final = len(glob.glob(os.path.join(CARPETA_SALIDA, "*.jpg")))
    print(f"¡Proceso finalizado! Ahora tienes {total_final} imágenes en total.")

if __name__ == "__main__":
    completar_dataset()