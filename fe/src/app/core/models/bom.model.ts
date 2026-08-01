export interface BomStatusInfo {
  id: string;
  name: string;
}

export interface BomUserInfo {
  id: string;
  fullName: string;
  username: string;
}

export interface BomDto {
  id: string;
  finishedProductId: string;
  finishedProductName?: string;
  finishedProductCode?: string;
  version: number;
  bomStatus?: BomStatusInfo;
  createdBy?: BomUserInfo;
  createdAt: string;
  items?: BomItemDto[];
}

export interface BomItemDto {
  id: string;
  bomId: string;
  materialProductId: string;
  materialProductName?: string;
  materialProductCode?: string;
  quantityPerUnit: number;
  unitName?: string;
  scrapRate?: number;
}

export interface NewBomItemData {
  materialProductId: string;
  quantityPerUnit: number;
  scrapRate: number;
}
