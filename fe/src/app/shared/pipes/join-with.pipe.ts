import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'joinWith',
  standalone: true,
})
export class JoinWithPipe implements PipeTransform {
  /**
   * Joins primary label and secondary code/sub-text with a custom separator (default: '~')
   * Example: {{ a.product?.name | joinWith: a.product?.code }} -> "Product Name ~ PROD-123"
   */
  transform(primary?: string | null, secondary?: string | null, separator: string = '~'): string {
    if (!primary && !secondary) return '—';
    if (!primary) return secondary!;
    if (!secondary) return primary;
    return `${primary} ${separator} ${secondary}`;
  }
}
