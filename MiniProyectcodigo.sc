import scala.util.{Try, Success, Failure}

def procesarMensaje(mensaje: String, clave: Int,
                    cifrado: (Char, Int) => Char): Try[String] =
  Try {
    require(mensaje != null && mensaje.nonEmpty,
      "El mensaje no puede ser nulo ni vacío")
    require(clave >= -1000 && clave <= 1000,
      s"Clave inválida: $clave (rango permitido: -1000 a 1000)")
    mensaje.map(c => cifrado(c, clave))
  }

val cesar: (Char, Int) => Char = (c, k) =>
  if (c >= 'A' && c <= 'Z')
    (((c - 'A' + k) % 26 + 26) % 26 + 'A').toChar
  else
    throw new IllegalArgumentException(s"Carácter no soportado: '$c'")

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
