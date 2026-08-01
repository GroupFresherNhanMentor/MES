export interface ProductTypeLookup {
  id: string;
  name: string;
  description?: string;
}

export interface UnitOfMeasureLookup {
  id: string;
  name: string;
  description?: string;
}

export interface ProductStatusLookup {
  id: string;
  name: string;
  description?: string;
}

export interface ProductDto {
  id: string;
  code: string;
  name: string;
  productType?: ProductTypeLookup | null;
  productTypeId?: string;
  productTypeName?: string | null;
  unit?: UnitOfMeasureLookup | null;
  unitId?: string;
  unitName?: string | null;
  productStatus?: ProductStatusLookup | null;
  productStatusId?: string;
  productStatusName?: string | null;
  version?: number | string;
  createdAt: string;
  createdBy?: any;
  updatedAt: string;
  updatedBy?: any;
}

export interface CreateProductRequest {
  code: string;
  name: string;
  productTypeId: string;
  unitId: string;
  productStatusId: string;
}

export interface UpdateProductRequest {
  name?: string;
  unitId?: string;
  productStatusId?: string;
}
