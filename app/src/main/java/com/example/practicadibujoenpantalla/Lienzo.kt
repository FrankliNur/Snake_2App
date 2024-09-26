package com.example.practicadibujoenpantalla

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.os.Handler
import android.os.Looper

class Lienzo(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    // Pintura y coordenadas del cuadrado negro
    private val paintNegro = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    // Pintura y coordenadas del cuadrado rojo
    private val paintRojo = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private var velocidad = 20f
    private val cuadradoSize = 100f

    // Lista para manejar múltiples cuadrados negros (como la serpiente)
    private val cuadradosNegros = mutableListOf<Pair<Float, Float>>()

    // Coordenadas para el cuadrado rojo
    private var cuadradoRojoX = 0f
    private var cuadradoRojoY = 0f
    private val cuadradoRojoSize = 100f

    private var ultimaDireccion = "derecha"
    private var juegoActivo = true  // Variable para controlar el estado del juego

    // Handler para gestionar el movimiento de la serpiente
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            if (juegoActivo) {
                moverCuadrados()
                detectarColision()
                detectarColisionConUnoMismo()  // Verificar colisión con sí mismo
                invalidate()
                handler.postDelayed(this, 50)  // Repite cada 50 ms si el juego está activo
            }
        }
    }

    init {
        // Iniciar la posición inicial para la cabeza de la serpiente
        cuadradosNegros.add(Pair(200f, 200f)) // Cambiado a una posición que no colisione
        cuadradosNegros.add(Pair(200f, 300f)) // Añadir un segundo cuadrado para evitar colisión inicial

        // Generar posición del cuadrado rojo de forma segura
        post {
            generarPosicionAleatoriaCuadradoRojo()
        }

        // Iniciar el movimiento de la serpiente
        handler.post(runnable)
    }

    // Método para mover todos los cuadrados
    private fun moverCuadrados() {
        // Obtener la posición de la "cabeza" de la serpiente (primer cuadrado)
        var (cuadradoX, cuadradoY) = cuadradosNegros.first()

        // Mover la cabeza según la dirección
        when (ultimaDireccion) {
            "arriba" -> cuadradoY -= velocidad
            "abajo" -> cuadradoY += velocidad
            "izquierda" -> cuadradoX -= velocidad
            "derecha" -> cuadradoX += velocidad
        }

        // Teletransportación horizontal (derecha-izquierda)
        if (cuadradoX >= width) {
            cuadradoX = 0f  // Aparece por la izquierda
        } else if (cuadradoX < 0f) {
            cuadradoX = (width - cuadradoSize)  // Aparece por la derecha
        }

        // Teletransportación vertical (arriba-abajo)
        if (cuadradoY >= height) {
            cuadradoY = 0f  // Aparece por abajo
        } else if (cuadradoY < 0f) {
            cuadradoY = (height - cuadradoSize)  // Aparece por arriba
        }

        // Actualizar la lista de cuadrados negros
        for (i in cuadradosNegros.size - 1 downTo 1) {
            cuadradosNegros[i] = cuadradosNegros[i - 1]
        }
        cuadradosNegros[0] = Pair(cuadradoX, cuadradoY)
    }

    // Método para detectar colisión entre la cabeza y el cuadrado rojo
    private fun detectarColision() {
        val (cuadradoX, cuadradoY) = cuadradosNegros.first()

        // Verificar colisión con el cuadrado rojo
        if (cuadradoX < cuadradoRojoX + cuadradoRojoSize &&
            cuadradoX + cuadradoSize > cuadradoRojoX &&
            cuadradoY < cuadradoRojoY + cuadradoRojoSize &&
            cuadradoY + cuadradoSize > cuadradoRojoY) {
            // Si hay colisión con el cuadrado rojo, añadir un nuevo cuadrado negro y mover el cuadrado rojo
            cuadradosNegros.add(cuadradosNegros.last())  // Añadir nuevo segmento
            cuadradosNegros.add(cuadradosNegros.last())
            cuadradosNegros.add(cuadradosNegros.last())
            cuadradosNegros.add(cuadradosNegros.last())
            generarPosicionAleatoriaCuadradoRojo()       // Generar nueva posición para el rojo
        }
    }

    // Método para detectar colisión con sí mismo
    private fun detectarColisionConUnoMismo() {
        val cabeza = cuadradosNegros.first()

        // Comprobar si la cabeza colisiona con cualquier otro segmento
        for (i in 1 until cuadradosNegros.size) {
            if (cabeza == cuadradosNegros[i]) {
                juegoActivo = false  // Detener el juego si colide consigo mismo
                return
            }
        }
    }

    // Método para generar coordenadas aleatorias del cuadrado rojo
    private fun generarPosicionAleatoriaCuadradoRojo() {
        // Asegurarse de que el ancho y el alto sean mayores a 0 antes de generar posiciones
        if (width > 0 && height > 0) {
            cuadradoRojoX = (0..(width - cuadradoRojoSize).toInt()).random().toFloat()
            cuadradoRojoY = (0..(height - cuadradoRojoSize).toInt()).random().toFloat()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dibujar todos los cuadrados negros (la "serpiente")
        for ((x, y) in cuadradosNegros) {
            canvas.drawRect(x, y, x + cuadradoSize, y + cuadradoSize, paintNegro)
        }

        // Dibujar el cuadrado rojo en la posición aleatoria
        canvas.drawRect(cuadradoRojoX, cuadradoRojoY, cuadradoRojoX + cuadradoRojoSize, cuadradoRojoY + cuadradoRojoSize, paintRojo)

        // Si el juego ha terminado, dibujar un mensaje de "Game Over"
        if (!juegoActivo) {
            val paintTexto = Paint().apply {
                color = Color.RED
                textSize = 100f
                style = Paint.Style.FILL
            }
            canvas.drawText("Game Over", width / 4f, height / 2f, paintTexto)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                // Cambiar la dirección según la zona tocada
                if (x < width / 4 && ultimaDireccion != "derecha") {
                    ultimaDireccion = "izquierda"
                } else if (x > 3 * width / 4 && ultimaDireccion != "izquierda") {
                    ultimaDireccion = "derecha"
                } else if (y < height / 2 && ultimaDireccion != "abajo") {
                    ultimaDireccion = "arriba"
                } else if (y >= height / 2 && ultimaDireccion != "arriba") {
                    ultimaDireccion = "abajo"
                }
            }
        }
        return true
    }

}
