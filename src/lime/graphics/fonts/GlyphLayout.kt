package lime.graphics.fonts

import lime.graphics.Texture
import lime.utils.Bag
import lime.utils.Pool
import org.joml.Vector2f
import kotlin.math.max

class GlyphLayout(var font: Font? = null) {
    class RenderGlyph {
        lateinit var texture: Texture
        var x = 0.0f
        var y = 0.0f
        var width = 0.0f
        var height = 0.0f
    }

    private inner class Line {
        val renderGlyphs = Bag<RenderGlyph>()
        var width = 0.0f

        fun addGlyph(x: Float, glyph: Glyph, scaleX: Float, scaleY: Float) {
            width = max(width, x + glyph.width)
            val renderGlyph = renderGlyphsPool.obtain()
            renderGlyph.texture = glyph.texture
            renderGlyph.x = glyph.x * scaleX
            renderGlyph.y = glyph.y * scaleY
            renderGlyph.width = glyph.width * scaleX
            renderGlyph.height = glyph.height * scaleY
            renderGlyphs += renderGlyph
        }

        fun reset() {
            renderGlyphs.forEach(renderGlyphsPool::free)
            renderGlyphs.clear()
            width = 0.0f
        }
    }

    var width = 0.0f
        private set

    var height = 0.0f
        private set

    private val lines = Bag<Line>()
    private val renderGlyphsPool = Pool { RenderGlyph() }
    private val linesPool = Pool { Line() }

    fun setText(text: String, lineSpacing: Float = 1.0f, hAlign: HAlign = HAlign.LEFT, vAlign: VAlign = VAlign.TOP, areaWidth: Float = Float.MAX_VALUE, areaHeight: Float = Float.MAX_VALUE, scaleX: Float = 1.0f, scaleY: Float = 1.0f) {
        reset()

        val font = requireNotNull(this.font)

        val actualLineSpacing = max(1.0f, lineSpacing)
        val position = Vector2f(0.0f, font.size)
        val glyph = Glyph()

        text.lineSequence().forEach {
            val line = linesPool.obtain()

            it.forEach { character ->
                font.getGlyphAndPosition(character, glyph, position)
                line.addGlyph(position.x, glyph, scaleX, scaleY)
            }

            lines += line

            position.x = 0.0f
            position.y += font.size * actualLineSpacing
        }

        if (lines.size > 0) {
            width = (lines.maxBy { it.width }?.width ?: 0.0f) * scaleX
            height = lines.size * font.size * actualLineSpacing * scaleY

            val actualAreaWidth = max(width, areaWidth)
            val actualAreaHeight = max(height, areaHeight)

            val yOffset = when (vAlign) {
                VAlign.TOP -> 0.0f
                VAlign.MIDDLE -> (actualAreaHeight - height) * 0.5f
                VAlign.BOTTOM -> actualAreaHeight - height
            }

            lines.forEach { line ->
                val xOffset = when (hAlign) {
                    HAlign.LEFT -> 0.0f
                    HAlign.CENTERED -> (actualAreaWidth - line.width) * 0.5f
                    HAlign.RIGHT -> actualAreaWidth - line.width
                }

                line.renderGlyphs.forEach {
                    it.x += xOffset
                    it.y += yOffset
                }
            }
        }
    }

    fun reset() {
        lines.forEach {
            it.reset()
            linesPool.free(it)
        }
        lines.clear()
    }

    fun forEachRenderGlyph(block: (RenderGlyph) -> Unit) {
        lines.forEach {
            it.renderGlyphs.forEach(block)
        }
    }
}