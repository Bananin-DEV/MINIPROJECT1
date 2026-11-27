Mini Proyecto: Higher Order Functions en Criptografía

Asignatura: Programación Funcional y Reactiva
Universidad: Universidad Técnica Particular de Loja (UTPL)
Carrera: Ingeniería Computacional
Estudiante: Mateo Valdivieso
Lenguaje: Scala

1. Objetivo

Construir un pequeño motor de cifrado en Scala que reciba la estrategia de cifrado como parámetro (una función), aplicando:

Funciones de orden superior (Higher Order Functions).
Funciones lambda como estrategias intercambiables de cifrado.
Transformación de colecciones inmutables con map.
Manejo de excepciones y validación de datos con Try.
2. Fundamento teórico
2.1 Cifrado César (desplazamiento)

Cada letra se desplaza un número fijo de posiciones en el alfabeto:

E(x, k) = (pos(x) + k) mod 26

Donde pos(x) es el índice de la letra (A = 0, B = 1, ..., Z = 25) y k es la clave entera. Para descifrar se aplica la misma función con la clave -k.

Ejemplo con "AD" y k = 1:

Letra	pos	(pos + 1) mod 26	Resultado
A	0	1	B
D	3	4	E

Resultado: "BE".

2.2 Cifrado XOR (lógico)

Se aplica la operación OR exclusiva a nivel de bits entre el código ASCII del carácter y la clave:

E(x, k) = ASCII(x) ⊕ k

En Scala se usa el operador ^. Ejemplo con la letra 's' y k = 5:

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

Es de orden superior porque recibe una función (cifrado) como parámetro. Así, la misma función sirve para César, XOR o cualquier estrategia futura que respete la firma (Char, Int) => Char.

Sus responsabilidades:

Validar el mensaje (no nulo ni vacío).
Validar la clave (rango permitido de -1000 a 1000).
Aplicar la estrategia a cada carácter con map.
Devolver un Try[String], de modo que los errores no rompen el programa y se capturan con Success / Failure.
3.2 Estrategias de cifrado (lambdas)
cesar: acepta solo letras mayúsculas A-Z. Usa ((x % 26) + 26) % 26 para que funcionen también las claves negativas (necesarias al descifrar).
xor: acepta solo ASCII imprimible (32 a 126) y usa el operador ^. Verifica además que el resultado siga en ese rango.
3.3 Manejo de errores
Situación	Mecanismo
Mensaje nulo o vacío	require dentro de Try
Clave fuera de rango	require dentro de Try
Carácter no soportado	IllegalArgumentException lanzada en la lambda
Resultado XOR no imprimible	IllegalArgumentException lanzada en la lambda
4. Código fuente

Archivo: src/main/scala/MotorCifrado.scala

scala
import scala.util.{Try, Success, Failure}

object MotorCifrado {

  // Función de orden superior: recibe la estrategia de cifrado como parámetro
  def procesarMensaje(mensaje: String, clave: Int,
                      cifrado: (Char, Int) => Char): Try[String] =
    Try {
      require(mensaje != null && mensaje.nonEmpty,
              "El mensaje no puede ser nulo ni vacío")
      require(clave >= -1000 && clave <= 1000,
              s"Clave inválida: $clave (rango permitido: -1000 a 1000)")
      mensaje.map(c => cifrado(c, clave))
    }

  // Estrategia 1: César (solo letras mayúsculas A-Z)
  val cesar: (Char, Int) => Char = (c, k) =>
    if (c >= 'A' && c <= 'Z')
      (((c - 'A' + k) % 26 + 26) % 26 + 'A').toChar
    else
      throw new IllegalArgumentException(s"Carácter no soportado: '$c'")

  // Estrategia 2: XOR (ASCII imprimible 32-126)
  val xor: (Char, Int) => Char = (c, k) => {
    if (c.toInt < 32 || c.toInt > 126)
      throw new IllegalArgumentException(s"Carácter no soportado: '$c'")
    val r = c.toInt ^ k
    if (r < 32 || r > 126)
      throw new IllegalArgumentException(s"Resultado fuera del ASCII imprimible: $r")
    r.toChar
  }

