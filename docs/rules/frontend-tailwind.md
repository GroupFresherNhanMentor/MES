# Frontend Tailwind CSS Rules

Angular 22 · Tailwind CSS v4 · Angular Material 22

## Core Principle

**All component-level styling is done with Tailwind utility classes in the template. No component `.scss` files.**

The only CSS files that exist are:
- `src/material-theme.scss` — Angular Material theme (required, do not touch)
- `src/styles.scss` — Global resets, Tailwind entry point, Material/MDC deep overrides, animations

---

## What Goes Where

| Styling concern | Where it lives |
|----------------|----------------|
| Layout, spacing, color, typography | Tailwind classes directly in template |
| Angular Material component overrides (`.mat-mdc-*`) | `styles.scss` only |
| MDC internal deep selectors (`.mdc-*`) | `styles.scss` only |
| Custom keyframe animations | `styles.scss` only |
| Reusable semantic utilities (badges, cards) | `styles.scss` as `@utility` blocks |
| Design tokens (colors, fonts) | `styles.scss` `@theme` block |

---

## Design Token Reference

All tokens are defined in `src/styles.scss` under `@theme` and are available as Tailwind utilities:

### Colors

| Token class | Value | Use for |
|------------|-------|---------|
| `bg-surface-app` | `#0c0a09` | Page/app background |
| `bg-surface-elevated` | `#1c1917` | Toolbar, sidenav, elevated surfaces |
| `bg-surface-card` | `#292524` | Cards, dialogs, dropdowns |
| `text-text-primary` | `#fafaf9` | Primary text |
| `text-text-secondary` | `#a8a29e` | Secondary / label text |
| `text-text-muted` | `#78716c` | Placeholders, hints |
| `border-border-default` | `#44403c` | Borders, dividers |
| `border-border-light` | `#292524` | Subtle borders |
| `text-primary` / `bg-primary` | `#ea580c` | Brand orange — buttons, active states |
| `text-success` / `bg-success` | `#65a30d` | Success states |
| `text-error` / `bg-error` | `#dc2626` | Error states |
| `text-warning` / `bg-warning` | `#d97706` | Warning states |

---

## Rules

### 1. No component `.scss` files

Components use `templateUrl` (or inline `template`) only. The `styleUrl` / `styles` property is **forbidden** unless adding one-off MDC deep overrides that genuinely cannot be done any other way (document why with a comment).

```typescript
// FORBIDDEN
@Component({
  templateUrl: './my.html',
  styleUrl: './my.scss', // ❌
})

// REQUIRED
@Component({
  templateUrl: './my.html', // ✅
})
```

### 2. No inline `style=""` attributes

Every style belongs in Tailwind classes. `style=""` in templates is forbidden.

```html
<!-- FORBIDDEN -->
<div style="color: red; padding: 16px;">

<!-- REQUIRED -->
<div class="text-error px-4">
```

### 3. Use design tokens, not raw hex values

Always reach for a design token class before writing an arbitrary value.

```html
<!-- FORBIDDEN — raw arbitrary value when a token exists -->
<div class="bg-[#1c1917] text-[#a8a29e]">

<!-- REQUIRED — semantic token -->
<div class="bg-surface-elevated text-text-secondary">
```

Arbitrary values (`bg-[#xxx]`) are allowed only when no token covers the case (e.g., one-off shadows, special widths).

### 4. Override Angular Material with the `!` modifier

When a Tailwind class must override a Material default, prefix it with `!` to emit `!important`.

```html
<!-- Override toolbar background -->
<mat-toolbar class="!bg-surface-elevated !h-14">

<!-- Override drawer width -->
<mat-drawer class="!w-[220px] !bg-surface-elevated">

<!-- Override progress bar position -->
<mat-progress-bar class="!fixed !top-0 !z-[9999]">
```

### 5. MDC deep overrides go in `styles.scss`

Angular Material renders internal `.mdc-*` class names that Tailwind cannot target. These overrides must live in `styles.scss` as plain CSS — never in a component file.

```css
/* styles.scss — OK */
.nav-item .mdc-list-item__content {
  display: flex !important;
  align-items: center !important;
}
```

```typescript
// component file — FORBIDDEN
styles: [`.nav-item .mdc-list-item__content { display: flex; }`]
```

### 6. Custom reusable utilities use `@utility` in `styles.scss`

If a combination of styles is reused across more than one component, define it as a named utility in `styles.scss` instead of duplicating classes.

```css
/* styles.scss */
@utility ff-card {
  background-color: var(--color-surface-card);
  border: 1px solid var(--color-border-default);
  border-radius: 8px;
  padding: 20px;
}
```

```html
<!-- template — use the utility name like any Tailwind class -->
<div class="ff-card">
```

### 7. Animations stay in `styles.scss`

`@keyframes` and animation utilities cannot be expressed in Tailwind v4 `@utility` blocks. Define them in `styles.scss` and reference by class name.

```css
/* styles.scss */
.ff-fade-in { animation: ff-fade-in 0.3s ease-out; }
@keyframes ff-fade-in {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}
```

### 8. Status badge pattern

Use the predefined `.ff-badge--*` global classes for status chips. Do not recreate badge styles per-component.

```html
<span class="ff-badge ff-badge--active">Active</span>
<span class="ff-badge ff-badge--cancelled">Cancelled</span>
```

Available variants: `pending`, `active`, `completed`, `onhold`, `cancelled`, `paused`, `running`, `idle`.

---

## Common Patterns

### Page header + filter toolbar

```html
<div class="flex items-center justify-between mb-6">
  <h1 class="text-xl font-semibold text-text-primary">Products</h1>
  <button mat-flat-button color="primary">
    <mat-icon>add</mat-icon> Add Product
  </button>
</div>
<div class="flex gap-3 items-center mb-4">
  <mat-form-field appearance="outline" class="w-[200px]">...</mat-form-field>
</div>
```

### Card container

```html
<div class="ff-card">
  ...content...
</div>
```

### KPI / stat grid

```html
<div class="grid grid-cols-[repeat(auto-fit,minmax(240px,1fr))] gap-4 mb-6">
  <div class="kpi-card">
    <span class="kpi-label">Total Orders</span>
    <span class="kpi-value">142</span>
  </div>
</div>
```

### Empty state

```html
<div class="flex flex-col items-center justify-center py-16 text-text-muted">
  <mat-icon class="text-5xl mb-2 opacity-40">inbox</mat-icon>
  <span class="text-sm">No records found</span>
</div>
```

---

## File Checklist (new component)

- [ ] No `styleUrl` / `styles` property in `@Component`
- [ ] No `style=""` attributes in template
- [ ] Colors use design token classes, not raw hex
- [ ] Material overrides use `!` prefix
- [ ] Any repeated style pattern defined in `styles.scss` as `@utility`, not duplicated
