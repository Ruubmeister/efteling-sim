/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { gridLocationDto } from './gridLocationDto';
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
    locationType: string;
    location: gridLocationDto;
    status: rideDto.status;
};
export namespace rideDto {
    export enum status {
        OPEN = 'OPEN',
        CLOSED = 'CLOSED',
        MAINTENANCE = 'MAINTENANCE',
    }
}

