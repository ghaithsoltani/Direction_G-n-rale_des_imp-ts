import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ContribuableService } from './contribuable.service';
import { Contribuable } from '../models/contribuable.model';

describe('ContribuableService', () => {
  let service: ContribuableService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(ContribuableService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should send a backend-compatible nested payload for a physical person', () => {
    const payload: Contribuable = {
      typeContribuable: 'PERSONNE_PHYSIQUE',
      nom: 'Doe',
      prenom: 'Jane',
      cin: '12345678',
      adresse: 'Rue de Tunis',
      ville: 'Tunis',
      codePostal: '1000',
      telephone: '20123456',
      email: 'jane@example.com',
      dateNaissanceOuCreation: '1990-05-01',
    };

    service.creer(payload, {
      secteurActivite: 'Services',
      codeActivitePrincipale: '6202A',
      libelleActivite: 'Conseil',
      regimeFiscal: 'REEL',
      adresseExercice: 'Avenue Habib Bourguiba',
      villeExercice: 'Tunis',
      dateDebutActivite: '2024-01-01',
    }).subscribe();

    const req = httpMock.expectOne((request) => request.url.endsWith('/api/contribuables'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      type: 'PERSONNE_PHYSIQUE',
      nom: 'Doe',
      prenom: 'Jane',
      cin: '12345678',
      email: 'jane@example.com',
      telephone: '20123456',
      adresse: {
        rue: 'Rue de Tunis',
        ville: 'Tunis',
        codePostal: '1000',
      },
      activite: {
        codeActivitePrincipale: '6202A',
        libelleActivite: 'Conseil',
        secteurActivite: 'Services',
        dateDebutActivite: '2024-01-01',
        adresseExercice: 'Avenue Habib Bourguiba',
        activitePrincipale: true,
      },
      dateNaissance: '1990-05-01',
      numeroPasseport: undefined,
      raisonSociale: undefined,
      registreCommerce: undefined,
    });

    req.flush({ id: '123', type: 'PERSONNE_PHYSIQUE' });
  });
});
