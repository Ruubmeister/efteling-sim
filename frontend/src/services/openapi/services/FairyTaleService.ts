/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { fairyTaleDto } from '../models/fairyTaleDto';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class FairyTaleService {

    /**
     * @returns fairyTaleDto A JSON array of fairy tales
     * @throws ApiError
     */
    public static getAllFairyTales(): CancelablePromise<Array<fairyTaleDto>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/fairy-tales',
        });
    }

    /**
     * @returns fairyTaleDto A random fairy tale
     * @throws ApiError
     */
    public static getRandomFairyTale(): CancelablePromise<fairyTaleDto> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/fairy-tales/random',
        });
    }

    /**
     * @param id
     * @param exclude
     * @returns fairyTaleDto A new location based on the request params
     * @throws ApiError
     */
    public static getNewFairyTale(
        id: string,
        exclude?: string,
    ): CancelablePromise<fairyTaleDto> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/fairy-tales/{id}/new-location',
            path: {
                'id': id,
            },
            query: {
                'exclude': exclude,
            },
        });
    }

}
