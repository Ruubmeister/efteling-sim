/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { coordinatesDto } from './coordinatesDto';

export type rideDto = {
    id: string;
    name: string;
    minimumAge: number;
    minimumLength: number;
    durationInSec: number;
    maxPersons: number;
    visitorsInLine: number;
    visitorsInRide: number;
    endTime: string;
    employeesToSkill: Record<string, string>;
    coordinates: coordinatesDto;
    locationType: string;
    status: rideDto.status;
};

export namespace rideDto {

    export enum status {
        OPEN = 'OPEN',
        CLOSED = 'CLOSED',
        MAINTENANCE = 'MAINTENANCE',
    }


}

