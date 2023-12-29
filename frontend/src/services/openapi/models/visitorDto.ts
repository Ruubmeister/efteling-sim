/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { coordinatesDto } from './coordinatesDto';

export type visitorDto = {
    id: string;
    dateOfBirth?: string;
    currentLocation: coordinatesDto;
    length?: number;
    targetLocation?: coordinatesDto;
    step?: number;
};

