🔐 Mini Proyecto: Higher Order Functions en Criptografía
	
Estudiante	Mateo Valdivieso
Carrera	Ingeniería Computacional
Universidad	Universidad Técnica Particular de Loja (UTPL)
Asignatura	Programación Funcional y Reactiva
Lenguaje	Scala
Entorno	IntelliJ IDEA (Scala Worksheet)
📑 Contenido
Objetivo
Fundamento teórico
Diseño de la solución
Estructura del repositorio
Cómo ejecutar en IntelliJ IDEA
Código fuente
Resultados
Preguntas de reflexión
Limitaciones y mejoras
Conclusiones
1. Objetivo

Construir un motor de cifrado en Scala que reciba la estrategia de cifrado como parámetro (una función), aplicando:

Funciones de orden superior (Higher Order Functions).
Funciones lambda como estrategias de cifrado intercambiables.
Transformación de colecciones inmutables con map.
Manejo de excepciones y validación de datos con Try.
2. Fundamento teórico
2.1 Cifrado César (desplazamiento)

Cada letra se desplaza un número fijo de posiciones en el alfabeto:

E(x, k) = (pos(x) + k) mod 26

pos(x) es el índice de la letra (A = 0, B = 1, ..., Z = 25) y k es la clave entera. Para descifrar se aplica la misma función con la clave -k.

Ejemplo con "AD" y k = 1:

Letra	pos	(pos + 1) mod 26	Resultado
A	0	1	B
D	3	4	E

Resultado: "BE".

2.2 Cifrado XOR (lógico)

Se aplica la operación OR exclusiva a nivel de bits entre el código ASCII del carácter y la clave:

E(x, k) = ASCII(x) ⊕ k

En Scala se usa el operador ^. Ejemplo con 's' y k = 5:

Dato	Decimal	Binario
's'	115	0111 0011
k	5	0000 0101
115 ^ 5	118	0111 0110

El carácter ASCII 118 es 'v'.

3. Diseño de la solución
3.1 Función de orden superior
scala
def procesarMensaje(mensaje: String, clave: Int,
                    cifrado: (Char, Int) => Char): Try[String]

Es de orden superior porque recibe una función (cifrado) como parámetro. La misma función sirve para César, XOR o cualquier estrategia futura con la firma (Char, Int) => Char.

Responsabilidades:

Validar el mensaje (no nulo ni vacío).
Validar la clave (rango de -1000 a 1000).
Aplicar la estrategia a cada carácter con map.
Devolver un Try[String] (Success o Failure), de modo que los errores se capturan sin interrumpir el programa.
3.2 Estrategias de cifrado (lambdas)
Estrategia	Caracteres aceptados	Detalle
cesar	Letras mayúsculas A-Z	Usa ((x % 26) + 26) % 26 para que las claves negativas funcionen al descifrar
xor	ASCII imprimible (32 a 126)	Usa el operador ^ y verifica que el resultado siga siendo imprimible
3.3 Manejo de errores
Situación	Mecanismo
Mensaje nulo o vacío	require dentro de Try
Clave fuera de rango	require dentro de Try
Carácter no soportado	IllegalArgumentException lanzada en la lambda
Resultado XOR no imprimible	IllegalArgumentException lanzada en la lambda
4. Estructura del repositorio
mini-proyecto-cifrado/
├── README.md
└── Cifrado.sc      ← Scala Worksheet con todo el código
5. Cómo ejecutar en IntelliJ IDEA

Requisitos: IntelliJ IDEA con el plugin Scala instalado (File > Settings > Plugins > Marketplace > Scala) y un JDK 11 o superior.

Clona el repositorio:
bash
git clone https://github.com/<tu-usuario>/mini-proyecto-cifrado.git
Abre la carpeta del repositorio en IntelliJ IDEA (File > Open).
Abre el archivo Cifrado.sc.
Pulsa el botón verde ▶ Run Worksheet de la barra superior del editor.
Los resultados aparecen en el panel derecho del worksheet.

Nota: si el panel solo muestra líneas del tipo // defined ..., cambia el modo del worksheet a Plain y ejecuta de nuevo.

6. Código fuente

Archivo: Cifrado.sc

scala
import scala.util.{Try, Success, Failure}

// Función de orden superior: recibe la estrategia de cifrado como parámetro
def procesarMensaje(mensaje: String, clave: Int,
                    cifrado: (Char, Int) => Char): Try[String] =
  Try {
    require(mensaje != null && mensaje.nonEmpty,
            "El mensaje no puede ser nulo ni vacío")
    require(clave >= -1000 && clave <= 1000,
            s"Clave inválida: $clave (rango permitido: -1000 a 1000)")
    mensaje.map(c => cifrado(c, clave))   // map sobre colección inmutable
  }

// Estrategia 1: César (solo letras mayúsculas A-Z)
val cesar: (Char, Int) => Char = (c, k) =>
  if (c >= 'A' && c <= 'Z')
    (((c - 'A' + k) % 26 + 26) % 26 + 'A').toChar   // +26 para claves negativas
  else
    throw new IllegalArgumentException(s"Carácter no soportado: '$c'")

// Estrategia 2: XOR (ASCII imprimible 32-126)
val xor: (Char, Int) => Char = (c, k) => {
  if (c.toInt < 32 || c.toInt > 126)
    throw new IllegalArgumentException(s"Carácter no soportado: '$c'")
  val r = c.toInt ^ k                               // operador ^
  if (r < 32 || r > 126)
    throw new IllegalArgumentException(s"Resultado fuera del ASCII imprimible: $r")
  r.toChar
}

