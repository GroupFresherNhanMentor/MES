# Phase 0 Research Findings: BOM List Page

## 1. Angular Signals & Filter Architecture

- **Decision**: Use Angular Signals for all component reactive state:
  ```typescript
  items = signal<BomDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  selectedProductId = signal<string | null>(null);
  selectedStatusId = signal<string | null>(null);
  
  productsList = signal<{ id: string; name: string; code: string }[]>([]);
  statusesList = signal<BomStatus[]>([]);
  ```
- **Rationale**: Clean, performant reactivity with zero RxJS subscription overhead in templates.

## 2. Role-Based Visibility Pattern

- **Decision**: Inject `AuthService` and compute `canCreate`:
  ```typescript
  private auth = inject(AuthService);
  currentUser = this.auth.getCurrentUser();
  canCreate = computed(() => {
    const r = this.currentUser?.role;
    return r === 'ADMIN' || r === 'PLANNER';
  });
  ```
- **Rationale**: Aligns with project authorization pattern while staying strictly reactive using Angular Signals.

## 3. Tailwind Styling Rules Compliance

- **Decision**: Zero component `.scss` file. Use Tailwind CSS utilities (`flex`, `gap-3`, `items-center`, `mb-6`, `w-[220px]`).
- **Badge Classes**:
  - `ACTIVE`: `ff-badge ff-badge--active`
  - `DRAFT`: `ff-badge ff-badge--pending`
  - `INACTIVE`: `ff-badge ff-badge--cancelled`
