# Phase 0 Research Findings: Create BOM Dialog

## 1. Angular Material Dialog Pattern

- **Decision**: Use `MatDialogRef` and `MAT_DIALOG_DATA` with standalone Angular component:
  ```typescript
  export class BomCreateDialog implements OnInit {
    private dialogRef = inject(MatDialogRef<BomCreateDialog>);
    private fb = inject(FormBuilder);
    private api = inject(ApiService);
    private router = inject(Router);

    form = this.fb.group({
      finishedProductId: ['', Validators.required],
      version: [1, [Validators.required, Validators.min(1)]],
    });
  }
  ```
- **Rationale**: Standard Angular Material 22 dialog pattern with full Reactive Form validation.

## 2. Product Type Filtering (FR-BOM-001)

- **Decision**: Fetch products from `GET /api/products?size=100` and filter for products of type `FINISHED_GOOD` or `SEMI_FINISHED`:
  ```typescript
  productsList = signal<ProductDto[]>([]);
  
  loadFinishedProducts() {
    this.api.get<{ items: ProductDto[] }>(`${API.products.base}?size=100`).subscribe(r => {
      if (r.success && r.data?.items) {
        // Filter FINISHED_GOOD or SEMI_FINISHED
        const filtered = r.data.items.filter(p => 
          p.productTypeName === 'FINISHED_GOOD' || 
          p.productTypeName === 'SEMI_FINISHED' ||
          p.productTypeId === 'PT-FIN' ||
          p.productTypeId === 'PT-SUB'
        );
        this.productsList.set(filtered.length > 0 ? filtered : r.data.items);
      }
    });
  }
  ```

## 3. Submission & Navigation Flow

- **Decision**: On submit:
  1. Set `loading` signal to `true`.
  2. Call `POST /api/boms` with payload `{ finishedProductId, version }`.
  3. On success, close dialog with `dialogRef.close(newBom)`.
  4. Navigate to `/boms/:newBom.id`.
