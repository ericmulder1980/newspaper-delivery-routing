Canvas: 390×844, carbon-weave background #0C0D0E. Total 3.3 s, plays once, holds last frame. Three sections: Finish 0–0.9 s, Klaar 0.9–1.9 s, Stats 1.9–3.3 s. All motion is a pure function of time T.

Easings (only three):

enter = easeOutExpo: 1 − 2^(−10t)
pop = easeOutQuart: 1 − (1−t)^4
draw = easeInOutSine: −(cos(πt) − 1)/2
tween(start,end,from,to,ease): clamp (T−start)/(end−start) to 0–1, apply ease, lerp.

Fonts/colors: Bebas Neue (display, often italic), Barlow (body). Yellow #E8FF00, ink #0E0F11, off-white #F4F1EA, card #1E2126, hairline #2E3137, grey #A7A9AE, light grey #C9CBCF.

1. Checkered flag (drawn first, under everything)

Grid 10 cols × 22 rows of 39 px squares, alternating ink/off-white ((row+col) % 2 == 0 → ink). Grid starts at y = −39 so there's an extra row above and below the screen; below the grid a 78 px gradient ink→#0C0D0E.
Whole flag translateX: enter(0, 0.7, −470, 0).
Whole flag translateY (lift to top band): draw(0.9, 1.5, 0, −674) → leaves a 170 px flag band at the top.
Wave amplitude amp: piecewise linear over T: 26 @0 → 18 @0.5 → 10 @1.5 → 6 @2.3, then constant.
Per column c: phase ph = T·9 − c·0.7; translateY = sin(ph)·amp; skewY = cos(ph)·amp·0.35 degrees, origin top-left.
2. Speed slashes (3 yellow parallelograms, 11 px tall, skewX −24°)

(x 40, y 300, w 180, delay 0.05), (120, 330, 120, 0.14), (0, 560, 220, 0.22).
Progress p = enter(delay, delay+0.5, 0, 1); translateX = (1−p)·(−420) + p·60.
Opacity = draw(delay+0.45, delay+0.75, 1, 0).
3. Title block (x 20, y 214, with t0 = 1.15)

Eyebrow row: 18×11 yellow slash + "FINISH · {naam}" Bebas 19, letter-spacing 0.14em, yellow. Opacity and slide: e = enter(1.30, 1.70, 0, 1), translateX (1−e)·−24.
Headline "RONDE / KLAAR" (two lines) Bebas italic 96, line-height 0.92, off-white, 12 px below eyebrow. Opacity pop(1.15, 1.50, 0, 1), scale pop(1.15, 1.60, 1.18, 1) from left-center.
4. #07 badge (right 20, y 214, 44 px tall, 12 px side padding, radius 6, yellow fill, Bebas italic 34 ink)

Opacity pop(1.4, 1.7, 0, 1), scale pop(1.4, 1.7, 1.3, 1).
5. Stats column (x 20–370, y 470 → bottom 24, gap 10, t0 = 1.9)

Tile style: card fill, 1 px hairline, radius 16, padding 16/18/14, min-height 104. Label Barlow 700 13, 0.08em, uppercase, grey. Value Bebas italic 52, yellow, tabular numerals. Sub Barlow 15 light grey.
Tile enter: p = enter(at, at+0.55, 0, 1) → opacity p, translateY (1−p)·60. Tile 1 at 1.9, tile 2 at 2.05.
Tile 1 "RONDETIJD": value mm:ss of pop(2.0, 2.8, seconds·0.6, seconds), rounded. Sub "Vandaag · hele looproute".
Tile 2 "KRANTEN BEZORGD": value round(pop(2.15, 2.9, 0, papers)). Sub "{straatdelen} straatdelen · 0 overgeslagen".
Progress bar: 6 px tall, radius 4, track #2A2D33, yellow fill width enter(2.8, 3.2, 0, 100)%.
CTA pinned to bottom: 76 px, radius 18, yellow, "VOLGENDE" Bebas 34 ink + 28 px chevron-right (stroke 2.6). Opacity enter(2.7, 3.2, 0, 1), translateY (1−c)·30. Tap → Top 5 screen.
Inputs: naam, rondetijd in seconden, aantal kranten, aantal straatdelen.
