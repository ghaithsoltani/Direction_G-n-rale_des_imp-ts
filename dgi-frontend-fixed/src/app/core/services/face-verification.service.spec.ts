import { TestBed } from '@angular/core/testing';

import { FaceVerificationService } from './face-verification.service';

describe('FaceVerificationService', () => {
  let service: FaceVerificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FaceVerificationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
