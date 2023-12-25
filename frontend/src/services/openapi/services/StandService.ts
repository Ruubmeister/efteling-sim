/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { dinnerDto } from '../models/dinnerDto';
import type { standDto } from '../models/standDto';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class StandService {

    /**
     * @returns standDto A JSON array of stands
     * @throws ApiError
     */
    public static getAllStands(): CancelablePromise<Array<standDto>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/stands',
        });
    }

    /**
     * @returns standDto A random stand
     * @throws ApiError
     */
    public static getRandomStand(): CancelablePromise<standDto> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/stands/random',
        });
    }

    /**
     * @param id
     * @param exclude
     * @returns standDto A new location based on the request params
     * @throws ApiError
     */
    public static getNewStand(
        id: string,
        exclude?: string,
    ): CancelablePromise<standDto> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/stands/{id}/new-location',
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
     * @returns standDto The stand that is requested
     * @throws ApiError
     */
    public static getStand(
        id: string,
    ): CancelablePromise<standDto> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/stands/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param ticket
     * @returns dinnerDto The order that is ready for picked up
     * @throws ApiError
     */
    public static getOrder(
        ticket: string,
    ): CancelablePromise<dinnerDto> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/stands/order/{ticket}',
            path: {
                'ticket': ticket,
            },
        });
    }

    /**
     * @param id
     * @param requestBody
     * @returns string Order dinner at a stand
     * @throws ApiError
     */
    public static postOrder(
        id: string,
        requestBody: Array<string>,
    ): CancelablePromise<string> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/v1/stands/{id}/order',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

}
