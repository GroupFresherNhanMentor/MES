export interface ProductDto {
  id: string;
  code: string;
  name: string;
  productTypeId: string;
  productTypeName: string | null;
  unitId: string;
  unitName: string | null;
  productStatusId: string;
  productStatusName: string | null;
  version: number;
  createdAt: string;
  createdBy: string | null;
  updatedAt: string;
  updatedBy: string | null;
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