// Muestra el resultado según sea Success o Failure
def mostrar(titulo: String, r: Try[String]): Unit = r match {
  case Success(v) => println(s"$titulo -> $v")
  case Failure(e) => println(s"$titulo -> ERROR: ${e.getMessage}")
}

// Caso César
val c1 = procesarMensaje("FUNCIONAL", 3, cesar)
mostrar("César cifrar   ", c1)
mostrar("César descifrar", c1.flatMap(procesarMensaje(_, -3, cesar)))

// Caso XOR
val x1 = procesarMensaje("scala", 5, xor)
mostrar("XOR cifrar     ", x1)
mostrar("XOR descifrar  ", x1.flatMap(procesarMensaje(_, 5, xor)))

// Pruebas de error
mostrar("Mensaje vacío  ", procesarMensaje("", 3, cesar))
mostrar("Mensaje nulo   ", procesarMensaje(null, 3, cesar))
mostrar("Clave enorme   ", procesarMensaje("HOLA", 999999, cesar))
mostrar("Carácter raro  ", procesarMensaje("HOLA 123!", 3, cesar))
mostrar("XOR carácter ñ ", procesarMensaje("año", 5, xor))
7. Resultados
7.1 Caso César

Cifrar "FUNCIONAL" con k = 3:

Original	F	U	N	C	I	O	N	A	L
Cifrado	I	X	Q	F	L	R	Q	D	O
Texto cifrado: IXQFLRQDO
Al descifrar con k = -3 se recupera: FUNCIONAL
7.2 Caso XOR

Cifrar "scala" con k = 5:

Carácter	ASCII	Operación	Resultado ASCII	Cifrado
s	115	115 ⊕ 5	118	v
c	99	99 ⊕ 5	102	f
a	97	97 ⊕ 5	100	d
l	108	108 ⊕ 5	105	i
a	97	97 ⊕ 5	100	d
Texto cifrado: vfdid
Al aplicar de nuevo la función con k = 5 se recupera: scala
7.3 Pruebas de manejo de errores
Prueba	Resultado capturado
Mensaje vacío	requirement failed: El mensaje no puede ser nulo ni vacío
Mensaje nulo	requirement failed: El mensaje no puede ser nulo ni vacío
Clave excesivamente grande (999999)	requirement failed: Clave inválida: 999999 (rango permitido: -1000 a 1000)
César con "HOLA 123!"	Carácter no soportado: ' '
XOR con "año"	Carácter no soportado: 'ñ'

En todos los casos el programa no se interrumpe: el error se captura dentro de Try y se muestra de forma controlada.

7.4 Salida de consola
César cifrar    -> IXQFLRQDO
César descifrar -> FUNCIONAL
XOR cifrar      -> vfdid
XOR descifrar   -> scala
Mensaje vacío   -> ERROR: requirement failed: El mensaje no puede ser nulo ni vacío
Mensaje nulo    -> ERROR: requirement failed: El mensaje no puede ser nulo ni vacío
Clave enorme    -> ERROR: requirement failed: Clave inválida: 999999 (rango permitido: -1000 a 1000)
Carácter raro   -> ERROR: Carácter no soportado: ' '
XOR carácter ñ  -> ERROR: Carácter no soportado: 'ñ'

📸 Captura de la ejecución en IntelliJ IDEA:

![Ejecución en IntelliJ](img/ejecucion.png)

8. Preguntas de reflexión

¿Por qué el método XOR es su propia función inversa con el mismo operador ^, mientras que el César requiere invertir el signo de la clave?

Porque el XOR cumple que (x ⊕ k) ⊕ k = x ⊕ (k ⊕ k) = x ⊕ 0 = x: aplicar dos veces la misma clave cancela el efecto. El César, en cambio, es un desplazamiento: para volver a la posición original hay que desplazar en sentido contrario, es decir, usar -k.

¿Qué ventajas ofrece el uso de map sobre un bucle for tradicional en este contexto?

map es declarativo: expresa qué transformación se aplica y no cómo se recorre la colección. No necesita índices ni variables mutables, produce una nueva colección sin modificar la original (inmutabilidad), el código es más corto y legible, y separa la lógica de transformación de la iteración, lo que facilita reutilizar y probar cada estrategia.

¿Cómo ayuda el tipado estático a prevenir errores al pasar funciones como argumentos?

El compilador exige que la función pasada tenga la firma (Char, Int) => Char. Si se pasa una función que recibe o devuelve otros tipos, el error aparece en compilación y no en ejecución, lo que garantiza que cualquier estrategia sea compatible con procesarMensaje antes de correr el programa.

9. Limitaciones y mejoras
El César solo admite mayúsculas A-Z; podría extenderse a minúsculas y a la ñ.
El XOR solo admite ASCII imprimible, para que el texto cifrado se pueda mostrar; con otro enfoque se podría trabajar con bytes.
Estos cifrados son didácticos: no son seguros para proteger información real.
Se podría agregar una estrategia nueva (por ejemplo, Vigenère) sin modificar procesarMensaje, que es justamente la ventaja de recibir la función como parámetro.
10. Conclusiones
Las funciones de orden superior permiten definir estrategias de cifrado dinámicas sin duplicar la lógica de validación y recorrido.
map sobre colecciones inmutables vuelve la transformación simple, segura y libre de efectos secundarios.
Try permite manejar los errores de forma elegante, devolviendo Success o Failure en lugar de detener el programa.
El tipado estático de Scala asegura, en compilación, que las funciones pasadas como argumentos sean compatibles.

Autor: BananinDEV · UTPL · Ingeniería Computacional
