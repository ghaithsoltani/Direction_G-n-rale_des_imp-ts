import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'trimName',
    standalone: true,
})
export class TrimNamePipe implements PipeTransform {
    transform(value: string | null | undefined): string {
        return (value ?? '').trim() || 'Utilisateur';
    }
}
