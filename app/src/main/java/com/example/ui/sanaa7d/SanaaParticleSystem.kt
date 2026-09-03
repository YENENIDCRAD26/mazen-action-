package com.example.ui.sanaa7d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Types of visual particles in Sana'a 7D Chase streets.
 */
enum class SanaaParticleType {
  TIRE_SMOKE,        // White/grey smoke puffs from drifting/accelerating tires
  SANAA_DUST_CLOUD,  // Sandy ochre dust clouds kicked up from ancient cobblestones & clay alleys
  NITRO_SPARK,       // Bright golden & cyan sparks during adrenaline rush & boost
  COLLISION_DEBRIS,  // Clay brick & dust flecks when scraping walls or hitting obstacles
  POLICE_SIREN_GLOW  // Subtle red/blue atmospheric flashes
}

/**
 * Individual particle model with physical properties.
 */
class SanaaParticle(
  var x: Float = 0f,
  var y: Float = 0f,
  var vx: Float = 0f,
  var vy: Float = 0f,
  var size: Float = 10f,
  var maxSize: Float = 30f,
  var alpha: Float = 1.0f,
  var initialAlpha: Float = 0.8f,
  var color: Color = Color.White,
  var type: SanaaParticleType = SanaaParticleType.TIRE_SMOKE,
  var life: Float = 1.0f, // 1.0 (new) down to 0.0 (dead)
  var decayRate: Float = 0.02f,
  var rotation: Float = 0f,
  var rotSpeed: Float = 0f
) {
  var isAlive: Boolean = true

  fun reset(
    newX: Float,
    newY: Float,
    newVx: Float,
    newVy: Float,
    newSize: Float,
    newMaxSize: Float,
    newAlpha: Float,
    newColor: Color,
    newType: SanaaParticleType,
    newDecayRate: Float,
    newRotSpeed: Float = 0f
  ) {
    x = newX
    y = newY
    vx = newVx
    vy = newVy
    size = newSize
    maxSize = newMaxSize
    alpha = newAlpha
    initialAlpha = newAlpha
    color = newColor
    type = newType
    life = 1.0f
    decayRate = newDecayRate
    rotation = Random.nextFloat() * 360f
    rotSpeed = newRotSpeed
    isAlive = true
  }

  fun update(dt: Float = 1.0f) {
    if (!isAlive) return

    // Position update with slight air resistance (drag)
    x += vx * dt
    y += vy * dt
    vx *= 0.95f
    vy *= 0.95f

    // Size expansion (smoke and dust expand as they disperse)
    if (size < maxSize) {
      size += (maxSize - size) * 0.08f * dt
    }

    // Rotation
    rotation += rotSpeed * dt

    // Lifetime & alpha falloff
    life -= decayRate * dt
    if (life <= 0f) {
      isAlive = false
      alpha = 0f
    } else {
      alpha = initialAlpha * (life * life) // Non-linear soft fade out
    }
  }
}

/**
 * State manager for the Sana'a Chase particle engine.
 * Maintains an active particle pool to avoid garbage collection pressure during 60fps renders.
 */
