export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
  timestamp: string;
}

export interface PageResponse<T> {
  items: T[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
}

export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
  keyword?: string;
}
