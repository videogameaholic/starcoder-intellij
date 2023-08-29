package com.videogameaholic.intellij.starcoder

import com.intellij.ide.ui.AntialiasingType
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.impl.FocusModeModel
import com.intellij.openapi.editor.impl.FontInfo
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.paint.EffectPainter
import com.intellij.util.ui.GraphicsUtil
import com.intellij.util.ui.UIUtil
import java.awt.*
import kotlin.math.abs
import kotlin.math.roundToInt

open class CodeGenHintRenderer(var text: String?) : EditorCustomElementRenderer {

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        return calcWidthInPixels(inlay.editor, text)
    }

    protected open fun getTextAttributes(editor: Editor): TextAttributes? {
        return editor.colorsScheme.getAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT)
    }

    override fun paint(inlay: Inlay<*>, g: Graphics, r: Rectangle, textAttributes: TextAttributes) {
        val editor = inlay.editor
        if (editor !is EditorImpl) return

        val focusModeRange = editor.focusModeRange
        val attributes = if (focusModeRange != null && (inlay.offset <= focusModeRange.startOffset || focusModeRange.endOffset <= inlay.offset)) {
            editor.getUserData(FocusModeModel.FOCUS_MODE_ATTRIBUTES) ?: getTextAttributes(editor)
        }
        else {
            getTextAttributes(editor)
        }

        paintHint(g, editor, r, text, attributes, textAttributes)
    }

    companion object {
        @JvmStatic
        fun calcWidthInPixels(editor: Editor, text: String?): Int {
            val fontMetrics = getFontMetrics(editor)
            return calcHintTextWidth(text, fontMetrics)
        }

        @JvmStatic
        fun paintHint(g: Graphics,
                      editor: EditorImpl,
                      r: Rectangle,
                      text: String?,
                      attributes: TextAttributes?,
                      textAttributes: TextAttributes) {
            val ascent = editor.ascent
            val descent = editor.descent
            val g2d = g as Graphics2D

            if (text != null && attributes != null) {
                val gap = 0
                val backgroundColor = attributes.backgroundColor
                if (backgroundColor != null) {
                    val alpha = if (isInsufficientContrast(attributes, textAttributes)) 1.0f else BACKGROUND_ALPHA
                    val config = GraphicsUtil.setupAAPainting(g)
                    GraphicsUtil.paintWithAlpha(g, alpha)
                    g.setColor(backgroundColor)
                    g.fillRoundRect(r.x + 2, r.y + gap, r.width - 4, r.height - gap * 2, 8, 8)
                    config.restore()
                }
                val foregroundColor = attributes.foregroundColor
                if (foregroundColor != null) {
                    val savedHint = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING)
                    val savedClip = g.getClip()

                    g.setColor(foregroundColor)
                    g.setFont(getFont(editor))
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, AntialiasingType.getKeyForCurrentScope(false))
                    g.clipRect(r.x + 3, r.y + 2, r.width - 6, r.height - 4)

                    val startX = r.x + 2
                    val startY = r.y + ascent
                    g.drawString(text, startX, startY)
                    g.setClip(savedClip)
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, savedHint)
                }
            }
            val effectColor = textAttributes.effectColor
            val effectType = textAttributes.effectType
            if (effectColor != null) {
                g.setColor(effectColor)
                val xStart = r.x
                val xEnd = r.x + r.width
                val y = r.y + ascent
                val font = editor.getColorsScheme().getFont(EditorFontType.PLAIN)
                when (effectType) {
                    EffectType.LINE_UNDERSCORE -> EffectPainter.LINE_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, descent, font)
                    EffectType.BOLD_LINE_UNDERSCORE -> EffectPainter.BOLD_LINE_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, descent, font)
                    EffectType.STRIKEOUT -> EffectPainter.STRIKE_THROUGH.paint(g2d, xStart, y, xEnd - xStart, editor.charHeight, font)
                    EffectType.WAVE_UNDERSCORE -> EffectPainter.WAVE_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, descent, font)
                    EffectType.BOLD_DOTTED_LINE -> EffectPainter.BOLD_DOTTED_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, descent, font)
                    else -> {}
                }
            }
        }

        private fun isInsufficientContrast(
            attributes: TextAttributes,
            surroundingAttributes: TextAttributes
        ): Boolean {
            val backgroundUnderHint = surroundingAttributes.backgroundColor
            if (backgroundUnderHint != null && attributes.foregroundColor != null) {
                val backgroundBlended = srcOverBlend(attributes.backgroundColor, backgroundUnderHint, BACKGROUND_ALPHA)

                val backgroundBlendedGrayed = backgroundBlended.toGray()
                val textGrayed = attributes.foregroundColor.toGray()
                val delta = abs(backgroundBlendedGrayed - textGrayed)
                return delta < 10
            }
            return false
        }

        private fun Color.toGray(): Double {
            return (0.30 * red) + (0.59 * green) + (0.11 * blue)
        }

        private fun srcOverBlend(foreground: Color, background: Color, foregroundAlpha: Float): Color {
            val r = foreground.red * foregroundAlpha + background.red * (1.0f - foregroundAlpha)
            val g = foreground.green * foregroundAlpha + background.green * (1.0f - foregroundAlpha)
            val b = foreground.blue * foregroundAlpha + background.blue * (1.0f - foregroundAlpha)
            return Color(r.roundToInt(), g.roundToInt(), b.roundToInt())
        }

        @JvmStatic
        fun getFontMetrics(editor: Editor): FontMetrics {
            return FontInfo.getFontMetrics(getFont(editor), FontInfo.getFontRenderContext(editor.contentComponent))
        }

        private fun getFont(editor: Editor): Font {
            val colorsScheme = editor.colorsScheme
            val attributes = editor.colorsScheme.getAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT)

            // TODO override foreground/background color here?

            val fontStyle = attributes?.fontType ?: Font.PLAIN
            return UIUtil.getFontWithFallback(colorsScheme.getFont(EditorFontType.forJavaStyle(fontStyle)))
        }

        @JvmStatic
        protected fun calcHintTextWidth(text: String?, fontMetrics: FontMetrics): Int {
            return if (text == null) 0 else fontMetrics.stringWidth(text) + 14
        }

        private const val BACKGROUND_ALPHA = 0.55f
    }
}