  def mostrar(titulo: String, r: Try[String]): Unit = r match {
    case Success(v) => println(s"$titulo -> $v")
    case Failure(e) => println(s"$titulo -> ERROR: ${e.getMessage}")
  }

  def main(args: Array[String]): Unit = {
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
  }
}
5. Cómo ejecutar

Con scala-cli:

bash
scala-cli run src/main/scala/MotorCifrado.scala

O con sbt:

bash
sbt run
6. Resultados
6.1 Caso César

Cifrar "FUNCIONAL" con k = 3:

Original	F	U	N	C	I	O	N	A	L
Cifrado	I	X	Q	F	L	R	Q	D	O

Resultado cifrado: IXQFLRQDO. Al descifrar "IXQFLRQDO" con k = -3 se recupera FUNCIONAL.

6.2 Caso XOR

Cifrar "scala" con k = 5:

Carácter	ASCII	XOR 5	Resultado ASCII	Cifrado
s	115	115 ⊕ 5	118	v
c	99	99 ⊕ 5	102	f
a	97	97 ⊕ 5	100	d
l	108	108 ⊕ 5	105	i
a	97	97 ⊕ 5	100	d

Resultado cifrado: vfdid. Al aplicar de nuevo la función con la misma clave k = 5 se recupera scala.

6.3 Pruebas de manejo de errores
Prueba	Resultado
Mensaje vacío	requirement failed: El mensaje no puede ser nulo ni vacío
Mensaje nulo	requirement failed: El mensaje no puede ser nulo ni vacío
Clave excesivamente grande	requirement failed: Clave inválida: 999999 (rango permitido: -1000 a 1000)
César con "HOLA 123!"	Carácter no soportado: ' '
XOR con "año"	Carácter no soportado: 'ñ'

En todos los casos el programa no se cae: el error se captura dentro de Try y se muestra de forma controlada.

6.4 Salida de consola

Pega aquí una captura o el texto de la salida real al ejecutar el programa.

[pegar salida de consola]
7. Preguntas para reflexionar

¿Por qué el método XOR es su propia función inversa con el mismo operador ^, mientras que el César requiere invertir el signo de la clave?

Porque el XOR cumple que (x ⊕ k) ⊕ k = x ⊕ (k ⊕ k) = x ⊕ 0 = x. Aplicar dos veces la misma clave cancela el efecto. El César, en cambio, es un desplazamiento: para volver a la posición original hay que desplazar en sentido contrario, es decir, usar -k.

¿Qué ventajas ofrece el uso de map sobre un bucle for tradicional en este contexto?

map es declarativo: expresa qué transformación se aplica y no cómo se recorre la colección. No necesita índices ni variables mutables, produce una nueva colección sin modificar la original (inmutabilidad), el código queda más corto y legible, y separa la lógica de transformación de la iteración, lo que facilita reutilizar y probar cada estrategia.

¿Cómo ayuda el tipado estático a prevenir errores al pasar funciones como argumentos?

El compilador exige que la función pasada tenga la firma (Char, Int) => Char. Si se pasa una función que recibe o devuelve otro tipo, el error aparece en compilación y no en ejecución. Esto garantiza que cualquier estrategia de cifrado sea compatible con procesarMensaje antes de correr el programa.

8. Conclusiones
Las funciones de orden superior permiten definir estrategias de cifrado dinámicas sin duplicar la lógica de validación y recorrido.
map sobre colecciones inmutables hace la transformación simple, segura y libre de efectos secundarios.
Try permite manejar los errores de forma elegante, devolviendo Success o Failure en lugar de interrumpir el programa.
El tipado estático de Scala asegura en compilación que las funciones pasadas como argumentos sean compatibles.
