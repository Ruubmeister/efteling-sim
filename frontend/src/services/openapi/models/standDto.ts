/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { gridLocationDto } from './gridLocationDto';
export type standDto = {
    id: string;
    name: string;
    locationType: string;
    location: gridLocationDto;
    meals: Array<string>;
    drinks: Array<string>;
    isOpen: boolean;
};

