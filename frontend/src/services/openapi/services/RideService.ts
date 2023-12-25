/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { rideDto } from '../models/rideDto';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class RideService {

    /**
     * @returns rideDto A JSON array of rides
     * @throws ApiError
     */
    public static getAllRides(): CancelablePromise<Array<rideDto>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/rides',
        });
    }

    /**
     * @returns rideDto A random ride
     * @throws ApiError
     */
    public static getRandomRide(): CancelablePromise<rideDto> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/rides/random',
        });
    }

    /**
     * @param id
     * @param exclude
     * @returns rideDto A new location based on the request params
     * @throws ApiError
     */
    public static getNewRide(
        id: string,
        exclude?: string,
    ): CancelablePromise<rideDto> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/rides/{id}/new-location',
            path: {
                'id': id,
            },
            query: {
                'exclude': exclude,
            },
        });
    }

    /**
     * @param id
     * @param requestBody
     * @returns rideDto The updated ride
     * @throws ApiError
     */
    public static putRideStatus(
        id: string,
        requestBody: rideDto,
    ): CancelablePromise<rideDto> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/v1/rides/{id}/status',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

}