class SanaaParticleSystem(
  val maxParticles: Int = 140
) {
  private val pool = ArrayList<SanaaParticle>(maxParticles)
  private val activeParticles = ArrayList<SanaaParticle>(maxParticles)

  init {
    for (i in 0 until maxParticles) {
      pool.add(SanaaParticle())
    }
  }

  val particles: List<SanaaParticle>
    get() = activeParticles

  /**
   * Spawns tire smoke from car / dabab wheels when accelerating or drifting.
   */
  fun emitTireSmoke(
    centerX: Float,
    centerY: Float,
    count: Int = 3,
    intensity: Float = 1.0f,
    isDrifting: Boolean = false
  ) {
    for (i in 0 until count) {
      val particle = obtainParticle() ?: break
      val angle = Math.toRadians((Random.nextFloat() * 80.0 + (if (isDrifting) 140.0 else 50.0))).toFloat()
      val speed = (Random.nextFloat() * 4f + 2f) * intensity
      val vx = cos(angle.toDouble()).toFloat() * speed * (if (Random.nextBoolean()) 1f else -1f)
      val vy = (Random.nextFloat() * 3f + 1f) * intensity // Floats upward/backward

      val greyTone = Random.nextFloat() * 0.25f + 0.70f
      val smokeColor = Color(greyTone, greyTone, greyTone)

      particle.reset(
        newX = centerX + (Random.nextFloat() - 0.5f) * 24f,
        newY = centerY + (Random.nextFloat() - 0.5f) * 12f,
        newVx = vx,
        newVy = vy,
        newSize = (Random.nextFloat() * 8f + 10f) * intensity,
        newMaxSize = (Random.nextFloat() * 20f + 28f) * intensity,
        newAlpha = (Random.nextFloat() * 0.35f + 0.45f).coerceAtMost(0.85f),
        newColor = smokeColor,
        newType = SanaaParticleType.TIRE_SMOKE,
        newDecayRate = Random.nextFloat() * 0.025f + 0.020f,
        newRotSpeed = (Random.nextFloat() - 0.5f) * 4f
      )
      activeParticles.add(particle)
    }
  }

  /**
   * Spawns rich sandy ochre dust clouds kicked up from the ancient clay alleys of Sana'a.
   */
  fun emitSanaaDustCloud(
    centerX: Float,
    centerY: Float,
    count: Int = 3,
    isClayAlley: Boolean = true
  ) {
    for (i in 0 until count) {
      val particle = obtainParticle() ?: break
      val vx = (Random.nextFloat() - 0.5f) * 6f
      val vy = -(Random.nextFloat() * 3.5f + 1.2f) // Rising dust billow

      // Warm Sana'a ancient sandstone ochre & terracotta dust palette
      val dustColors = if (isClayAlley) {
        listOf(
          Color(0xFFE0A96D), // Warm clay sand
          Color(0xFFD4A373), // Ancient cobblestone dust
          Color(0xFFC89556), // Desert sandstone
          Color(0xFFF4A261)  // Golden sunlit dust
        )
      } else {
        listOf(
          Color(0xFFB0A8A0), // Asphalt dust
          Color(0xFFD5CEBE), // Sidewalk mortar dust
          Color(0xFFE8DCC4)  // Street silt
        )
      }
      val chosenColor = dustColors[Random.nextInt(dustColors.size)]

      particle.reset(
        newX = centerX + (Random.nextFloat() - 0.5f) * 30f,
        newY = centerY + (Random.nextFloat() - 0.5f) * 16f,
        newVx = vx,
        newVy = vy,
        newSize = Random.nextFloat() * 10f + 8f,
        newMaxSize = Random.nextFloat() * 26f + 32f,
        newAlpha = Random.nextFloat() * 0.4f + 0.45f,
        newColor = chosenColor,
        newType = SanaaParticleType.SANAA_DUST_CLOUD,
        newDecayRate = Random.nextFloat() * 0.022f + 0.018f,
        newRotSpeed = (Random.nextFloat() - 0.5f) * 3f
      )
      activeParticles.add(particle)
    }
  }

  /**
   * Spawns glowing nitro sparks during adrenaline rush or boost.
   */
  fun emitNitroSparks(
    centerX: Float,
    centerY: Float,
    count: Int = 4
  ) {
    for (i in 0 until count) {
      val particle = obtainParticle() ?: break
      val angle = Random.nextFloat() * 360f
      val speed = Random.nextFloat() * 8f + 4f
      val vx = cos(Math.toRadians(angle.toDouble())).toFloat() * speed
      val vy = sin(Math.toRadians(angle.toDouble())).toFloat() * speed + 2f

      val sparkColors = listOf(
        Color(0xFFFFD54F), // Gold spark
        Color(0xFF00E5FF), // Cyan adrenaline spark
        Color(0xFFFF9100), // Fire amber
        Color(0xFF76FF03)  // Mischief neon
      )
      val sparkColor = sparkColors[Random.nextInt(sparkColors.size)]

      particle.reset(
        newX = centerX + (Random.nextFloat() - 0.5f) * 16f,
        newY = centerY + (Random.nextFloat() - 0.5f) * 16f,
        newVx = vx,
        newVy = vy,
        newSize = Random.nextFloat() * 3f + 2.5f,
        newMaxSize = Random.nextFloat() * 5f + 4f,
        newAlpha = 0.95f,
        newColor = sparkColor,
        newType = SanaaParticleType.NITRO_SPARK,
        newDecayRate = Random.nextFloat() * 0.05f + 0.04f,
        newRotSpeed = (Random.nextFloat() - 0.5f) * 10f
      )
      activeParticles.add(particle)
    }
  }

  /**
   * Emits collision dust and clay fragments when scraping walls or hitting obstacles.
   */
  fun emitCollisionDebris(
    centerX: Float,
    centerY: Float,
    count: Int = 8
  ) {
    for (i in 0 until count) {
      val particle = obtainParticle() ?: break
      val angle = Random.nextFloat() * 360f
      val speed = Random.nextFloat() * 9f + 3f
      val vx = cos(Math.toRadians(angle.toDouble())).toFloat() * speed
      val vy = sin(Math.toRadians(angle.toDouble())).toFloat() * speed

      val debrisColor = if (Random.nextBoolean()) Color(0xFFC0392B) else Color(0xFFD4AC0D)

      particle.reset(
        newX = centerX,
        newY = centerY,
        newVx = vx,
        newVy = vy,
        newSize = Random.nextFloat() * 6f + 4f,
        newMaxSize = Random.nextFloat() * 12f + 8f,
        newAlpha = 0.9f,
        newColor = debrisColor,
        newType = SanaaParticleType.COLLISION_DEBRIS,
        newDecayRate = Random.nextFloat() * 0.045f + 0.035f,
        newRotSpeed = (Random.nextFloat() - 0.5f) * 12f
      )
      activeParticles.add(particle)
    }
  }

  /**
   * Updates all active particles and recycles dead ones back into the pool.
   */
  fun update(dt: Float = 1.0f) {
    var i = 0
    while (i < activeParticles.size) {
      val particle = activeParticles[i]
      particle.update(dt)
      if (!particle.isAlive) {
        activeParticles.removeAt(i)
        pool.add(particle)
      } else {
        i++
      }
    }
  }

  private fun obtainParticle(): SanaaParticle? {
    return if (pool.isNotEmpty()) {
      pool.removeAt(pool.size - 1)
    } else null
  }
}

