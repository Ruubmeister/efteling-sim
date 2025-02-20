/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { visitorDto } from '../models/visitorDto';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class VisitorService {
    /**
     * @returns visitorDto A JSON array of visitors
     * @throws ApiError
     */
    public static getAllVisitors(): CancelablePromise<Array<visitorDto>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/visitors',
        });
    }
    /**
     * @param id
     * @returns visitorDto The visitor that is requested
     * @throws ApiError
     */
    public static getVisitor(
        id: string,
    ): CancelablePromise<visitorDto> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/visitors/{id}',
            path: {
                'id': id,
            },
        });
    }
}
