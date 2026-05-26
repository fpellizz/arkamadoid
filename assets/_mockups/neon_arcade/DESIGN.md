---
name: Neon Arcade
colors:
  surface: '#131319'
  surface-dim: '#131319'
  surface-bright: '#393840'
  surface-container-lowest: '#0e0e14'
  surface-container-low: '#1b1b22'
  surface-container: '#1f1f26'
  surface-container-high: '#2a2930'
  surface-container-highest: '#35343b'
  on-surface: '#e4e1ea'
  on-surface-variant: '#ddbed1'
  inverse-surface: '#e4e1ea'
  inverse-on-surface: '#303037'
  outline: '#a5889b'
  outline-variant: '#574050'
  surface-tint: '#fface8'
  primary: '#fface8'
  on-primary: '#5e0053'
  primary-container: '#ff24e4'
  on-primary-container: '#520049'
  inverse-primary: '#ad009b'
  secondary: '#d3fbff'
  on-secondary: '#00363a'
  secondary-container: '#00eefc'
  on-secondary-container: '#00686f'
  tertiary: '#dbc900'
  on-tertiary: '#363100'
  tertiary-container: '#bdad00'
  on-tertiary-container: '#474000'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffd7f0'
  primary-fixed-dim: '#fface8'
  on-primary-fixed: '#3a0033'
  on-primary-fixed-variant: '#840076'
  secondary-fixed: '#7df4ff'
  secondary-fixed-dim: '#00dbe9'
  on-secondary-fixed: '#002022'
  on-secondary-fixed-variant: '#004f54'
  tertiary-fixed: '#fae500'
  tertiary-fixed-dim: '#dbc900'
  on-tertiary-fixed: '#1f1c00'
  on-tertiary-fixed-variant: '#4f4800'
  background: '#131319'
  on-background: '#e4e1ea'
  surface-variant: '#35343b'
typography:
  display-lg:
    fontFamily: Space Mono
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -2px
  headline-lg:
    fontFamily: Space Mono
    fontSize: 32px
    fontWeight: '700'
    lineHeight: '1.2'
  headline-lg-mobile:
    fontFamily: Space Mono
    fontSize: 24px
    fontWeight: '700'
    lineHeight: '1.2'
  body-md:
    fontFamily: Space Mono
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.5'
  label-sm:
    fontFamily: Space Mono
    fontSize: 12px
    fontWeight: '700'
    lineHeight: '1'
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  unit: 8px
  gutter: 16px
  margin-mobile: 16px
  margin-desktop: 32px
  bezel-width: 40px
---

## Brand & Style
The brand personality is high-energy, nostalgic, and unapologetically electric. It targets enthusiasts of the 1980s coin-op era, evoking the sensory overload of a dim, neon-lit arcade. 

The design style is **Retro / Vaporwave** mixed with **Tactile Arcade** elements. The interface mimics the physical presence of an Arkanoid cabinet, utilizing CRT scanline effects, chromatic aberration, and glowing interactive elements. The goal is to make the user feel like they are standing in front of a heavy plywood and plastic machine, with the "screen" recessed behind a glass bezel.

## Colors
This design system utilizes a high-contrast palette built on a foundation of **Deep Space Black (#05050A)**. 

- **Neon Pink (#FF00E5):** Used for primary actions, level titles, and high-score highlights.
- **Electric Blue (#00F0FF):** Used for player lives, paddle status, and secondary UI borders.
- **Bright Yellow (#FFEA00):** Reserved for "Power-Up" notifications, coin insertion alerts, and warnings.
- **Deep Space Black:** The primary background color, representing the infinite void of the game screen.

All colors must be applied with an "outer glow" (box-shadow) to simulate the phosphorescent bleed of a CRT monitor.

## Typography
The typography utilizes **Space Mono** to bridge the gap between technical monospaced efficiency and a retro-futuristic aesthetic. 

Headlines should be rendered in all-caps with a heavy text-shadow in a contrasting neon color to create a "vibrating" effect. Body text remains monospaced for a computer-terminal feel. For a more authentic 8-bit look, anti-aliasing should be disabled on smaller labels where possible.

## Layout & Spacing
The layout follows a **Fixed Grid** model that mimics a 4:3 aspect ratio arcade monitor centered within a cabinet frame. 

- **The Bezel:** A fixed outer margin (bezel-width) that features a metallic texture and "Cabinet Art" illustrations.
- **The Screen:** A central container with a subtle CSS `radial-gradient` and overlaying scanlines (1px repeating linear gradient).
- **HUD (Heads-Up Display):** Fixed positions at the top and bottom corners of the "screen" area for scores and lives.
- **Breakpoints:** On mobile, the "bezel" is hidden to maximize game space, while on desktop, the cabinet frame is fully visible to enhance the immersive experience.

## Elevation & Depth
Depth is achieved through **Tonal Layers and Light Emission** rather than standard shadows.

- **The Screen Recess:** Inner shadows on the screen container create the illusion of glass sitting behind a plastic frame.
- **The CRT Bloom:** Elements do not cast shadows *downward*; they emit light *outward*. Use multiple layers of `drop-shadow` with varying blur radii (4px, 12px, 20px) to simulate glowing pixels.
- **Cabinet Hardware:** Physical controls (joysticks/buttons) use high-contrast gradients and "rim lighting" (1px highlights on top edges) to appear as physical objects sitting on a control deck.

## Shapes
The shape language is **Soft (1)** for structural elements and **Pill-shaped** for interactive buttons.

The main screen container has a `0.75rem` radius to simulate the curved corners of vintage glass monitors. Power-up icons and falling "pills" use a full `rounded-xl` (pill) shape. Active game blocks (bricks) must remain perfectly sharp (0px) to preserve the grid-based pixel logic of the gameplay.

## Components
- **Arcade Buttons:** Primary buttons are circular, using a heavy `inset` box-shadow to appear concave. On "press," the glow intensity doubles.
- **The Paddle:** A horizontal capsule with a metallic gradient (Silver/Electric Blue) and a bright white highlight in the center.
- **LCD Readouts:** Information like "High Score" is displayed in a dedicated "Digital Tube" container with a dim red background and bright red text.
- **CRT Overlay:** A global `::after` element on the main container with a `scanline` animation and a subtle `flicker` keyframe.
- **Input Fields:** Styled as "Insert Name" prompts with a blinking underscore cursor (`_`) and a monospaced font.
- **Checkboxes:** Toggle switches designed to look like heavy-duty plastic rocker switches found on the back of arcade hardware.