/**
 * Jetpack Compose Canvas overlay that renders the active particles with high-performance drawing.
 */
@Composable
fun SanaaParticleCanvas(
  particleSystem: SanaaParticleSystem,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier.fillMaxSize()) {
    drawSanaaParticles(particleSystem)
  }
}

/**
 * Draw routine for rendering tire smoke, dust clouds, and sparks on Canvas.
 */
fun DrawScope.drawSanaaParticles(particleSystem: SanaaParticleSystem) {
  val particles = particleSystem.particles
  for (i in particles.indices) {
    val p = particles[i]
    if (!p.isAlive || p.alpha <= 0.01f) continue

    val center = Offset(p.x, p.y)

    when (p.type) {
      SanaaParticleType.TIRE_SMOKE -> {
        // Soft billowing smoke circles
        drawCircle(
          color = p.color.copy(alpha = p.alpha * 0.6f),
          radius = p.size,
          center = center
        )
        // Inner dense puff
        drawCircle(
          color = Color.White.copy(alpha = p.alpha * 0.35f),
          radius = p.size * 0.55f,
          center = center
        )
      }

      SanaaParticleType.SANAA_DUST_CLOUD -> {
        // Earthy sand & clay dust clouds
        rotate(p.rotation, pivot = center) {
          drawCircle(
            color = p.color.copy(alpha = p.alpha * 0.7f),
            radius = p.size,
            center = center
          )
          // Atmospheric soft outer haze
          drawCircle(
            color = p.color.copy(alpha = p.alpha * 0.25f),
            radius = p.size * 1.45f,
            center = center
          )
        }
      }

      SanaaParticleType.NITRO_SPARK -> {
        // Bright glowing point with streak
        drawCircle(
          color = p.color.copy(alpha = p.alpha),
          radius = p.size,
          center = center
        )
        // Radiant sparkle line
        drawLine(
          color = Color.White.copy(alpha = p.alpha * 0.9f),
          start = Offset(p.x - p.vx * 1.5f, p.y - p.vy * 1.5f),
          end = center,
          strokeWidth = p.size * 0.75f
        )
      }

      SanaaParticleType.COLLISION_DEBRIS -> {
        rotate(p.rotation, pivot = center) {
          drawRect(
            color = p.color.copy(alpha = p.alpha),
            topLeft = Offset(p.x - p.size / 2f, p.y - p.size / 2f),
            size = androidx.compose.ui.geometry.Size(p.size, p.size)
          )
        }
      }

      SanaaParticleType.POLICE_SIREN_GLOW -> {
        drawCircle(
          color = p.color.copy(alpha = p.alpha * 0.3f),
          radius = p.size * 2f,
          center = center
        )
      }
    }
  }
}
